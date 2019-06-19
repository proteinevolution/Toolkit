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

import de.proteinevolution.auth.UserSessions
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.common.models.ConstantsV2
import de.proteinevolution.user.{ User, UserConfig }
import io.circe.syntax._
import io.circe.{ Json, JsonObject }
import javax.inject.{ Inject, Singleton }
import play.api.mvc.{ Action, AnyContent, ControllerComponents }
import reactivemongo.bson.BSONDocument

import scala.concurrent.ExecutionContext

@Singleton
class ValidationController @Inject()(userSessions: UserSessions, constants: ConstantsV2, cc: ControllerComponents)(
    implicit ec: ExecutionContext
) extends ToolkitController(cc) {

  final private[this] def isValid(v: Boolean): Json = JsonObject("isValid" -> Json.fromBoolean(v)).asJson

  def validateModellerKey(inputOpt: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    inputOpt match {
      case Some(input) =>
        userSessions.getUser.flatMap { user =>
          if (user.userConfig.hasMODELLERKey) {
            fuccess(Ok(isValid(true)))
          } else if (input == constants.modellerKey) {
            userSessions
              .modifyUserWithCache(
                BSONDocument(User.ID -> user.userID),
                BSONDocument(
                  "$set" ->
                  BSONDocument(
                    s"${User.USER_CONFIG}.${UserConfig.HASMODELLERKEY}" ->
                    true
                  )
                )
              )
              .map {
                case Some(_) =>
                  Ok(isValid(true))
                case None =>
                  BadRequest
              }
          } else {
            fuccess(Ok(isValid(false)))
          }
        }
      case None =>
        fuccess(BadRequest)
    }
  }

}
