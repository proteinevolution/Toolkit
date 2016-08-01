package controllers

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
          maybeUser <- futureUser
          resultPage <- maybeUser.map { user =>
              if (user.checkPassword(userForm.password)) {
                Future.successful{
                  Logger.info(" and they succeded!")
                  // add the user to the current sessions
                  Session.addUser(sessionID, user)

                  // create a modifier document to change the last login date
                  val selector = BSONDocument(User.IDDB -> user.userID)
                  val modifier = BSONDocument("$set" -> BSONDocument(
                                     User.DATELASTLOGIN -> BSONDateTime(new DateTime().getMillis)))
                  userCollection.flatMap(_.update(selector, modifier))
                  Ok(LoggedIn(user))
                }
              } else {
                Future.successful{
                  Logger.info(" but they had the wrong password.")
                  Ok(LoginIncorrect())
                }
              }
          }.getOrElse{
            Logger.info(" but they had the wrong account name.")
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

        //println(LoginCounter)

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

          Redirect("/@/backend") // TODO if logged in, users should not need to re-authenticate

        }
        else {
          LoginCounter = LoginCounter + 1

          //println(LoginCounter)

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
    Logger.info(sessionID + " wants to Sign up!")
    User.formSignUp.bindFromRequest.fold(
      errors =>
        Future.successful {
          Logger.info(" but there was an error in the submit form: " + errors.toString)
          Ok(LoginError())
        },

      // if no error, then insert the user to the collection
      user =>
        if (user.accountType < 1) {
          Future.successful{
            Logger.info(" but they did not accept the ToS!")
            Ok(MustAcceptToS())
          }
        } else {/*
          // Check database for existing users with the same login name
          def futureUser =
            userCollection.flatMap(_.find(Json.obj(MongoDBUser.NAMELOGIN -> user.nameLogin)).one[MongoDBUser])
          futureUser.map {
            case Some(databaseUser) =>
              Ok(AccountNameUsed())
            case None =>*/
              // Create the database entry
              val currentDateTime = Some(new DateTime())
              val newUser = user.copy(
                dateLastLogin = currentDateTime,
                dateCreated   = currentDateTime,
                dateUpdated   = currentDateTime)
              userCollection.flatMap(_.insert(newUser)).map(_ => {
              Logger.info(" and they succeeded!")
              Session.addUser(sessionID, user)
              Ok(LoggedIn(newUser)).withSession {
                Session.closeSessionRequest(request, sessionID) // Send Session Cookie
              }}
            )
          //}
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
    val oldSessionID = Session.requestSessionID(request) // grabs the Old Session ID
    Session.removeUser(oldSessionID)  // Remove the User from the association
    val sessionID = Session.newSessionID(request) // Generates a new session ID

    Ok(views.html.auth.message("You have been logged out Successfully")).withSession {
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
    //val authAction = userManager.VerifyEmail(name_login, token)
    Ok(views.html.auth.message("Verification"))
  }
}
