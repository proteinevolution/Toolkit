package controllers

import javax.inject.{Inject, Singleton}

import actors.WebSocketActor.{LogOut, ChangeSessionID}
import akka.actor.ActorRef
import models.auth._
import models.database.users.{User, UserToken}
import models.job.JobActorAccess
import models.mailing.{PasswordChangedMail, ResetPasswordMail, ChangePasswordMail, NewUserWelcomeMail}
import models.tools.ToolFactory
import modules.LocationProvider
import modules.tel.TEL
import org.joda.time.DateTime
import play.Logger
import play.api.cache._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller}
import play.api.libs.mailer._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Controller for Authentication interactions
  * Created by astephens on 03.04.16.
  */

@Singleton
final class Auth @Inject() (             webJarAssets     : WebJarAssets,
                                     val messagesApi      : MessagesApi,
                                         jobActorAccess   : JobActorAccess,
                                         toolFactory      : ToolFactory,
                            implicit val mailerClient     : MailerClient,
                            implicit val locationProvider : LocationProvider,
   @NamedCache("userCache") implicit val userCache        : CacheApi,
@NamedCache("wsActorCache") implicit val wsActorCache     : CacheApi,
                                     val reactiveMongoApi : ReactiveMongoApi) // Mailing Controller
                              extends Controller with I18nSupport
                                                 with JSONTemplate
                                                 with UserSessions
                                                 with Common {


  /**
    * Returns the sign in form
    *
    * @param userName usually the eMail address
    * @return
    */
  def signIn(userName : String) = Action { implicit request =>
    Ok(views.html.auth.signin(userName))
  }

  /**
    * Returns the sign up form
    *
    * @return
    */
  def signUp() = Action { implicit request =>
    Ok(views.html.auth.signup())
  }


  /**
    * Shows a small profile in the login panel when the User is signed in.
    *
    * @return
    */
  def miniProfile() : Action[AnyContent] = Action.async { implicit request =>
    getUser.map { user =>
      user.userData match {
        case Some(userData) =>
          Ok(views.html.auth.miniprofile(user))
        case None =>
          BadRequest
      }
    }
  }

  /**
    * User wants to sign out
    * -> remove the sessionID from the database, Overwrite their cookie and give them a new Session ID
    *
    * @return
    */
  def signOut() : Action[AnyContent] = Action.async { implicit request =>
    getUser.map { user =>
      removeUserFromCache(user)

      Redirect(routes.Application.index()).withNewSession.flashing(
        "success" -> "You've been logged out"
      )
    }
  }

  /**
    * A logged in User would like to edit their personal data
    *
    * @return
    */
  def profile() : Action[AnyContent] = Action.async { implicit request =>
    getUser.map { user =>
      user.userData match {
        case Some(userData) =>
          Ok(views.html.auth.profile(user))
            .withSession(sessionCookie(request, user.sessionID.get, Some(userData.nameLogin)))
        case None =>
          // User was not logged in
          Redirect(routes.Application.index())
      }
    }
  }

  /**
    * Sending user name as JSON to the mithril model in joblist
    *
    * @return
    */


  def profile2json() : Action[AnyContent] = Action.async {implicit request =>

    getUser.map { user =>
      user.userData match {
        case Some(userData) => Ok(Json.obj("user"  -> userData.nameLogin))
        case _ => NotFound
      }
    }

  }
  def getUserData : Action[AnyContent] = Action.async { implicit request =>
    getUser.map { user =>
      Logger.info("Sending user data.")
      Ok(Json.toJson(user.userData))
    }
  }


  /**
    * Submission of the sign in form, user wants to authenticate themself
    * Checks the Database for the user and logs them in if their password matches
    *
    * @return
    */
  def signInSubmit() : Action[AnyContent] = Action.async { implicit request =>
    getUser.flatMap { unregisteredUser =>
      if (unregisteredUser.accountType < 0) {
        // Evaluate the Form
        FormDefinitions.SignIn.bindFromRequest.fold(
          errors =>
            Future.successful {
              Logger.info(" but there was an error in the submit form: " + errors.toString)
              Ok(LoginError())
            },

          // if no error, then insert the user to the collection
          signInFormUser => {
            val futureUser = findUser(BSONDocument(User.NAMELOGIN -> signInFormUser.nameLogin))
            futureUser.flatMap {
              case Some(databaseUser) =>
                // Check the password
                if (databaseUser.checkPassword(signInFormUser.password) && databaseUser.accountType > 0) {
                  // create a modifier document to change the last login date in the Database
                  val selector = BSONDocument(User.IDDB -> databaseUser.userID)
                  // Change the login time and give the new Session ID to the user.
                  // Additionally add the watched jobs to the users watchlist.
                  val modifier = BSONDocument("$set"             ->
                                 BSONDocument(User.SESSIONID     -> databaseUser.sessionID.getOrElse(BSONObjectID.generate()),
                                              User.DATELASTLOGIN -> BSONDateTime(new DateTime().getMillis)))
                  // TODO this adds the non logged in user's jobs to the now logged in user's job list
                  //                            "$addToSet"        ->
                  //               BSONDocument(User.JOBS          ->
                  //               BSONDocument("$each"            -> unregisteredUser.jobs)))
                  // Finally add the edits to the collection
                  modifyUserWithCache(selector, modifier).map {
                    case Some(loggedInUser) =>
                      Logger.info("\n-[old user]-\n"
                              + unregisteredUser.toString
                              + "\n-[new user]-\n"
                              + loggedInUser.toString)
                      // Remove the old, not logged in user
                      //removeUser(BSONDocument(User.IDDB -> unregisteredUser.userID))
                      removeUserFromCache(unregisteredUser)

                      // Tell the job actors to copy all jobs connected to the old user to the new user
                      wsActorCache.get(unregisteredUser.userID.stringify) match {
                        case Some(wsActors) =>
                          val actorList : List[ActorRef] = wsActors : List[ActorRef]
                          wsActorCache.set(loggedInUser.userID.stringify, actorList)
                          actorList.foreach(_ ! ChangeSessionID(loggedInUser.sessionID.get))
                          wsActorCache.remove(unregisteredUser.userID.stringify)
                        case None =>
                      }

                      // Everything is ok, let the user know that they are logged in now
                      Ok(LoggedIn(loggedInUser))
                        .withSession(sessionCookie(request, loggedInUser.sessionID.get, Some(loggedInUser.getUserData.nameLogin)))
                    case None =>
                      Ok(LoginIncorrect())
                  }
                } else if (databaseUser.accountType < 1) {
                  // User needs to Verify first
                  Future.successful(Ok(MustVerify()))
                } else {
                  // Wrong Password, show the error message
                  Future.successful(Ok(LoginIncorrect()))
                }
              case None =>
                Future.successful {
                  Ok(LoginIncorrect())
                }
            }
          })
      } else {
        Future.successful(Ok(AlreadyLoggedIn()))
      }
    }
  }


  /**
    * Submission of the sign up form, user wants to register
    * Checks Database if there is a preexisting user and adds them if there is none
    *
    * @return
    */
  def signUpSubmit() : Action[AnyContent] = Action.async { implicit request =>
    getUser.flatMap { user =>
      if (user.accountType < 0) {
        // Create a new user from the information given in the form
        FormDefinitions.SignUp(user).bindFromRequest.fold(
          errors =>
            // Something went wrong with the Form.
            Future.successful {
              Ok(FormError())
            },

          // if no error, then insert the user to the collection
          signUpFormUser => {
            if (signUpFormUser.accountType < 0) {
              // User did not accept the Terms of Service but managed to get around the JS form validation
              Future.successful {
                Ok(MustAcceptToS())
              }
            } else {
              // Check database for existing users with the same login name
              val selector = BSONDocument("$or"          ->
                                     List(BSONDocument(User.NAMELOGIN -> signUpFormUser.getUserData.nameLogin)))
                                          //BSONDocument(User.EMAIL     -> signUpFormUser.getUserData.eMail.head)))
              findUser(selector).flatMap {
                case Some(otherUser) =>
                  //Logger.info("Found a user: " + otherUser.getUserData)
                  if (otherUser.getUserData.nameLogin == signUpFormUser.getUserData.nameLogin) {
                    // Other user with the same username exists. Show error message.
                    Future.successful(Ok(AccountNameUsed()))
                  } else {
                    // Other user with the same eMail exists. Show error message.
                    Future.successful(Ok(AccountEmailUsed()))
                  }
                case None =>
                  // Create the database entry.
                  val newUser = signUpFormUser.copy(userID     = BSONObjectID.generate(),
                                                    sessionID  = None,
                                                    userTokens = List(UserToken(tokenType = 1, eMail = Some(signUpFormUser.getUserData.eMail.head))))
                  upsertUser(newUser).map {
                    case Some(registeredUser) =>
                      // All done. User is registered, now send the welcome eMail
                      val eMail = NewUserWelcomeMail(registeredUser, registeredUser.userTokens.head.token)
                      eMail.send
                      Ok(SignedUp)
                    case None =>
                      Ok(FormError())
                  }
              }
            }
          }
        )
      } else {
        Future.successful(Ok(AccountNameUsed()))
      }
    }
  }

  /**
    * Function handles the profile edit form submission.
    *
    * @return
    */
  def profileSubmit() : Action[AnyContent] = Action.async { implicit request =>
    getUser.flatMap { user : User =>
      user.userData match {
        case Some(userData) =>
          // change the userData with the help of the form input
          FormDefinitions.ProfileEdit(user).bindFromRequest.fold(
            errors =>
              Future.successful{
                Ok(FormError())
              },
            // when there are no errors, then insert the user to the collection
            {
              case Some(editedProfileUserData) =>
                // create a modifier document to change the last login date in the Database
                val bsonCurrentTime = BSONDateTime(new DateTime().getMillis)
                val selector = BSONDocument(User.IDDB          -> user.userID)
                val modifier = BSONDocument("$set"             ->
                               BSONDocument(User.USERDATA      -> editedProfileUserData,
                                            User.DATELASTLOGIN -> bsonCurrentTime,
                                            User.DATEUPDATED   -> bsonCurrentTime))
                modifyUserWithCache(selector, modifier).map {
                  case Some(updatedUser) =>
                    // Everything is ok, let the user know that they are logged in now
                    Ok(EditSuccessful(updatedUser))
                  case None =>
                    // User has been found in the DB at first but now it cant be retrieved
                    Ok(LoginError())
                }
              case None =>
                // Password was incorrect
                Future.successful(Ok(PasswordWrong()))
            }
          )
        case None =>
          // User was not logged in
          Future.successful(Ok(NotLoggedIn()))
      }
    }
  }

  /**
    * Allows a User to change their password. A confirmation eMail is sent for them to
    * ensure a secure change
    *
    * @return
    */
  def passwordChangeSubmit() : Action[AnyContent] = Action.async { implicit request =>
    getUser.flatMap { user : User =>
      user.userData match {
        case Some(userData) =>
          // Validate the password and return the new password Hash
          FormDefinitions.ProfilePasswordEdit(user).bindFromRequest.fold(
            errors =>
              Future.successful{
                Ok(FormError(errors.errors.mkString(",\n")))
              },
            // when there are no errors, then insert the user to the collection
            {
              case Some(newPasswordHash) =>
                // Generate a new Token to wait for the confirmation eMail
                val token = UserToken(tokenType = 2, passwordHash = Some(newPasswordHash))
                // create a modifier document to change the last login date in the Database
                val bsonCurrentTime = BSONDateTime(new DateTime().getMillis)
                // Push to the database using selector and modifier
                val selector = BSONDocument(User.IDDB          -> user.userID)
                val modifier = BSONDocument("$set"             ->
                               BSONDocument(User.DATELASTLOGIN -> bsonCurrentTime,
                                            User.DATEUPDATED   -> bsonCurrentTime),
                                            "$push"            ->
                               BSONDocument(User.USERTOKENS    -> token))
                modifyUserWithCache(selector, modifier).map {
                  case Some(updatedUser) =>
                    // All done. Now send the eMail
                    val eMail = ChangePasswordMail(updatedUser, token.token)
                    eMail.send
                    // Everything is ok, let the user know that they are logged in now
                    Ok(PasswordChanged(updatedUser))
                  case None =>
                    // User has been found in the DB at first but now it cant be retrieved
                    Ok(LoginError())
                }
              case None =>
                // Password was incorrect
                Future.successful(Ok(PasswordWrong()))
            }
          )
        case None =>
          // User was not logged in
          Future.successful(Ok(NotLoggedIn()))
      }
    }
  }

  /**
    * Allows a User to reset their password. A confirmation eMail is sent for them to
    * ensure a secure change
    *
    * @return
    */
  def resetPassword : Action[AnyContent] = Action.async { implicit request =>
    FormDefinitions.ForgottenPasswordEdit.bindFromRequest.fold(
      errors =>
        Future.successful{
          Ok(FormError())
        },
      // when there are no errors, then insert the user to the collection
      {
        case Some(formOutput : (Option[String], Option[String])) =>
          val selector : BSONDocument = formOutput match {
            case (Some(nameLogin: String), Some(eMail: String)) =>
              BSONDocument("$or" -> List(BSONDocument(User.NAMELOGIN -> nameLogin), BSONDocument(User.EMAIL -> eMail)))
            case (Some(nameLogin: String), None) =>
              BSONDocument(User.NAMELOGIN -> nameLogin)
            case (None, Some(eMail: String)) =>
              BSONDocument(User.EMAIL -> eMail)
            case (None, None) =>
              BSONDocument.empty
          }
          if (selector != BSONDocument.empty) {
            findUser(selector).flatMap {
              case Some(user) =>
                user.userData match {
                  case Some(userData) =>

                    // Generate a new Token to wait for the confirmation eMail
                    val token = UserToken(tokenType = 3)
                    // create a modifier document to change the last login date in the Database
                    val bsonCurrentTime = BSONDateTime(new DateTime().getMillis)
                    // Push to the database using selector and modifier
                    val selector = BSONDocument(User.IDDB          -> user.userID)
                    val modifier = BSONDocument("$set"             ->
                                   BSONDocument(User.DATEUPDATED   -> bsonCurrentTime),
                                                "$push"            ->
                                   BSONDocument(User.USERTOKENS    -> token))
                    modifyUser(selector, modifier).map {
                      case Some(registeredUser) =>
                        // All done. User is registered, now send the welcome eMail
                        val eMail = ResetPasswordMail(registeredUser, token.token)
                        eMail.send
                        Ok(PasswordRequestSent)
                      case None =>
                        Ok(FormError())
                    }

                  case None =>
                  // User is not registered? Should not happen.
                  Future.successful(Ok(NoSuchUser))
                }
              case None =>
                // No user found.
                Future.successful(Ok(NoSuchUser))
            }
          } else {
            // User has sent an empty form.
            Future.successful(Ok(OneParameterNeeded))
          }
      }
    )
  }

  /**
    * Allows a User to change their password. A confirmation eMail is sent for them to
    * ensure a secure change
    *
    * @return
    */
  def resetPasswordChange : Action[AnyContent] = Action.async { implicit request =>
    getUser.flatMap { user : User =>
      // Validate the password and return the new password Hash
      FormDefinitions.ForgottenPasswordChange.bindFromRequest.fold(
        errors =>
          Future.successful(Ok(FormError(errors.errors.mkString(",\n")))),
        { newPasswordHash =>
          user.userTokens.find(token => token.tokenType == 4 && token.userID.isDefined) match {
            case Some(token) =>
              val bsonCurrentTime = BSONDateTime(new DateTime().getMillis)
              // Push to the database using selector and modifier
              val selector = BSONDocument(User.IDDB          -> token.userID)
              val modifier = BSONDocument("$set"             ->
                             BSONDocument(User.DATEUPDATED   -> bsonCurrentTime,
                                          User.PASSWORD      -> newPasswordHash),
                                          "$pull"            ->
                             BSONDocument(User.USERTOKENS    ->
                             BSONDocument(UserToken.TOKEN    -> token.token)))
              modifyUser(selector, modifier).flatMap {
                case Some(userWithUpdatedAccount) =>
                  modifyUserWithCache(BSONDocument(User.IDDB -> user.userID),
                                      BSONDocument("$pull" ->
                                      BSONDocument(User.USERTOKENS ->
                                      BSONDocument(UserToken.TOKEN -> token.token)))).map {
                    case Some(updatedUser) =>
                      // All done. Now send the eMail to notify the user that the password has been changed
                      val eMail = PasswordChangedMail(updatedUser)
                      eMail.send
                      Ok(PasswordChanged(updatedUser))
                    case None =>
                      Ok(DatabaseError)
                  }
                case None =>
                  // User has been found in the DB at first but now it cant be retrieved
                  Future.successful(Ok(DatabaseError))
              }
            case None =>
              Future.successful(NotFound)
          }
        }
      )
    }
  }

  /**
    * Verifies a Token which was sent to the Users eMail address.
    * Token Types: 1 - eMail verification
    *              2 - password change verification
    *              3 - password reset verification
    *              4 -                             + reset
    *
    * @param userName
    * @param token
    * @return
    */
  def verification(userName : String, token : String) = Action.async { implicit request =>
    getUser.flatMap { user : User =>
      // Grab the user from the database in case that the logged in user is not the user to verify
      findUser(BSONDocument(User.NAMELOGIN -> userName)).flatMap {
        case Some(userToVerify) =>
          // Filter the correct token
          val matchingToken = userToVerify.userTokens.find(_.token == token)
          matchingToken match {
            case Some(usedToken) =>
              // generate new list of tokens minus the type of token used for this token
              val remainingTokens : List[UserToken] = userToVerify.userTokens.filterNot(_.tokenType == usedToken.tokenType)
              usedToken.tokenType match {


                case 1 => // Token for eMail verification
                  modifyUser(BSONDocument(User.IDDB        -> userToVerify.userID),
                             BSONDocument("$set"           ->
                             BSONDocument(User.ACCOUNTTYPE -> 1,
                                          User.DATEUPDATED -> BSONDateTime(new DateTime().getMillis),
                                          User.USERTOKENS  -> remainingTokens))).map {
                  case Some(modifiedUser) =>
                    Ok(views.html.main(webJarAssets, toolFactory.values.values.toSeq.sortBy(_.toolNameLong),
                      "Account verification was successful. Please log in."))
                  case None =>  // Could not save the modified user to the DB
                    Ok(views.html.main(webJarAssets, toolFactory.values.values.toSeq.sortBy(_.toolNameLong),
                      "Verification was not successful due to a database error. Please try again later."))
                }


                case 2 => // Token for password change validation
                  usedToken.passwordHash match {
                    case Some(newPassword) =>
                      modifyUser(BSONDocument(User.IDDB        -> userToVerify.userID),
                                 BSONDocument("$set"           ->
                                 BSONDocument(User.PASSWORD    -> newPassword,
                                              User.DATEUPDATED -> BSONDateTime(new DateTime().getMillis),
                                              User.USERTOKENS  -> remainingTokens),
                                              "$unset"         ->
                                 BSONDocument(User.SESSIONID   -> "",
                                              User.CONNECTED   -> ""))).map {
                        case Some(modifiedUser) =>
                          removeUserFromCache(user = modifiedUser, withDB = false)
                          val eMail = PasswordChangedMail(modifiedUser)
                          eMail.send
                          if (modifiedUser.connected) {
                            // Force Log Out on all connected users.
                            (wsActorCache.get(modifiedUser.userID.stringify) : Option[List[ActorRef]]) match {
                              case Some(webSocketActors) =>
                                webSocketActors.foreach(_ ! LogOut)
                              case None =>
                            }
                          }
                          // User modified properly
                          Ok(views.html.main(webJarAssets, toolFactory.values.values.toSeq.sortBy(_.toolNameLong),
                            "Password change verification was successful. Please log in with Your new password."))
                        case None =>  // Could not save the modified user to the DB
                          Ok(views.html.main(webJarAssets, toolFactory.values.values.toSeq.sortBy(_.toolNameLong),
                            "Verification was not successful due to a database error. Please try again later."))
                      }
                    case None =>
                      // This should not happen - Failsafe
                      Future.successful(
                        Ok(views.html.main(webJarAssets, toolFactory.values.values.toSeq.sortBy(_.toolNameLong),
                        "The Password you had entered was insufficient, please create a new one.")))
                  }


                case 3 =>
                  // Give a token to the current user to allow them to change the password in a different view
                  val newToken = UserToken(tokenType=4, token=usedToken.token, userID = Some(userToVerify.userID))
                  modifyUserWithCache(BSONDocument(User.IDDB        -> user.userID),
                                      BSONDocument("$set"           ->
                                      BSONDocument(User.DATEUPDATED -> BSONDateTime(new DateTime().getMillis)),
                                                   "$push"          ->
                                      BSONDocument(User.USERTOKENS  -> newToken))).map {
                    case Some(changedUser) =>
                      Ok(views.html.main(webJarAssets, toolFactory.values.values.toSeq.sortBy(_.toolNameLong),
                        "","passwordReset"))
                    case None =>  // Could not save the modified user to the DB
                      Ok(views.html.main(webJarAssets, toolFactory.values.values.toSeq.sortBy(_.toolNameLong),
                        "Verification was not successful due to a database error. Please try again later."))
                  }
                case _ =>
                  Future.successful(Ok(views.html.main(webJarAssets, toolFactory.values.values.toSeq.sortBy(_.toolNameLong),
                    "There was an error with finding your token.")))
              }

            case None => // No Token in DB
              Future.successful(Ok(views.html.main(webJarAssets, toolFactory.values.values.toSeq.sortBy(_.toolNameLong),
                "The token you used is not valid.")))
          }
        case None => // No user with matching Username in DB
          Future.successful(Ok(views.html.main(webJarAssets, toolFactory.values.values.toSeq.sortBy(_.toolNameLong),
            "Verification was not successful. Please try again.")))
      }
    }
  }
}
