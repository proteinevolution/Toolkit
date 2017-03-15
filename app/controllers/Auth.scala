package controllers

import javax.inject.{Inject, Singleton}

import actors.WebSocketActor.ChangeSessionID
import akka.actor.ActorRef
import models.auth._
import models.database.users.{User, UserToken}
import models.job.JobActorAccess
import models.mailing.{ChangePasswordMail, NewUserWelcomeMail}
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
                            implicit val mailerClient     : MailerClient,
                            implicit val locationProvider : LocationProvider,
   @NamedCache("userCache") implicit val userCache        : CacheApi,
@NamedCache("wsActorCache") implicit val wsActorCache    : CacheApi,
                                     val tel              : TEL,
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
                if (databaseUser.checkPassword(signInFormUser.password)) {
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
                } else {
                  Future.successful {
                    // Wrong Password, show the error message
                    Ok(LoginIncorrect())
                  }
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
            if (signUpFormUser.accountType < 1) {
              // User did not accept the Terms of Service but managed to get around the JS form validation
              Future.successful {
                Ok(MustAcceptToS())
              }
            } else {
              // Check database for existing users with the same login name
              val selector = BSONDocument("$or"          ->
                                     List(BSONDocument(User.NAMELOGIN -> signUpFormUser.getUserData.nameLogin),
                                          BSONDocument(User.EMAIL     -> signUpFormUser.getUserData.eMail.head)))
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
                  val selector = BSONDocument(User.IDDB       -> user.userID)
                  val modifier = BSONDocument("$set"          ->
                                 BSONDocument(User.USERDATA   -> signUpFormUser.getUserData),
                                              "$push"         ->
                                 BSONDocument(User.USERTOKENS -> UserToken(tokenType = 1, newEmail = Some(signUpFormUser.getUserData.eMail.head))))
                  modifyUserWithCache(selector,modifier).map {
                    case Some(registeredUser) =>
                      // All done. User is registered, now send the welcome eMail
                      val eMail = NewUserWelcomeMail(tel, registeredUser, registeredUser.userTokens.head.token)
                      eMail.send
                      Ok(LoggedIn(registeredUser)).withSession(sessionCookie(request, registeredUser.sessionID.get, Some(registeredUser.getUserData.nameLogin)))
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
                Ok(FormError())
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
                    val eMail = ChangePasswordMail(tel, updatedUser, token.token)
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
    * Verifies a Token which was sent to the Users eMail address.
    * Token Types: 1 - eMail verification
    *              2 - password change verification
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
              val newTokens : List[UserToken] = userToVerify.userTokens.filterNot(_.tokenType == usedToken.tokenType)
              usedToken.tokenType match {
                case 1 =>
                  // Token for eMail verification
                  modifyUserWithCache(
                             BSONDocument(User.IDDB        -> userToVerify.userID),
                             BSONDocument("$set"           ->
                             BSONDocument(User.ACCOUNTTYPE -> 1,
                                          User.DATEUPDATED -> BSONDateTime(new DateTime().getMillis),
                                          User.USERTOKENS  -> newTokens))).map {
                  case Some(modifiedUser) =>
                    Ok(views.html.auth.message("Account verification was successful."))
                  case None =>
                    Ok(views.html.auth.message("Verification was not successful. Please try again."))
                }
                case 2 =>
                  // Token for password change validation
                  modifyUser(BSONDocument(User.IDDB        -> userToVerify.userID),
                             BSONDocument("$set"           ->
                             BSONDocument(User.PASSWORD    -> usedToken.passwordHash.getOrElse(""),
                                          User.DATEUPDATED -> BSONDateTime(new DateTime().getMillis),
                                          User.USERTOKENS  -> newTokens),
                                          "$unset"         ->
                             BSONDocument(User.SESSIONID   -> "",
                                          User.CONNECTED   -> ""))).map {
                    case Some(modifiedUser) =>
                      removeUserFromCache(user = userToVerify, withDB = false)
                      if(userToVerify.connected) {
                        // TODO maybe add a forced logout here to ensure all open windows are getting logged out.
                      }
                      Ok(views.html.auth.message("Password change verification was successful. Please log in with Your new password."))
                    case None =>
                      Ok(views.html.auth.message("Verification was not successful. Please try again."))
                  }
              }
            case None =>
              Future.successful(Ok(views.html.auth.message("Verification was not successful. Please try again.")))
          }
        case None =>
          Future.successful(Ok(views.html.auth.message("Verification was not successful. Please try again.")))
      }
    }
  }
}
