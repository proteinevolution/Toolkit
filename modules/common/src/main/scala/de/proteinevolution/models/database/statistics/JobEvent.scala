package de.proteinevolution.models.database.statistics

import java.time.ZonedDateTime

import de.proteinevolution.models.database.jobs.JobState._
import de.proteinevolution.models.util.ZonedDateTimeHelper
import play.api.libs.json._
import reactivemongo.bson.{
  BSONDateTime,
  BSONDocument,
  BSONDocumentReader,
  BSONDocumentWriter
}

case class JobEvent(
    jobState: JobState,
    timestamp: Option[ZonedDateTime],
    runtime: Long = 0L
)

object JobEvent {

  val JOBSTATE  = "jobState"
  val TIMESTAMP = "timestamp"
  val RUNTIME   = "runtime"

  implicit object JsonReader extends Reads[JobEvent] {
    override def reads(json: JsValue): JsResult[JobEvent] = json match {
      case obj: JsObject =>
        try {
          val jobState  = (obj \ JOBSTATE).asOpt[JobState].getOrElse(Error)
          val timestamp = (obj \ TIMESTAMP).asOpt[ZonedDateTime]
          val runtime   = (obj \ RUNTIME).asOpt[Long].getOrElse(0L)
          JsSuccess(JobEvent(jobState, timestamp, runtime))
        } catch {
          case cause: Throwable => JsError(cause.getMessage)
        }
      case _ => JsError("expected.jsobject")
    }
  }

  implicit object JsonWriter extends Writes[JobEvent] {
    override def writes(jobEvent: JobEvent): JsObject = Json.obj(
      JOBSTATE  -> jobEvent.jobState,
      TIMESTAMP -> jobEvent.timestamp,
      RUNTIME   -> jobEvent.runtime
    )
  }

  implicit object Reader extends BSONDocumentReader[JobEvent] {
    def read(bson: BSONDocument): JobEvent = {
      JobEvent(
        bson.getAs[JobState](JOBSTATE).getOrElse(Error),
        bson
          .getAs[BSONDateTime](TIMESTAMP)
          .map(dt => ZonedDateTimeHelper.getZDT(dt)),
        bson.getAs[Long](RUNTIME).getOrElse(0L)
      )
    }
  }

  implicit object Writer extends BSONDocumentWriter[JobEvent] {
    def write(jobEvent: JobEvent): BSONDocument = BSONDocument(
      JOBSTATE -> jobEvent.jobState,
      TIMESTAMP -> BSONDateTime(
        jobEvent.timestamp.fold(-1L)(_.toInstant.toEpochMilli)
      ),
      RUNTIME -> jobEvent.runtime
    )
  }
}
