package models.database.jobs

import com.typesafe.config.ConfigFactory
import models.Constants
import models.tools.Toolitem
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.libs.json._
import play.twirl.api.Html
import reactivemongo.bson._
import reactivemongo.play.json._

case class Job(mainID: BSONObjectID, // ID of the Job in the System
               parentID: Option[BSONObjectID] = None, // ID of the Parent Job
               jobID: String, // User visible ID of the Job
               ownerID: Option[BSONObjectID] = None, // User to whom the Job belongs
               project: Option[BSONObjectID] = None,
               status: JobState, // Status of the Job
               emailUpdate: Boolean = false, // Owner wants to be notified when the job is ready
               deletion: Option[JobDeletion] = None, // Deletion Flag showing the reason for the deletion
               tool: String, // Tool used for this Job
               label: Option[String],
               watchList: List[BSONObjectID] = List.empty, // List of the users who watch this job, None if not public
               commentList: List[BSONObjectID] = List.empty, // List of comment IDs for the Job
               clusterData: Option[JobClusterData] = None, // Cluster Data
               dateCreated: Option[DateTime], // Creation time of the Job
               dateUpdated: Option[DateTime], // Last Updated on
               dateViewed: Option[DateTime]) // Last Viewed on
    extends Constants {

  // Returns if the job is private or not
  def isPrivate: Boolean = {
    ownerID.isDefined // TODO why is this the only measure for being a private job?
  }

  /**
    * Returns a clean JSON Object representation of the Job (used in the websockets to push a job)
    *
    * @return
    */
  def cleaned(): JsObject = {

    Json.obj(
      Job.JOBID        -> jobID,
      "project"      -> project,
      Job.STATUS       -> status,
      Job.DATECREATED    -> dateCreated.get,
      Job.TOOL     -> tool,
      "toolnameLong" -> ConfigFactory.load().getString(s"Tools.$tool.longname")
    )
  }

  /**
    * Returns a clean JSON Object representation of the Job (used in the Job Manager)
    *
    * @return
    */
  def jobManagerJob(): JsObject = {
    val dtf = DateTimeFormat.forPattern("dd.MM.yyyy HH:mm:ss")
    Json.obj(
      Job.JOBID       -> jobID,
      Job.PROJECT     -> project,
      Job.STATUS      -> status,
      Job.TOOL        -> tool,
      Job.COMMENTLIST -> commentList.length,
      Job.DATECREATED -> dateCreated.map(dt => Json.obj("string" -> dtf.print(dt), "timestamp" -> dt.getMillis)),
      Job.DATEUPDATED -> dateUpdated.map(dt => Json.obj("string" -> dtf.print(dt), "timestamp" -> dt.getMillis)),
      Job.DATEVIEWED  -> dateViewed.map(dt => Json.obj("string" -> dtf.print(dt), "timestamp" -> dt.getMillis))
    )
  }
  override def toString: String = {
    s"""--[Job Object]--
        |mainID: ${this.mainID}
        |jobID: ${this.jobID}
        |tool: ${this.tool}
        |state: ${this.status}
        |ownerID: ${this.ownerID.map(_.stringify).getOrElse("no Owner")}
        |created on: ${this.dateCreated.map(_.toString()).getOrElse("--")}
        |--[Job Object end]--
     """.stripMargin
  }
}

// Server returns such an object when asked for a job
case class Jobitem(mainID: String,
                   newMainID: String, // Used for job resubmission
                   jobID: String,
                   project: String,
                   state: JobState,
                   ownerName: String,
                   createdOn: String,
                   toolitem: Toolitem,
                   views: Seq[String],
                   paramValues: Map[String, String])

object Job {
  // Constants for the JSON object identifiers
  val ID          = "id" // name for the ID in scala
  val IDDB        = "_id" //              ID in MongoDB
  val PARENTID    = "parentID" //              ID of the parent job
  val JOBID       = "jobID" //              ID for the job
  val PROJECT     = "project" //              project id
  val OWNERID     = "ownerID" //              ID of the job owner
  val OWNER       = "owner" //              Name of the job owner
  val STATUS      = "status" //              Status of the job field
  val EMAILUPDATE = "emailUpdate" //              check if the user wants a notification when the job is done
  val DELETION    = "deletion" //              Deletion status flag
  val TOOL        = "tool" //              name of the tool field
  val LABEL       = "label"
  val WATCHLIST   = "watchList" //              optional list of watching users references
  val COMMENTLIST = "commentList" //              comment list references
  val CLUSTERDATA = "clusterData" //              detailed data on the cluster usage
  val DATECREATED = "dateCreated" //              created on field
  val DATEUPDATED = "dateUpdated" //              changed on field
  val DATEVIEWED  = "dateViewed" //              last view on field

