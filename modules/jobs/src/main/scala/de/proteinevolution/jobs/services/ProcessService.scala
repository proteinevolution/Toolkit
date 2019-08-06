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
import cats.data.{ EitherT, OptionT }
import cats.implicits._
import de.proteinevolution.common.models.ConstantsV2
import de.proteinevolution.common.models.ToolName._
import de.proteinevolution.jobs.db.ResultFileAccessor
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
    extends Logging {

  private[this] val scriptPath: String = config.get[String]("server_scripts")

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
      forwardHitsMode: String,
      sequenceLengthMode: String,
      eval: Double,
      selected: Seq[Int]
  ): EitherT[Future, DecodingFailure, java.io.File] = {
    EitherT((for {
      json <- resultFiles.getResults(jobId)
      tool <- toolFinder.getTool(jobId)
    } yield {
      (json, tool)
    }).map {
      case (json, tool) =>
        resultFiles.parseResult(tool, json).flatMap {
          result =>
            val accStrParsed: String = (tool, forwardHitsMode, sequenceLengthMode) match {
              case (HHBLITS, "eval", "aln") | (HHPRED, "eval", "aln") =>
                result.HSPS.filter(_.eValue <= eval).map { _.num }.mkString(" ")
              case (HMMER, "eval", "aln") =>
                result.HSPS
                  .filter(_.eValue <= eval)
                  .map { hit =>
                    result.alignment.alignment(hit.num - 1).accession + "\n"
                  }
                  .size
                  .toString
              case (PSIBLAST, "eval", "aln") =>
                eval.toString
              case (HMMER, "eval", "full") | (PSIBLAST, "eval", "full") | (HHBLITS, "eval", "full")=>
                result.HSPS.filter(_.eValue <= eval).map { _.accession + " " }.mkString
              case (_, "selected", "aln") => selected.mkString("\n")
              case (_, "selected", "full") =>
                selected.map { num =>
                  "%s ".format(result.HSPS(num - 1).accession)
                }.mkString
              case _ => throw new IllegalStateException("parsing accession identifiers failed")
            }
            val tempFileName = "forwardingFile"
            ProcessFactory(
              (constants.jobPath + jobId).toFile,
              tempFileName,
              jobId,
              tool,
              forwardHitsMode,
              sequenceLengthMode,
              accStrParsed,
              result.db,
              scriptPath,
              config
            ).run().exitValue() match {
              case 0 =>
                val file = new java.io.File(
                  s"${constants.jobPath}${constants.SEPARATOR}$jobId${constants.SEPARATOR}results${constants.SEPARATOR}$tempFileName"
                )
                if (file.exists) {
                  Right(file)
                } else {
                  val error = "Could not generate forwarding data"
                  logger.error(error)
                  Left(DecodingFailure(error, Nil))
                }
              case _ =>
                val error = "Could not generate forwarding data"
                logger.error(error)
                Left(DecodingFailure(error, Nil))
            }
        }
      case _ =>
        val error = "parsing result json failed."
        logger.error(error)
        Left(DecodingFailure(error, Nil))
    })
  }

}
