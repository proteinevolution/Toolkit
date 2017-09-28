package models.database.jobs

import java.time.ZonedDateTime

import util.ZonedDateTimeHelper
import com.typesafe.config.ConfigFactory
import models.tools.Toolitem
import play.api.libs.json._
import reactivemongo.bson._
import reactivemongo.play.json._

case class Job(mainID       : BSONObjectID           = BSONObjectID.generate, // ID of the Job in the System
               hash         : String                 = "",                    // Non unique ID to identify duplicate jobs
               jobID        : String, // User visible ID of the Job
               ownerID      : Option[BSONObjectID]   = None, // User to whom the Job belongs
               isPublic     : Boolean                = false,
               state        : JobState               = Submitted, // Status of the Job
               emailUpdate  : Boolean                = false, // Owner wants to be notified when the job is ready
               tool         : String, // Tool used for this Job
               watchList    : List[BSONObjectID]     = List.empty, // List of the users who watch this job, None if not public
               commentList  : List[BSONObjectID]     = List.empty, // List of comment IDs for the Job
               clusterData  : Option[JobClusterData] = None, // Cluster Data
               dateCreated  : Option[ZonedDateTime]  = Some(ZonedDateTime.now), // Creation time of the Job
               dateUpdated  : Option[ZonedDateTime]  = Some(ZonedDateTime.now), // Last Updated on
               dateViewed   : Option[ZonedDateTime]  = Some(ZonedDateTime.now), // Last Viewed on
               dateDeletion : Option[ZonedDateTime]  = None, // Date the job should be deleted on (if non standard)
               IPHash       : Option[String]) // hash of the ip
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
      Job.STATE        -> state,
      Job.DATECREATED  -> dateCreated.map(_.toInstant.toEpochMilli),
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
      Job.STATE        -> state,
      Job.TOOL         -> tool,
      Job.COMMENTLIST  -> commentList.length,
      Job.DATECREATED  -> dateCreated.map(_.toInstant.toEpochMilli),
      Job.DATEUPDATED  -> dateUpdated.map(_.toInstant.toEpochMilli),
      Job.DATEVIEWED   -> dateViewed.map(_.toInstant.toEpochMilli),
      Job.TOOLNAMELONG -> ConfigFactory.load().getString(s"Tools.$tool.longname")
    )
  }
  override def toString: String = {
    s"""--[Job Object]--
        |mainID: ${this.mainID}
        |jobID: ${this.jobID}
        |tool: ${this.tool}
        |state: ${this.state}
        |ownerID: ${this.ownerID.map(_.stringify).getOrElse("no Owner")}
        |created on: ${this.dateCreated.map(_.toString()).getOrElse("--")}
        |--[Job Object end]--
     """.stripMargin
  }

  def isFinished: Boolean = {
    state == Done || state == Error
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
  val JOBID        = "jobID" //              ID for the job
  val HASH         = "hash"
  val PROJECT      = "project" //              project id
  val OWNERID      = "ownerID" //              ID of the job owner
  val OWNER        = "owner" //              Name of the job owner
  val STATUS       = "status" //              Status of the job field
  val STATE        = "state"
  val EMAILUPDATE  = "emailUpdate" //              check if the user wants a notification when the job is done
  val DELETION     = "deletion" //              Deletion status flag
  val TOOL         = "tool" //              name of the tool field
  val LABEL        = "label"
  val WATCHLIST    = "watchList" //              optional list of watching users references
  val COMMENTLIST  = "commentList" //              comment list references
  val CLUSTERDATA  = "clusterData" //              detailed data on the cluster usage
  val SGEID        = s"$CLUSTERDATA.${JobClusterData.SGEID}"
  val DATECREATED  = "dateCreated" //              created on field
  val DATEUPDATED  = "dateUpdated" //              changed on field
  val DATEVIEWED   = "dateViewed" //              last view on field
  val DATEDELETION = "dateDeletion" // date wh
  val TOOLNAMELONG = "toolnameLong" //           long tool name
  val IPHASH       = "IPHash" //                  ip hash

  implicit object JsonReader extends Reads[Job] {
    // TODO this is unused at the moment, as there is no convertion of JSON -> Job needed.
    override def reads(json: JsValue): JsResult[Job] = json match {
      case obj: JsObject =>
        try {
          val mainID       = (obj \ ID).asOpt[String]
          val jobID        = (obj \ JOBID).asOpt[String]
          val ownerID      = (obj \ OWNERID).asOpt[String]
          val project      = (obj \ PROJECT).asOpt[String]
          val state        = (obj \ STATE).asOpt[JobState]
          val tool         = (obj \ TOOL).asOpt[String]
          val label        = (obj \ LABEL).asOpt[String]
          val watchList    = (obj \ WATCHLIST).asOpt[List[String]]
          val commentList  = (obj \ COMMENTLIST).asOpt[List[String]]
          val dateCreated  = (obj \ DATECREATED).asOpt[String]
          val dateUpdated  = (obj \ DATEUPDATED).asOpt[String]
          val dateViewed   = (obj \ DATEVIEWED).asOpt[String]
          val toolnameLong = (obj \ TOOLNAMELONG).asOpt[String]
          val IPHash       = (obj \ IPHASH).asOpt[String]
          val datetimenow  = ZonedDateTime.now()
          JsSuccess(
            Job(
              mainID = BSONObjectID.generate(),
              jobID = "",
              ownerID = Some(BSONObjectID.generate()),
              state = state.get,
              tool = "",
              dateCreated = Some(datetimenow),
              dateUpdated = Some(datetimenow),
              dateViewed = Some(datetimenow),
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
      JOBID        -> job.jobID,
      HASH         -> job.hash,
      OWNERID      -> job.ownerID,
      STATE        -> job.state,
      EMAILUPDATE  -> job.emailUpdate,
      TOOL         -> job.tool,
      WATCHLIST    -> job.watchList,
      COMMENTLIST  -> job.commentList,
      CLUSTERDATA  -> job.clusterData,
      DATECREATED  -> job.dateCreated.fold(-1L)(_.toInstant.toEpochMilli),
      DATEUPDATED  -> job.dateUpdated.fold(-1L)(_.toInstant.toEpochMilli),
      DATEVIEWED   -> job.dateViewed.fold(-1L)(_.toInstant.toEpochMilli),
      DATEDELETION -> job.dateDeletion.map(_.toInstant.toEpochMilli),
      IPHASH       -> job.IPHash
    )
  }

  /**
    * Object containing the writer for the Class
    */
  implicit object Reader extends BSONDocumentReader[Job] {
    def read(bson: BSONDocument): Job = {
      Job(
        mainID       = bson.getAs[BSONObjectID](IDDB).getOrElse(BSONObjectID.generate()),
        jobID        = bson.getAs[String](JOBID).getOrElse("Error loading Job Name"),
        hash         = bson.getAs[String](HASH).getOrElse(""),
        ownerID      = bson.getAs[BSONObjectID](OWNERID),
        state        = bson.getAs[JobState](STATE).getOrElse(bson.getAs[JobState](STATUS).getOrElse(Error)),
        emailUpdate  = bson.getAs[Boolean](EMAILUPDATE).getOrElse(false),
        tool         = bson.getAs[String](TOOL).getOrElse(""),
        watchList    = bson.getAs[List[BSONObjectID]](WATCHLIST).getOrElse(List.empty),
        commentList  = bson.getAs[List[BSONObjectID]](COMMENTLIST).getOrElse(List.empty),
        clusterData  = bson.getAs[JobClusterData](CLUSTERDATA),
        dateCreated  = bson.getAs[BSONDateTime](DATECREATED).map(dt => ZonedDateTimeHelper.getZDT(dt)),
        dateUpdated  = bson.getAs[BSONDateTime](DATEUPDATED).map(dt => ZonedDateTimeHelper.getZDT(dt)),
        dateViewed   = bson.getAs[BSONDateTime](DATEVIEWED).map(dt => ZonedDateTimeHelper.getZDT(dt)),
        dateDeletion = bson.getAs[BSONDateTime](DATEDELETION).map(dt => ZonedDateTimeHelper.getZDT(dt)),
        IPHash       = bson.getAs[String](IPHASH)
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
        JOBID        -> job.jobID,
        HASH         -> job.hash,
        OWNERID      -> job.ownerID,
        STATE        -> job.state,
        EMAILUPDATE  -> job.emailUpdate,
        TOOL         -> job.tool,
        WATCHLIST    -> job.watchList,
        COMMENTLIST  -> job.commentList,
        CLUSTERDATA  -> job.clusterData,
        DATECREATED  -> BSONDateTime(job.dateCreated.fold(-1L)(_.toInstant.toEpochMilli)),
        DATEUPDATED  -> BSONDateTime(job.dateUpdated.fold(-1L)(_.toInstant.toEpochMilli)),
        DATEVIEWED   -> BSONDateTime(job.dateViewed.fold(-1L)(_.toInstant.toEpochMilli)),
        DATEDELETION -> job.dateDeletion.map(d => BSONDateTime(d.toInstant.toEpochMilli)),
        IPHASH       -> job.IPHash
      )
    }
  }
}
