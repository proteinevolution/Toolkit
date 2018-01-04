package de.proteinevolution.tools.controllers
import javax.inject.{ Inject, Singleton }

import com.typesafe.config.ConfigFactory
import de.proteinevolution.tools.models.{ HHContext, ResultContext }
import de.proteinevolution.tools.services.{ KleisliProvider, ProcessFactory, ToolNameGetService }
import play.api.mvc.{ AbstractController, Action, AnyContent }
import better.files._
import de.proteinevolution.models.{ Constants, ToolNames }

import scala.sys.process.Process
import cats.implicits._
import cats.data.OptionT
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
    val futureScript = toolFinder.getTool(jobID).map {
      case HHOMP   => (serverScripts + "/templateAlignmentHHomp.sh").toFile
      case HHBLITS => (serverScripts + "/templateAlignmentHHblits.sh").toFile
      case HHPRED  => (serverScripts + "/templateAlignment.sh").toFile
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
      case "alnEval" | "evalFull" => (json \ "evalue").as[String]
      case "aln"                  => (json \ "checkboxes").as[List[Int]].mkString("\n")
    }

    kleisliProvider
      .resK(jobID)
      .flatMap {
        case Some(jsValue) =>
          kleisliProvider.toolK(jobID).map { // TODO composition instead of mapping
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
      .map { tuple =>
        val numListStr =
          if (mode != "full")
            getAccString(tuple._1, tuple._2, accStr, mode)
          else
            numericAccString(tuple._1, tuple._2, accStr)
        ProcessFactory((constants.jobPath + jobID).toFile,
                       jobID,
                       tuple._1.value,
                       filename,
                       mode,
                       numListStr,
                       tuple._2.db).run().exitValue()
      }
      .map {
        case 0 =>
          Ok
        case _ =>
          BadRequest
      }
  }

  /**
   * TODO
   * all the code below looks quite messy but this depends on the scripts in lib/
   */
  // Only first draft of an abstraction but this can be smoothened
  private[this] def getAccString(toolName: ToolNames.ToolName,
                                 result: SearchResult[HSP],
                                 accStr: String,
                                 mode: String): String = {
    val evalString = (toolName, mode) match {
      case (HHBLITS, "alnEval") | (ToolNames.HHPRED, "alnEval") =>
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
      case _ => throw new IllegalStateException("tool has no evalue")
    }
    evalString
  }

  // find better name for this function later and merge it with the one above (all depends on the non-uniform input json)
  private[this] def numericAccString(toolName: ToolNames.ToolName,
                                     result: SearchResult[HSP],
                                     accStr: String): String = {

    val numList = accStr.split("\n").map(_.toInt)

    toolName match {
      case HMMER =>
        numList.map { num =>
          result.HSPS(num - 1).accession + " "
        }.mkString
      case PSIBLAST =>
        numList.map { num =>
          result.HSPS(num - 1).accession + " "
        }.mkString
      case HHBLITS =>
        numList.map { num =>
          result.HSPS(num - 1).template.accession + " "
        }.mkString
      case _ => throw new IllegalStateException("tool does not support forwarding in full mode")

    }
  }

}
