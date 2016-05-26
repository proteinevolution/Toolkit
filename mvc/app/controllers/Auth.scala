package controllers

import javax.inject.{Singleton, Inject}

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
    * @param userName usually the eMail address
    * @return
    */
  def signIn(userName : String) = Action { implicit request =>
    Ok(views.html.authform.signin(SignIn.inputForm,userName))
  }

  /**
    * Submission of the sign in form, user wants to authenticate themself
    * Checks the Database for the user and logs them in if their password matches
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
        BadRequest("Login Error: " + form.errors.toString)
      },
      _ => {
        // Check the User Database for the user and return the User if there is a match.
        val authAction: AuthAction = userManager.SignIn(form.get._1,  // name_login
                                                        form.get._2)  // password

        if (authAction.success) {
          Session.addUser(sessionID, authAction.user_o.get)
        }
        Ok(views.html.authform.authmessage(authAction)).withSession {
          Session.closeSessionRequest(request, sessionID) // Send Session Cookie
        }
      }
    )
  }

  /**
    * Returns the sign up form
    * @return
    */
  def signUp() = Action { implicit request =>
    Ok(views.html.authform.signup(SignUp.inputForm))
  }

  /**
    * Submission of the sign up form, user wants to register
    * Checks Database if there is a preexisting user and adds them if there is none
    * @return
    */
  def signUpSubmit() = Action { implicit request =>
    val sessionID = Session.requestSessionID(request)
    val form = SignUp.inputForm.bindFromRequest
    form.fold(
      formWithErrors => {
        BadRequest("Login Error: " + form.errors.toString())
      },
      _ => {
        /* while it should not be easy to get to this point without accepting the ToS or mismatching Passwords
         * (due to the javascript preventing them)
         * there should still be a serverside backup to keep them from happening */
        if (form.get._6) {
          Ok(views.html.authform.authmessage(MustAcceptToS()))
        }
        if (form.get._7) {
          Ok(views.html.authform.authmessage(PasswordMismatch()))
        }

        val authAction : AuthAction = userManager.SignUp(form.get._1, // name_login
                                                         form.get._2, // name_last
                                                         form.get._3, // name_first
                                                         form.get._4, // email
                                                         form.get._5) // password // TODO maybe hash the password here?

        if (authAction.success) {
          Session.addUser(sessionID, authAction.user_o.get)
        }

        Ok(views.html.authform.authmessage(authAction)).withSession {
          Session.closeSessionRequest(request, sessionID) // Send Session Cookie
        }
      }
    )
  }

  /**
    * User wants to sign out, Overwrite their cookie and give them a new Session ID
    * @return
    */
  def signOut() = Action { implicit request =>
    val oldSessionID = Session.requestSessionID(request) // grabs the Old Session ID
    Session.removeUser(oldSessionID)  // Remove the User from the association
    val sessionID = Session.newSessionID(request) // Generates a new session ID

    Ok(views.html.authform.authmessage(LoggedOut())).withSession {
      Session.closeSessionRequest(request, sessionID)   // Send Session Cookie
    }
  }

  /**
    * Verifies a Users Email
    * @param userName
    * @param token
    * @return
    */
  def verification(userName : String, token : String) = Action { implicit request =>
    val sessionID = Session.requestSessionID(request)



    Ok(views.html.authform.authmessage(LoggedOut())).withSession {
      Session.closeSessionRequest(request, sessionID)   // Send Session Cookie
    }
  }
}
