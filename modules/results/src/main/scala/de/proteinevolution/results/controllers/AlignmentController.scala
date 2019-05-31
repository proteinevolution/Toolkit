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

import cats.data.EitherT
import cats.implicits._
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.common.models.ConstantsV2
import de.proteinevolution.results.db.ResultFileAccessor
import de.proteinevolution.results.models.{ AlignmentClustalLoadHitsForm, AlignmentGetForm, AlignmentLoadHitsForm }
import de.proteinevolution.results.results.{ AlignmentResult, Common }
import javax.inject.{ Inject, Singleton }
import play.api.mvc.{ Action, ControllerComponents }

import scala.concurrent.{ ExecutionContext, Future }

@Singleton
final class AlignmentController @Inject()(
    resultFiles: ResultFileAccessor,
    constants: ConstantsV2,
    cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends ToolkitController(cc) {

  def getAln(jobID: String): Action[AlignmentGetForm] = Action(circe.json[AlignmentGetForm]).async { implicit request =>
    (for {
      json <- EitherT.liftF(resultFiles.getResults(jobID))
      r    <- EitherT.fromEither[Future](json.hcursor.downField(request.body.resultName).as[AlignmentResult])
    } yield
      request.body.checkboxes.distinct.map { num =>
        ">" + r.alignment { num - 1 }.accession + "\n" + r.alignment { num - 1 }.seq + "\n"
      }).value.map {
      case Right(list) => Ok(list.mkString)
      case Left(_)     => NotFound
    }
  }

  def loadHits(jobID: String): Action[AlignmentLoadHitsForm] = Action(circe.json[AlignmentLoadHitsForm]).async {
    implicit request =>
      (for {
        json <- EitherT.liftF(resultFiles.getResults(jobID))
        r    <- EitherT.fromEither[Future](json.hcursor.downField(request.body.resultName).as[AlignmentResult])
      } yield
        (!(request.body.end > r.alignment.length || request.body.start > r.alignment.length),
         r.alignment.slice(request.body.start, request.body.end).map(views.html.alignment.alignmentrow(_)))).value.map {
        case Right((inRange, hits)) =>
          if (inRange) {
            Ok(hits.mkString)
          } else {
            BadRequest
          }
        case Left(_) => NotFound
      }
  }

  def loadHitsClustal(jobID: String): Action[AlignmentClustalLoadHitsForm] =
    Action(circe.json[AlignmentClustalLoadHitsForm]).async { implicit request =>
      (for {
        json <- EitherT.liftF(resultFiles.getResults(jobID))
        r    <- EitherT.fromEither[Future](json.hcursor.downField(request.body.resultName).as[AlignmentResult])
      } yield Common.clustal(r, 0, constants.breakAfterClustal, request.body.color)).value.map {
        case Right(hits) => Ok(hits.mkString)
        case Left(_)     => NotFound
      }
    }

}
