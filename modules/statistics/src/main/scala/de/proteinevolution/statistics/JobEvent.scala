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

package de.proteinevolution.statistics

import java.time.ZonedDateTime

import de.proteinevolution.common.models.database.jobs.JobState
import de.proteinevolution.common.models.database.jobs.JobState._
import de.proteinevolution.common.models.util.ZonedDateTimeHelper
import io.circe.generic.semiauto.{ deriveDecoder, deriveEncoder }
import io.circe.{ Decoder, Encoder }
import reactivemongo.api.bson.{ BSONDateTime, BSONDocument, BSONDocumentReader, BSONDocumentWriter }

final case class JobEvent(
    jobState: JobState,
    timestamp: Option[ZonedDateTime],
    runtime: Option[Long] = Some(0L)
)

object JobEvent {

  // TODO refactor mongo document keys
  final val JOBSTATE  = "jobState"
  final val TIMESTAMP = "timestamp"
  final val RUNTIME   = "runtime"

  implicit val jobEventDecoder: Decoder[JobEvent] = deriveDecoder

  implicit val jobEventEncoder: Encoder[JobEvent] = deriveEncoder

  implicit def reader: BSONDocumentReader[JobEvent] =
    BSONDocumentReader[JobEvent] { bson =>
      JobEvent(
        bson.getAsTry[JobState](JOBSTATE).getOrElse(Error),
        bson.getAsOpt[BSONDateTime](TIMESTAMP).map(ZonedDateTimeHelper.getZDT),
        bson.getAsOpt[Long](RUNTIME)
      )
    }

  implicit def writer: BSONDocumentWriter[JobEvent] =
    BSONDocumentWriter[JobEvent] { jobEvent =>
      BSONDocument(
        JOBSTATE  -> jobEvent.jobState,
        TIMESTAMP -> BSONDateTime(jobEvent.timestamp.fold(-1L)(_.toInstant.toEpochMilli)),
        RUNTIME   -> jobEvent.runtime
      )
    }
}
