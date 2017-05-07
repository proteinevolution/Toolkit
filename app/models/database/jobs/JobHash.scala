package models.database.jobs

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


case class JobHash(mainID        : BSONObjectID,
                   inputHash     : String,
                   runscriptHash : String,
                   dbName        : Option[String],
                   dbMtime       : Option[String],
                   toolName      : String,
                   toolHash      : String,
                   dateCreated   : Option[DateTime],
                   jobID         : String,
                   active        : Boolean = false)

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
  val ACTIVE        = "active"


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
      bson.getAs[String](JOBID).getOrElse(""),
      bson.getAs[Boolean](ACTIVE).getOrElse(false)
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
      JOBID         -> jobHash.jobID,
      ACTIVE        -> jobHash.active
    )
  }
}
