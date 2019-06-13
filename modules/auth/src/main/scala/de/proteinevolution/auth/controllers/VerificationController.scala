/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.proteinevolution.auth.controllers

import java.time.ZonedDateTime

import de.proteinevolution.auth.dao.UserDao
import de.proteinevolution.auth.models.JSONTemplate
import de.proteinevolution.auth.models.MailTemplate._
import de.proteinevolution.auth.services.UserSessionService
import de.proteinevolution.auth.util.UserAction
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.tel.env.Env
import de.proteinevolution.user.{User, UserToken, AccountType}
import javax.inject.{Inject, Singleton}
import play.api.Environment
import play.api.cache._
import play.api.libs.mailer._
import play.api.mvc._
import reactivemongo.bson._

import scala.concurrent.{ExecutionContext, Future}

@Singleton
final class VerificationController @Inject()(
                                              userDao: UserDao,
                                              userSessionService: UserSessionService,
                                              @NamedCache("wsActorCache") wsActorCache: SyncCacheApi,
                                              environment: Environment,
                                              cc: ControllerComponents,
                                              env: Env,
                                              userAction: UserAction
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
   * @param token
   * @return
   */
  def verification(usernameOrEmail: String, token: String): Action[AnyContent] = userAction.async { implicit request =>
    // Grab the user from the database in case that the logged in user is not the user to verify
    userDao.findUserByUsernameOrEmail(usernameOrEmail, usernameOrEmail).flatMap {
      case Some(userToVerify) =>
        userToVerify.userToken match {
          case Some(userToken) =>
            if (userToken.token == token) {
              userToken.tokenType match {

                case UserToken.EMAIL_VERIFICATION_TOKEN =>
                  userDao.updateAccountType(userToVerify.userID, AccountType.NORMALUSER).map {
                    case Some(updatedUser) =>
                      userSessionService.updateUserInCache(updatedUser)
                      Ok(verificationSuccessful(userToVerify))
                    case None => // Could not save the modified user to the DB
                      Ok(databaseError)
                  }

                case UserToken.PASSWORD_CHANGE_TOKEN =>
                  userToken.passwordHash match {
                    case Some(newPassword) =>
                      val newSessionId: BSONObjectID = BSONObjectID.generate()
                      userDao
                        .modifyUser(
                          userToVerify.userID,
                          BSONDocument(
                            "$set" ->
                            BSONDocument(
                              User.PASSWORD    -> newPassword,
                              User.DATEUPDATED -> BSONDateTime(ZonedDateTime.now.toInstant.toEpochMilli),
                              User.SESSIONID   -> newSessionId
                            ),
                            "$unset" ->
                            BSONDocument(User.SESSIONID -> "", User.CONNECTED -> "", User.USERTOKEN -> "")
                          )
                        )
                        .map {
                          case Some(modifiedUser) =>
                            // take use out of cache to prevent problems with invalid login states
                            userSessionService.removeUserFromCache(request.user)
                            val eMail = PasswordChangedMail(modifiedUser, environment, env)
                            eMail.send
                            // User modified properly. Use new session id for user => login lost in all other sessions
                            Ok(passwordChangeAccepted(modifiedUser)).withSession(
                              userSessionService.sessionCookie(request, newSessionId)
                            )
                          case None => // Could not save the modified user to the DB - failsave in case the DB is down
                            Ok(databaseError)
                        }
                    case None =>
                      // This should not happen - Failsafe when the password hash got overwritten somehow
                      Future.successful(Ok(passwordChangeFailed))
                  }

                case UserToken.PASSWORD_CHANGE_SEPARATE_WINDOW_TOKEN =>
                  // Give a token to the current user to allow him to change the password in a different view (Password Recovery)
                  // TODO: Move password change logic in one route
                  val newToken =
                    UserToken(
                      tokenType = UserToken.PASSWORD_CHANGE_TOKEN,
                      token = userToken.token,
                      userID = Some(userToVerify.userID)
                    )
                  val modifier = BSONDocument(
                    "$set" -> BSONDocument(
                      User.DATEUPDATED -> BSONDateTime(ZonedDateTime.now.toInstant.toEpochMilli),
                      User.USERTOKEN   -> newToken
                    )
                  )
                  userSessionService.modifyUserWithCache(request.user.userID, modifier).map {
                    case Some(_) => Ok(showPasswordResetView)
                    case None => // Could not save the modified user to the DB
                      Ok(databaseError)
                  }
                case _ => Future.successful(Ok(tokenNotFound()))
              }

            } else {
              // No Token in DB
              Future.successful(Ok(tokenMismatch()))
            }
          case None => Future.successful(Ok(tokenNotFound()))
        }
      case None => Future.successful(Ok(accountError()))
    }
  }

}
