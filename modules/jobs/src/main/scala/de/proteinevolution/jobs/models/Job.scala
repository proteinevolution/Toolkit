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
      "parentID"     -> parentID.asJson,
      "status"       -> status.asJson,
      "code"         -> toolConfig.values(tool).code.asJson,
      "tool"         -> tool.asJson,
      "toolnameLong" -> config.get[String](s"Tools.$tool.longname").asJson,
      "dateCreated"  -> dateCreated.map(_.toInstant.toEpochMilli).asJson,
      "dateUpdated"  -> dateUpdated.map(_.toInstant.toEpochMilli).asJson,
      "dateViewed"   -> dateViewed.map(_.toInstant.toEpochMilli).asJson
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

  final val ID            = "id"
  final val PARENT_ID     = "parentID"
  final val HASH          = "hash"
  final val OWNER_ID      = "ownerID"
  final val IS_PUBLIC     = "isPublic"
  final val STATUS        = "status"
  final val EMAIL_UPDATE  = "emailUpdate"
  final val TOOL          = "tool"
  final val CODE          = "code"
  final val LABEL         = "label"
  final val WATCH_LIST    = "watchList"
  final val CLUSTER_DATA  = "clusterData"
  final val SGE_ID        = s"$CLUSTER_DATA.${JobClusterData.SGE_ID}"
  final val DATE_CREATED  = "dateCreated"
  final val DATE_UPDATED  = "dateUpdated"
  final val DATE_VIEWED   = "dateViewed"
  final val DATE_DELETED  = "dateDeleted"
  final val TOOLNAME_LONG = "toolnameLong"
  final val IP_HASH       = "ipHash"

  // TODO Bson macros handler
  implicit object Reader extends BSONDocumentReader[Job] {
    def read(bson: BSONDocument): Job = {
      Job(
        jobID = bson.getAs[String](ID).getOrElse("Error loading Job Name"),
        parentID = bson.getAs[String](PARENT_ID),
        hash = bson.getAs[String](HASH),
        ownerID = bson.getAs[String](OWNER_ID),
        isPublic = bson.getAs[Boolean](IS_PUBLIC).getOrElse(false),
        status = bson.getAs[JobState](STATUS).getOrElse(Error),
        emailUpdate = bson.getAs[Boolean](EMAIL_UPDATE).getOrElse(false),
        tool = bson.getAs[String](TOOL).getOrElse(""),
        watchList = bson.getAs[List[String]](WATCH_LIST).getOrElse(List.empty),
        clusterData = bson.getAs[JobClusterData](CLUSTER_DATA),
        dateCreated = bson.getAs[BSONDateTime](DATE_CREATED).map(dt => ZonedDateTimeHelper.getZDT(dt)),
        dateUpdated = bson.getAs[BSONDateTime](DATE_UPDATED).map(dt => ZonedDateTimeHelper.getZDT(dt)),
        dateViewed = bson.getAs[BSONDateTime](DATE_VIEWED).map(dt => ZonedDateTimeHelper.getZDT(dt)),
        dateDeletion = bson.getAs[BSONDateTime](DATE_DELETED).map(dt => ZonedDateTimeHelper.getZDT(dt)),
        IPHash = bson.getAs[String](IP_HASH)
      )
    }
  }

  implicit object Writer extends BSONDocumentWriter[Job] {
    def write(job: Job): BSONDocument = {
      BSONDocument(
        ID           -> job.jobID,
        PARENT_ID    -> job.parentID,
        HASH         -> job.hash,
        OWNER_ID     -> job.ownerID,
        IS_PUBLIC    -> job.isPublic,
        STATUS       -> job.status,
        EMAIL_UPDATE -> job.emailUpdate,
        TOOL         -> job.tool,
        WATCH_LIST   -> job.watchList,
        CLUSTER_DATA -> job.clusterData,
        DATE_CREATED -> BSONDateTime(job.dateCreated.fold(-1L)(_.toInstant.toEpochMilli)),
        DATE_UPDATED -> BSONDateTime(job.dateUpdated.fold(-1L)(_.toInstant.toEpochMilli)),
        DATE_VIEWED  -> BSONDateTime(job.dateViewed.fold(-1L)(_.toInstant.toEpochMilli)),
        DATE_DELETED -> job.dateDeletion.map(d => BSONDateTime(d.toInstant.toEpochMilli)),
        IP_HASH      -> job.IPHash
      )
    }
  }

}
