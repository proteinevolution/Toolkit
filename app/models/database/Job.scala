package models.database

import org.joda.time.DateTime
import reactivemongo.bson._

/** ?
  *
  * @param mainID
  * @param jobType
  * @param parentID
  * @param jobID
  * @param ownerID
  * @param status
  * @param tool
  * @param statID
  * @param dateCreated
  * @param dateUpdated
  * @param dateViewed
  *
  * Maps MySQL schema with some fields renamed (e.g. type is a reserved word in Scala)
  * +------------+------------------+------+-----+-------------------+-----------------------------+
    | Field      | Type             | Null | Key | Default           | Extra                       |
    +------------+------------------+------+-----+-------------------+-----------------------------+
    | main_id    | int(10) unsigned | NO   | PRI | NULL              | auto_increment              |
    | type       | varchar(50)      | YES  |     | NULL              |                             |
    | parent_id  | int(11)          | YES  |     | NULL              |                             |
    | job_id     | varchar(100)     | YES  |     | NULL              |                             |
    | user_id    | int(11)          | YES  |     | NULL              |                             |
    | status     | char(1)          | YES  |     | NULL              |                             |
    | tool       | varchar(100)     | YES  |     | NULL              |                             |
    | stat_id    | int(11)          | YES  |     | NULL              |                             |
    | created_on | timestamp        | NO   |     | CURRENT_TIMESTAMP |                             |
    | updated_on | timestamp        | NO   |     | CURRENT_TIMESTAMP | on update CURRENT_TIMESTAMP |
    | viewed_on  | timestamp        | NO   |     | CURRENT_TIMESTAMP | on update CURRENT_TIMESTAMP |
    +------------+------------------+------+-----+-------------------+-----------------------------+
  *
  *
  */


case class Job(mainID      : BSONObjectID,            // ID of the Job in the System
               jobType     : String,                  // Type of job
               parentID    : Option[BSONObjectID],    // ID of the Parent Job
               jobID       : String,                  // User visible ID of the Job
               sessionID   : BSONObjectID,            // ID referencing the session Object
               userID      : Option[BSONObjectID],    // User to whom the Job belongs
               status      : String,                  // Status of the Job
               tool        : String,                  // Tool used for this Job
               statID      : String,                  //
               dateCreated : Option[DateTime],        // Creation time of the Job
               dateUpdated : Option[DateTime],        // Last Updated on
               dateViewed  : Option[DateTime])        // Last Viewed on

object Job {
  // Constants for the JSON object identifiers
  val ID            = "id"            // name for the ID in scala
  val IDDB          = "_id"           //              ID in MongoDB
  val JOBTYPE       = "jobType"       //              Type of the Job
  val PARENTID      = "parentID"      //              ID of the parent job
  val JOBID         = "jobID"         //              ID for the job
  val SESSIONID     = "sessionID"     //              ID of the Session
  val USERID        = "userID"        //              ID of the job owner
  val STATUS        = "status"        //              Status of the job field
  val TOOL          = "tool"          //              name of the tool field
  val STATID        = "statID"        //              ID of the stats for this Job
  val DATECREATED   = "dateCreated"   //              created on field
  val DATEUPDATED   = "dateUpdated"   //              changed on field
  val DATEVIEWED    = "dateViewed"    //              last view on field

  /**
    * Object containing the writer for the Class
    */
  implicit object Reader extends BSONDocumentReader[Job] {
    def read(bson : BSONDocument): Job = {
      Job(mainID      = bson.getAs[BSONObjectID](IDDB).get,
          jobType     = bson.getAs[String](JOBTYPE).get,
          parentID    = bson.getAs[BSONObjectID](PARENTID),
          jobID       = bson.getAs[String](JOBID).get,
          sessionID   = bson.getAs[BSONObjectID](SESSIONID).get,
          userID      = bson.getAs[BSONObjectID](USERID),
          status      = bson.getAs[String](STATUS).get,
          tool        = bson.getAs[String](TOOL).get,
          statID      = bson.getAs[String](STATID).get,
          dateCreated = bson.getAs[BSONDateTime](DATECREATED).map(dt => new DateTime(dt.value)),
          dateUpdated = bson.getAs[BSONDateTime](DATEUPDATED).map(dt => new DateTime(dt.value)),
          dateViewed  = bson.getAs[BSONDateTime](DATEVIEWED).map(dt => new DateTime(dt.value)))
    }
  }

  /**
    * Object containing the writer for the Class
    */
  implicit object Writer extends BSONDocumentWriter[Job] {
    def write(job: Job) : BSONDocument = BSONDocument(
      ID          -> job.mainID,
      JOBTYPE     -> job.jobType,
      PARENTID    -> job.parentID,
      JOBID       -> job.jobID,
      SESSIONID   -> job.sessionID,
      USERID      -> job.userID,
      STATUS      -> job.status,
      TOOL        -> job.tool,
      STATID      -> job.statID,
      DATECREATED -> BSONDateTime(job.dateCreated.fold(-1L)(_.getMillis)),
      DATEUPDATED -> BSONDateTime(job.dateUpdated.fold(-1L)(_.getMillis)),
      DATEVIEWED  -> BSONDateTime(job.dateViewed.fold(-1L)(_.getMillis)))
  }
}