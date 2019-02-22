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
import com.typesafe.config.Config
import de.proteinevolution.jobs.models.Job
import de.proteinevolution.common.parsers.FASTA
import de.proteinevolution.tel.RunscriptPathProvider
import de.proteinevolution.tel.env.Env
import de.proteinevolution.util.FNV
import javax.inject.{ Inject, Singleton }
import play.api.{ Configuration, Logging }

import scala.util.hashing.MurmurHash3

@Singleton
final class GeneralHashService @Inject()(runscriptPathProvider: RunscriptPathProvider, config: Configuration)
    extends Logging {

  def generateHash(params: Map[String, String]): BigInt = {
    FNV.hash64(params.toString.getBytes())
  }

  def generateRSHash(toolname: String): String = {
    val runscript = runscriptPathProvider.get() + s"$toolname.sh"
    (for {
      in <- File(runscript).newInputStream.autoClosed
    } yield MurmurHash3.stringHash(in.lines.mkString, 0).toString).get()
  }

  def generateToolHash(name: String): String = {
    MurmurHash3.stringHash(config.get[Config](s"Tools.$name").toString, 0).toString
  }

  def generateJobHash(job: Job, params: Map[String, String], env: Env): String = {
    // filter unique parameters
    val paramsWithoutUniques: Map[String, String] =
    params - Job.JOBID - Job.EMAILUPDATE - "public" - "jobid" - Job.IPHASH - "parentID" - "htb_length" - "alignment" - "file"
    logger.info(
      s"[JobDAO.generateJobHash] Hashing values: ${paramsWithoutUniques.map(kv => s"${kv._1} ${kv._2}").mkString(", ")}"
    )
    val sequenceHash = params.get("alignment") match {
      case Some(alignment) =>
        FASTA.fromString(alignment) match {
          case Some(fastA) =>
            fastA.generateHashCode(MurmurHash3.stringHash)
          case None =>
            ""
        }
      case None =>
        ""
    }

    val dbParam = params match {
      case x if x.isDefinedAt("standarddb") =>
        val STANDARDDB = (env.get("STANDARD") + "/" + params.getOrElse("standarddb", "")).toFile
        (Some("standarddb"), Some(STANDARDDB.lastModifiedTime.toString))
      case x if x.isDefinedAt("hhsuitedb") =>
        val HHSUITEDB = env.get("HHSUITE").toFile
        (Some("hhsuitedb"), Some(HHSUITEDB.lastModifiedTime.toString))
      case x if x.isDefinedAt("hhblitsdb") =>
        val HHBLITSDB = env.get("HHBLITS").toFile
        (Some("hhblitsdb"), Some(HHBLITSDB.lastModifiedTime.toString))
      case _ => (None, None)
    }

    s"""$sequenceHash
       |${generateHash(paramsWithoutUniques).toString()}
       |${generateRSHash(job.tool)}
       |${dbParam._1.getOrElse("")}
       |${dbParam._2.getOrElse("")}
       |${job.tool}
       |${generateToolHash(job.tool)}""".stripMargin
  }

}
