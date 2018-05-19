package de.proteinevolution.models.search

import javax.inject.{ Inject, Singleton }

import better.files._
import com.typesafe.config.ConfigFactory
import de.proteinevolution.models.database.jobs.Job
import de.proteinevolution.parsers.FASTA
import de.proteinevolution.tel.RunscriptPathProvider
import de.proteinevolution.tel.env.Env
import play.api.Logger
import de.proteinevolution.tools.FNV
import scala.util.hashing.MurmurHash3

@Singleton
final class JobDAO @Inject()(runscriptPathProvider: RunscriptPathProvider) {

  /**
   * generates Param hash for matching already existing jobs
   *
   * @param params
   * @return
   */
  def generateHash(params: Map[String, String]): BigInt = {

    FNV.hash64(params.toString.getBytes())

  }

  /**
   * hashes the runscripts which is used for a job
   *
   * @param toolname
   * @return
   */
  def generateRSHash(toolname: String): String = {
    val runscript = runscriptPathProvider.get() + s"$toolname.sh"
    val source    = scala.io.Source.fromFile(runscript)
    val content   = try { source.getLines().mkString } finally { source.close() }

    MurmurHash3.stringHash(content, 0).toString

  }

  /**
   * hashes the tool version and version of helper scripts specified in the tools.conf config
   *
   * @param name
   * @return
   */
  def generateToolHash(name: String): String = {
    MurmurHash3.stringHash(ConfigFactory.load().getConfig(s"Tools.$name").toString, 0).toString
  }

  /**
   * Generates a JobHash for the job from the supplied parameters
   * @param job
   * @param params
   * @return
   */
  def generateJobHash(job: Job, params: Map[String, String], env: Env): String = {
    // filter unique parameters
    val paramsWithoutUniques: Map[String, String] =
    params - Job.ID - Job.IDDB - Job.JOBID - Job.EMAILUPDATE - "public" - "jobid" - Job.IPHASH - "parentID" - "htb_length" - "alignment" - "file"

    Logger.info(
      s"[JobDAO.enerateJobHash] Hashing values: ${paramsWithoutUniques.map(kv => s"${kv._1} ${kv._2}").mkString(", ")}"
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

    // Create the job Hash depending on what db is used
    val dbParam = params match {
      case x if x isDefinedAt "standarddb" =>
        val STANDARDDB = (env.get("STANDARD") + "/" + params.getOrElse("standarddb", "")).toFile
        (Some("standarddb"), Some(STANDARDDB.lastModifiedTime.toString))

      case x if x isDefinedAt "hhsuitedb" =>
        val HHSUITEDB = env.get("HHSUITE").toFile
        (Some("hhsuitedb"), Some(HHSUITEDB.lastModifiedTime.toString))

      case x if x isDefinedAt "hhblitsdb" =>
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
