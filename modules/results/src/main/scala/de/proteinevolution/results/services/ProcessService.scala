package de.proteinevolution.results.services

import better.files._
import cats.data.{ EitherT, OptionT }
import cats.implicits._
import de.proteinevolution.models.ToolName._
import de.proteinevolution.models.{ ConstantsV2, ToolName }
import de.proteinevolution.results.db.ResultFileAccessor
import de.proteinevolution.results.models.{ ForwardMode, ForwardingData }
import de.proteinevolution.results.results.{ HSP, SearchResult }
import de.proteinevolution.results.services.ResultsRepository.ResultsService
import io.circe.DecodingFailure
import javax.inject.{ Inject, Singleton }
import play.api.{ Configuration, Logging }

import scala.concurrent.{ ExecutionContext, Future }
import scala.sys.process.Process

@Singleton
final class ProcessService @Inject()(
    config: Configuration,
    toolFinder: ToolNameGetService,
    resultFiles: ResultFileAccessor,
    constants: ConstantsV2
)(implicit ec: ExecutionContext)
    extends ResultsRepository
    with Logging {

  private[this] val scriptPath: String = config.get[String]("server_scripts")

  private[this] val resultsService = ResultsService(toolFinder, resultFiles)

  def templateAlignment(jobId: String, accession: String): OptionT[Future, Int] = {
    for {
      file <- OptionT.liftF(toolFinder.getTool(jobId).map {
        case HHOMP   => (scriptPath + "/templateAlignmentHHomp.sh").toFile
        case HHBLITS => (scriptPath + "/templateAlignmentHHblits.sh").toFile
        case HHPRED  => (scriptPath + "/templateAlignment.sh").toFile
        case _       => throw new IllegalStateException("tool either not found nor not supported")
      })
      isExec <- OptionT.pure[Future](file.isExecutable)
      if isExec
    } yield {

      val env: List[(String, String)] = List(
        "jobID"        -> jobId,
        "accession"    -> accession,
        "ENVIRONMENT"  -> config.get[String]("environment"),
        "BIOPROGSROOT" -> config.get[String]("bioprogs_root"),
        "DATABASES"    -> config.get[String]("db_root")
      )

      Process(file.pathAsString, (constants.jobPath + jobId).toFile.toJava, env: _*).run().exitValue()
    }
  }

  def forwardAlignment(
      jobId: String,
      mode: ForwardMode,
      form: ForwardingData
  ): EitherT[Future, DecodingFailure, Int] = {
    EitherT((for {
      json <- getResults(jobId).run(resultsService)
      tool <- getTool(jobId).run(resultsService)
    } yield {
      (json, tool)
    }).map {
      case (Some(json), tool) =>
        parseResult(tool, json).map {
          case (result, _) =>
            val accStr = mode.toString match {
              case "alnEval" | "evalFull" => form.evalue.getOrElse("")
              case "aln" | "full"         => form.checkboxes.toSeq.mkString("\n")
            }
            val accStrParsed = parseAccString(tool, result, accStr, mode)
            ProcessFactory(
              (constants.jobPath + jobId).toFile,
              jobId,
              tool.value,
              form.fileName.getOrElse(""),
              mode,
              accStrParsed,
              result.db,
              scriptPath,
              config
            ).run().exitValue()
        }
      case _ =>
        val error = "parsing result json failed."
        logger.error(error)
        Left(DecodingFailure(error, Nil))
    })
  }

  private[this] def parseAccString(
      toolName: ToolName,
      result: SearchResult[HSP],
      accStr: String,
      mode: ForwardMode
  ): String = {
    (toolName, mode.toString) match {
      case (HHBLITS, "alnEval") | (HHPRED, "alnEval") =>
        result.HSPS.filter(_.eValue <= accStr.toDouble).map { _.num }.mkString(" ")
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
        result.HSPS.filter(_.eValue <= accStr.toDouble).map { _.accession + " " }.mkString
      case (_, "full") =>
        val numList = accStr.split("\n").map(_.toInt)
        numList.map { num =>
          if (toolName == HHBLITS)
            "%s ".format(result.HSPS(num - 1).accession)
          else
            "%s ".format(result.HSPS(num - 1).accession)
        }.mkString
      case _ => throw new IllegalStateException("parsing accession identifiers failed")
    }
  }

}
