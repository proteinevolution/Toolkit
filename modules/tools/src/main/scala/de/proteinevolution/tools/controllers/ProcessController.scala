package de.proteinevolution.tools.controllers
import javax.inject.{ Inject, Singleton }

import com.typesafe.config.ConfigFactory
import de.proteinevolution.tools.models.{ ForwardMode, HHContext, ResultContext }
import de.proteinevolution.tools.services.{ KleisliProvider, ProcessFactory, ToolNameGetService }
import play.api.mvc.{ AbstractController, Action, AnyContent }
import better.files._
import de.proteinevolution.models.{ Constants, ToolNames }

import scala.sys.process.Process
import de.proteinevolution.tools.results.{ HSP, SearchResult }
import ToolNames._

import scala.concurrent.ExecutionContext

@Singleton
class ProcessController @Inject()(ctx: HHContext,
                                  toolFinder: ToolNameGetService,
                                  constants: Constants,
                                  kleisliProvider: KleisliProvider,
                                  resultContext: ResultContext)(implicit ec: ExecutionContext)
    extends AbstractController(ctx.controllerComponents) {

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
            case 0 => Ok
            case _ => BadRequest
          }
        }
      }
  }

  def forwardAlignment(jobID: String, mode: ForwardMode): Action[AnyContent] = Action.async { implicit request =>
    val json     = request.body.asJson.get
    val filename = (json \ "fileName").as[String]
    val accStr = mode.toString match {
      case "alnEval" | "evalFull" => (json \ "evalue").as[String]
      case "aln" | "full"         => (json \ "checkboxes").as[List[Int]].mkString("\n")
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
                         filename,
                         mode,
                         accStrParsed,
                         result.db).run().exitValue()
      }
      .map {
        case 0 =>
          Ok
        case _ =>
          BadRequest
      }
  }

  private[this] def parseAccString(toolName: ToolNames.ToolName,
                                   result: SearchResult[HSP],
                                   accStr: String,
                                   mode: ForwardMode): String = {
    (toolName, mode.toString) match {
      case (HHBLITS, "alnEval") | (HHPRED, "alnEval") =>
        result.HSPS.filter(_.info.evalue < accStr.toDouble).map { _.num }.mkString(" ")
      case (HMMER, "alnEval") =>
        result.HSPS
          .filter(_.evalue < accStr.toDouble)
          .map { hit =>
            result.alignment.alignment(hit.num - 1).accession + "\n"
          }
          .size
          .toString
      case (PSIBLAST, "alnEval") =>
        accStr
      case (_, "aln") => accStr
      case (HMMER, "evalFull") | (PSIBLAST, "evalFull") =>
        result.HSPS.filter(_.evalue < accStr.toDouble).map { _.accession + " " }.mkString
      case (HHBLITS, "evalFull") =>
        result.HSPS.filter(_.info.evalue < accStr.toDouble).map { _.template.accession + " " }.mkString
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
