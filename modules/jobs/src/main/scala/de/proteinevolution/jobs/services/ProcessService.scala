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

import java.util.UUID

import better.files._
import cats.data.{ EitherT, OptionT }
import cats.implicits._
import de.proteinevolution.common.models.ConstantsV2
import de.proteinevolution.common.models.ToolName._
import de.proteinevolution.jobs.db.ResultFileAccessor
import de.proteinevolution.jobs.models.ForwardingData
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
      form: ForwardingData
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
            val generateAlignmentScript = (scriptPath + "/generateAlignment.sh").toFile // HHPRED, HHBLITS alnEval
            val retrieveFullSeq         = (scriptPath + "/retrieveFullSeq.sh").toFile
            val retrieveAlnEval         = (scriptPath + "/retrieveAlnEval.sh").toFile // Hmmer & PSIBLAST alnEval
            val retrieveFullSeqHHblits  = (scriptPath + "/retrieveFullSeqHHblits.sh").toFile // why so little abstractions ???

            val fullEvalAccs: () => String =
              () => result.HSPS.filter(_.eValue <= form.eval).map { _.accession + " " }.mkString

            val fullSelectedAccs: () => String =
              () =>
                form.selected.map { num =>
                  "%s ".format(result.HSPS(num - 1).accession)
                }.mkString

            val tempFileName = UUID.randomUUID().toString
            val (script, params) = (tool, form.forwardHitsMode, form.sequenceLengthMode) match {
              case (HHBLITS, "eval", "aln") | (HHPRED, "eval", "aln") =>
                (
                  generateAlignmentScript,
                  List(
                    "jobID"    -> jobId,
                    "filename" -> tempFileName,
                    "numList" ->
                    result.HSPS.filter(_.eValue <= form.eval).map { _.num }.mkString(" ")
                  )
                )
              case (HMMER, "eval", "aln") =>
                val accStr: String = result.HSPS
                  .filter(_.eValue <= form.eval)
                  .map { hit =>
                    result.alignment.alignment(hit.num - 1).accession + "\n"
                  }
                  .size
                  .toString
                (retrieveAlnEval, List("accessionsStr" -> accStr, "filename" -> tempFileName, "mode" -> "count"))
              case (PSIBLAST, "eval", "aln") =>
                (
                  retrieveAlnEval,
                  List("accessionsStr" -> form.eval.toString, "filename" -> tempFileName, "mode" -> "eval")
                )

              case (HMMER, "selected", "aln") =>
                (
                  retrieveAlnEval,
                  List(
                    "accessionsStr" -> form.selected.mkString("\n"),
                    "filename"      -> tempFileName,
                    "mode"          -> "selHmmer"
                  )
                )
              case (PSIBLAST, "selected", "aln") =>
                (
                  retrieveAlnEval,
                  List("accessionsStr" -> form.selected.mkString("\n"), "filename" -> tempFileName, "mode" -> "sel")
                )
              case (HHPRED, "selected", "aln") | (HHBLITS, "selected", "aln") =>
                (
                  generateAlignmentScript,
                  List("jobID" -> jobId, "filename" -> tempFileName, "numList" -> form.selected.mkString("\n"))
                )
              case (PSIBLAST, "eval", "full") | (HMMER, "eval", "full") =>
                (
                  retrieveFullSeq,
                  List(
                    "jobID"         -> jobId,
                    "accessionsStr" -> fullEvalAccs(),
                    "filename"      -> tempFileName,
                    "db"            -> result.db
                  )
                )
              case (HHBLITS, "eval", "full") =>
                (
                  retrieveFullSeqHHblits,
                  List(
                    "jobID"         -> jobId,
                    "accessionsStr" -> fullEvalAccs(),
                    "filename"      -> tempFileName,
                    "db"            -> result.db
                  )
                )
              case (HHBLITS, "selected", "full") =>
                (
                  retrieveFullSeqHHblits,
                  List(
                    "jobID"         -> jobId,
                    "accessionsStr" -> fullSelectedAccs(),
                    "filename"      -> tempFileName,
                    "db"            -> result.db
                  )
                )
              case (PSIBLAST, "selected", "full") | (HMMER, "selected", "full") =>
                (
                  retrieveFullSeq,
                  List(
                    "jobID"         -> jobId,
                    "accessionsStr" -> fullSelectedAccs(),
                    "filename"      -> tempFileName,
                    "db"            -> result.db
                  )
                )

              case _ => throw new IllegalArgumentException("no valid parameters for processing a forwarding job")
            }

            val env: List[(String, String)] = List(
              "ENVIRONMENT"  -> config.get[String]("environment"),
              "BIOPROGSROOT" -> config.get[String]("bioprogs_root"),
              "DATABASES"    -> config.get[String]("db_root")
            )

            Process(script.pathAsString, (constants.jobPath + jobId).toFile.toJava, params ++ env: _*)
              .run()
              .exitValue() match {
              case 0 =>
                val file = new java.io.File(
                  s"${constants.jobPath}${constants.SEPARATOR}$jobId${constants.SEPARATOR}results${constants.SEPARATOR}$tempFileName.fa"
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
