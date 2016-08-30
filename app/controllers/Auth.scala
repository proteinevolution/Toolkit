package controllers

import javax.inject.{Singleton, Inject}

import models.database.User
import models.auth._
import org.joda.time.DateTime
import play.Logger
import play.api.cache._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson._

import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.Future

/**
  * Controller for Authentication interactions
  * Created by astephens on 03.04.16.
  */

@Singleton
final class Auth @Inject() (webJarAssets     : WebJarAssets,
                        val messagesApi      : MessagesApi,
   @NamedCache("userCache") userCache        : CacheApi,
                        val reactiveMongoApi : ReactiveMongoApi,
                        val mailing          : Mailing) // Mailing Controller
                    extends Controller with I18nSupport
                                       with JSONTemplate
                                       with backendLogin
                                       with Common
                                       with UserSessions {


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
  def miniProfile() = Action.async { implicit request =>
    getUser(request, userCollection, userCache).map { user =>
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
  def signOut() = Action.async { implicit request =>
    getUser(request, userCollection, userCache).map { user =>
      userCache.remove(user.userID.stringify)
      userCollection.flatMap(_.update(BSONDocument(User.IDDB -> user.userID),
                                      BSONDocument("$unset" -> BSONDocument(User.SESSIONID -> ""))))

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
  def profile() = Action.async { implicit request =>
    getUser(request, userCollection, userCache).map { user =>
      user.userData match {
        case Some(userData) =>
          Ok(views.html.auth.profile(user))
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
  def signInSubmit() = Action.async { implicit request =>
    getUser(request, userCollection, userCache).flatMap { unregisteredUser =>
    Logger.info(unregisteredUser.sessionID.get.stringify + " wants to Sign in!")

    // Evaluate the Form
    FormDefinitions.SignIn.bindFromRequest.fold(
      errors =>
        Future.successful{
          Logger.info(" but there was an error in the submit form: " + errors.toString)
          Ok(LoginError())
        },

      // if no error, then insert the user to the collection
      signInFormUser => {
        val futureUser = userCollection.flatMap(_.find(BSONDocument(User.NAMELOGIN -> signInFormUser.nameLogin)).one[User])
        futureUser.flatMap {
          case Some(databaseUser) =>
            // Check the password
            if (databaseUser.checkPassword(signInFormUser.password)) {
              Future {
                // add the remaining jobs from the previous session to the jobs of the now logged in user
                val loggedInUser = databaseUser.copy(sessionID = unregisteredUser.sessionID,
                                                     jobs      = databaseUser.jobs ::: unregisteredUser.jobs)

                // create a modifier document to change the last login date in the Database
                val selector = BSONDocument(User.IDDB          -> loggedInUser.userID)
                val modifier = BSONDocument("$set"             ->
                               BSONDocument(User.SESSIONID     -> unregisteredUser.sessionID,
                                            User.DATELASTLOGIN -> BSONDateTime(new DateTime().getMillis)),
                                            "$addToSet"        ->
                               BSONDocument(User.JOBS          ->
                               BSONDocument("$each"            -> unregisteredUser.jobs)))
                // Finally add the edits to the collection
                userCollection.flatMap(_.update(selector, modifier))
                // Remove the old, not logged in user
                userCollection.flatMap(_.remove(BSONDocument(User.IDDB -> unregisteredUser.userID)))

                // Make sure the Cache is updated
                updateUser(loggedInUser, userCache)

                // Everything is ok, let the user know that they are logged in now
                Ok(LoggedIn(loggedInUser)).withSession(sessionCookie(request, loggedInUser.sessionID.get))
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
  def signUpSubmit() = Action.async { implicit request =>
    getUser(request, userCollection, userCache).flatMap { user =>
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
          val futureUser = userCollection.flatMap(_.find(BSONDocument(User.NAMELOGIN -> signUpFormUser.getUserData.nameLogin)).one[User])
          futureUser.flatMap {
            case Some(otherUser) =>
              // Other user with the same username exists. Show error message.
              Future.successful(Ok(AccountNameUsed()))
            case None =>
              // Create the database entry.
              userCollection.flatMap(_.update(BSONDocument(User.IDDB -> user.userID),signUpFormUser)).map { a =>
                // All done. User is registered
                // Make sure the Cache is updated
                updateUser(user, userCache)
                Ok(LoggedIn(signUpFormUser)).withSession(sessionCookie(request, user.sessionID.get))
              }
          }
        }
      }
    )
  }
  }

  def profileSubmit() = Action.async { implicit request =>
    getUser(request, userCollection, userCache).flatMap { user =>
    user.userData match {
      case Some(userData) =>
        FormDefinitions.ProfileEdit.bindFromRequest.fold(
          errors =>
            Future.successful{
              Ok(FormError())
            },
          // if no error, then insert the user to the collection
          profileEditFormUser => {
            def futureUser = userCollection.flatMap(_.find(BSONDocument(User.IDDB -> user.userID)).one[User])
            // Get the user option into the present
              futureUser.flatMap {
                case Some(userFromDB) =>
                  Future {
                    // Create a modified user object
                    if (userFromDB.checkPassword(profileEditFormUser.password)) {
                      val modifiedUser = userFromDB.copy(sessionID = user.sessionID,
                        userData = Some(profileEditFormUser.toUserData(userFromDB.getUserData)),
                        dateLastLogin = Some(new DateTime()),
                        dateUpdated = Some(new DateTime()))

                      // create a modifier document to change the last login date in the Database
                      val selector = BSONDocument(User.IDDB -> userFromDB.userID)
                      val modifier = BSONDocument("$set" -> modifiedUser)
                      userCollection.flatMap(_.update(selector, modifier))

                      // Everything is ok, let the user know that they are logged in now
                      Ok(EditSuccessful(user))
                    } else {
                      // Password did not match.
                      Ok(PasswordWrong())
                    }
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
    * Verifies a Users Email
    *
    * @param name_login
    * @param token
    * @return
    */
  def verification(name_login : String, token : String) = Action { implicit request =>
    //val authAction = userManager.VerifyEmail(name_login, token)
    Ok(views.html.auth.message("Verification"))
  }



  /*
  def login = Action { implicit request =>

    val sessionID = requestSessionID
    val user : User = getUser

    NoCache(Ok(views.html.backend.login(webJarAssets, "0")).withNewSession)

  }

  def backend = Action { implicit request =>

    val sessionID = requestSessionID
    val user : User = getUser

    //TODO allow direct access to the backend route if user is already authenticated as admin

    if((request.headers.get("referer").getOrElse("").equals("http://" + request.host + "/login") || request.headers.get("referer").getOrElse("").matches("http://" + request.host + "/@/backend.*")) && !this.loggedOut)  {

      Ok(views.html.backend.backend(webJarAssets,views.html.backend.backend_maincontent(), "Backend")).withSession {
        closeSessionRequest(request, sessionID)
      }

    }

    else {

      Status(404)(views.html.errors.pagenotfound())

    }
  }

  var LoginCounter = 0


  def backendLogin () = Action { implicit ctx =>

    val sessionID = requestSessionID(ctx)
    Logger.info(sessionID + " wants to Sign in!")

    // Evaluate the Form
    val form = loginForm.bindFromRequest


    form.fold(
      // Form has errors, return "Bad Request" - most likely timed out or user tampered with form
      formWithErrors => {
        LoginCounter = LoginCounter + 1

        println("Login failed: "+LoginCounter+" attempts on " + Calendar.getInstance().getTime + " from " + ctx.remoteAddress)

        if (LoginCounter > 4) {

          Thread.sleep(20000)
          LoginCounter = 0

        }

        val sessionID = requestSessionID(ctx)
        val user : User = getUser
        Ok(views.html.backend.login(webJarAssets, LoginCounter.toString)).withSession {
          closeSessionRequest(ctx, sessionID)
        }
      },
      _ => {
        // TODO Check the User Database for the user and return the User if there is a match.

        if (form.get._1 == "test" && form.get._2 == "test") {
          LoginCounter = 0

          println("Login to backend detected on: " + Calendar.getInstance().getTime + " from " + ctx.remoteAddress + " with session " + sessionID)
          this.loggedOut = false
          NoCache(Redirect("/@/backend")) // TODO if logged in, users should not need to re-authenticate

        }
        else {
          LoginCounter = LoginCounter + 1

          println("Login failed: "+LoginCounter+" attempts on " + Calendar.getInstance().getTime + " from " + ctx.remoteAddress)

          if (LoginCounter > 4) {

            Thread.sleep(20000)
            LoginCounter = 0

          }

          val sessionID = requestSessionID(ctx)
          val user : User = getUser
          Ok(views.html.backend.login(webJarAssets, LoginCounter.toString)).withSession {
            closeSessionRequest(ctx, sessionID)
          }

        }

      }
    )



  def logout() = Action { implicit ctx =>

    println("logout from dashboard with on " + Calendar.getInstance().getTime + " from " + ctx.remoteAddress + " with " +  ctx.session)
    this.loggedOut = true
    NoCache(Redirect(routes.Application.index())).withNewSession


  }
  }*/

  // Mock up function to let a user access to a page only when they are logged in as a user with certain rights
  def backendAccess() = Action.async { implicit request =>
    getUser(request, userCollection, userCache).map { user =>
      if (user.isSuperuser) {
        NoCache(Redirect(routes.Backend.access))
      } else {
        NotFound
      }
    }
  }
}
