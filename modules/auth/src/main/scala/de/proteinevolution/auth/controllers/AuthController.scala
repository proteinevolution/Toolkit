package de.proteinevolution.auth.controllers

import java.time.ZonedDateTime

import akka.actor.ActorRef
import de.proteinevolution.auth.UserSessions
import de.proteinevolution.auth.dao.UserDao
import de.proteinevolution.auth.models.MailTemplate._
import de.proteinevolution.auth.models.{ FormDefinitions, JSONTemplate }
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.models.database.users.{ User, UserToken }
import de.proteinevolution.models.message.Session.ChangeSessionID
import de.proteinevolution.tel.env.Env
import io.circe.syntax._
import javax.inject.{ Inject, Singleton }
import play.api.cache.{ NamedCache, SyncCacheApi }
import play.api.libs.mailer.MailerClient
import play.api.mvc.{ Action, AnyContent, ControllerComponents }
import play.api.{ Environment, Logger }
import reactivemongo.bson.{ BSONDateTime, BSONDocument, BSONObjectID }

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class AuthController @Inject()(
    userSessions: UserSessions,
    userDao: UserDao,
    cc: ControllerComponents,
    @NamedCache("wsActorCache") wsActorCache: SyncCacheApi,
    environment: Environment,
    env: Env
)(implicit ec: ExecutionContext, mailerClient: MailerClient)
    extends ToolkitController(cc)
    with JSONTemplate {

  private val logger = Logger(this.getClass)

  def signOut: Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.map { user =>
      userSessions.removeUserFromCache(user)
      Redirect("/").withNewSession.flashing(
        "success" -> "You've been logged out"
      )
    }
  }

  def getUserData: Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.map { user =>
      logger.info("Sending user data.")
      Ok(user.userData.asJson)
    }
  }

  def signInSubmit: Action[AnyContent] =
    Action.async { implicit request =>
      userSessions.getUser.flatMap { unregisteredUser =>
        if (unregisteredUser.accountType < 0) {
          // Evaluate the Form
          FormDefinitions.signIn.bindFromRequest.fold(
            _ =>
              Future.successful {
                Ok(loginError())
            },
            // if no error, then insert the user to the collection
            signInFormUser => {
              val futureUser = userDao.findUser(
                BSONDocument(
                  "$or" -> List(BSONDocument(User.EMAIL -> signInFormUser.nameLogin),
                                BSONDocument(User.NAMELOGIN -> signInFormUser.nameLogin))
                )
              )
              futureUser.flatMap {
                case Some(databaseUser) =>
                  // Check the password
                  if (databaseUser.checkPassword(signInFormUser.password) && databaseUser.accountType > 0) {
                    // create a modifier document to change the last login date in the Database
                    val selector = BSONDocument(User.IDDB -> databaseUser.userID)
                    // Change the login time and give the new Session ID to the user.
                    // Additionally add the watched jobs to the users watchlist.
                    val modifier = userSessions.getUserModifier(databaseUser, forceSessionID = true)
                    // TODO this adds the non logged in user's jobs to the now logged in user's job list
                    //                            "$addToSet"        ->
                    //               BSONDocument(User.JOBS          ->
                    //               BSONDocument("$each"            -> unregisteredUser.jobs)))
                    // Finally add the edits to the collection
                    userSessions.modifyUserWithCache(selector, modifier).map {
                      case Some(loggedInUser) =>
                        logger.info(
                          "\n-[old user]-\n"
                          + unregisteredUser.toString
                          + "\n-[new user]-\n"
                          + loggedInUser.toString
                        )
                        // Remove the old, not logged in user
                        //removeUser(BSONDocument(User.IDDB -> unregisteredUser.userID))
                        userSessions.removeUserFromCache(unregisteredUser)

                        // Tell the job actors to copy all jobs connected to the old user to the new user
                        wsActorCache.get[List[ActorRef]](unregisteredUser.userID.stringify) match {
                          case Some(wsActors) =>
                            val actorList: List[ActorRef] = wsActors: List[ActorRef]
                            wsActorCache.set(loggedInUser.userID.stringify, actorList)
                            actorList.foreach(_ ! ChangeSessionID(loggedInUser.sessionID.get))
                            wsActorCache.remove(unregisteredUser.userID.stringify)
                          case None =>
                        }

                        // Everything is ok, let the user know that they are logged in now
                        Ok(loggedIn(loggedInUser)).withSession(
                          userSessions.sessionCookie(request, loggedInUser.sessionID.get)
                        )
                      case None =>
                        Ok(loginIncorrect())
                    }
                  } else if (databaseUser.accountType < 1) {
                    // User needs to Verify first
                    Future.successful(Ok(mustVerify()))
                  } else {
                    // Wrong Password, show the error message
                    Future.successful(Ok(loginIncorrect()))
                  }
                case None =>
                  Future.successful {
                    Ok(loginIncorrect())
                  }
              }
            }
          )
        } else {
          Future.successful(Ok(alreadyLoggedIn()))
        }
      }
    }

  def signUpSubmit: Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { user =>
      if (user.accountType < User.NORMALUSERAWAITINGREGISTRATION) {
        // Create a new user from the information given in the form
        FormDefinitions
          .signUp(user)
          .bindFromRequest
          .fold(
            _ =>
              // Something went wrong with the Form.
              Future.successful {
                Ok(formError())
            },
            // if no error, then insert the user to the collection
            signUpFormUser => {
              if (signUpFormUser.accountType < User.NORMALUSERAWAITINGREGISTRATION) {
                // User did not accept the Terms of Service but managed to get around the JS form validation
                Future.successful {
                  Ok(mustAcceptToS())
                }
              } else {
                // Check database for existing users with the same email
                val selector = BSONDocument(
                  "$or" -> List(BSONDocument(User.EMAIL -> signUpFormUser.getUserData.eMail),
                                BSONDocument(User.NAMELOGIN -> signUpFormUser.getUserData.nameLogin))
                )
                userDao.findUser(selector).flatMap {
                  case Some(otherUser) =>
                    if (signUpFormUser.getUserData.eMail == otherUser.getUserData.eMail) {
                      Future.successful(Ok(accountEmailUsed()))
                    } else {
                      Future.successful(Ok(accountNameUsed()))
                    }
                  case None =>
                    // Create the database entry.
                    val newUser = signUpFormUser.copy(
                      userID = BSONObjectID.generate(),
                      sessionID = None,
                      userToken =
                        Some(UserToken(tokenType = User.REGISTEREDUSER, eMail = Some(signUpFormUser.getUserData.eMail)))
                    )
                    userDao.upsertUser(newUser).map {
                      case Some(registeredUser) =>
                        // All done. User is registered, now send the welcome eMail
                        registeredUser.userToken match {
                          case Some(token) =>
                            val eMail = NewUserWelcomeMail(registeredUser, token.token, environment, env)
                            eMail.send
                            Ok(signedUp)
                          case None => Ok(tokenMismatch())
                        }
                      case None =>
                        Ok(formError())
                    }
                }
              }
            }
          )
      } else {
        Future.successful(Ok(accountNameUsed()))
      }
    }
  }

  def resetPassword: Action[AnyContent] = Action.async { implicit request =>
    FormDefinitions.forgottenPasswordEdit.bindFromRequest.fold(
      _ =>
        Future.successful {
          Ok(formError())
      },
      // when there are no errors, then insert the user to the collection
      {
        case Some(userNameOrEmail: String) =>
          val selector =
            BSONDocument(
              "$or" -> List(
                BSONDocument(User.EMAIL     -> userNameOrEmail),
                BSONDocument(User.NAMELOGIN -> userNameOrEmail)
              )
            )

          userDao.findUser(selector).flatMap {
            case Some(user) =>
              user.userData match {
                case Some(_) =>
                  // Generate a new Token to wait for the confirmation eMail
                  val token = UserToken(tokenType = 3)
                  // create a modifier document to change the last login date in the Database
                  val bsonCurrentTime = BSONDateTime(ZonedDateTime.now.toInstant.toEpochMilli)
                  // Push to the database using selector and modifier
                  val selector = BSONDocument(User.IDDB -> user.userID)
                  val modifier = BSONDocument(
                    "$set" ->
                    BSONDocument(User.DATEUPDATED -> bsonCurrentTime),
                    "$set" ->
                    BSONDocument(User.USERTOKEN -> token)
                  )
                  userSessions.modifyUserWithCache(selector, modifier).map {
                    case Some(registeredUser) =>
                      // All done. User is registered, now send the welcome eMail
                      val eMail =
                        ResetPasswordMail(registeredUser, token.token, environment, env: Env)
                      eMail.send
                      Ok(passwordRequestSent)
                    case None =>
                      Ok(formError())
                  }

                case None =>
                  // User is not registered? Should not happen.
                  Future.successful(Ok(noSuchUser))
              }
            case None =>
              // No user found.
              Future.successful(Ok(noSuchUser))
          }
      }
    )
  }

  def resetPasswordChange: Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { user: User =>
      // Validate the password and return the new password Hash
      FormDefinitions.forgottenPasswordChange.bindFromRequest.fold(
        errors => Future.successful(Ok(formError(errors.errors.mkString(",\n")))), { newPasswordHash =>
          user.userToken match {
            case Some(token) =>
              if (token.tokenType == 4 && token.userID.isDefined) {
                val bsonCurrentTime = BSONDateTime(ZonedDateTime.now.toInstant.toEpochMilli)
                // Push to the database using selector and modifier
                val selector = BSONDocument(User.IDDB -> token.userID)
                val modifier =
                  BSONDocument(
                    "$set" ->
                    BSONDocument(User.DATEUPDATED -> bsonCurrentTime, User.PASSWORD -> newPasswordHash),
                    "$unset" ->
                    BSONDocument(User.USERTOKEN -> "")
                  )
                userSessions.modifyUserWithCache(selector, modifier).flatMap {
                  case Some(userWithUpdatedAccount) =>
                    userSessions
                      .modifyUserWithCache(
                        BSONDocument(User.IDDB -> userWithUpdatedAccount.userID),
                        BSONDocument(
                          "$unset" ->
                          BSONDocument(User.USERTOKEN -> "")
                        )
                      )
                      .map {
                        case Some(updatedUser) =>
                          // All done. Now send the eMail to notify the user that the password has been changed
                          val eMail = PasswordChangedMail(updatedUser, environment, env: Env)
                          eMail.send
                          Ok(passwordChanged(updatedUser))
                        case None =>
                          Ok(databaseError)
                      }
                  case None =>
                    // User has been found in the DB at first but now it cant be retrieved
                    Future.successful(Ok(databaseError))
                }
              } else {
                Future.successful(Ok(tokenMismatch()))
              }
            case None => Future.successful(Ok(tokenNotFound()))
          }
        }
      )
    }
  }

  def passwordChangeSubmit(): Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { user: User =>
      user.userData match {
        case Some(_) =>
          // Validate the password and return the new password Hash
          FormDefinitions
            .profilePasswordEdit(user)
            .bindFromRequest
            .fold(
              errors =>
                Future.successful {
                  Ok(formError(errors.errors.mkString(",\n")))
              },
              // when there are no errors, then insert the user to the collection
              {
                case Some(newPasswordHash) =>
                  // Generate a new Token to wait for the confirmation eMail
                  val token = UserToken(tokenType = 2, passwordHash = Some(newPasswordHash))
                  // create a modifier document to change the last login date in the Database
                  val bsonCurrentTime = BSONDateTime(ZonedDateTime.now.toInstant.toEpochMilli)
                  // Push to the database using selector and modifier
                  val selector = BSONDocument(User.IDDB -> user.userID)
                  val modifier = BSONDocument(
                    "$set" ->
                    BSONDocument(User.DATELASTLOGIN -> bsonCurrentTime, User.DATEUPDATED -> bsonCurrentTime),
                    "$set" ->
                    BSONDocument(User.USERTOKEN -> token)
                  )
                  userSessions.modifyUserWithCache(selector, modifier).map {
                    case Some(updatedUser) =>
                      // All done. Now send the eMail
                      val eMail = ChangePasswordMail(updatedUser, token.token, environment, env: Env)
                      eMail.send
                      // Everything is ok, let the user know that they are logged in now
                      Ok(passwordChanged(updatedUser))
                    case None =>
                      // User has been found in the DB at first but now it cant be retrieved
                      Ok(loginError())
                  }
                case None =>
                  // Password was incorrect
                  Future.successful(Ok(passwordWrong()))
              }
            )
        case None =>
          // User was not logged in
          Future.successful(Ok(notLoggedIn()))
      }
    }
  }

  def profileSubmit(): Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { user: User =>
      user.userData match {
        case Some(userData) =>
          // change the userData with the help of the form input
          FormDefinitions
            .profileEdit(user)
            .bindFromRequest
            .fold(
              _ =>
                Future.successful {
                  Ok(formError())
              },
              // when there are no errors, then insert the user to the collection
              {
                case Some(editedProfileUserData) =>
                  // create a modifier document to change the last login date in the Database
                  val bsonCurrentTime = BSONDateTime(ZonedDateTime.now.toInstant.toEpochMilli)
                  val selector        = BSONDocument(User.IDDB -> user.userID)
                  val modifier = BSONDocument(
                    "$set" ->
                    BSONDocument(User.USERDATA      -> editedProfileUserData.copy(nameLogin = userData.nameLogin),
                                 User.DATELASTLOGIN -> bsonCurrentTime,
                                 User.DATEUPDATED   -> bsonCurrentTime)
                  )

                  if (editedProfileUserData.eMail != user.getUserData.eMail) {
                    val selectorMail = BSONDocument(BSONDocument(User.EMAIL -> editedProfileUserData.eMail))
                    userDao.findUser(selectorMail).flatMap {
                      case Some(_) =>
                        Future.successful(Ok(accountEmailUsed()))
                      case None => Future.successful(NotFound)
                    }
                  }
                  userSessions.modifyUserWithCache(selector, modifier).map {
                    case Some(updatedUser) =>
                      // Everything is ok, let the user know that they are logged in now
                      Ok(editSuccessful(updatedUser))
                    case None =>
                      // User has been found in the DB at first but now it cant be retrieved
                      Ok(loginError())
                  }

                case None =>
                  // Password was incorrect
                  Future.successful(Ok(passwordWrong()))
              }
            )
        case None =>
          // User was not logged in
          Future.successful(Ok(notLoggedIn()))
      }
    }
  }

}
