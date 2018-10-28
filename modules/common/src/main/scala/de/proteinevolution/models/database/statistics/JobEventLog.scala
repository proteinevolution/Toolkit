package de.proteinevolution.models.database.statistics

import java.time.ZonedDateTime

import de.proteinevolution.models.database.jobs.JobState
import de.proteinevolution.models.database.jobs.JobState._
import io.circe.generic.auto._
import reactivemongo.bson._

case class JobEventLog(
    jobID: String,
    toolName: String,
    internalJob: Boolean = false,
    events: List[JobEvent] = List.empty[JobEvent],
    runtime: Long = 0L
) {

  def addJobStateEvent(jobState: JobState): JobEventLog = {
    val runtimeDiff: Long =
      events.head.timestamp.map(d => ZonedDateTime.now.toInstant.toEpochMilli - d.toInstant.toEpochMilli).getOrElse(0L)
    this.copy(
      events = events.::(JobEvent(jobState, Some(ZonedDateTime.now), Some(runtimeDiff))),
      runtime = runtime + runtimeDiff
    )
  }

  def isDeleted: Boolean = events.exists(_.jobState == Deleted)

  def hasFailed: Boolean = events.exists(_.jobState == Error)

  def dateCreated: ZonedDateTime =
    events.find(_.jobState == Submitted).flatMap(_.timestamp).getOrElse(ZonedDateTime.now)

  override def toString: String = {
    s"""---[JobEventLog Object]---
       |jobID: $jobID
       |tool name: $toolName
       |internalJob? ${if (internalJob) { "yes" } else { "no" }}
       |events: ${events.mkString(",")}""".stripMargin
  }

}

object JobEventLog {

  import io.circe.{ Decoder, Encoder, HCursor, Json }

  final val JOBID          = "jobID"
  final val TOOLNAME    = "tool"
  final val INTERNALJOB = "internalJob"
  final val EVENTS      = "events"
  final val RUNTIME     = "runtime"

  implicit val jobEventLogDecoder: Decoder[JobEventLog] = (c: HCursor) =>
    for {
      id          <- c.downField(JOBID).as[String]
      toolName    <- c.downField(TOOLNAME).as[String]
      internalJob <- c.downField(INTERNALJOB).as[Boolean]
      runtime     <- c.downField(RUNTIME).as[Long]
      events      <- c.downField(EVENTS).as[List[Json]]
    } yield
      new JobEventLog(
        id,
        toolName,
        internalJob,
        events.flatMap(_.hcursor.as[JobEvent].toOption),
        runtime
    )

  // TODO make fully automatically encodable by adjusting the keys in the frontend
  implicit val jobEventLogEncoder: Encoder[JobEventLog] =
    Encoder.forProduct5(JOBID, TOOLNAME, INTERNALJOB, EVENTS, RUNTIME)(
      l => (l.jobID, l.toolName, l.internalJob, l.events, l.runtime)
    )

  implicit object Reader extends BSONDocumentReader[JobEventLog] {
    def read(bson: BSONDocument): JobEventLog = {
      JobEventLog(
        jobID = bson.getAs[String](JOBID).getOrElse(""),
        toolName = bson.getAs[String](TOOLNAME).getOrElse(""),
        internalJob = bson.getAs[Boolean](INTERNALJOB).getOrElse(false),
        events = bson.getAs[List[JobEvent]](EVENTS).getOrElse(List.empty),
        runtime = bson.getAs[Long](RUNTIME).getOrElse(0L)
      )
    }
  }

  implicit object Writer extends BSONDocumentWriter[JobEventLog] {
    def write(jobEventLog: JobEventLog): BSONDocument = BSONDocument(
      JOBID        -> jobEventLog.jobID,
      TOOLNAME    -> jobEventLog.toolName,
      INTERNALJOB -> jobEventLog.internalJob,
      EVENTS      -> jobEventLog.events,
      RUNTIME     -> jobEventLog.runtime
    )
  }

}
