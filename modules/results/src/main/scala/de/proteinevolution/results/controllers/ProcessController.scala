package de.proteinevolution.results.controllers

import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.models.ToolName._
import de.proteinevolution.models.ToolName
import de.proteinevolution.results.models.{ ForwardMode, ForwardingData, HHContext }
import de.proteinevolution.results.results.{ HSP, SearchResult }
import de.proteinevolution.results.services.{ ProcessFactory, ProcessService, ResultsRepository }
import javax.inject.{ Inject, Singleton }
import play.api.libs.concurrent.Futures
import play.api.libs.concurrent.Futures._
import play.api.mvc.{ Action, AnyContent }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

@Singleton
class ProcessController @Inject()(
    ctx: HHContext,
    service: ProcessService
)(implicit ec: ExecutionContext, futures: Futures)
    extends ToolkitController(ctx.controllerComponents)
    with ResultsRepository {

  def templateAlignment(jobId: String, accession: String): Action[AnyContent] = Action.async { implicit request =>
    service.templateAlignment(jobId, accession).value.map {
      case Some(0) => NoContent
      case _       => BadRequest
    }
  }

  def forwardAlignment(jobId: String, mode: ForwardMode): Action[ForwardingData] =
    Action(circe.json[ForwardingData]).async { implicit request =>
        .withTimeout(220.seconds)
        .map {
          case 0 =>
            NoContent
          case _ =>
            BadRequest
        }
        .recover {
          case _: scala.concurrent.TimeoutException =>
            InternalServerError("timeout")
        }
    }

  private[this] def parseAccString(
      toolName: ToolName,
      result: SearchResult[HSP],
      accStr: String,
      mode: ForwardMode
  ): String = {
    (toolName, mode.toString) match {
      case (HHBLITS, "alnEval") | (HHPRED, "alnEval") =>
        result.HSPS.filter(_.info.eval <= accStr.toDouble).map { _.num }.mkString(" ")
      case (HMMER, "alnEval") =>
        result.HSPS
          .filter(_.eValue <= accStr.toDouble)
          .map { hit =>
            result.alignment.alignment(hit.num - 1).accession + "\n"
          }
          .size
          .toString
      case (PSIBLAST, "alnEval") =>
        accStr
      case (_, "aln") => accStr
      case (HMMER, "evalFull") | (PSIBLAST, "evalFull") =>
        result.HSPS.filter(_.eValue <= accStr.toDouble).map { _.accession + " " }.mkString
      case (HHBLITS, "evalFull") =>
        result.HSPS.filter(_.info.eval <= accStr.toDouble).map { _.template.accession + " " }.mkString
      case (_, "full") =>
        val numList = accStr.split("\n").map(_.toInt)
        numList.map { num =>
          if (toolName == HHBLITS)
            result.HSPS(num - 1).template.accession + " "
          else
            result.HSPS(num - 1).accession + " "
        }.mkString
      case _ => throw new IllegalStateException("parsing accession identifiers failed")
    }
  }

}
