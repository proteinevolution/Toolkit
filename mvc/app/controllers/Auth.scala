package controllers

import javax.inject.{Singleton, Inject}

import models.sessions.Session
import models.auth.{SignUp, SignIn}
import play.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

/**
  * Controller for Authentication interactions
  * Created by astephens on 03.04.16.
  */

@Singleton
class Auth @Inject() (val messagesApi: MessagesApi) extends Controller with I18nSupport {

  def signIn(userName : String) = Action { implicit request =>
    val sessionID = Session.requestSessionID(request)
    Logger.info(sessionID + " wants to Sign in!")

    Ok(views.html.authform.signin(SignIn.inputForm,userName)).withSession {
      Session.closeSessionRequest(request, sessionID)   // Send Session Cookie
    }
  }

  def signInSubmit() = Action { implicit request =>
    val sessionID = Session.requestSessionID(request)
    Logger.info(sessionID + " wants to Sign in!")

    val form = SignIn.inputForm.bindFromRequest
    form.fold(
      formWithErrors => {
        BadRequest("Login Error")
      },
      _ => Logger.info("")
    )

    Ok(views.html.authform.authmessage("Welcome. You are now logged in.", routes.Auth.signOut().url)).withSession {
      Session.closeSessionRequest(request, sessionID)   // Send Session Cookie
    }
  }

  def signUp() = Action { implicit request =>
    val sessionID = Session.requestSessionID(request)

    Ok(views.html.authform.signup(SignUp.inputForm)).withSession {
      Session.closeSessionRequest(request, sessionID)   // Send Session Cookie
    }
  }

  def signUpSubmit() = Action { implicit request =>
    val sessionID = Session.requestSessionID(request)
    Logger.info(sessionID + " wants to Register!")
    val form = SignUp.inputForm.bindFromRequest
    form.fold(
      formWithErrors => {
        BadRequest("Login Error:" + form.errors.toString())
      },
      _ => {
        Ok(views.html.authform.authmessage("Welcome." + form.data.toString() + "You are now registered and logged in.", routes.Auth.signOut().url)).withSession {
          Session.closeSessionRequest(request, sessionID) // Send Session Cookie
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
