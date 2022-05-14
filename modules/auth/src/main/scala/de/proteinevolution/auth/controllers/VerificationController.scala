/*
 * Copyright 2018 Dept. of Protein Evolution, Max Planck Institute for Biology
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

import de.proteinevolution.auth.dao.UserDao
import de.proteinevolution.auth.models.JSONTemplate
import de.proteinevolution.auth.services.UserSessionService
import de.proteinevolution.auth.util.UserAction
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.user.{ AccountType, UserToken }
import javax.inject.{ Inject, Singleton }
import play.api.libs.mailer._
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
final class VerificationController @Inject(
    userDao: UserDao,
    userSessionService: UserSessionService,
    cc: ControllerComponents,
    userAction: UserAction
)(implicit ec: ExecutionContext, mailerClient: MailerClient)
    extends ToolkitController(cc)
    with JSONTemplate {

  /**
   * Verifies a token which was sent to the users Email address.
   * @param username
   *   username for which verification was requested
   * @param token
   *   token which was sent via mail
   * @return
   */
  def verifyEmailAddress(username: String, token: String): Action[AnyContent] = userAction.async { implicit request =>
    // Grab the user from the database in case that the logged in user is not the user to verify
    userDao.findUserByUsername(username).flatMap {
      case Some(userToVerify) =>
        userToVerify.userToken match {
          case Some(userToken) =>
            if (userToken.token == token && userToken.tokenType == UserToken.EMAIL_VERIFICATION_TOKEN) {
              if (userToken.eMail.get == userToVerify.userData.get.eMail) {
                userDao.updateAccountType(userToVerify.userID, AccountType.REGISTEREDUSER, resetUserToken = true).map {
                  case Some(updatedUser) =>
                    userSessionService.updateUserInCache(updatedUser)
                    Ok(verificationSuccessful(userToVerify))
                  case None => // Could not save the modified user to the DB
                    Ok(databaseError)
                }
              } else {
                Future.successful(Ok(verificationMailMismatch()))
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
