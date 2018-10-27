package de.proteinevolution.models.database.statistics

import java.time.ZonedDateTime

import de.proteinevolution.models.database.jobs.JobState
import de.proteinevolution.models.database.jobs.JobState._
import de.proteinevolution.models.util.ZonedDateTimeHelper
import io.circe.generic.JsonCodec
import reactivemongo.bson.{ BSONDateTime, BSONDocument, BSONDocumentReader, BSONDocumentWriter }

@JsonCodec case class JobEvent(
    jobState: JobState,
    timestamp: Option[ZonedDateTime],
    runtime: Option[Long] = Some(0L)
)

object JobEvent {

  // TODO refactor mongo document keys
  final val JOBSTATE  = "jobState"
  final val TIMESTAMP = "timestamp"
  final val RUNTIME   = "runtime"

  implicit object Reader extends BSONDocumentReader[JobEvent] {
    def read(bson: BSONDocument): JobEvent = {
      JobEvent(
        bson.getAs[JobState](JOBSTATE).getOrElse(Error),
        bson.getAs[BSONDateTime](TIMESTAMP).map(dt => ZonedDateTimeHelper.getZDT(dt)),
        bson.getAs[Long](RUNTIME)
      )
    }
  }

  implicit object Writer extends BSONDocumentWriter[JobEvent] {
    def write(jobEvent: JobEvent): BSONDocument = BSONDocument(
      JOBSTATE  -> jobEvent.jobState,
      TIMESTAMP -> BSONDateTime(jobEvent.timestamp.fold(-1L)(_.toInstant.toEpochMilli)),
      RUNTIME   -> jobEvent.runtime
    )
  }

}
