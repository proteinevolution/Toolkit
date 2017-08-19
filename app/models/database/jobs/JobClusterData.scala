package models.database.jobs

import java.time.{ZonedDateTime, Instant, ZoneId}
import play.api.libs.json._
import reactivemongo.bson._

/**
  * Created by astephens on 27.01.17.
  */
case class JobClusterData(sgeID: String, // sun grid engine job id
                          memory: Option[String],
                          threads: Option[Int],
                          hardruntime: Option[String],
                          dateStarted: Option[ZonedDateTime] = Some(ZonedDateTime.now),
                          dateFinished: Option[ZonedDateTime] = None) {
  def runtime: Long = {
    val now = ZonedDateTime.now
    dateFinished.getOrElse(now).toInstant.toEpochMilli - dateStarted.getOrElse(now).toInstant.toEpochMilli
  }
}

object JobClusterData {
  val SGEID        = "sgeid"
  val MEMORY       = "memory"
  val THREADS      = "threads"
  val HARDRUNTIME  = "hardruntime"
  val DATESTARTED  = "started"
  val DATEFINISHED = "finished"

  implicit object JsonReader extends Reads[JobClusterData] {
    // TODO this is unused at the moment, as there is no convertion of JSON -> Job needed.
    override def reads(json: JsValue): JsResult[JobClusterData] = json match {
      case obj: JsObject =>
        try {
          val sgeID        = (obj \ SGEID).asOpt[String]
          val memory       = (obj \ MEMORY).asOpt[Int]
          val hardruntime  = (obj \ HARDRUNTIME).asOpt[String]
          val threads      = (obj \ THREADS).asOpt[Int]
          val dateStarted  = (obj \ DATESTARTED).asOpt[String]
          val dateFinished = (obj \ DATEFINISHED).asOpt[String]
          JsSuccess(
            JobClusterData(sgeID = "",
                           memory = Some(""),
                           threads = Some(0),
                           hardruntime = Some(""),
                           dateStarted = Some(ZonedDateTime.now),
                           dateFinished = Some(ZonedDateTime.now))
          )
        } catch {
          case cause: Throwable => JsError(cause.getMessage)
        }
      case _ => JsError("expected.jsobject")
    }
  }

  implicit object JobWrites extends Writes[JobClusterData] {
    def writes(job: JobClusterData): JsObject = Json.obj(
      SGEID        -> job.sgeID,
      MEMORY       -> job.memory,
      THREADS      -> job.threads,
      HARDRUNTIME  -> job.hardruntime,
      DATESTARTED  -> job.dateStarted,
      DATEFINISHED -> job.dateFinished
    )
  }

  /**
    * Object containing the reader for the Class
    */
  implicit object Reader extends BSONDocumentReader[JobClusterData] {
    def read(bson: BSONDocument): JobClusterData = {
      JobClusterData(
        sgeID = bson.getAs[String](SGEID).getOrElse(""),
        memory = bson.getAs[String](MEMORY),
        threads = bson.getAs[Int](THREADS),
        hardruntime = bson.getAs[String](HARDRUNTIME),
        dateStarted = bson.getAs[BSONDateTime](DATESTARTED).map(dt => ZonedDateTime.ofInstant(Instant.ofEpochMilli(dt.value), ZoneId.systemDefault())),
        dateFinished = bson.getAs[BSONDateTime](DATESTARTED).map(dt => ZonedDateTime.ofInstant(Instant.ofEpochMilli(dt.value), ZoneId.systemDefault()))
      )
    }
  }

  /**
    * Object containing the writer for the Class
    */
  implicit object Writer extends BSONDocumentWriter[JobClusterData] {
    def write(clusterData: JobClusterData): BSONDocument = BSONDocument(
      SGEID        -> clusterData.sgeID,
      MEMORY       -> clusterData.memory,
      THREADS      -> clusterData.threads,
      HARDRUNTIME  -> clusterData.hardruntime,
      DATESTARTED  -> BSONDateTime(clusterData.dateStarted.fold(-1L)(_.toInstant.toEpochMilli)),
      DATEFINISHED -> BSONDateTime(clusterData.dateStarted.fold(-1L)(_.toInstant.toEpochMilli))
    )
  }
}
