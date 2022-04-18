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
import de.proteinevolution.auth.services.UserSessionService
import de.proteinevolution.auth.util.UserAction
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.common.models.ConstantsV2
import io.circe.syntax._
import io.circe.{ Json, JsonObject }
import javax.inject.{ Inject, Singleton }
import play.api.mvc.{ Action, AnyContent, ControllerComponents }

import scala.concurrent.ExecutionContext

@Singleton
class ValidationController @Inject() (
    userSessions: UserSessionService,
    userDao: UserDao,
    constants: ConstantsV2,
    cc: ControllerComponents,
    userAction: UserAction
)(implicit
    ec: ExecutionContext
) extends ToolkitController(cc) {

  final private[this] def isValid(v: Boolean): Json = JsonObject("isValid" -> Json.fromBoolean(v)).asJson

  def validateModellerKey(inputOpt: Option[String]): Action[AnyContent] = userAction.async { implicit request =>
    inputOpt match {
      case Some(input) =>
        val user = request.user
        if (user.userConfig.hasMODELLERKey) {
          fuccess(Ok(isValid(true)))
        } else if (input == constants.modellerKey) {
          userDao.updateUserConfig(user.userID, user.userConfig.copy(hasMODELLERKey = true)).map {
            case Some(updatedUser) =>
              userSessions.updateUserInCache(updatedUser)
              Ok(isValid(true))
            case None =>
              BadRequest
          }
        } else {
          fuccess(Ok(isValid(false)))
        }
      case None =>
        fuccess(BadRequest)
    }
  }

}
