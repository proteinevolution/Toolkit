/*
 * Copyright 2018 Dept. of Protein Evolution, Max Planck Institute for Biology
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

import de.proteinevolution.common.models.util.ZonedDateTimeHelper
import io.circe.generic.JsonCodec
import reactivemongo.api.bson._

@JsonCodec case class JobClusterData(
    sgeID: String,
    memory: Option[Int],
    threads: Option[Int],
    hardruntime: Option[Int],
    dateStarted: Option[ZonedDateTime] = Some(ZonedDateTime.now),
    dateFinished: Option[ZonedDateTime] = None
) {

  def runtime: Long = {
    val now = ZonedDateTime.now
    dateFinished.getOrElse(now).toInstant.toEpochMilli - dateStarted.getOrElse(now).toInstant.toEpochMilli
  }

}

object JobClusterData {

  final val SGE_ID        = "sgeID"
  final val MEMORY        = "memory"
  final val THREADS       = "threads"
  final val HARDRUNTIME   = "hardruntime"
  final val DATE_STARTED  = "dateStarted"
  final val DATE_FINISHED = "dateFinished"

  implicit def reader: BSONDocumentReader[JobClusterData] =
    BSONDocumentReader[JobClusterData] { bson =>
      JobClusterData(
        sgeID = bson.getAsTry[String](SGE_ID).getOrElse(""),
        memory = bson.getAsOpt[Int](MEMORY),
        threads = bson.getAsOpt[Int](THREADS),
        hardruntime = bson.getAsOpt[Int](HARDRUNTIME),
        dateStarted = bson.getAsOpt[BSONDateTime](DATE_STARTED).map(ZonedDateTimeHelper.getZDT),
        dateFinished = bson.getAsOpt[BSONDateTime](DATE_STARTED).map(ZonedDateTimeHelper.getZDT)
      )
    }

  implicit def writer: BSONDocumentWriter[JobClusterData] =
    BSONDocumentWriter[JobClusterData] { clusterData =>
      BSONDocument(
        SGE_ID        -> clusterData.sgeID,
        MEMORY        -> clusterData.memory,
        THREADS       -> clusterData.threads,
        HARDRUNTIME   -> clusterData.hardruntime,
        DATE_STARTED  -> BSONDateTime(clusterData.dateStarted.fold(-1L)(_.toInstant.toEpochMilli)),
        DATE_FINISHED -> BSONDateTime(clusterData.dateStarted.fold(-1L)(_.toInstant.toEpochMilli))
      )
    }
}
