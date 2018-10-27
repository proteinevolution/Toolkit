package de.proteinevolution.results.controllers

import cats.data.OptionT
import cats.implicits._
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.results.db.ResultFileAccessor
import de.proteinevolution.models.ConstantsV2
import de.proteinevolution.results.models.{ AlignmentClustalLoadHitsForm, AlignmentGetForm, AlignmentLoadHitsForm }
import de.proteinevolution.results.results.{ AlignmentResult, Common }
import javax.inject.Inject
import play.api.mvc.{ Action, ControllerComponents }

import scala.concurrent.{ ExecutionContext, Future }

class AlignmentController @Inject()(
    resultFiles: ResultFileAccessor,
    constants: ConstantsV2,
    cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends ToolkitController(cc) {

  def getAln(jobID: String): Action[AlignmentGetForm] = Action(circe.json[AlignmentGetForm]).async {
    implicit request =>
      (for {
        json       <- OptionT(resultFiles.getResults(jobID))
        r          <- OptionT.fromOption[Future](json.hcursor.downField(request.body.resultName).as[AlignmentResult].toOption)
        checkboxes <- OptionT.pure[Future](request.body.checkboxes.distinct)
      } yield {
        checkboxes.map { num => ">" + r.alignment { num - 1 }.accession + "\n" + r.alignment { num - 1 }.seq + "\n"
        }
      }).value.map {
        case Some(list) => Ok(list.mkString)
        case None       => NotFound
      }
  }

  def loadHits(jobID: String): Action[AlignmentLoadHitsForm] = Action(circe.json[AlignmentLoadHitsForm]).async {
    implicit request =>
      (for {
        json <- OptionT(resultFiles.getResults(jobID))
        r    <- OptionT.fromOption[Future](json.hcursor.downField(request.body.resultName).as[AlignmentResult].toOption)
      } yield {
        (!(request.body.end > r.alignment.length || request.body.start > r.alignment.length),
         r.alignment.slice(request.body.start, request.body.end).map(views.html.alignment.alignmentrow(_)))
      }).value.map {
        case Some((inRange, hits)) =>
          if (inRange) {
            Ok(hits.mkString)
          } else {
            BadRequest
          }
        case None => NotFound
      }
  }

  def loadHitsClustal(jobID: String): Action[AlignmentClustalLoadHitsForm] =
    Action(circe.json[AlignmentClustalLoadHitsForm]).async { implicit request =>
      (for {
        json <- OptionT(resultFiles.getResults(jobID))
        r    <- OptionT.fromOption[Future](json.hcursor.downField(request.body.resultName).as[AlignmentResult].toOption)
      } yield {
        Common.clustal(r, 0, constants.breakAfterClustal, request.body.color)
      }).value.map {
        case Some(hits) => Ok(hits.mkString)
        case None       => NotFound
      }
    }

}
