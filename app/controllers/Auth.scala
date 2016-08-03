package controllers

import java.util.Calendar
import javax.inject.{Singleton, Inject}

import models.database.User
import models.sessions.Session
import models.auth._
import org.joda.time.DateTime
import play.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

import play.modules.reactivemongo.{ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.FailoverStrategy
import reactivemongo.api.collections.bson.BSONCollection
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
                        val reactiveMongoApi : ReactiveMongoApi,
                        val mailing          : Mailing) // Mailing Controller
                    extends Controller with I18nSupport
                                       with JSONTemplate
                                       with backendLogin
                                       with ReactiveMongoComponents {


  // get the collection 'users'
  def userCollection = reactiveMongoApi.database.map(_.collection("users").as[BSONCollection](FailoverStrategy()))

  /**
    * Returns the sign in form
    *
    * @param userName usually the eMail address
    * @return
    */
  def signIn(userName : String) = Action { implicit request =>
    Ok(views.html.auth.signin(User.formSignIn,userName))
  }

  /**
    * Submission of the sign in form, user wants to authenticate themself
    * Checks the Database for the user and logs them in if their password matches
    *
    * @return
    */
  def signInSubmit() = Action.async { implicit request =>
    val sessionID = Session.requestSessionID(request)
    Logger.info(sessionID + " wants to Sign in!")

    // Evaluate the Form
    User.formSignIn.bindFromRequest.fold(
      errors =>
        Future.successful{
          Logger.info(" but there was an error in the submit form: " + errors.toString)
          Ok(LoginError())
        },

      // if no error, then insert the user to the collection
      userForm => {
        def futureUser = userCollection.flatMap(_.find(BSONDocument(User.NAMELOGIN -> userForm.nameLogin)).one[User])
        for {
        // Get the user option into the present
          maybeUser <- futureUser
          resultPage <- maybeUser.map { user =>
            // Check the password
            if (user.checkPassword(userForm.password)) {
              Future.successful{
                // add the user to the current sessions
                Session.addUser(sessionID, user)

                // create a modifier document to change the last login date in the Database
                val selector = BSONDocument(User.IDDB -> user.userID)
                val modifier = BSONDocument("$set" -> BSONDocument(
                  User.DATELASTLOGIN -> BSONDateTime(new DateTime().getMillis)))
                userCollection.flatMap(_.update(selector, modifier))
                // Everything is ok, let the user know that they are logged in now
                Ok(LoggedIn(user))
              }
            } else {
              Future.successful{
                // Wrong Password, show the error message
                Ok(LoginIncorrect())
              }
            }
          }.getOrElse{
            // There is no such User account, let the User know about that
            Future.successful(Ok(LoginIncorrect()))
          }
        } yield resultPage
      })
  }


  def login = Action { implicit request =>

    val session_id = Session.requestSessionID(request)
    val user_o : Option[User] = Session.getUser(session_id)

    Ok(views.html.backend.login(webJarAssets, "0", user_o)).withSession {
      Session.closeSessionRequest(request, session_id)
    }

  }

  def backend = Action { implicit request =>

    val session_id = Session.requestSessionID(request)
    val user_o : Option[User] = Session.getUser(session_id)

  //TODO allow direct access to the backend route if user is already authenticated as admin

  if(request.headers.get("referer").getOrElse("").equals("http://" + request.host + "/login") || request.headers.get("referer").getOrElse("").matches("http://" + request.host + "/@/backend.*")) {

    Ok(views.html.backend.backend(webJarAssets,views.html.backend.backend_maincontent(), "Backend", user_o)).withSession {
      Session.closeSessionRequest(request, session_id)
    }

  }

  else {

    Status(404)(views.html.errors.pagenotfound())

    }
  }

  var LoginCounter = 0

  def backendLogin () = Action { implicit ctx =>

    val sessionID = Session.requestSessionID(ctx)
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

        val sessionID = Session.requestSessionID(ctx)
        val user_o : Option[User] = Session.getUser(sessionID)
        Ok(views.html.backend.login(webJarAssets, LoginCounter.toString, user_o)).withSession {
          Session.closeSessionRequest(ctx, sessionID)
        }
      },
      _ => {
        // TODO Check the User Database for the user and return the User if there is a match.

        if (form.get._1 == "test" && form.get._2 == "test") {
          LoginCounter = 0

          println("Login to backend detected on: " + Calendar.getInstance().getTime + " from " + ctx.remoteAddress + " with session " + sessionID)
          Redirect("/@/backend") // TODO if logged in, users should not need to re-authenticate

        }
        else {
          LoginCounter = LoginCounter + 1

          println("Login failed: "+LoginCounter+" attempts on " + Calendar.getInstance().getTime + " from " + ctx.remoteAddress)

          if (LoginCounter > 4) {

            Thread.sleep(20000)
            LoginCounter = 0

          }

          val sessionID = Session.requestSessionID(ctx)
          val user_o : Option[User] = Session.getUser(sessionID)
          Ok(views.html.backend.login(webJarAssets, LoginCounter.toString, user_o)).withSession {
            Session.closeSessionRequest(ctx, sessionID)
          }

        }

      }
    )
  }

  // Mock up function to let a user access to a page only when they are logged in as a user with certain rights
  def backendAccess() = Action { implicit request =>
    val sessionID = Session.requestSessionID(request)
    val user_o = Session.getUser(sessionID)
    if (user_o.get.isSuperuser) {
      Redirect("@/backend")
    } else {
      Status(404)(views.html.errors.pagenotfound())
    }
  }

  /**
    * Returns the sign up form
    *
    * @return
    */
  def signUp() = Action { implicit request =>
    Ok(views.html.auth.signup(User.formSignUp))
  }

  /**
    * Submission of the sign up form, user wants to register
    * Checks Database if there is a preexisting user and adds them if there is none
    *
    * @return
    */
  def signUpSubmit() = Action.async { implicit request =>
    val sessionID = Session.requestSessionID(request)
    User.formSignUp.bindFromRequest.fold(
      errors =>
        // Something went wrong with the Form.
        Future.successful {
          Ok(LoginError())
        },

      // if no error, then insert the user to the collection
      newUser =>
        if (newUser.accountType < 1) {
          // User did not accept the Terms of Service but managed to get around the JS form validation
          Future.successful{
            Ok(MustAcceptToS())
          }
        } else {
          // Check database for existing users with the same login name
          val futureUser = userCollection.flatMap(_.find(BSONDocument(User.NAMELOGIN -> newUser.nameLogin)).one[User])
          futureUser.flatMap { possibleOtherUser =>
            if (possibleOtherUser.isDefined) {
              // Other user with the same username exists. Show error message.
              Future.successful(Ok(AccountNameUsed()))
            } else {
              // Create the database entry.
              userCollection.flatMap(_.insert(newUser)).map { _ =>
                Session.addUser(sessionID, newUser)
                // All done. User is registered
                Ok(LoggedIn(newUser)).withSession {
                  Session.closeSessionRequest(request, sessionID) // Send Session Cookie
                }
              }
            }
          }
        }
    )
  }

  /**
    * Shows a small profile in the login panel when the User is signed in.
    *
    * @return
    */
  def miniProfile() = Action { implicit request =>
    val session = Session.requestSessionID(request)
    val user_o  = Session.getUser(session)
    user_o match {
      case Some(user) =>
        Ok(views.html.auth.miniprofile(user))
      case None =>
        BadRequest
    }
  }

  /**
    * User wants to sign out, Overwrite their cookie and give them a new Session ID
    *
    * @return
    */
  def signOut() = Action { implicit request =>
    val sessionID = Session.requestSessionID(request) // grab the Old Session ID
    Session.removeUser(sessionID)  // Remove the User from the association

    Redirect(routes.Application.index()).withNewSession.flashing(
      "success" -> "You've been logged out"
    )
  }


  def logout() = Action {
    Redirect(routes.Application.index()).withNewSession.flashing(
      "success" -> "You've been logged out"
    )
  }

  def profile() = Action { implicit request =>
    val sessionID = Session.requestSessionID(request)
    val user_o  = Session.getUser(sessionID)
    user_o match {
      case Some(user) =>
        Ok(views.html.auth.profile(user, User.formProfileEdit, User.formProfilePasswordEdit))
      case None =>
        // User was not logged in
        Redirect(routes.Application.index())
    }
  }

  def profileSubmit() = Action.async { implicit request =>
    val sessionID = Session.requestSessionID(request)
    val user_o = Session.getUser(sessionID)
    user_o match {
      case Some(user) =>
        User.formProfileEdit.bindFromRequest.fold(
          errors =>
            Future.successful{
              Ok(FormError())
            },
          // if no error, then insert the user to the collection
          userDataForm => {
            def futureUser = userCollection.flatMap(_.find(BSONDocument(User.IDDB -> user.userID)).one[User])
            for {
            // Get the user option into the present
              maybeUser <- futureUser
              resultPage <- maybeUser.map { userFromDB =>
                Future.successful{
                  // Create a modified user object
                  val modifiedUser = userFromDB.copy(userData      = userDataForm,
                                                     dateLastLogin = Some(new DateTime()),
                                                     dateUpdated   = Some(new DateTime()))

                  // overwrite the sessions based user
                  Session.editUser(sessionID, modifiedUser)

                  // create a modifier document to change the last login date in the Database
                  val selector = BSONDocument(User.IDDB -> userFromDB.userID)
                  val modifier = BSONDocument("$set"    -> modifiedUser)
                  userCollection.flatMap(_.update(selector, modifier))
                  // Everything is ok, let the user know that they are logged in now
                  Ok(EditSuccessful(user))
                }
              }.getOrElse(Future.successful(Ok(FormError())))
            } yield resultPage
          }
        )
      case None =>
        // User was not logged in
        Future.successful(Ok(NotLoggedIn()))
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
}
