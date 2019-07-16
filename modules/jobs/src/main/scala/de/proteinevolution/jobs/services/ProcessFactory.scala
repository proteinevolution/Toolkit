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
import de.proteinevolution.common.models.ToolName.{HHBLITS, HHPRED, HMMER, PSIBLAST}
import de.proteinevolution.jobs.models.ForwardMode
import play.api.Configuration

import scala.sys.process
import scala.sys.process.Process

private[jobs] object ProcessFactory {

  def apply(
      resultFile: File,
      jobID: String,
      toolName: String,
      tempFileName: String,
      mode: ForwardMode,
      accString: String,
      db: String,
      basePath: String,
      config: Configuration
  ): process.ProcessBuilder = {

    val generateAlignmentScript = (basePath + "/generateAlignment.sh").toFile // HHPRED, HHBLITS alnEval
    val retrieveFullSeq         = (basePath + "/retrieveFullSeq.sh").toFile
    val retrieveAlnEval         = (basePath + "/retrieveAlnEval.sh").toFile // Hmmer & PSIBLAST alnEval
    val retrieveFullSeqHHblits  = (basePath + "/retrieveFullSeqHHblits.sh").toFile // why so little abstractions ???

    val (script, params) = (toolName, mode.toString) match {
      case (HHBLITS.value, "alnEval") | (HHPRED.value, "alnEval") =>
        (generateAlignmentScript, List("jobID" -> jobID, "filename" -> tempFileName, "numList" -> accString))
      case (HMMER.value, "alnEval") =>
        (retrieveAlnEval, List("accessionsStr" -> accString, "filename" -> tempFileName, "mode" -> "count"))
      case (PSIBLAST.value, "alnEval") =>
        (retrieveAlnEval, List("accessionsStr" -> accString, "filename" -> tempFileName, "mode" -> "eval"))
      case (HMMER.value, "aln") =>
        (retrieveAlnEval, List("accessionsStr" -> accString, "filename" -> tempFileName, "mode" -> "selHmmer"))
      case (PSIBLAST.value, "aln") =>
        (retrieveAlnEval, List("accessionsStr" -> accString, "filename" -> tempFileName, "mode" -> "sel"))
      case (HHPRED.value, "aln") | (HHBLITS.value, "aln") =>
        (generateAlignmentScript, List("jobID" -> jobID, "filename" -> tempFileName, "numList" -> accString))
      case (PSIBLAST.value, "evalFull") | (HMMER.value, "evalFull") =>
        (retrieveFullSeq, List("jobID" -> jobID, "accessionsStr" -> accString, "filename" -> tempFileName, "db" -> db))
      case (HHBLITS.value, "evalFull") | (HHBLITS.value, "full") =>
        (retrieveFullSeqHHblits,
         List("jobID" -> jobID, "accessionsStr" -> accString, "filename" -> tempFileName, "db" -> db))
      case (PSIBLAST.value, "full") | (HMMER.value, "full") =>
        (retrieveFullSeq, List("jobID" -> jobID, "accessionsStr" -> accString, "filename" -> tempFileName, "db" -> db))
      case _ => throw new IllegalArgumentException("no valid parameters for processing a forwarding job")
    }

    val env: List[(String, String)] = List(
      "ENVIRONMENT"  -> config.get[String]("environment"),
      "BIOPROGSROOT" -> config.get[String]("bioprogs_root"),
      "DATABASES"    -> config.get[String]("db_root")
    )

    Process(script.pathAsString, resultFile.toJava, params ++ env: _*)
  }

}
