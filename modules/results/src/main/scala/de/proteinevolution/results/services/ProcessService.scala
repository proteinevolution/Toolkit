package de.proteinevolution.results.services

import de.proteinevolution.models.ToolName._
import de.proteinevolution.results.db.ResultFileAccessor
import de.proteinevolution.results.services.ResultsRepository.ResultsService
import javax.inject.{ Inject, Singleton }
import play.api.Configuration
import better.files._
import cats.data.OptionT
import cats.implicits._
import de.proteinevolution.models.ConstantsV2
import de.proteinevolution.results.models.{ ForwardMode, ForwardingData }
import io.circe.Json

import scala.sys.process.Process
import scala.concurrent.{ ExecutionContext, Future }

@Singleton
class ProcessService @Inject()(
    config: Configuration,
    toolFinder: ToolNameGetService,
    resultFiles: ResultFileAccessor,
    constants: ConstantsV2
)(implicit ec: ExecutionContext)
    extends ResultsRepository {

  private val scriptPath: String = config.get[String]("server_scripts")

  private val resultsService = ResultsService(toolFinder, resultFiles)

  def templateAlignment(jobId: String, accession: String): OptionT[Future, Int] = {
    for {
      file <- OptionT.liftF(toolFinder.getTool(jobId).map {
        case HHOMP   => (scriptPath + "/templateAlignmentHHomp.sh").toFile
        case HHBLITS => (scriptPath + "/templateAlignmentHHblits.sh").toFile
        case HHPRED  => (scriptPath + "/templateAlignment.sh").toFile
        case _       => throw new IllegalStateException("tool either not found nor not supported")
      })
      isExec <- OptionT.pure(file.isExecutable)
      if isExec
    } yield {
      Process(file.pathAsString, (constants.jobPath + jobId).toFile.toJava, "jobID" -> jobId, "accession" -> accession)
        .run()
        .exitValue()
    }
  }

  def forwardAlignment(jobId: String, mode: ForwardMode, form: ForwardingData) = {

    for {
      json <- OptionT(getResults(jobId).run(resultsService))
    } yield {

    }

  }

  private def parseResult(tool: String, json: Json) = {



  }

  /*
   val data     = request.body
      val filename = data.fileName
      val accStr = mode.toString match {
        case "alnEval" | "evalFull" => data.evalue.getOrElse("")
        case "aln" | "full"         => data.checkboxes.toSeq.mkString("\n")
      }
      getResults(jobId)
        .run(resultsService)
        .flatMap {
          case Some(json) =>
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
              case _ => throw new IllegalArgumentException("tool has no hitlist")
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
   */

}
