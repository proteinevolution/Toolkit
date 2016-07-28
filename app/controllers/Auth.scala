package controllers

import javax.inject.{Singleton, Inject}

import models.database.User
import models.sessions.Session
import models.auth._
import play.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results._
import play.api.mvc.{Action, Controller}
import play.api.libs.json._

/**
  * Controller for Authentication interactions
  * Created by astephens on 03.04.16.
  */

@Singleton
final class Auth @Inject() (userManager : UserManager,
                      webJarAssets: WebJarAssets,
                  val messagesApi : MessagesApi) extends Controller with I18nSupport with JSONTemplate with backendLogin {

  /**
    * Returns the sign in form
    *
    * @param userName usually the eMail address
    * @return
    */
  def signIn(userName : String) = Action { implicit request =>
    Ok(views.html.auth.signin(SignIn.inputForm,userName))
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
        BadRequest("Login Error: " + form.errors.toString)
      },
      _ => {
        // Check the User Database for the user and return the User if there is a match.
        val authAction: AuthAction = userManager.SignIn(form.get._1,  // name_login
                                                        form.get._2)  // password

        if (authAction.success) {
          Session.addUser(sessionID, authAction.user_o.get)
        }
        // Send a JSON with the status to the user so that the Form can be modified dynamically
        val json = Json.toJson(authActionToJSON(authAction))
        Ok(json).withSession {
          Session.closeSessionRequest(request, sessionID) // Send Session Cookie
        }
      }
    )
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

    if(!request.headers.get("referer").getOrElse("").equals("http://" + request.host + "/login")) {

      Status(404)(views.html.errors.pagenotfound())
      //Ok(views.html.backend.backend(webJarAssets, "Backend", user_o)).withSession {
      //  Session.closeSessionRequest(request, session_id)
      //}

    }

    else {
      Ok(views.html.backend.backend(webJarAssets,views.html.backend.backend_maincontent(), "Backend", user_o)).withSession {
        Session.closeSessionRequest(request, session_id)
      }
    }

  }

  var LoginCounter = 0


  def backendLogin () = Action { implicit request =>

    val sessionID = Session.requestSessionID(request)
    Logger.info(sessionID + " wants to Sign in!")


    // Evaluate the Form
    val form = loginForm.bindFromRequest


    form.fold(
      // Form has errors, return "Bad Request" - most likely timed out or user tampered with form
      formWithErrors => {
        LoginCounter = LoginCounter + 1

        //println(LoginCounter)

        if (LoginCounter > 4) {

          Thread.sleep(20000)
          LoginCounter = 0

        }

        val sessionID = Session.requestSessionID(request)
        val user_o : Option[User] = Session.getUser(sessionID)
        Ok(views.html.backend.login(webJarAssets, LoginCounter.toString, user_o)).withSession {
          Session.closeSessionRequest(request, sessionID)
        }
      },
      _ => {
        // Check the User Database for the user and return the User if there is a match.

        val authAction: AuthAction = userManager.backendLogin(form.get._1, // name_login
          form.get._2) // password

        if (authAction.success) {
          LoginCounter = 0
          Session.addUser(sessionID, authAction.user_o.get)

          //Ok(views.html.backend.backend(webJarAssets, "Backend", authAction.user_o)).withSession {
          //  Session.closeSessionRequest(request, sessionID)
          //
          Redirect("/backend") // TODO if logged in, users should not need to re-authenticate

        }
        else {
          LoginCounter = LoginCounter + 1

          //println(LoginCounter)

          if (LoginCounter > 4) {

            Thread.sleep(20000)
            LoginCounter = 0

          }

          val sessionID = Session.requestSessionID(request)
          val user_o : Option[User] = Session.getUser(sessionID)
          Ok(views.html.backend.login(webJarAssets, LoginCounter.toString, user_o)).withSession {
            Session.closeSessionRequest(request, sessionID)
          }

        }

      }
    )
  }



  /**
    * Returns the sign up form
    *
    * @return
    */
  def signUp() = Action { implicit request =>
    Ok(views.html.auth.signup(SignUp.inputForm))
  }

  /**
    * Submission of the sign up form, user wants to register
    * Checks Database if there is a preexisting user and adds them if there is none
    *
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
          Ok(views.html.auth.message(MustAcceptToS()))
        }
        if (form.get._7) {
          Ok(views.html.auth.message(PasswordMismatch()))
        }

        val authAction : AuthAction = userManager.SignUp(form.get._1, // name_login
                                                         form.get._2, // name_last
                                                         form.get._3, // name_first
                                                         form.get._4, // email
                                                         form.get._5) // password // TODO maybe hash the password here?

        if (authAction.success) {
          Session.addUser(sessionID, authAction.user_o.get)
        }
        // Send a JSON with the status to the user so that the Form can be modified dynamically
        val json = Json.toJson(authActionToJSON(authAction))
        Ok(json).withSession {
          Session.closeSessionRequest(request, sessionID) // Send Session Cookie
        }
      }
    )
  }

  def miniProfile() = Action { implicit request =>
    val session = Session.requestSessionID(request)
    val user_o = Session.getUser(session)
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
    val oldSessionID = Session.requestSessionID(request) // grabs the Old Session ID
    Session.removeUser(oldSessionID)  // Remove the User from the association
    val sessionID = Session.newSessionID(request) // Generates a new session ID

    Ok(views.html.auth.message(LoggedOut())).withSession {
      Session.closeSessionRequest(request, sessionID)   // Send Session Cookie
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
    val authAction = userManager.VerifyEmail(name_login, token)
    Ok(views.html.auth.message(authAction))
  }
}
