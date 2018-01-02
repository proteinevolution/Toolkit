package de.proteinevolution.tools.controllers
import javax.inject.{ Inject, Singleton }

import com.typesafe.config.ConfigFactory
import de.proteinevolution.tools.models.{ HHContext, ResultContext }
import de.proteinevolution.tools.services.{ ProcessFactory, ToolNameGetService }
import play.api.mvc.{ AbstractController, Action, AnyContent }
import better.files._
import de.proteinevolution.models.{ Constants, ToolNames }

import scala.sys.process.Process
import cats.implicits._
import cats.data.OptionT
import de.proteinevolution.db.ResultFileAccessor
import de.proteinevolution.tools.results.{ HSP, SearchResult }

import scala.concurrent.ExecutionContext

@Singleton
class ProcessController @Inject()(ctx: HHContext,
                                  toolFinder: ToolNameGetService,
                                  constants: Constants,
                                  resultContext: ResultContext,
                                  resultFiles: ResultFileAccessor)(implicit ec: ExecutionContext)
    extends AbstractController(ctx.controllerComponents) {

  private val serverScripts = ConfigFactory.load().getString("serverScripts")

  def templateAlignment(jobID: String, accession: String): Action[AnyContent] = Action.async { implicit request =>
    val futureScript = toolFinder.getTool(jobID).map {
      case x if x == ToolNames.HHOMP   => (serverScripts + "/templateAlignmentHHomp.sh").toFile
      case x if x == ToolNames.HHBLITS => (serverScripts + "/templateAlignmentHHblits.sh").toFile
      case x if x == ToolNames.HHPRED  => (serverScripts + "/templateAlignment.sh").toFile
      case _                           => throw new IllegalStateException("tool either not found nor not supported")
    }
    (for {
      file <- OptionT.liftF(futureScript)
    } yield file).value.map {
      case Some(f) =>
        if (!f.isExecutable)
          BadRequest
        else {
          Process(f.pathAsString, (constants.jobPath + jobID).toFile.toJava, "jobID" -> jobID, "accession" -> accession)
            .run()
            .exitValue() match {
            case 0 => Ok
            case _ => BadRequest
          }
        }
      case None => BadRequest
    }
  }

  def forwardAlignment(jobID: String, mode: String): Action[AnyContent] = Action.async { implicit request =>
    val json     = request.body.asJson.get
    val filename = (json \ "fileName").as[String]
    val accStr = mode match {
      case "alnEval" => (json \ "evalue").as[String]
      case "aln"     => (json \ "checkboxes").as[List[Int]].mkString("\n")
    }

    val res = resultFiles
      .getResults(jobID)
      .map {
        case None => throw new IllegalArgumentException("no result found")
        case Some(jsValue) =>
          val resultFuture = toolFinder.getTool(jobID).map {
            case toolName if toolName == ToolNames.HHBLITS =>
              (toolName, resultContext.hhblits.parseResult(jsValue).asInstanceOf[SearchResult[HSP]])
            case toolName if toolName == ToolNames.HHPRED =>
              (toolName, resultContext.hhpred.parseResult(jsValue).asInstanceOf[SearchResult[HSP]])
            case toolName if toolName == ToolNames.HHOMP =>
              (toolName, resultContext.hhomp.parseResult(jsValue).asInstanceOf[SearchResult[HSP]])
            case toolName if toolName == ToolNames.HMMER =>
              (toolName, resultContext.hmmer.parseResult(jsValue).asInstanceOf[SearchResult[HSP]])
            case toolName if toolName == ToolNames.PSIBLAST =>
              (toolName, resultContext.psiblast.parseResult(jsValue).asInstanceOf[SearchResult[HSP]])
            case _ => throw new IllegalArgumentException("tool has no hitlist")
          }

          (for {
            res <- OptionT.liftF(resultFuture)
          } yield res).value.map {
            case Some((toolName, r)) =>
              val numListStr = getAccString(toolName, r, accStr, mode)
              ProcessFactory((constants.jobPath + jobID).toFile, jobID, toolName.value, filename, mode, numListStr)
                .run()
                .exitValue() match {
                case 0 => 0
                case _ => 1
              }
            case None => throw new IllegalStateException
          }
      }
      .flatten

    (for {
      i <- OptionT.liftF(res)
    } yield i).value.map {
      case Some(exitCode) =>
        if (exitCode == 0)
          Ok
        else
          BadRequest
      case None => BadRequest
    }
  }

  // Only first draft of an abstraction but this can be smoothened
  private[this] def getAccString(toolName: ToolNames.ToolName,
                                 result: SearchResult[HSP],
                                 accStr: String,
                                 mode: String): String = {
    val evalString = (toolName, mode) match {
      case (ToolNames.HHBLITS, "alnEval") | (ToolNames.HHPRED, "alnEval") =>
        result.HSPS.filter(_.info.evalue < accStr.toDouble).map { _.num }.mkString(" ")
      case (ToolNames.HMMER, "alnEval") =>
        result.HSPS
          .filter(_.evalue < accStr.toDouble)
          .map { hit =>
            result.alignment.alignment(hit.num - 1).accession + "\n"
          }
          .size
          .toString
      case (ToolNames.PSIBLAST, "alnEval") =>
        accStr //result.HSPS.filter(_.evalue < eval).map { _.accession + " " }.mkString
      case (ToolNames.HMMER, "aln")    => accStr
      case (ToolNames.PSIBLAST, "aln") => accStr
      case (ToolNames.HHBLITS, "aln")  => accStr
      case (ToolNames.HHPRED, "aln")   => accStr
      case _                           => throw new IllegalStateException("tool has no evalue")
    }
    evalString
  }

}
