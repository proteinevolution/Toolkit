package de.proteinevolution.tools.controllers
import javax.inject.{ Inject, Singleton }
import com.typesafe.config.ConfigFactory
import de.proteinevolution.tools.models.{ ForwardMode, ForwardingData, HHContext, ResultContext }
import de.proteinevolution.tools.services.{ KleisliProvider, ProcessFactory, ToolNameGetService }
import play.api.mvc.{ AbstractController, Action, AnyContent }
import better.files._
import de.proteinevolution.models.{ ConstantsV2, ToolName }

import scala.sys.process.Process
import de.proteinevolution.tools.results.{ HSP, SearchResult }
import ToolName._
import play.api.libs.circe.Circe
import play.api.libs.concurrent.Futures

import scala.concurrent.duration._
import play.api.libs.concurrent.Futures._

import scala.concurrent.ExecutionContext

@Singleton
class ProcessController @Inject()(ctx: HHContext,
                                  toolFinder: ToolNameGetService,
                                  constants: ConstantsV2,
                                  kleisliProvider: KleisliProvider,
                                  resultContext: ResultContext)(implicit ec: ExecutionContext, futures: Futures)
    extends AbstractController(ctx.controllerComponents)
    with Circe {

  private val serverScripts = ConfigFactory.load().getString("serverScripts")

  def templateAlignment(jobID: String, accession: String): Action[AnyContent] = Action.async { implicit request =>
    toolFinder
      .getTool(jobID)
      .map {
        case HHOMP   => (serverScripts + "/templateAlignmentHHomp.sh").toFile
        case HHBLITS => (serverScripts + "/templateAlignmentHHblits.sh").toFile
        case HHPRED  => (serverScripts + "/templateAlignment.sh").toFile
        case _       => throw new IllegalStateException("tool either not found nor not supported")
      }
      .map { file =>
        if (!file.isExecutable)
          BadRequest
        else {
          Process(file.pathAsString,
                  (constants.jobPath + jobID).toFile.toJava,
                  "jobID"     -> jobID,
                  "accession" -> accession)
            .run()
            .exitValue() match {
            case 0 => NoContent
            case _ => BadRequest
          }
        }
      }
  }

  def forwardAlignment(jobID: String, mode: ForwardMode): Action[ForwardingData] =
    Action(circe.json[ForwardingData]).async { implicit request =>
      val data     = request.body
      val filename = data.fileName
      val accStr = mode.toString match {
        case "alnEval" | "evalFull" => data.evalue.getOrElse("")
        case "aln" | "full"         => data.checkboxes.toSeq.mkString("\n")
      }
      kleisliProvider
        .resK(jobID)
        .flatMap {
          case Some(jsValue) =>
            kleisliProvider.toolK(jobID).map {
              case HHBLITS =>
                (HHBLITS, resultContext.hhblits.parseResult(jsValue).asInstanceOf[SearchResult[HSP]])
              case HHPRED =>
                (HHPRED, resultContext.hhpred.parseResult(jsValue).asInstanceOf[SearchResult[HSP]])
              case HHOMP =>
                (HHOMP, resultContext.hhomp.parseResult(jsValue).asInstanceOf[SearchResult[HSP]])
              case HMMER =>
                (HMMER, resultContext.hmmer.parseResult(jsValue).asInstanceOf[SearchResult[HSP]])
              case PSIBLAST =>
                (PSIBLAST, resultContext.psiblast.parseResult(jsValue).asInstanceOf[SearchResult[HSP]])
              case _ => throw new IllegalArgumentException("tool has no hitlist")
            }
          case None => throw new IllegalStateException
        }
        .map {
          case (toolName, result) =>
            val accStrParsed = parseAccString(toolName, result, accStr, mode)
            ProcessFactory((constants.jobPath + jobID).toFile,
                           jobID,
                           toolName.value,
                           filename.getOrElse(""),
                           mode,
                           accStrParsed,
                           result.db).run().exitValue()
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

  private[this] def parseAccString(toolName: ToolName,
                                   result: SearchResult[HSP],
                                   accStr: String,
                                   mode: ForwardMode): String = {
    (toolName, mode.toString) match {
      case (HHBLITS, "alnEval") | (HHPRED, "alnEval") =>
        result.HSPS.filter(_.info.evalue <= accStr.toDouble).map { _.num }.mkString(" ")
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
        result.HSPS.filter(_.evalue <= accStr.toDouble).map { _.accession + " " }.mkString
      case (HHBLITS, "evalFull") =>
        result.HSPS.filter(_.info.evalue <= accStr.toDouble).map { _.template.accession + " " }.mkString
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
