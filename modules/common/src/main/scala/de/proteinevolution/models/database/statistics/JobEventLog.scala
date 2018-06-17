package de.proteinevolution.models.database.statistics

import java.time.ZonedDateTime
import de.proteinevolution.models.database.jobs.JobState._
import play.api.libs.json._
import reactivemongo.bson._

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
    this.copy(events = events.::(JobEvent(jobState, Some(ZonedDateTime.now), runtimeDiff)),
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

  val ID          = "mainID"
  val IDDB        = "_id"
  val TOOLNAME    = "tool"
  val INTERNALJOB = "internalJob"
  val EVENTS      = "events"
  val RUNTIME     = "runtime"

  implicit object JsonReader extends Reads[JobEventLog] {
    override def reads(json: JsValue): JsResult[JobEventLog] = json match {
      case obj: JsObject =>
        try {
          val mainID      = BSONObjectID.parse((obj \ ID).as[String]).getOrElse(BSONObjectID.generate())
          val events      = (obj \ EVENTS).asOpt[List[JobEvent]].getOrElse(List.empty)
          val runtime     = (obj \ RUNTIME).as[Long]
          val toolName    = (obj \ TOOLNAME).as[String]
          val internalJob = (obj \ INTERNALJOB).as[Boolean]
          JsSuccess(JobEventLog(mainID, toolName, internalJob, events, runtime))
        } catch {
          case cause: Throwable => JsError(cause.getMessage)
        }
      case _ => JsError("expected.jsobject")
    }
  }

  implicit object JsonWriter extends Writes[JobEventLog] {
    override def writes(jobEventLog: JobEventLog): JsObject = Json.obj(
      ID          -> jobEventLog.mainID.stringify,
      TOOLNAME    -> jobEventLog.toolName,
      INTERNALJOB -> jobEventLog.internalJob,
      EVENTS      -> jobEventLog.events,
      RUNTIME     -> jobEventLog.runtime
    )
  }

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
