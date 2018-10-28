package de.proteinevolution.models.database.jobs

import java.time.ZonedDateTime

import de.proteinevolution.models.database.jobs.JobState._
import de.proteinevolution.models.util.ZonedDateTimeHelper
import de.proteinevolution.services.ToolConfig
import io.circe.java8.time._
import io.circe.syntax._
import io.circe.{ Encoder, JsonObject }
import play.api.Configuration
import reactivemongo.bson._

// TODO remove default values
case class Job(
    jobID: String,
    parentID: Option[String] = None,
    hash: Option[String] = None,
    ownerID: Option[BSONObjectID] = None,
    isPublic: Boolean = false,
    status: JobState = Submitted,
    emailUpdate: Boolean = false,
    tool: String,
    watchList: List[BSONObjectID] = List.empty,
    clusterData: Option[JobClusterData] = None,
    dateCreated: Option[ZonedDateTime] = Some(ZonedDateTime.now),
    dateUpdated: Option[ZonedDateTime] = Some(ZonedDateTime.now),
    dateViewed: Option[ZonedDateTime] = Some(ZonedDateTime.now),
    dateDeletion: Option[ZonedDateTime] = None,
    IPHash: Option[String]
) {

  def cleaned(toolConfig: ToolConfig)(implicit config: Configuration): JsonObject = {
    JsonObject(
      Job.JOBID        -> jobID.asJson,
      Job.STATUS       -> status.asJson,
      Job.DATECREATED  -> dateCreated.map(_.toInstant.toEpochMilli).asJson,
      Job.TOOL         -> tool.asJson,
      Job.CODE         -> toolConfig.values(tool).code.asJson,
      Job.TOOLNAMELONG -> config.get[String](s"Tools.$tool.longname").asJson
    )
  }

  def jobManagerJob()(implicit config: Configuration): JsonObject = {
    JsonObject(
      Job.JOBID        -> jobID.asJson,
      Job.STATUS       -> status.asJson,
      Job.TOOL         -> tool.asJson,
      Job.DATECREATED  -> dateCreated.map(_.toInstant.toEpochMilli).asJson,
      Job.DATEUPDATED  -> dateUpdated.map(_.toInstant.toEpochMilli).asJson,
      Job.DATEVIEWED   -> dateViewed.map(_.toInstant.toEpochMilli).asJson,
      Job.TOOLNAMELONG -> config.get[String](s"Tools.$tool.longname").asJson
    )
  }

  override def toString: String = {
    s"""--[Job Object]--
        |jobID: ${this.jobID}
        |parentID: ${this.parentID}
        |tool: ${this.tool}
        |state: ${this.status}
        |ownerID: ${this.ownerID.map(_.stringify).getOrElse("no owner")}
        |created on: ${this.dateCreated.map(_.toString()).getOrElse("--")}
        |--[Job Object end]--
     """.stripMargin
  }

  def isPrivate: Boolean = ownerID.isDefined && !isPublic

  def isFinished: Boolean = status == Done || status == Error

}

object Job {

  final val JOBID        = "job_id"
  final val PARENTID     = "parent_id"
  final val HASH         = "hash"
  final val PROJECT      = "project"
  final val OWNERID      = "owner_id"
  final val OWNER        = "owner"
  final val ISPUBLIC     = "is_public"
  final val STATUS       = "status"
  final val EMAILUPDATE  = "email_update"
  final val DELETION     = "deletion"
  final val TOOL         = "tool"
  final val CODE         = "code"
  final val LABEL        = "label"
  final val WATCHLIST    = "watch_list"
  final val CLUSTERDATA  = "cluster_data"
  final val SGEID        = "cluster_data.sge_id"
  final val DATECREATED  = "date_created"
  final val DATEUPDATED  = "date_updated"
  final val DATEVIEWED   = "date_viewed"
  final val DATEDELETION = "date_deleted"
  final val TOOLNAMELONG = "toolname_long"
  final val IPHASH       = "ip_hash"

  // TODO manual wiring is a code smell - no consistent key schema
  implicit val jobEncoder: Encoder[Job] = Encoder.forProduct15(
    JOBID,
    PARENTID,
    HASH,
    OWNERID,
    ISPUBLIC,
    STATUS,
    EMAILUPDATE,
    TOOL,
    WATCHLIST,
    CLUSTERDATA,
    DATECREATED,
    DATEUPDATED,
    DATEVIEWED,
    DATEDELETION,
    IPHASH
  )(
    job =>
      (job.jobID,
       job.parentID,
       job.hash,
       job.ownerID.map(_.stringify),
       job.isPublic,
       job.status,
       job.emailUpdate,
       job.tool,
       job.watchList.map(_.stringify),
       job.clusterData,
       job.dateCreated,
       job.dateUpdated,
       job.dateViewed,
       job.dateDeletion,
       job.IPHash)
  )

  // TODO Bson macros handler
  implicit object Reader extends BSONDocumentReader[Job] {
    def read(bson: BSONDocument): Job = {
      Job(
        jobID = bson.getAs[String](JOBID).getOrElse("Error loading Job Name"),
        parentID = bson.getAs[String](PARENTID),
        hash = bson.getAs[String](HASH),
        ownerID = bson.getAs[BSONObjectID](OWNERID),
        isPublic = bson.getAs[Boolean](ISPUBLIC).getOrElse(false),
        status = bson.getAs[JobState](STATUS).getOrElse(Error),
        emailUpdate = bson.getAs[Boolean](EMAILUPDATE).getOrElse(false),
        tool = bson.getAs[String](TOOL).getOrElse(""),
        watchList = bson.getAs[List[BSONObjectID]](WATCHLIST).getOrElse(List.empty),
        clusterData = bson.getAs[JobClusterData](CLUSTERDATA),
        dateCreated = bson.getAs[BSONDateTime](DATECREATED).map(dt => ZonedDateTimeHelper.getZDT(dt)),
        dateUpdated = bson.getAs[BSONDateTime](DATEUPDATED).map(dt => ZonedDateTimeHelper.getZDT(dt)),
        dateViewed = bson.getAs[BSONDateTime](DATEVIEWED).map(dt => ZonedDateTimeHelper.getZDT(dt)),
        dateDeletion = bson.getAs[BSONDateTime](DATEDELETION).map(dt => ZonedDateTimeHelper.getZDT(dt)),
        IPHash = bson.getAs[String](IPHASH)
      )
    }
  }

  implicit object Writer extends BSONDocumentWriter[Job] {
    def write(job: Job): BSONDocument = {
      BSONDocument(
        JOBID        -> job.jobID,
        PARENTID     -> job.parentID,
        HASH         -> job.hash,
        OWNERID      -> job.ownerID,
        ISPUBLIC     -> job.isPublic,
        STATUS       -> job.status,
        EMAILUPDATE  -> job.emailUpdate,
        TOOL         -> job.tool,
        WATCHLIST    -> job.watchList,
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
