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
import de.proteinevolution.jobs.models.{ AlignmentGetForm, AlignmentLoadHitsForm }
import de.proteinevolution.jobs.results.AlignmentResult
import io.circe.syntax._
import javax.inject.{ Inject, Singleton }
import play.api.mvc.{ Action, ControllerComponents }

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
final class AlignmentController @Inject()(
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

  def loadAlignmentHits(jobID: String): Action[AlignmentLoadHitsForm] =
    userAction(circe.json[AlignmentLoadHitsForm]).async { implicit request =>
      jobDao.findJob(jobID).flatMap {
        case Some(job) =>
          if (job.isPublic || job.ownerID.equals(request.user.userID)) {
            // access allowed to job
            (for {
              json <- EitherT.liftF(resultFiles.getResults(jobID))
              r    <- EitherT.fromEither[Future](json.hcursor.downField(request.body.resultName).as[AlignmentResult])
            } yield r.alignment
              .slice(request.body.start.getOrElse(0), request.body.end.getOrElse(r.alignment.length))).value.map {
              case Right(hits) => Ok(hits.asJson)
              case Left(_)     => NotFound
            }
          } else {
            fuccess(Unauthorized)
          }
        case _ => fuccess(NotFound)
      }
    }
}
