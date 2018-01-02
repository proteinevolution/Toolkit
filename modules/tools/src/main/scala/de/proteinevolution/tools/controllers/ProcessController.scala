package de.proteinevolution.tools.controllers
import javax.inject.{ Inject, Singleton }

import com.typesafe.config.ConfigFactory
import de.proteinevolution.tools.models.{ HHContext, ResultContext }
import de.proteinevolution.tools.services.ToolNameGetService
import play.api.mvc.{ AbstractController, Action, AnyContent }
import better.files._
import de.proteinevolution.models.{ Constants, ToolNames }

import scala.sys.process.Process
import cats.implicits._
import cats.data.OptionT
import de.proteinevolution.db.ResultFileAccessor
import de.proteinevolution.tools.results.{ HSP, SearchResult }
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class ProcessController @Inject()(ctx: HHContext,
                                  toolFinder: ToolNameGetService,
                                  constants: Constants,
                                  resultContext: ResultContext,
                                  resultFiles: ResultFileAccessor)(implicit ec: ExecutionContext)
    extends AbstractController(ctx.controllerComponents) {

  private val serverScripts = ConfigFactory.load().getString("serverScripts")

  private val generateAlignmentScript = (serverScripts + "/generateAlignment.sh").toFile
  //private val retrieveFullSeq         = (serverScripts + "/retrieveFullSeqHHblits.sh").toFile

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

  // dynamic: tool, method - first dynamic : tool alnEval

  def forwardAlignment(jobID: String, mode: String): Action[AnyContent] = Action.async { implicit request =>

    println("CALLEDFORWARDALIGNMENT")
    val json     = request.body.asJson.get
    val filename = (json \ "fileName").as[String]
    val eval     = (json \ "evalue").as[String]

    val futureTuple = toolFinder.getTool(jobID).map {
      case x if x == ToolNames.HHBLITS => (serverScripts + "/generateAlignment.sh").toFile
      case x if x == ToolNames.HHPRED  => (serverScripts + "/generateAlignment.sh").toFile
      case _                           => throw new IllegalStateException("tool either not found nor not supported")
    }

    val _ = (futureTuple, mode)

    if (!generateAlignmentScript.isExecutable) {
      Future.successful(BadRequest)
    } else {
      val res = resultFiles
        .getResults(jobID)
        .map {
          case None => throw new IllegalArgumentException("no result found")
          case Some(jsValue) =>
            val resultFuture = toolFinder.getTool(jobID).map {
              case x if x == ToolNames.HHBLITS =>
                resultContext.hhblits.parseResult(jsValue).asInstanceOf[SearchResult[HSP]]
              case x if x == ToolNames.HHPRED =>
                resultContext.hhpred.parseResult(jsValue).asInstanceOf[SearchResult[HSP]]
              case x if x == ToolNames.HHOMP =>
                resultContext.hhomp.parseResult(jsValue).asInstanceOf[SearchResult[HSP]]
              case x if x == ToolNames.HMMER =>
                resultContext.hmmer.parseResult(jsValue).asInstanceOf[SearchResult[HSP]]
              case x if x == ToolNames.PSIBLAST =>
                resultContext.psiblast.parseResult(jsValue).asInstanceOf[SearchResult[HSP]]
              case _ => throw new IllegalArgumentException("tool has no hitlist")
            }

            (for {
              res <- OptionT.liftF(resultFuture)
            } yield res).value.map {
              case Some(r) =>
                val numListStr = getNumListEval(r, eval.toDouble)
                Process(generateAlignmentScript.pathAsString,
                        (constants.jobPath + jobID).toFile.toJava,
                        "jobID"    -> jobID,
                        "filename" -> filename,
                        "numList"  -> numListStr).run().exitValue() match {
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
        case Some(statuscode) =>
          if (statuscode == 0)
            Ok
          else
            BadRequest
        case None => BadRequest
      }
    }
  }

  private[this] def getNumListEval(result: SearchResult[HSP], eval: Double): String = {
    val numList = result.HSPS.filter(_.info.evalue < eval).map { _.num }
    numList.mkString(" ")
  }

}
