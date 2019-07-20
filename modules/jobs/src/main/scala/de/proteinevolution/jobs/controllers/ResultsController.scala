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

import cats.data.EitherT
import cats.implicits._
import de.proteinevolution.auth.util.UserAction
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.common.models.ConstantsV2
import de.proteinevolution.jobs.dao.JobDao
import de.proteinevolution.jobs.db.ResultFileAccessor
import de.proteinevolution.jobs.models.AlignmentGetForm
import de.proteinevolution.jobs.results.AlignmentResult
import io.circe.JsonObject
import io.circe.syntax._
import javax.inject.{ Inject, Singleton }
import play.api.mvc.{ Action, AnyContent, ControllerComponents }

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
final class ResultsController @Inject()(
    resultFiles: ResultFileAccessor,
    constants: ConstantsV2,
    cc: ControllerComponents,
    jobDao: JobDao,
    userAction: UserAction
)(implicit ec: ExecutionContext)
    extends ToolkitController(cc) {

  def getAln(jobID: String): Action[AlignmentGetForm] = Action(circe.json[AlignmentGetForm]).async { implicit request =>
    (for {
      json <- EitherT.liftF(resultFiles.getResults(jobID))
      r    <- EitherT.fromEither[Future](json.hcursor.downField(request.body.resultName).as[AlignmentResult])
    } yield request.body.checkboxes.distinct.map { num =>
      ">" + r.alignment { num - 1 }.accession + "\n" + r.alignment { num - 1 }.seq + "\n"
    }).value.map {
      case Right(list) => Ok(list.mkString)
      case Left(_)     => NotFound
    }
  }

  def loadResults(jobID: String): Action[AnyContent] =
    userAction.async { implicit request =>
      jobDao.findJob(jobID).flatMap {
        case Some(job) =>
          if (job.isPublic || job.ownerID.equals(request.user.userID)) {
            // access allowed to job
            // TODO catch errors and serve NotFound
            resultFiles.getResults(jobID).map { json =>
              Ok(json)
            }
          } else {
            fuccess(Unauthorized)
          }
        case _ => fuccess(NotFound)
      }
    }

  def loadAlignmentHits(jobID: String, start: Option[Int], end: Option[Int]): Action[AnyContent] =
    userAction.async { implicit request =>
      jobDao.findJob(jobID).flatMap {
        case Some(job) =>
          if (job.isPublic || job.ownerID.equals(request.user.userID)) {
            // access allowed to job
            (for {
              json <- EitherT.liftF(resultFiles.getResults(jobID))
              r    <- EitherT.fromEither[Future](json.hcursor.downField("alignment").as[AlignmentResult])
            } yield r).value.map {
              case Right(r) =>
                val s = start.getOrElse(0)
                val e = end.getOrElse(r.alignment.length)
                Ok(
                  JsonObject(
                    "alignments" -> r.alignment.slice(s, e).asJson,
                    "total"      -> r.alignment.length.asJson,
                    "start"      -> s.asJson,
                    "end"        -> e.asJson
                  ).asJson
                )
              case Left(_) => NotFound
            }
          } else {
            fuccess(Unauthorized)
          }
        case _ => fuccess(NotFound)
      }
    }
}
