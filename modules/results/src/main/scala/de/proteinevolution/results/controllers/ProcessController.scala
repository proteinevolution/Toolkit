package de.proteinevolution.results.controllers

import better.files._
import de.proteinevolution.base.controllers.ToolkitController
import de.proteinevolution.results.db.ResultFileAccessor
import de.proteinevolution.models.ToolName._
import de.proteinevolution.models.{ ConstantsV2, ToolName }
import de.proteinevolution.results.models.{
  ForwardMode,
  ForwardingData,
  HHContext,
  ResultContext
}
import de.proteinevolution.results.results.{ HSP, SearchResult }
import de.proteinevolution.results.services.ResultsRepository.ResultsService
import de.proteinevolution.results.services.{
  ProcessFactory,
  ResultsRepository,
  ToolNameGetService
}
import javax.inject.{ Inject, Singleton }
import play.api.Configuration
import play.api.libs.concurrent.Futures
import play.api.libs.concurrent.Futures._
import play.api.mvc.{ Action, AnyContent }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.sys.process.Process

@Singleton
class ProcessController @Inject()(
    ctx: HHContext,
    toolFinder: ToolNameGetService,
    resultFiles: ResultFileAccessor,
    constants: ConstantsV2,
    resultContext: ResultContext,
    config: Configuration
)(implicit ec: ExecutionContext, futures: Futures)
    extends ToolkitController(ctx.controllerComponents)
    with ResultsRepository {

  private val scriptPath: String = config.get[String]("serverScripts")

  private val resultsService = ResultsService(toolFinder, resultFiles)

  def templateAlignment(jobId: String, accession: String): Action[AnyContent] =
    Action.async { implicit request =>
      toolFinder
        .getTool(jobId)
        .map {
          case HHOMP   => (scriptPath + "/templateAlignmentHHomp.sh").toFile
          case HHBLITS => (scriptPath + "/templateAlignmentHHblits.sh").toFile
          case HHPRED  => (scriptPath + "/templateAlignment.sh").toFile
          case _ =>
            throw new IllegalStateException(
              "tool either not found nor not supported"
            )
        }
        .map { file =>
          if (!file.isExecutable)
            BadRequest
          else {
            Process(file.pathAsString,
                    (constants.jobPath + jobId).toFile.toJava,
                    "jobID"     -> jobId,
                    "accession" -> accession).run().exitValue() match {
              case 0 => NoContent
              case _ => BadRequest
            }
          }
        }
    }

  def forwardAlignment(
      jobId: String,
      mode: ForwardMode
  ): Action[ForwardingData] =
    Action(circe.json[ForwardingData]).async { implicit request =>
      val data     = request.body
      val filename = data.fileName
      val accStr = mode.toString match {
        case "alnEval" | "evalFull" => data.evalue.getOrElse("")
        case "aln" | "full"         => data.checkboxes.toSeq.mkString("\n")
      }
      getResults(jobId)
        .run(resultsService)
        .flatMap {
          case Some(jsValue) =>
            getTool(jobId).run(resultsService).map {
              case HHBLITS =>
                (HHBLITS, resultContext.hhblits.parseResult(jsValue))
              case HHPRED =>
                (HHPRED, resultContext.hhpred.parseResult(jsValue))
              case HHOMP =>
                (HHOMP, resultContext.hhomp.parseResult(jsValue))
              case HMMER =>
                (HMMER, resultContext.hmmer.parseResult(jsValue))
              case PSIBLAST =>
                (PSIBLAST, resultContext.psiblast.parseResult(jsValue))
              case _ =>
                throw new IllegalArgumentException("tool has no hitlist")
            }
          case None => throw new IllegalStateException
        }
        .map {
          case (toolName, result) =>
            val accStrParsed = parseAccString(toolName, result, accStr, mode)
            ProcessFactory((constants.jobPath + jobId).toFile,
                           jobId,
                           toolName.value,
                           filename.getOrElse(""),
                           mode,
                           accStrParsed,
                           result.db,
                           scriptPath).run().exitValue()
        }
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
        result.HSPS
          .filter(_.info.evalue <= accStr.toDouble)
          .map { _.num }
          .mkString(" ")
      case (HMMER, "alnEval") =>
        result.HSPS
          .filter(_.evalue <= accStr.toDouble)
          .map { hit =>
            result.alignment.alignment(hit.num - 1).accession + "\n"
          }
          .size
          .toString
      case (PSIBLAST, "alnEval") =>
        accStr
      case (_, "aln") => accStr
      case (HMMER, "evalFull") | (PSIBLAST, "evalFull") =>
        result.HSPS
          .filter(_.evalue <= accStr.toDouble)
          .map { _.accession + " " }
          .mkString
      case (HHBLITS, "evalFull") =>
        result.HSPS
          .filter(_.info.evalue <= accStr.toDouble)
          .map { _.template.accession + " " }
          .mkString
      case (_, "full") =>
        val numList = accStr.split("\n").map(_.toInt)
        numList.map { num =>
          if (toolName == HHBLITS)
            result.HSPS(num - 1).template.accession + " "
          else
            result.HSPS(num - 1).accession + " "
        }.mkString
      case _ =>
        throw new IllegalStateException("parsing accession identifiers failed")
    }
  }

}
