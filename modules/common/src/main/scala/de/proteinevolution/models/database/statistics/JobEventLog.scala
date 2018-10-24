package de.proteinevolution.models.database.statistics

import java.time.ZonedDateTime
import de.proteinevolution.models.database.jobs.JobState
import de.proteinevolution.models.database.jobs.JobState._
import reactivemongo.bson._
import io.circe.generic.auto._

case class JobEventLog(
    mainID: BSONObjectID = BSONObjectID.generate(), // ID of the Job in the System
    toolName: String,
    internalJob: Boolean = false,
    events: List[JobEvent] = List.empty[JobEvent],
    runtime: Long = 0L
) {

  def addJobStateEvent(jobState: JobState): JobEventLog = {
    val runtimeDiff: Long =
      events.head.timestamp.map(d => ZonedDateTime.now.toInstant.toEpochMilli - d.toInstant.toEpochMilli).getOrElse(0L)
    this.copy(events = events.::(JobEvent(jobState, Some(ZonedDateTime.now), Some(runtimeDiff))),
              runtime = runtime + runtimeDiff)
  }

  def isDeleted: Boolean = {
    events.exists(_.jobState == Deleted)
  }

  def hasFailed: Boolean = {
    events.exists(_.jobState == Error)
  }

  def dateCreated: ZonedDateTime = {
    events.find(_.jobState == Submitted).flatMap(_.timestamp).getOrElse(ZonedDateTime.now)
  }

  override def toString: String = {
    s"""---[JobEventLog Object]---
       |mainID: ${mainID.stringify}
       |tool name: $toolName
       |internalJob? ${if (internalJob) { "yes" } else { "no" }}
       |events: ${events.mkString(",")}""".stripMargin
  }
}

object JobEventLog {

  import io.circe.{ Decoder, HCursor, Json }

  final val ID          = "mainID"
  final val IDDB        = "_id"
  final val TOOLNAME    = "tool"
  final val INTERNALJOB = "internalJob"
  final val EVENTS      = "events"
  final val RUNTIME     = "runtime"

  implicit val jobEventLogDecoder: Decoder[JobEventLog] = (c: HCursor) =>
    for {
      id          <- c.downField(ID).as[String]
      toolName    <- c.downField(TOOLNAME).as[String]
      internalJob <- c.downField(INTERNALJOB).as[Boolean]
      runtime     <- c.downField(RUNTIME).as[Long]
      events      <- c.downField(EVENTS).as[List[Json]]
    } yield
      new JobEventLog(
        BSONObjectID.parse(id).getOrElse(BSONObjectID.generate()),
        toolName,
        internalJob,
        events.flatMap(_.hcursor.as[JobEvent].toOption),
        runtime
    )

  /*
  implicit object JsonWriter extends Writes[JobEventLog] {
    override def writes(jobEventLog: JobEventLog): JsObject = Json.obj(
      ID          -> jobEventLog.mainID.stringify,
      TOOLNAME    -> jobEventLog.toolName,
      INTERNALJOB -> jobEventLog.internalJob,
      EVENTS      -> jobEventLog.events,
      RUNTIME     -> jobEventLog.runtime
    )
  } */

  implicit object Reader extends BSONDocumentReader[JobEventLog] {
    def read(bson: BSONDocument): JobEventLog = {
      JobEventLog(
        mainID = bson.getAs[BSONObjectID](IDDB).getOrElse(BSONObjectID.generate()),
        toolName = bson.getAs[String](TOOLNAME).getOrElse(""),
        internalJob = bson.getAs[Boolean](INTERNALJOB).getOrElse(false),
        events = bson.getAs[List[JobEvent]](EVENTS).getOrElse(List.empty),
        runtime = bson.getAs[Long](RUNTIME).getOrElse(0L)
      )
    }
  }

  implicit object Writer extends BSONDocumentWriter[JobEventLog] {
    def write(jobEventLog: JobEventLog): BSONDocument = BSONDocument(
      IDDB        -> jobEventLog.mainID,
      TOOLNAME    -> jobEventLog.toolName,
      INTERNALJOB -> jobEventLog.internalJob,
      EVENTS      -> jobEventLog.events,
      RUNTIME     -> jobEventLog.runtime
    )
  }

  /**
   * Returns the jobEvent list partitioned in to a map of tools
   * @param jobEventList
   * @return
   */
  def toSortedMap(jobEventList: List[JobEventLog]): Map[String, List[JobEventLog]] = {
    var jobEventMap = Map.empty[String, List[JobEventLog]]
    jobEventList.foreach { jobEvent =>
      jobEventMap = jobEventMap.updated(jobEvent.toolName,
                                        jobEventMap.getOrElse(jobEvent.toolName, List.empty[JobEventLog]).::(jobEvent))
    }

    jobEventMap
  }
}
