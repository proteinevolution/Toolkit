package models.database.jobs

import models.search.JobDAO
import modules.tel.env.Env
import better.files._
import org.joda.time.DateTime
import reactivemongo.bson.{BSONDateTime, BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}

/**
  *
  * Created by snam on 23.08.16.
  *
  * the JobHash also contains non-hashed information to avoid collision as much as possible. Only the input file and the parameters will be hashed
  * due performance reasons. Note that not every tool/job uses a database.
  *
  */
case class JobHash(mainID: BSONObjectID,
                   inputHash: String,
                   runscriptHash: String,
                   dbName: Option[String],
                   dbMtime: Option[String],
                   toolName: String,
                   toolHash: String,
                   dateCreated: Option[DateTime],
                   jobID: String)

object JobHash {
  val ID            = "_id"
  val INPUTHASH     = "hash"
  val RUNSCRIPTHASH = "rshash"
  val DBNAME        = "dbname"
  val DBMTIME       = "dbmtime"
  val TOOLNAME      = "toolname"
  val TOOLHASH      = "toolhash"
  val DATECREATED   = "dateCreated"
  val JOBID         = "jobID"

  implicit object Reader extends BSONDocumentReader[JobHash] {
    override def read(bson: BSONDocument): JobHash = JobHash(
      bson.getAs[BSONObjectID](ID).getOrElse(BSONObjectID.generate()),
      bson.getAs[String](INPUTHASH).getOrElse("No matching hash value found"),
      bson.getAs[String](RUNSCRIPTHASH).getOrElse("No matching hash value found"),
      bson.getAs[String](DBNAME),
      bson.getAs[String](DBMTIME),
      bson.getAs[String](TOOLNAME).getOrElse(""),
      bson.getAs[String](TOOLHASH).getOrElse("No matching hash value found"),
      bson.getAs[BSONDateTime](DATECREATED).map(dt => new DateTime(dt.value)),
      bson.getAs[String](JOBID).getOrElse("")
    )
  }

  implicit object Writer extends BSONDocumentWriter[JobHash] {
    override def write(jobHash: JobHash): BSONDocument = BSONDocument(
      ID            -> jobHash.mainID,
      INPUTHASH     -> jobHash.inputHash,
      RUNSCRIPTHASH -> jobHash.runscriptHash,
      DBNAME        -> jobHash.dbName,
      DBMTIME       -> jobHash.dbMtime,
      TOOLNAME      -> jobHash.toolName,
      TOOLHASH      -> jobHash.toolHash,
      DATECREATED   -> BSONDateTime(jobHash.dateCreated.fold(-1L)(_.getMillis)),
      JOBID         -> jobHash.jobID
    )
  }

  /**
    * Generates a JobHash for the job from the supplied parameters
    * @param job
    * @param params
    * @return
    */
  def generateJobHash(job: Job, params: Map[String, String], env : Env, jobDAO: JobDAO): JobHash = {
    // filter unique parameters
    val paramsWithoutMainID = params - Job.ID - Job.IDDB - Job.JOBID - Job.EMAILUPDATE - "public"

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

      case _ => (Some("none"), Some("1970-01-01T00:00:00Z"))
    }

    JobHash(
      mainID = job.mainID,
      inputHash = jobDAO.generateHash(paramsWithoutMainID).toString(),
      runscriptHash = jobDAO.generateRSHash(job.tool),
      dbName = dbParam._1, // field must exist so that elasticsearch can do a bool query on multiple fields
      dbMtime = dbParam._2, // use unix epoch time
      toolName = job.tool,
      toolHash = jobDAO.generateToolHash(job.tool),
      dateCreated = job.dateCreated,
      jobID = job.jobID
    )
  }
}
