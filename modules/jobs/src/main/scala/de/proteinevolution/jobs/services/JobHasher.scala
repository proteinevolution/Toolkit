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
import de.proteinevolution.common.parsers.FASTA
import de.proteinevolution.jobs.models.Job
import de.proteinevolution.tel.RunscriptPathProvider
import de.proteinevolution.util.FNV
import javax.inject.{ Inject, Singleton }
import play.api.Configuration

import scala.util.hashing.MurmurHash3

@Singleton
final class JobHasher @Inject()(
    runscriptPathProvider: RunscriptPathProvider,
    config: Configuration
) {

  private[this] def generateHash(params: Map[String, String]): BigInt = FNV.hash64(params.toString.getBytes())

  private[this] def generateRSHash(toolname: String): String =
    File(runscriptPathProvider.get() + s"$toolname.sh").newInputStream.autoClosed
      .map(in => MurmurHash3.stringHash(in.lines.mkString, 0).toString)
      .get()

  private[this] def generateToolHash(name: String): String =
    MurmurHash3.stringHash(config.get[Config](s"Tools.$name").toString, 0).toString

  /**
   * params which won't get hashed
   * TODO: we write all params into job descriptor files but we should model them, issue #705
   */
  private[this] final val EXCLUDED =
    Set(Job.ID, Job.EMAIL_UPDATE, "public", "jobid", Job.IP_HASH, "parentID", "htb_length", "alignment", "file")

  def generateJobHash(job: Job, params: Map[String, String]): String = {
    val hashable: Map[String, String] = params -- EXCLUDED

    val sequenceHash: Option[Int] = for {
      alignment <- params.get("alignment")
      fastA     <- FASTA.fromString(alignment)
    } yield fastA.generateHashCode(MurmurHash3.stringHash)

    val (dbName, dbVersion): (String, String) = params match {
      case p if p.isDefinedAt("standarddb") =>
        ("standarddb",
         (config.get[String]("tel.env.STANDARD") + "/" + params.getOrElse("standarddb", "")).toFile.lastModifiedTime.toString)
      case p if p.isDefinedAt("hhsuitedb") =>
        ("hhsuitedb", config.get[String]("tel.env.HHSUITE").toFile.lastModifiedTime.toString)
      case p if p.isDefinedAt("hhblitsdb") =>
        ("hhblitsdb", config.get[String]("tel.env.HHBLITS").toFile.lastModifiedTime.toString)
      case _ => ("", "")
    }

    s"""${sequenceHash.map(_.toString).getOrElse("")}
       |${generateHash(hashable).toString()}
       |${generateRSHash(job.tool)}
       |$dbName
       |$dbVersion
       |${job.tool}
       |${generateToolHash(job.tool)}""".stripMargin
  }

}
