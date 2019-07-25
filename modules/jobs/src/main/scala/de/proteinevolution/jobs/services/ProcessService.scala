/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.proteinevolution.jobs.services

import better.files._
import cats.data.{EitherT, OptionT}
import cats.implicits._
import de.proteinevolution.common.models.ToolName._
import de.proteinevolution.common.models.{ConstantsV2, ToolName}
import de.proteinevolution.jobs.db.ResultFileAccessor
import de.proteinevolution.jobs.models.{ForwardMode, ForwardingData}
import de.proteinevolution.jobs.results.{HSP, SearchResult}
import de.proteinevolution.jobs.services.ResultsRepository.ResultsService
import io.circe.DecodingFailure
import javax.inject.{Inject, Singleton}
import play.api.{Configuration, Logging}

import scala.concurrent.{ExecutionContext, Future}
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

  private[this] val resultsService = ResultsService(resultFiles)

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
      tool <- toolFinder.getTool(jobId)
    } yield {
      (json, tool)
    }).map {
      case (json, tool) =>
        parseResult(tool, json).map { result =>
            val accStr = mode.toString match {
              case "alnEval" | "evalFull" => form.evalue.getOrElse("")
              case "aln" | "full" => form.checkboxes.toSeq.mkString("\n")
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
