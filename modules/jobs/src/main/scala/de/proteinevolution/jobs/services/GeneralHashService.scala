package de.proteinevolution.jobs.services

import better.files._
import com.typesafe.config.Config
import de.proteinevolution.jobs.models.Job
import de.proteinevolution.parsers.FASTA
import de.proteinevolution.tel.RunscriptPathProvider
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

  def generateJobHash(job: Job, params: Map[String, String]): String = {
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
        val STANDARDDB = (config.get[String]("tel.env.STANDARD") + "/" + params.getOrElse("standarddb", "")).toFile
        (Some("standarddb"), Some(STANDARDDB.lastModifiedTime.toString))
      case x if x.isDefinedAt("hhsuitedb") =>
        val HHSUITEDB = config.get[String]("tel.env.HHSUITE").toFile
        (Some("hhsuitedb"), Some(HHSUITEDB.lastModifiedTime.toString))
      case x if x.isDefinedAt("hhblitsdb") =>
        val HHBLITSDB = config.get[String]("tel.env.HHBLITS").toFile
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
