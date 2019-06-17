/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.proteinevolution.jobs.models

import java.time.ZonedDateTime

import de.proteinevolution.common.models.database.jobs.JobState._
import de.proteinevolution.common.models.database.jobs.JobState
import de.proteinevolution.common.models.util.ZonedDateTimeHelper
import de.proteinevolution.tools.ToolConfig
import io.circe.JsonObject
import io.circe.syntax._
import play.api.Configuration
import reactivemongo.bson._

// TODO remove default values
case class Job(
    jobID: String,
    parentID: Option[String] = None,
    hash: Option[String] = None,
    ownerID: Option[String] = None,
    isPublic: Boolean = false,
    status: JobState = Submitted,
    emailUpdate: Boolean = false,
    tool: String,
    watchList: List[String] = List.empty,
    clusterData: Option[JobClusterData] = None,
    dateCreated: Option[ZonedDateTime] = Some(ZonedDateTime.now),
    dateUpdated: Option[ZonedDateTime] = Some(ZonedDateTime.now),
    dateViewed: Option[ZonedDateTime] = Some(ZonedDateTime.now),
    dateDeletion: Option[ZonedDateTime] = None,
    IPHash: Option[String]
) {

  def cleaned(toolConfig: ToolConfig)(implicit config: Configuration): JsonObject = {
    JsonObject(
      "jobID"        -> jobID.asJson,
      "status"       -> status.asJson,
      "dateCreated"  -> dateCreated.map(_.toInstant.toEpochMilli).asJson,
      "tool"         -> tool.asJson,
      "code"         -> toolConfig.values(tool).code.asJson,
      "toolnameLong" -> config.get[String](s"Tools.$tool.longname").asJson
    )
  }

  def jobManagerJob()(implicit config: Configuration): JsonObject = {
    JsonObject(
      "jobID"        -> jobID.asJson,
      "status"       -> status.asJson,
      "tool"         -> tool.asJson,
      "dateCreated"  -> dateCreated.map(_.toInstant.toEpochMilli).asJson,
      "dateUpdated"  -> dateUpdated.map(_.toInstant.toEpochMilli).asJson,
      "dateViewed"   -> dateViewed.map(_.toInstant.toEpochMilli).asJson,
      "toolnameLong" -> config.get[String](s"Tools.$tool.longname").asJson
    )
  }

  override def toString: String = {
    s"""--[Job Object]--
        |jobID: ${this.jobID}
        |parentID: ${this.parentID}
        |tool: ${this.tool}
        |state: ${this.status}
        |ownerID: ${this.ownerID.getOrElse("no owner")}
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
  final val SGEID        = s"$CLUSTERDATA.${JobClusterData.SGEID}"
  final val DATECREATED  = "date_created"
  final val DATEUPDATED  = "date_updated"
  final val DATEVIEWED   = "date_viewed"
  final val DATEDELETION = "date_deleted"
  final val TOOLNAMELONG = "toolname_long"
  final val IPHASH       = "ip_hash"

  // TODO Bson macros handler
  implicit object Reader extends BSONDocumentReader[Job] {
    def read(bson: BSONDocument): Job = {
      Job(
        jobID = bson.getAs[String](JOBID).getOrElse("Error loading Job Name"),
        parentID = bson.getAs[String](PARENTID),
        hash = bson.getAs[String](HASH),
        ownerID = bson.getAs[String](OWNERID),
        isPublic = bson.getAs[Boolean](ISPUBLIC).getOrElse(false),
        status = bson.getAs[JobState](STATUS).getOrElse(Error),
        emailUpdate = bson.getAs[Boolean](EMAILUPDATE).getOrElse(false),
        tool = bson.getAs[String](TOOL).getOrElse(""),
        watchList = bson.getAs[List[String]](WATCHLIST).getOrElse(List.empty),
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
