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

package de.proteinevolution.jobs.controllers

import de.proteinevolution.auth.services.UserSessionService
import de.proteinevolution.auth.util.UserAction
import de.proteinevolution.common.models.ConstantsV2
import de.proteinevolution.jobs.models.HHContext
import de.proteinevolution.jobs.results.Common
import javax.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.http.ContentTypes
import play.api.mvc.{AbstractController, Action, AnyContent}

import scala.concurrent.ExecutionContext

@Singleton
class FileController @Inject()(
    ctx: HHContext,
    config: Configuration,
    constants: ConstantsV2,
    userSessionService: UserSessionService,
    userAction: UserAction
)(implicit ec: ExecutionContext)
    extends AbstractController(ctx.controllerComponents)
    with ContentTypes {

  def getStructureFile(accession: String): Action[AnyContent] = Action { implicit request =>
    val db = Common.identifyDatabase(accession)
    val ending = db match {
      case "scop"  => "pdb"
      case "ecod"  => "pdb"
      case "mmcif" => "cif"
    }
    val filepath = db match {
      case "scop" =>
        config.get[String]("tel.env.SCOPE")
      case "ecod" =>
        config.get[String]("tel.env.ECOD")
      case "mmcif" =>
        config.get[String]("tel.env.CIF")
    }
    Ok.sendFile(new java.io.File(s"$filepath${constants.SEPARATOR}$accession.$ending")).as(BINARY)
  }

  def file(filename: String, jobID: String): Action[AnyContent] = userAction { implicit request =>
    val file = new java.io.File(
      s"${constants.jobPath}${constants.SEPARATOR}$jobID${constants.SEPARATOR}results${constants.SEPARATOR}$filename"
    )
    if (file.exists) {
      Ok.sendFile(file)
        .withSession(userSessionService.sessionCookie(request, request.user.sessionID.get))
        .as(TEXT) // text/plain in order to open the file in a new browser tab
    } else {
      NoContent
    }
  }
}
