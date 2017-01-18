package controllers

import javax.inject.{Inject, Singleton}

import models.database.{User, UserToken}
import models.auth._
import models.mailing.NewUserWelcomeMail
import modules.LocationProvider
import modules.common.RandomString
import modules.tel.TEL
import org.joda.time.DateTime
import play.Logger
import play.api.cache._
import play.api.i18n.{I18nSupport, MessagesApi}
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
final class Auth @Inject() (    webJarAssets     : WebJarAssets,
                            val messagesApi      : MessagesApi,
                   implicit val mailerClient     : MailerClient,
                                implicit val locationProvider: LocationProvider,
@NamedCache("userCache") implicit val userCache  : CacheApi,
                                val tel : TEL,
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
      removeUser(user)

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
            .withSession(sessionCookie(request, user.sessionID.get))
        case None =>
          // User was not logged in
          Redirect(routes.Application.index())
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
    // Evaluate the Form
    FormDefinitions.SignIn.bindFromRequest.fold(
      errors =>
        Future.successful{
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
              val selector = BSONDocument(User.IDDB          -> databaseUser.userID)
              // Change the login time and give the new Session ID to the user.
              // Additionally add the watched jobs to the users watchlist.
              val modifier = BSONDocument("$set"             ->
                             BSONDocument(User.SESSIONID     -> unregisteredUser.sessionID,
                                          User.DATELASTLOGIN -> BSONDateTime(new DateTime().getMillis)),
                                          "$push"            ->
                             BSONDocument(User.JOBS          ->
                             BSONDocument("$each"            -> unregisteredUser.jobs)))
              // Finally add the edits to the collection
              modifyUser(selector, modifier).map {
                case Some(loggedInUser) =>
                  // Remove the old, not logged in user
                  removeUser(BSONDocument(User.IDDB -> unregisteredUser.userID))

                  // Make sure the Cache is updated
                  updateUserCache(loggedInUser)

                  // Everything is ok, let the user know that they are logged in now
                  Ok(LoggedIn(loggedInUser)).withSession(sessionCookie(request, loggedInUser.sessionID.get))
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
            val futureUser = findUser(BSONDocument(User.NAMELOGIN -> signUpFormUser.getUserData.nameLogin))
            futureUser.flatMap {
              case Some(otherUser) =>
                // Other user with the same username exists. Show error message.
                Future.successful(Ok(AccountNameUsed()))
              case None =>
                // Create the database entry.
                val selector = BSONDocument(User.IDDB       -> user.userID)
                val modifier = BSONDocument("$set"          ->
                               BSONDocument(User.USERDATA   -> signUpFormUser.getUserData),
                                            "$push"         ->
                               BSONDocument(User.USERTOKENS -> UserToken(tokenType = 1)))
                modifyUser(selector,modifier).map {
                  case Some(registeredUser) =>
                    // All done. User is registered, now send the welcome eMail
                    val eMail = NewUserWelcomeMail(tel, registeredUser, registeredUser.userTokens.head.token)
                    eMail.send
                    // Make sure the Cache is updated
                    updateUserCache(registeredUser)
                    Ok(LoggedIn(registeredUser)).withSession(sessionCookie(request, registeredUser.sessionID.get))
                  case None =>
                    Ok(FormError())
                }
            }
          }
        }
      )
    }
  }

  /**
    * Function handles the profile edit form submission
 *
    * @return
    */
  def profileSubmit() : Action[AnyContent] = Action.async { implicit request =>
    getUser.flatMap { user =>
      user.userData match {
        case Some(userData) =>
          FormDefinitions.ProfileEdit.bindFromRequest.fold(
            errors =>
              Future.successful{
                Ok(FormError())
              },
            // when there are no errors, then insert the user to the collection
            profileEditFormUser => {
              def futureUser = findUser(BSONDocument(User.IDDB -> user.userID))
              // Get the user option into the present
              futureUser.flatMap {
                case Some(userFromDB) =>
                    // Create a modified user object
                    if (userFromDB.checkPassword(profileEditFormUser.password)) {
                      // create a modifier document to change the last login date in the Database
                      val bsonCurrentTime = BSONDateTime(new DateTime().getMillis)
                      val selector = BSONDocument(User.IDDB -> user.userID)
                      val modifier = BSONDocument("$set"    ->
                                     BSONDocument(User.USERDATA      -> profileEditFormUser.toUserData(userFromDB.getUserData),
                                                  User.DATELASTLOGIN -> bsonCurrentTime,
                                                  User.DATEUPDATED   -> bsonCurrentTime))
                      modifyUser(selector, modifier).map{
                        case Some(updatedUser) =>
                          // Update the user cache
                          updateUserCache(updatedUser)
                          // Everything is ok, let the user know that they are logged in now
                          Ok(EditSuccessful(updatedUser))
                        case None =>
                          // User has been found in the DB at first but now it cant be retrieved
                          Ok(LoginError())
                      }
                    } else {
                      // Password did not match.
                      Future.successful(Ok(PasswordWrong()))
                    }
                case None =>
                  // User got logged out while editing the form.
                  Future.successful(Ok(LoginError()))
              }
            }
          )
        case None =>
          // User was not logged in
          Future.successful(Ok(NotLoggedIn()))
      }
    }
  }

  /**
    * Verifies a Users Email // TODO need to reimplement this.
    *
    * @param name_login
    * @param token
    * @return
    */
  def verification(name_login : String, token : String) = Action { implicit request =>
    //val authAction = userManager.VerifyEmail(name_login, token)
    Ok(views.html.auth.message("Verification"))
  }

  // Mock up function to let a user access to a page only when they are logged in as a user with certain rights
  def backendAccess() : Action[AnyContent] = Action.async { implicit request =>
    getUser.map { user =>
      if (user.isSuperuser) {
        NoCache(Redirect(routes.Backend.access))
      } else {
        NotFound
      }
    }
  }
}