  implicit object JsonReader extends Reads[Job] {
    // TODO this is unused at the moment, as there is no convertion of JSON -> Job needed.
    override def reads(json: JsValue): JsResult[Job] = json match {
      case obj: JsObject =>
        try {
          val mainID      = (obj \ ID).asOpt[String]
          val parentID    = (obj \ PARENTID).asOpt[String]
          val jobID       = (obj \ JOBID).asOpt[String]
          val ownerID     = (obj \ OWNERID).asOpt[String]
          val project     = (obj \ PROJECT).asOpt[String]
          val status      = (obj \ STATUS).asOpt[JobState]
          val deletion    = (obj \ DELETION).asOpt[JobDeletion]
          val tool        = (obj \ TOOL).asOpt[String]
          val label       = (obj \ LABEL).asOpt[String]
          val watchList   = (obj \ WATCHLIST).asOpt[List[String]]
          val commentList = (obj \ COMMENTLIST).asOpt[List[String]]
          val dateCreated = (obj \ DATECREATED).asOpt[String]
          val dateUpdated = (obj \ DATEUPDATED).asOpt[String]
          val dateViewed  = (obj \ DATEVIEWED).asOpt[String]
          JsSuccess(
            Job(
              mainID = BSONObjectID.generate(), // TODO need to find out how to get the main id as it is needed for the job
              parentID = None,
              jobID = "",
              ownerID = Some(BSONObjectID.generate()),
              project = Some(BSONObjectID.generate()),
              status = status.get,
              deletion = deletion,
              tool = "",
              label = Some(""),
              dateCreated = Some(new DateTime()),
              dateUpdated = Some(new DateTime()),
              dateViewed = Some(new DateTime())
            ))
        } catch {
          case cause: Throwable => JsError(cause.getMessage)
        }
      case _ => JsError("expected.jsobject")
    }
  }

  implicit object JobWrites extends Writes[Job] {
    def writes(job: Job): JsObject = Json.obj(
      IDDB        -> job.mainID,
      PARENTID    -> job.parentID,
      JOBID       -> job.jobID,
      OWNERID     -> job.ownerID,
      PROJECT     -> job.project,
      STATUS      -> job.status,
      EMAILUPDATE -> job.emailUpdate,
      DELETION    -> job.deletion,
      TOOL        -> job.tool,
      WATCHLIST   -> job.watchList,
      COMMENTLIST -> job.commentList,
      CLUSTERDATA -> job.clusterData,
      DATECREATED -> job.dateCreated.fold(-1L)(_.getMillis),
      DATEUPDATED -> job.dateUpdated.fold(-1L)(_.getMillis),
      DATEVIEWED  -> job.dateViewed.fold(-1L)(_.getMillis)
    )
  }

  /**
    * Object containing the writer for the Class
    */
  implicit object Reader extends BSONDocumentReader[Job] {
    def read(bson: BSONDocument): Job = {
      Job(
        mainID = bson.getAs[BSONObjectID](IDDB).getOrElse(BSONObjectID.generate()),
        parentID = bson.getAs[BSONObjectID](PARENTID),
        jobID = bson.getAs[String](JOBID).getOrElse("Error loading Job Name"),
        ownerID = bson.getAs[BSONObjectID](OWNERID),
        project = bson.getAs[BSONObjectID](PROJECT),
        status = bson.getAs[JobState](STATUS).getOrElse(Error),
        emailUpdate = bson.getAs[Boolean](EMAILUPDATE).getOrElse(false),
        deletion = bson.getAs[JobDeletion](DELETION),
        tool = bson.getAs[String](TOOL).getOrElse(""),
        label = bson.getAs[String](LABEL),
        watchList = bson.getAs[List[BSONObjectID]](WATCHLIST).getOrElse(List.empty),
        commentList = bson.getAs[List[BSONObjectID]](COMMENTLIST).getOrElse(List.empty),
        clusterData = bson.getAs[JobClusterData](CLUSTERDATA),
        dateCreated = bson.getAs[BSONDateTime](DATECREATED).map(dt => new DateTime(dt.value)),
        dateUpdated = bson.getAs[BSONDateTime](DATEUPDATED).map(dt => new DateTime(dt.value)),
        dateViewed = bson.getAs[BSONDateTime](DATEVIEWED).map(dt => new DateTime(dt.value))
      )
    }
  }

  /**
    * Object containing the writer for the Class
    */
  implicit object Writer extends BSONDocumentWriter[Job] {
    def write(job: Job): BSONDocument = {
      val emailUpdate = if (job.emailUpdate) Some(true) else None
      BSONDocument(
        IDDB        -> job.mainID,
        PARENTID    -> job.parentID,
        JOBID       -> job.jobID,
        OWNERID     -> job.ownerID,
        PROJECT     -> job.project,
        STATUS      -> job.status,
        EMAILUPDATE -> emailUpdate,
        DELETION    -> job.deletion,
        TOOL        -> job.tool,
        LABEL       -> job.label,
        WATCHLIST   -> job.watchList,
        COMMENTLIST -> job.commentList,
        CLUSTERDATA -> job.clusterData,
        DATECREATED -> BSONDateTime(job.dateCreated.fold(-1L)(_.getMillis)),
        DATEUPDATED -> BSONDateTime(job.dateUpdated.fold(-1L)(_.getMillis)),
        DATEVIEWED  -> BSONDateTime(job.dateViewed.fold(-1L)(_.getMillis))
      )
    }
  }
}
