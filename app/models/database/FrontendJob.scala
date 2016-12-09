package models.database


import org.joda.time.DateTime
import play.api.libs.json._
import reactivemongo.bson._
import reactivemongo.play.json._

/**
 * storing information for frontend jobs for tool statistics (frontend tools should be usable without internet connection)
 *
 */


case class FrontendJob(mainID : BSONObjectID,                // ID of the Job in the System
               jobType     : String,                      // Type of job
               parentID    : Option[BSONObjectID] = None, // ID of the Parent Job
               ownerID     : Option[BSONObjectID] = None, // User to whom the Job belongs
               deletion    : Option[JobDeletion] = None,      // Deletion Flag showing the reason for the deletion
               tool        : String,                      // Tool used for this Job
               dateCreated : Option[DateTime])            // Creation time of the Job


object FrontendJob {
  // Constants for the JSON object identifiers
  val ID            = "id"            // name for the ID in scala
  val IDDB          = "_id"           //              ID in MongoDB
  val JOBTYPE       = "jobType"       //              Type of the Job
  val PARENTID      = "parentID"      //              ID of the parent job
  val OWNERID       = "ownerID"       //              ID of the job owner
  val TOOL          = "tool"          //              name of the tool field
  val DATECREATED   = "dateCreated"   //              created on field

  //implicit val format: Format[Job] = Json.format[Job]


  implicit object JsonReader extends Reads[FrontendJob] {
    // TODO this is unused at the moment, as there is no convertion of JSON -> Job needed.
    override def reads(json: JsValue): JsResult[FrontendJob] = json match {
      case obj: JsObject => try {
        val mainID      = (obj \ ID).asOpt[String]
        val jobType     = (obj \ JOBTYPE).asOpt[String]
        val parentID    = (obj \ PARENTID).asOpt[String]
        val ownerID     = (obj \ OWNERID).asOpt[String]
        val tool        = (obj \ TOOL).asOpt[String]
        val dateCreated = (obj \ DATECREATED).asOpt[String]

        JsSuccess(FrontendJob(
          mainID      = BSONObjectID.generate(),  // TODO need to find out how to get the main id as it is needed for the job
          jobType     = "",
          parentID    = None,
          ownerID     = Some(BSONObjectID.generate()),
          tool        = "",
          dateCreated = Some(new DateTime())))
      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }
      case _ => JsError("expected.jsobject")
    }
  }


  implicit object FrontendJobWrites extends Writes[FrontendJob] {
    def writes (job : FrontendJob) : JsObject = Json.obj(
      IDDB        -> job.mainID,
      JOBTYPE     -> job.jobType,
      PARENTID    -> job.parentID,
      OWNERID     -> job.ownerID,
      TOOL        -> job.tool,
      DATECREATED -> BSONDateTime(job.dateCreated.fold(-1L)(_.getMillis))
    )
  }

  /**
   * Object containing the writer for the Class
   */
  implicit object Reader extends BSONDocumentReader[FrontendJob] {
    def read(bson : BSONDocument): FrontendJob = {
      FrontendJob(mainID      = bson.getAs[BSONObjectID](IDDB).getOrElse(BSONObjectID.generate()),
        jobType     = bson.getAs[String](JOBTYPE).getOrElse("Error loading Job Type"),
        parentID    = bson.getAs[BSONObjectID](PARENTID),
        ownerID     = bson.getAs[BSONObjectID](OWNERID),
        tool        = bson.getAs[String](TOOL).getOrElse(""),
        dateCreated = bson.getAs[BSONDateTime](DATECREATED).map(dt => new DateTime(dt.value))
      )
    }
  }

  /**
   * Object containing the writer for the Class
   */
  implicit object Writer extends BSONDocumentWriter[FrontendJob] {
    def write(job: FrontendJob) : BSONDocument = BSONDocument(
      IDDB        -> job.mainID,
      JOBTYPE     -> job.jobType,
      PARENTID    -> job.parentID,
      OWNERID     -> job.ownerID,
      TOOL        -> job.tool,
      DATECREATED -> BSONDateTime(job.dateCreated.fold(-1L)(_.getMillis))
    )
  }
}


