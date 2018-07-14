package de.proteinevolution.verification.controllers

import java.time.ZonedDateTime

import javax.inject.{ Inject, Singleton }
import akka.actor.ActorRef
import controllers.AssetsFinder
import de.proteinevolution.auth.UserSessions
import de.proteinevolution.auth.dao.UserDao
import de.proteinevolution.auth.models.JSONTemplate
import de.proteinevolution.models.database.users.{ User, UserToken }
import de.proteinevolution.auth.models.MailTemplate._
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.message.actors.WebSocketActor.LogOut
import de.proteinevolution.services.ToolConfig
import de.proteinevolution.tel.env.Env
import play.api.cache._
import play.api.mvc._
import play.api.libs.mailer._
import reactivemongo.bson._
import org.webjars.play.WebJarsUtil
import play.api.Environment

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
final class VerificationController @Inject()(
    webJarsUtil: WebJarsUtil,
    userDao: UserDao,
    toolConfig: ToolConfig,
    userSessions: UserSessions,
    @NamedCache("wsActorCache") wsActorCache: SyncCacheApi,
    environment: Environment,
    environment2: play.Environment,
    assets: AssetsFinder,
    cc: ControllerComponents,
    env: Env
)(implicit ec: ExecutionContext, mailerClient: MailerClient)
    extends ToolkitController(cc)
    with JSONTemplate {

  /**
   * Verifies a Token which was sent to the Users eMail address.
   * Token Types: 1 - eMail verification
   * 2 - password change verification
   * 3 - password reset verification
   * 4 -                             + reset
   *
   *
   * @param token
   * @return
   */
  def verification(nameLogin: String, token: String): Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.flatMap { user: User =>
      // Grab the user from the database in case that the logged in user is not the user to verify
      // TODO check for both name or email
      userDao.findUser(BSONDocument(User.NAMELOGIN -> nameLogin)).flatMap {
        case Some(userToVerify) =>
          userToVerify.userToken match {
            case Some(userToken) =>
              if (userToken.token == token) {
                userToken.tokenType match {
                  case 1 => // Token for eMail verification
                    userDao
                      .modifyUser(
                        BSONDocument(User.IDDB -> userToVerify.userID),
                        BSONDocument(
                          "$set" ->
                          BSONDocument(User.ACCOUNTTYPE -> 1,
                                       User.DATEUPDATED -> BSONDateTime(ZonedDateTime.now.toInstant.toEpochMilli)),
                          BSONDocument(
                            "$unset" ->
                            BSONDocument(User.USERTOKEN -> "")
                          )
                        )
                      )
                      .map {
                        case Some(_) =>
                          Ok(
                            views.html.main(assets,
                                            webJarsUtil,
                                            toolConfig.values.values.toSeq.sortBy(_.toolNameLong),
                                            "Account verification was successful. Please log in.",
                                            "",
                                            environment)
                          )
                        case None => // Could not save the modified user to the DB
                          Ok(
                            views.html.main(
                              assets,
                              webJarsUtil,
                              toolConfig.values.values.toSeq.sortBy(_.toolNameLong),
                              "Verification was not successful due to a database error. Please try again later.",
                              "",
                              environment
                            )
                          )
                      }
                  case 2 => // Token for password change validation
                    userToken.passwordHash match {
                      case Some(newPassword) =>
                        userDao
                          .modifyUser(
                            BSONDocument(User.IDDB -> userToVerify.userID),
                            BSONDocument(
                              "$set" ->
                              BSONDocument(
                                User.PASSWORD    -> newPassword,
                                User.DATEUPDATED -> BSONDateTime(ZonedDateTime.now.toInstant.toEpochMilli)
                              ),
                              "$unset" ->
                              BSONDocument(User.SESSIONID -> "", User.CONNECTED -> "", User.USERTOKEN -> "")
                            )
                          )
                          .map {
                            case Some(modifiedUser) =>
                              userSessions.removeUserFromCache(user)
                              val eMail = PasswordChangedMail(modifiedUser, environment2, env)
                              eMail.send
                              // Force Log Out on all connected users.
                              (wsActorCache.get(modifiedUser.userID.stringify): Option[List[ActorRef]]) match {
                                case Some(webSocketActors) =>
                                  webSocketActors.foreach(_ ! LogOut)
                                case None =>
                              }
                              // User modified properly
                              Ok(
                                views.html.main(
                                  assets,
                                  webJarsUtil,
                                  toolConfig.values.values.toSeq.sortBy(_.toolNameLong),
                                  "Password change verification was successful. Please log in with Your new password.",
                                  "",
                                  environment
                                )
                              )
                            case None => // Could not save the modified user to the DB - failsave in case the DB is down
                              Ok(
                                views.html.main(
                                  assets,
                                  webJarsUtil,
                                  toolConfig.values.values.toSeq.sortBy(_.toolNameLong),
                                  "Verification was not successful due to a database error. Please try again later.",
                                  "",
                                  environment
                                )
                              )
                          }
                      case None =>
                        // This should not happen - Failsafe when the password hash got overwritten somehow
                        Future.successful(
                          Ok(
                            views.html.main(
                              assets,
                              webJarsUtil,
                              toolConfig.values.values.toSeq.sortBy(_.toolNameLong),
                              "The Password you have entered was insufficient, please create a new one.",
                              "",
                              environment
                            )
                          )
                        )
                    }

                  case 3 =>
                    // Give a token to the current user to allow him to change the password in a different view (Password Recovery)
                    val newToken =
                      UserToken(tokenType = 4, token = userToken.token, userID = Some(userToVerify.userID))
                    val selector = BSONDocument(User.IDDB -> user.userID)
                    val modifier = BSONDocument(
                      "$set" -> BSONDocument(
                        User.DATEUPDATED -> BSONDateTime(ZonedDateTime.now.toInstant.toEpochMilli),
                        User.USERTOKEN   -> newToken
                      )
                    )
                    userSessions.modifyUserWithCache(selector, modifier).map {
                      case Some(_) =>
                        Ok(
                          views.html.main(assets,
                                          webJarsUtil,
                                          toolConfig.values.values.toSeq.sortBy(_.toolNameLong),
                                          "",
                                          "passwordReset",
                                          environment)
                        )
                      case None => // Could not save the modified user to the DB
                        Ok(
                          views.html.main(
                            assets,
                            webJarsUtil,
                            toolConfig.values.values.toSeq.sortBy(_.toolNameLong),
                            "Verification was not successful due to a database error. Please try again later.",
                            "",
                            environment
                          )
                        )
                    }
                  case _ =>
                    Future.successful(
                      Ok(
                        views.html.main(assets,
                                        webJarsUtil,
                                        toolConfig.values.values.toSeq.sortBy(_.toolNameLong),
                                        "There was an error finding your token.",
                                        "",
                                        environment)
                      )
                    )
                }

              } else {
                // No Token in DB
                Future.successful(
                  Ok(
                    views.html.main(assets,
                                    webJarsUtil,
                                    toolConfig.values.values.toSeq.sortBy(_.toolNameLong),
                                    "The token you used is not valid.",
                                    "",
                                    environment)
                  )
                )
              }
            case None =>
              Future.successful(
                Ok(
                  views.html.main(assets,
                                  webJarsUtil,
                                  toolConfig.values.values.toSeq.sortBy(_.toolNameLong),
                                  "There was an error finding your token.",
                                  "",
                                  environment)
                )
              )
          }
        case None =>
          Future.successful(
            Ok(
              views.html.main(assets,
                              webJarsUtil,
                              toolConfig.values.values.toSeq.sortBy(_.toolNameLong),
                              "There was an error finding your account.",
                              "",
                              environment)
            )
          )
      }
    }
  }

}
