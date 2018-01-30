package controllers

import java.time.ZonedDateTime
import javax.inject.{ Inject, Singleton }

import actors.WebSocketActor.{ ChangeSessionID, LogOut }
import akka.actor.ActorRef
import de.proteinevolution.common.LocationProvider
import de.proteinevolution.models.Constants
import models.UserSessions
import models.auth._
import de.proteinevolution.models.database.users.{ User, UserConfig, UserToken }
import models.tools.ToolFactory
import de.proteinevolution.db.MongoStore
import models.mailing.MailTemplate.{ ChangePasswordMail, NewUserWelcomeMail, PasswordChangedMail, ResetPasswordMail }
import play.Logger
import play.api.cache._
import play.api.i18n.I18nSupport
import play.api.libs.json.Json
import play.api.mvc._
import play.api.libs.mailer._
import reactivemongo.bson._
import org.webjars.play.WebJarsUtil
import play.api.Environment
import scala.concurrent.{ Await, ExecutionContext, Future }

/**
 * Controller for Authentication interactions
 *
 */
@Singleton
final class Auth @Inject()(webJarsUtil: WebJarsUtil,
                           mongoStore: MongoStore,
                           toolFactory: ToolFactory,
                           userSessions: UserSessions,
                           implicit val mailerClient: MailerClient,
                           implicit val locationProvider: LocationProvider,
                           @NamedCache("userCache") implicit val userCache: SyncCacheApi,
                           @NamedCache("wsActorCache") implicit val wsActorCache: SyncCacheApi, // Mailing Controller
                           constants: Constants,
                           environment: Environment,
                           cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with I18nSupport
    with JSONTemplate
    with CommonController {

  /**
   * User wants to sign out
   * -> remove the sessionID from the database, Overwrite their cookie and give them a new Session ID
   *
   * @return
   */
  def signOut(): Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.map { user =>
      userSessions.removeUserFromCache(user)

      Redirect(routes.Application.index()).withNewSession.flashing(
        "success" -> "You've been logged out"
      )
    }
  }

  /**
   * Sending user name as JSON to the mithril model in joblist
   *
   * @return
   */
  def profile2json(): Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.map { user =>
      user.userData match {
        case Some(userData) => Ok(Json.obj("user" -> userData.nameLogin))
        case _              => NotFound
      }
    }

  }

  def getUserData: Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.map { user =>
      Logger.info("Sending user data.")
      Ok(Json.toJson(user.userData))
    }
  }

  def matchUserToPW(username: String, password: String): Future[Boolean] = {
    mongoStore.findUser(BSONDocument("userData.nameLogin" -> username)).map {
      case Some(user) if user.checkPassword(password) => true
      case None                                       => false
    }
  }

  // add header authentication layer so that this cannot be curled easily by checking db if user matches with pw

  def BasicSecured[A]()(action: Action[A]): Action[A] = Action.async(action.parser) { request =>
    request.headers
      .get("Authorization")
      .flatMap { authorization =>
        authorization.split(" ").drop(1).headOption.filter { encoded =>
          new String(org.apache.commons.codec.binary.Base64.decodeBase64(encoded.getBytes)).split(":").toList match {
            case u :: p :: Nil if Await.result(matchUserToPW(u, p), scala.concurrent.duration.Duration.Inf) => true
            case _                                                                                          => false
          }
        }
      }
      .map(_ => action(request))
      .getOrElse {
        //Future.successful(Unauthorized.withHeaders("WWW-Authenticate" -> """Basic realm="Secured Area""""))
        Future.successful(Ok(loginIncorrect()))
      }
  }

  /**
   * Submission of the sign in form
   * Checks the Database for the user and logs him in if password matches
   *
   * @return
   */
  def signInSubmit(): Action[AnyContent] =
    Action.async { implicit request =>
      userSessions.getUser.flatMap { unregisteredUser =>
        if (unregisteredUser.accountType < 0) {
          // Evaluate the Form
          FormDefinitions.signIn.bindFromRequest.fold(
            errors =>
              Future.successful {
                Ok(loginError())
            },
            // if no error, then insert the user to the collection
            signInFormUser => {
              val futureUser = mongoStore.findUser(
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
                        Logger.info(
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
                        Ok(loggedIn(loggedInUser))
                          .withSession(
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

  /**
   * Submission of the sign up form
   * Checks Database if there is a preexisting user and adds him if there is none
   *
   * @return
   */
  def signUpSubmit(): Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { user =>
      if (user.accountType < 0) {
        // Create a new user from the information given in the form
        FormDefinitions
          .signUp(user)
          .bindFromRequest
          .fold(
            errors =>
              // Something went wrong with the Form.
              Future.successful {
                Ok(formError())
            },
            // if no error, then insert the user to the collection
            signUpFormUser => {
              if (signUpFormUser.accountType < 0) {
                // User did not accept the Terms of Service but managed to get around the JS form validation
                Future.successful {
                  Ok(mustAcceptToS())
                }
              } else {
                // Check database for existing users with the same email
                val selector = BSONDocument("$or" -> List(
                  BSONDocument(User.EMAIL     -> signUpFormUser.getUserData.eMail),
                  BSONDocument(User.NAMELOGIN -> signUpFormUser.getUserData.nameLogin)))
                mongoStore.findUser(selector).flatMap {
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
                      userToken = Some(UserToken(tokenType = 1, eMail = Some(signUpFormUser.getUserData.eMail)))
                    )
                    mongoStore.upsertUser(newUser).map {
                      case Some(registeredUser) =>
                        // All done. User is registered, now send the welcome eMail
                        registeredUser.userToken match {
                          case Some(token) =>
                            val eMail = NewUserWelcomeMail(registeredUser, token.token)
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

  /**
   * Function handles the profile edit form submission.
   *
   * @return
   */
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
                    mongoStore.findUser(selectorMail).flatMap {
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

  /**
   * Allows a User to change his password.
   * sent him an verification link, that he needs
   * to open
   *
   * @return
   */
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
                  val modifier = BSONDocument("$set" ->
                                              BSONDocument(User.DATELASTLOGIN -> bsonCurrentTime,
                                                           User.DATEUPDATED   -> bsonCurrentTime),
                                              "$set" ->
                                              BSONDocument(User.USERTOKEN -> token))
                  userSessions.modifyUserWithCache(selector, modifier).map {
                    case Some(updatedUser) =>
                      // All done. Now send the eMail
                      val eMail = ChangePasswordMail(updatedUser, token.token)
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

  /**
   * Allows a User to reset his password. A confirmation eMail is send to him to
   * ensure a secure change
   *
   * @return
   */
  def resetPassword: Action[AnyContent] = Action.async { implicit request =>
    FormDefinitions.forgottenPasswordEdit.bindFromRequest.fold(
      _ =>
        Future.successful {
          Ok(formError())
      },
      // when there are no errors, then insert the user to the collection
      {
        case Some(user: (String)) =>
          val selector =
            BSONDocument("$or" -> List(BSONDocument(User.EMAIL -> user), BSONDocument(User.NAMELOGIN -> user)))

          mongoStore.findUser(selector).flatMap {
            case Some(user) =>
              user.userData match {
                case Some(_) =>
                  // Generate a new Token to wait for the confirmation eMail
                  val token = UserToken(tokenType = 3)
                  // create a modifier document to change the last login date in the Database
                  val bsonCurrentTime = BSONDateTime(ZonedDateTime.now.toInstant.toEpochMilli)
                  // Push to the database using selector and modifier
                  val selector = BSONDocument(User.IDDB -> user.userID)
                  val modifier = BSONDocument("$set" ->
                                              BSONDocument(User.DATEUPDATED -> bsonCurrentTime),
                                              "$set" ->
                                              BSONDocument(User.USERTOKEN -> token))
                  userSessions.modifyUserWithCache(selector, modifier).map {
                    case Some(registeredUser) =>
                      // All done. User is registered, now send the welcome eMail
                      val eMail = ResetPasswordMail(registeredUser, token.token)
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

  /**
   * after user clicks on the verification
   * link he can reset his password
   *
   * @return
   */
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
                  BSONDocument("$set" ->
                               BSONDocument(User.DATEUPDATED -> bsonCurrentTime, User.PASSWORD -> newPasswordHash),
                               "$unset" ->
                               BSONDocument(User.USERTOKEN -> ""))
                userSessions.modifyUserWithCache(selector, modifier).flatMap {
                  case Some(userWithUpdatedAccount) =>
                    userSessions
                      .modifyUserWithCache(BSONDocument(User.IDDB -> userWithUpdatedAccount.userID),
                                           BSONDocument(
                                             "$unset" ->
                                             BSONDocument(User.USERTOKEN -> "")
                                           ))
                      .map {
                        case Some(updatedUser) =>
                          // All done. Now send the eMail to notify the user that the password has been changed
                          val eMail = PasswordChangedMail(updatedUser)
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

  /**
   * Verifies a Token which was sent to the Users eMail address.
   * Token Types: 1 - eMail verification
   * 2 - password change verification
   * 3 - password reset verification
   * 4 -                             + reset
   *
   *
   * @param token
   * @return
   */
  def verification(nameLogin: String, token: String): Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { user: User =>
      // Grab the user from the database in case that the logged in user is not the user to verify
      // TODO check for both name or email
      mongoStore.findUser(BSONDocument(User.NAMELOGIN -> nameLogin)).flatMap {
        case Some(userToVerify) =>
          userToVerify.userToken match {
            case Some(userToken) =>
              if (userToken.token == token) {
                userToken.tokenType match {
                  case 1 => // Token for eMail verification
                    mongoStore
                      .modifyUser(
                        BSONDocument(User.IDDB -> userToVerify.userID),
                        BSONDocument(
                          "$set" ->
                          BSONDocument(User.ACCOUNTTYPE -> 1,
                                       User.DATEUPDATED -> BSONDateTime(ZonedDateTime.now.toInstant.toEpochMilli)),
                          BSONDocument(
                            "$unset" ->
                            BSONDocument(User.USERTOKEN -> "")
                          )
                        )
                      )
                      .map {
                        case Some(_) =>
                          Ok(
                            views.html.main(webJarsUtil,
                                            toolFactory.values.values.toSeq.sortBy(_.toolNameLong),
                                            "Account verification was successful. Please log in.",
                                            "",
                                            environment)
                          )
                        case None => // Could not save the modified user to the DB
                          Ok(
                            views.html.main(
                              webJarsUtil,
                              toolFactory.values.values.toSeq.sortBy(_.toolNameLong),
                              "Verification was not successful due to a database error. Please try again later.",
                              "",
                              environment
                            )
                          )
                      }
                  case 2 => // Token for password change validation
                    userToVerify.userToken match {
                      case Some(token) =>
                        token.passwordHash match {
                          case Some(newPassword) =>
                            mongoStore
                              .modifyUser(
                                BSONDocument(User.IDDB -> userToVerify.userID),
                                BSONDocument(
                                  "$set" ->
                                  BSONDocument(
                                    User.PASSWORD    -> newPassword,
                                    User.DATEUPDATED -> BSONDateTime(ZonedDateTime.now.toInstant.toEpochMilli)
                                  ),
                                  "$unset" ->
                                  BSONDocument(User.SESSIONID -> "", User.CONNECTED -> "", User.USERTOKEN -> "")
                                )
                              )
                              .map {
                                case Some(modifiedUser) =>
                                  userSessions.removeUserFromCache(user)
                                  val eMail = PasswordChangedMail(modifiedUser)
                                  eMail.send
                                  // Force Log Out on all connected users.
                                  (wsActorCache.get(modifiedUser.userID.stringify): Option[List[ActorRef]]) match {
                                    case Some(webSocketActors) =>
                                      webSocketActors.foreach(_ ! LogOut)
                                    case None =>
                                  }
                                  // User modified properly
                                  Ok(
                                    views.html.main(
                                      webJarsUtil,
                                      toolFactory.values.values.toSeq.sortBy(_.toolNameLong),
                                      "Password change verification was successful. Please log in with Your new password.",
                                      "",
                                      environment
                                    )
                                  )
                                case None => // Could not save the modified user to the DB
                                  Ok(
                                    views.html.main(
                                      webJarsUtil,
                                      toolFactory.values.values.toSeq.sortBy(_.toolNameLong),
                                      "Verification was not successful due to a database error. Please try again later.",
                                      "",
                                      environment
                                    )
                                  )
                              }
                          case None =>
                            // This should not happen - Failsafe
                            Future.successful(
                              Ok(
                                views.html
                                  .main(
                                    webJarsUtil,
                                    toolFactory.values.values.toSeq.sortBy(_.toolNameLong),
                                    "The Password you had entered was insufficient, please create a new one.",
                                    "",
                                    environment
                                  )
                              )
                            )
                        }
                      case None => Future.successful(NotFound)
                    }

                  case 3 =>
                    // Give a token to the current user to allow him to change the password in a different view (Password Recovery)
                    val newToken =
                      UserToken(tokenType = 4, token = userToken.token, userID = Some(userToVerify.userID))
                    val selector = BSONDocument(User.IDDB -> user.userID)
                    val modifier = BSONDocument(
                      "$set" -> BSONDocument(
                        User.DATEUPDATED -> BSONDateTime(ZonedDateTime.now.toInstant.toEpochMilli),
                        User.USERTOKEN   -> newToken
                      )
                    )
                    userSessions.modifyUserWithCache(selector, modifier).map {
                      case Some(_) =>
                        Ok(
                          views.html.main(webJarsUtil,
                                          toolFactory.values.values.toSeq.sortBy(_.toolNameLong),
                                          "",
                                          "passwordReset",
                                          environment)
                        )
                      case None => // Could not save the modified user to the DB
                        Ok(
                          views.html.main(
                            webJarsUtil,
                            toolFactory.values.values.toSeq.sortBy(_.toolNameLong),
                            "Verification was not successful due to a database error. Please try again later.",
                            "",
                            environment
                          )
                        )
                    }
                  case _ =>
                    Future.successful(
                      Ok(
                        views.html.main(webJarsUtil,
                                        toolFactory.values.values.toSeq.sortBy(_.toolNameLong),
                                        "There was an error finding your token.",
                                        "",
                                        environment)
                      )
                    )
                }

              } else {
                // No Token in DB
                Future.successful(
                  Ok(
                    views.html.main(webJarsUtil,
                                    toolFactory.values.values.toSeq.sortBy(_.toolNameLong),
                                    "The token you used is not valid.",
                                    "",
                                    environment)
                  )
                )
              }
            case None =>
              Future.successful(
                Ok(
                  views.html.main(webJarsUtil,
                                  toolFactory.values.values.toSeq.sortBy(_.toolNameLong),
                                  "There was an error finding your token.",
                                  "",
                                  environment)
                )
              )
          }
        case None =>
          Future.successful(
            Ok(
              views.html.main(webJarsUtil,
                              toolFactory.values.values.toSeq.sortBy(_.toolNameLong),
                              "There was an error finding your account.",
                              "",
                              environment)
            )
          )
      }
    }
  }

  def validateModellerKey(input: String): Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { user =>
      if (user.userConfig.hasMODELLERKey) {
        Future.successful(Ok(Json.obj("isValid" -> true)))
      } else if (input == constants.modellerKey) {
        userSessions
          .modifyUserWithCache(BSONDocument(User.IDDB -> user.userID),
                               BSONDocument(
                                 "$set" ->
                                 BSONDocument(
                                   s"${User.USERCONFIG}.${UserConfig.HASMODELLERKEY}" ->
                                   true
                                 )
                               ))
          .map {
            case Some(_) =>
              Ok(Json.obj("isValid" -> true))
            case None =>
              BadRequest
          }
      } else {
        Future.successful(Ok(Json.obj("isValid" -> false)))
      }
    }
  }
}
