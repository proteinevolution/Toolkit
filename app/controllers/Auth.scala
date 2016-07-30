package controllers
import scala.concurrent.ExecutionContext.Implicits.global
import javax.inject.{Singleton, Inject}

import models.database.MongoDBUser
import models.sessions.Session
import models.auth._
import org.joda.time.DateTime
import play.Logger
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import play.api.libs.json._
import play.modules.reactivemongo.{ReactiveMongoApi, ReactiveMongoComponents, MongoController}
import reactivemongo.api.gridfs.ReadFile
import reactivemongo.play.json._
import reactivemongo.play.json.collection.JSONCollection

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

  import java.util.UUID

  type JSONReadFile = ReadFile[JSONSerializationPack.type, JsString]

  // get the collection 'users'
  def userCollection = reactiveMongoApi.database.map(_.collection[JSONCollection]("users"))

  // get the collection 'userData'
  def userDataCollection = reactiveMongoApi.database.map(_.collection[JSONCollection]("userData"))


  /**
    * Returns the sign in form
    *
    * @param userName usually the eMail address
    * @return
    */
  def signIn(userName : String) = Action { implicit request =>
    Ok(views.html.auth.signin(MongoDBUser.formSignIn,userName))
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
    MongoDBUser.formSignIn.bindFromRequest.fold(
      errors =>
        Future.successful{
          Logger.info(" but there was an error in the submit form: " + errors.toString)
          Ok(LoginError())
        },

      // if no error, then insert the user to the collection
      userForm => {
        def futureUser = userCollection.flatMap(_.find(Json.obj(MongoDBUser.NAMELOGIN -> userForm.nameLogin)).one[MongoDBUser])
        for {
          maybeUser <- futureUser
          resultPage <- maybeUser.map { user =>
              if (user.checkPassword(userForm.password)) {
                Future.successful{
                  Logger.info(" and they succeded!")
                  // add the user to the current sessions
                  Session.addUser(sessionID, user)

                  // create a modifier document to change the last login date
                  val modifier = Json.obj("$set" -> Json.obj(MongoDBUser.DATELASTLOGIN -> new DateTime().getMillis))
                  userCollection.flatMap(_.update(Json.obj(MongoDBUser.IDDB -> user.id), modifier))
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

  /*
  def login = Action { implicit request =>

    val session_id = Session.requestSessionID(request)
    val user_o : Option[MongoDBUser] = Session.getUser(session_id)

    Ok(views.html.backend.login(webJarAssets, "0", user_o)).withSession {
      Session.closeSessionRequest(request, session_id)
    }

  }

  /*def backend = Action { implicit request =>

    val session_id = Session.requestSessionID(request)
    val user_o : Option[User] = Session.getUser(session_id)

  //TODO allow direct access to the backend route if user is already authenticated as admin

  if(!request.headers.get("referer").getOrElse("").equals("http://" + request.host + "/login")) {

  Status(404)(views.html.errors.pagenotfound())

  }

  else {
  Ok(views.html.backend.backend(webJarAssets, "Backend", user_o)).withSession {
  Session.closeSessionRequest(request, session_id)
  }
  }

  }*/

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
        val user_o : Option[MongoDBUser] = Session.getUser(sessionID)
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

          Ok(views.html.backend.backend(webJarAssets, "Backend", authAction.user_o)).withSession {
            Session.closeSessionRequest(request, sessionID)
          }

        }
        else {
          LoginCounter = LoginCounter + 1

          //println(LoginCounter)

          if (LoginCounter > 4) {

            Thread.sleep(20000)
            LoginCounter = 0

          }

          val sessionID = Session.requestSessionID(request)
          val user_o : Option[MongoDBUser] = Session.getUser(sessionID)
          Ok(views.html.backend.login(webJarAssets, LoginCounter.toString, user_o)).withSession {
            Session.closeSessionRequest(request, sessionID)
          }

        }

      }
    )
  }
  */

  /**
    * Returns the sign up form
    *
    * @return
    */
  def signUp() = Action { implicit request =>
    Ok(views.html.auth.signup(MongoDBUser.formSignUp))
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
    MongoDBUser.formSignUp.bindFromRequest.fold(
      errors =>
        Future.successful {
          Logger.info(" but there was an error in the submit form: " + errors.toString)
          Ok(LoginError())
        },

      // if no error, then insert the user to the collection
      user =>
        if (user.accountType == 0) {
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
                id            = user.id.orElse(Some(UUID.randomUUID().toString)),
                lastLoginDate = currentDateTime,
                creationDate  = currentDateTime,
                updateDate    = currentDateTime)
              userCollection.flatMap(_.insert(newUser)).map(_ => {
              Logger.info(" and they succeded!")
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
