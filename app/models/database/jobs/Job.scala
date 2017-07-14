package models.database.jobs

import com.typesafe.config.ConfigFactory
import models.Constants
import models.tools.Toolitem
import org.joda.time.DateTime
import play.api.libs.json._
import reactivemongo.bson._
import reactivemongo.play.json._
import javax.inject.Inject

case class Job(mainID: BSONObjectID, // ID of the Job in the System
               parentID: Option[BSONObjectID] = None, // ID of the Parent Job
               jobID: String, // User visible ID of the Job
               ownerID: Option[BSONObjectID] = None, // User to whom the Job belongs
               isPublic: Boolean = false,
               status: JobState, // Status of the Job
               emailUpdate: Boolean = false, // Owner wants to be notified when the job is ready
               deletion: Option[JobDeletion] = None, // Deletion Flag showing the reason for the deletion
               tool: String, // Tool used for this Job
               watchList: List[BSONObjectID] = List.empty, // List of the users who watch this job, None if not public
               commentList: List[BSONObjectID] = List.empty, // List of comment IDs for the Job
               clusterData: Option[JobClusterData] = None, // Cluster Data
               dateCreated: Option[DateTime], // Creation time of the Job
               dateUpdated: Option[DateTime], // Last Updated on
               dateViewed: Option[DateTime], // Last Viewed on
               IPHash: Option[String]) // hash of the ip
   {

  // Returns if the job is private or not
  def isPrivate: Boolean = {
    ownerID.isDefined && !isPublic // TODO why is this the only measure for being a private job?
  }

  /**
    * Returns a clean JSON Object representation of the Job (used in the websockets to push a job)
    *
    * @return
    */
  def cleaned(): JsObject = {
    Json.obj(
      Job.JOBID        -> jobID,
      Job.STATUS       -> status,
      Job.DATECREATED  -> dateCreated.map(_.getMillis),
      Job.TOOL         -> tool,
      Job.TOOLNAMELONG -> ConfigFactory.load().getString(s"Tools.$tool.longname")
    )
  }

  /**
    * Returns a clean JSON Object representation of the Job (used in the Job Manager)
    *
    * @return
    */
  def jobManagerJob(): JsObject = {
    Json.obj(
      Job.JOBID        -> jobID,
      Job.STATUS       -> status,
      Job.TOOL         -> tool,
      Job.COMMENTLIST  -> commentList.length,
      Job.DATECREATED  -> dateCreated.map(_.getMillis),
      Job.DATEUPDATED  -> dateUpdated.map(_.getMillis),
      Job.DATEVIEWED   -> dateViewed.map(_.getMillis),
      Job.TOOLNAMELONG -> ConfigFactory.load().getString(s"Tools.$tool.longname")
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
case class Jobitem(jobID: String,
                   state: JobState,
                   ownerName: String,
                   dateCreated: String,
                   toolitem: Toolitem,
                   views: Seq[String],
                   paramValues: Map[String, String])

object Job {
  // Constants for the JSON object identifiers
  val ID           = "id" // name for the ID in scala
  val IDDB         = "_id" //              ID in MongoDB
  val PARENTID     = "parentID" //              ID of the parent job
  val JOBID        = "jobID" //              ID for the job
  val PROJECT      = "project" //              project id
  val OWNERID      = "ownerID" //              ID of the job owner
  val OWNER        = "owner" //              Name of the job owner
  val STATUS       = "status" //              Status of the job field
  val EMAILUPDATE  = "emailUpdate" //              check if the user wants a notification when the job is done
  val DELETION     = "deletion" //              Deletion status flag
  val TOOL         = "tool" //              name of the tool field
  val LABEL        = "label"
  val WATCHLIST    = "watchList" //              optional list of watching users references
  val COMMENTLIST  = "commentList" //              comment list references
  val CLUSTERDATA  = "clusterData" //              detailed data on the cluster usage
  val DATECREATED  = "dateCreated" //              created on field
  val DATEUPDATED  = "dateUpdated" //              changed on field
  val DATEVIEWED   = "dateViewed" //              last view on field
  val TOOLNAMELONG = "toolnameLong" //           long tool name
  val IPHASH       = "IPHash" //                  ip hash

  implicit object JsonReader extends Reads[Job] {
    // TODO this is unused at the moment, as there is no convertion of JSON -> Job needed.
    override def reads(json: JsValue): JsResult[Job] = json match {
      case obj: JsObject =>
        try {
          val mainID       = (obj \ ID).asOpt[String]
          val parentID     = (obj \ PARENTID).asOpt[String]
          val jobID        = (obj \ JOBID).asOpt[String]
          val ownerID      = (obj \ OWNERID).asOpt[String]
          val project      = (obj \ PROJECT).asOpt[String]
          val status       = (obj \ STATUS).asOpt[JobState]
          val deletion     = (obj \ DELETION).asOpt[JobDeletion]
          val tool         = (obj \ TOOL).asOpt[String]
          val label        = (obj \ LABEL).asOpt[String]
          val watchList    = (obj \ WATCHLIST).asOpt[List[String]]
          val commentList  = (obj \ COMMENTLIST).asOpt[List[String]]
          val dateCreated  = (obj \ DATECREATED).asOpt[String]
          val dateUpdated  = (obj \ DATEUPDATED).asOpt[String]
          val dateViewed   = (obj \ DATEVIEWED).asOpt[String]
          val toolnameLong = (obj \ TOOLNAMELONG).asOpt[String]
          val IPHash       = (obj \ IPHASH).asOpt[String]
          JsSuccess(
            Job(
              mainID = BSONObjectID.generate(),
              parentID = None,
              jobID = "",
              ownerID = Some(BSONObjectID.generate()),
              status = status.get,
              deletion = deletion,
              tool = "",
              dateCreated = Some(new DateTime()),
              dateUpdated = Some(new DateTime()),
              dateViewed = Some(new DateTime()),
              IPHash = IPHash
            )
          )
        } catch {
          case cause: Throwable => JsError(cause.getMessage)
        }
      case _ => JsError("expected.jsobject")
    }
  }

  implicit object JobWrites extends Writes[Job] {
    def writes(job: Job): JsObject = Json.obj(
      IDDB         -> job.mainID,
      PARENTID     -> job.parentID,
      JOBID        -> job.jobID,
      OWNERID      -> job.ownerID,
      STATUS       -> job.status,
      EMAILUPDATE  -> job.emailUpdate,
      DELETION     -> job.deletion,
      TOOL         -> job.tool,
      WATCHLIST    -> job.watchList,
      COMMENTLIST  -> job.commentList,
      CLUSTERDATA  -> job.clusterData,
      DATECREATED  -> job.dateCreated.fold(-1L)(_.getMillis),
      DATEUPDATED  -> job.dateUpdated.fold(-1L)(_.getMillis),
      DATEVIEWED   -> job.dateViewed.fold(-1L)(_.getMillis),
      IPHASH       -> job.IPHash
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
        status = bson.getAs[JobState](STATUS).getOrElse(Error),
        emailUpdate = bson.getAs[Boolean](EMAILUPDATE).getOrElse(false),
        deletion = bson.getAs[JobDeletion](DELETION),
        tool = bson.getAs[String](TOOL).getOrElse(""),
        watchList = bson.getAs[List[BSONObjectID]](WATCHLIST).getOrElse(List.empty),
        commentList = bson.getAs[List[BSONObjectID]](COMMENTLIST).getOrElse(List.empty),
        clusterData = bson.getAs[JobClusterData](CLUSTERDATA),
        dateCreated = bson.getAs[BSONDateTime](DATECREATED).map(dt => new DateTime(dt.value)),
        dateUpdated = bson.getAs[BSONDateTime](DATEUPDATED).map(dt => new DateTime(dt.value)),
        dateViewed = bson.getAs[BSONDateTime](DATEVIEWED).map(dt => new DateTime(dt.value)),
        IPHash = bson.getAs[String](IPHASH)
      )
    }
  }

  /**
    * Object containing the writer for the Class
    */
  implicit object Writer extends BSONDocumentWriter[Job] {
    def write(job: Job): BSONDocument = {
      BSONDocument(
        IDDB         -> job.mainID,
        PARENTID     -> job.parentID,
        JOBID        -> job.jobID,
        OWNERID      -> job.ownerID,
        STATUS       -> job.status,
        EMAILUPDATE  -> job.emailUpdate,
        DELETION     -> job.deletion,
        TOOL         -> job.tool,
        WATCHLIST    -> job.watchList,
        COMMENTLIST  -> job.commentList,
        CLUSTERDATA  -> job.clusterData,
        DATECREATED  -> BSONDateTime(job.dateCreated.fold(-1L)(_.getMillis)),
        DATEUPDATED  -> BSONDateTime(job.dateUpdated.fold(-1L)(_.getMillis)),
        DATEVIEWED   -> BSONDateTime(job.dateViewed.fold(-1L)(_.getMillis)),
        IPHASH       -> job.IPHash
      )
    }
  }
}
