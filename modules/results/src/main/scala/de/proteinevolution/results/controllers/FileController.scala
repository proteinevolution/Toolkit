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

package de.proteinevolution.results.controllers

import de.proteinevolution.auth.UserSessions
import de.proteinevolution.common.models.ConstantsV2
import de.proteinevolution.results.models.HHContext
import de.proteinevolution.results.results.Common
import javax.inject.Inject
import play.api.Configuration
import play.api.http.ContentTypes
import play.api.mvc.{AbstractController, Action, AnyContent}

import scala.concurrent.ExecutionContext

class FileController @Inject()(
    ctx: HHContext,
    config: Configuration,
    constants: ConstantsV2,
    userSessions: UserSessions
)(implicit ec: ExecutionContext)
    extends AbstractController(ctx.controllerComponents)
    with ContentTypes {

  def getStructureFile(filename: String): Action[AnyContent] = Action { implicit request =>
    val db = Common.identifyDatabase(filename.replaceAll("(.cif)|(.pdb)", ""))
    val filepath = db match {
      case "scop" =>
        config.get[String]("tel.env.SCOPE")
      case "ecod" =>
        config.get[String]("tel.env.ECOD")
      case "mmcif" =>
        config.get[String]("tel.env.CIF")
    }
    Ok.sendFile(new java.io.File(s"$filepath${constants.SEPARATOR}$filename")).as(BINARY)
  }

  def file(filename: String, jobID: String): Action[AnyContent] = Action.async { implicit request =>
    userSessions.getUser.map { user =>
      val file = new java.io.File(
        s"${constants.jobPath}${constants.SEPARATOR}$jobID${constants.SEPARATOR}results${constants.SEPARATOR}$filename"
      )
      if (file.exists) {
        Ok.sendFile(file)
          .withSession(userSessions.sessionCookie(request, user.sessionID.get))
          .as(TEXT) // text/plain in order to open the file in a new browser tab
      } else {
        NoContent
      }
    }
  }

}
