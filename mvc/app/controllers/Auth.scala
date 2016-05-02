package controllers

import javax.inject.{Singleton, Inject}

import models.database.User
import models.sessions.Session
import models.auth._
import play.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

/**
  * Controller for Authentication interactions
  * Created by astephens on 03.04.16.
  */

@Singleton
class Auth @Inject() (userManager : UserManager,
                  val messagesApi : MessagesApi) extends Controller with I18nSupport {

  /**
    * Returns the sign in form
 *
    * @param userName usually the eMail address
    * @return
    */
  def signIn(userName : String) = Action { implicit request =>
    Ok(views.html.authform.signin(SignIn.inputForm,userName))
  }

  /**
    * Submission of the sign in form, user wants to authenticate themself
    * Checks the Database for the user and logs them in if their password matches
 *
    * @return
    */
  def signInSubmit() = Action { implicit request =>
    val sessionID = Session.requestSessionID(request)
    Logger.info(sessionID + " wants to Sign in!")

    // Evaluate the Form
    val form = SignIn.inputForm.bindFromRequest
    form.fold(
      // Form has errors, return "Bad Request" - most likely timed out or user tampered with form
      formWithErrors => {
        BadRequest("Login Error: " + form.errors.toString())
      },
      _ => {
        // Check the User Database for the user and return the User if there is a match.
        val authAction: AuthAction = userManager.SignIn(form.data)
        authAction match {

          case LoggedIn(user: User) =>
            Ok(views.html.authform.authmessage("Welcome, " + user.name_last + ". \n" +
                                               "You are now registered and logged in.",
                                               routes.Auth.signOut().url)).withSession {
              Session.closeSessionRequest(request, sessionID) // Send Session Cookie
            }

          case LoginIncorrect() =>
            Ok(views.html.authform.authmessage("There is no User with this E-Mail or the Password is incorrect.",
                                               routes.Auth.signIn("").url))

          case _ => InternalServerError
        }
      }
    )
  }

  def signUp() = Action { implicit request =>
    Ok(views.html.authform.signup(SignUp.inputForm))
  }

  def signUpSubmit() = Action { implicit request =>
    val sessionID = Session.requestSessionID(request)
    Logger.info(sessionID + " wants to Register!")
    val form = SignUp.inputForm.bindFromRequest
    form.fold(
      formWithErrors => {
        BadRequest("Login Error: " + form.errors.toString())
      },
      _ => {
        val authAction : AuthAction = userManager.SignUp(form.data)
        authAction match {

          case LoggedIn(user : User) =>
            Ok(views.html.authform.authmessage("Welcome, " + user.name_last + ". \n" +
              "You are now registered and logged in.",
              routes.Auth.signOut().url)).withSession {
              Session.closeSessionRequest(request, sessionID) // Send Session Cookie
            }

          case EmailUsed() =>
            Ok(views.html.authform.authmessage("The provided E-Mail has been used already.", routes.Auth.signUp().url))

          case _ =>
            InternalServerError
        }
      }
    )
  }

  def signOut() = Action { implicit request =>
    val sessionID = Session.requestSessionID(request)
    Logger.info(sessionID + " wants to Logout!")

    Ok(views.html.authform.authmessage("Bye! You are now logged out!", routes.Auth.signIn("").url)).withSession {
      Session.closeSessionRequest(request, sessionID)   // Send Session Cookie
    }
  }
}
