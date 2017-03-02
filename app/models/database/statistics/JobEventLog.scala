package models.database.statistics

import models.database.jobs.JobState
import org.joda.time.DateTime
import play.api.libs.json._
import reactivemongo.bson._

/**
  * Created by astephens on 19.02.17.
  */
case class JobEventLog(mainID      : BSONObjectID,                // ID of the Job in the System
                       toolName    : String,
                       internalJob : Boolean        = false,
                       events      : List[JobEvent],
                       runtime     : Long           = 0L) {
  def addJobStateEvent(jobState : JobState) : JobEventLog = {
    val runtimeDiff : Long = events.head.timestamp.map(d => DateTime.now.getMillis - d.getMillis).getOrElse(0L)
    this.copy(events  = events.::(JobEvent(jobState, Some(DateTime.now), runtimeDiff)),
              runtime = runtime + runtimeDiff)
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
      case obj: JsObject => try {
        val mainID  = BSONObjectID.parse((obj \ ID).as[String]).getOrElse(BSONObjectID.generate())
        val events  = (obj \ EVENTS).asOpt[List[JobEvent]].getOrElse(List.empty)
        val runtime = (obj \ RUNTIME).as[Long]
        val toolName = (obj \ TOOLNAME).as[String]
        val internalJob = (obj \ INTERNALJOB).as[Boolean]
        JsSuccess(JobEventLog(mainID, toolName, internalJob, events, runtime))
      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }
      case _ => JsError("expected.jsobject")
    }
  }

  implicit object JsonWriter extends Writes[JobEventLog] {
    override def writes(jobEventLog : JobEventLog) : JsObject = Json.obj(
      ID          -> jobEventLog.mainID.stringify,
      TOOLNAME    -> jobEventLog.toolName,
      INTERNALJOB -> jobEventLog.internalJob,
      EVENTS      -> jobEventLog.events,
      RUNTIME     -> jobEventLog.runtime
    )
  }

  implicit object Reader extends BSONDocumentReader[JobEventLog] {
    def read(bson : BSONDocument) : JobEventLog = {
      JobEventLog(
        mainID      = bson.getAs[BSONObjectID](IDDB).getOrElse(BSONObjectID.generate()),
        toolName    = bson.getAs[String](TOOLNAME).getOrElse(""),
        internalJob = bson.getAs[Boolean](INTERNALJOB).getOrElse(false),
        events      = bson.getAs[List[JobEvent]](EVENTS).getOrElse(List.empty),
        runtime     = bson.getAs[Long](RUNTIME).getOrElse(0L)
      )
    }
  }

  implicit object Writer extends BSONDocumentWriter[JobEventLog] {
    def write(jobEventLog : JobEventLog) : BSONDocument = BSONDocument(
      IDDB        -> jobEventLog.mainID,
      TOOLNAME    -> jobEventLog.toolName,
      INTERNALJOB -> jobEventLog.internalJob,
      EVENTS      -> jobEventLog.events,
      RUNTIME     -> jobEventLog.runtime
    )
  }
}