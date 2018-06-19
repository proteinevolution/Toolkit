package de.proteinevolution.models.database.jobs

import java.time.ZonedDateTime

import de.proteinevolution.models.util.ZonedDateTimeHelper
import play.api.libs.json._
import reactivemongo.bson._

case class JobClusterData(
    sgeID: String, // sun grid engine job id
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
  val SGEID        = "sgeid"
  val MEMORY       = "memory"
  val THREADS      = "threads"
  val HARDRUNTIME  = "hardruntime"
  val DATESTARTED  = "started"
  val DATEFINISHED = "finished"

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
        memory = bson.getAs[Int](MEMORY),
        threads = bson.getAs[Int](THREADS),
        hardruntime = bson.getAs[Int](HARDRUNTIME),
        dateStarted = bson.getAs[BSONDateTime](DATESTARTED).map(dt => ZonedDateTimeHelper.getZDT(dt)),
        dateFinished = bson.getAs[BSONDateTime](DATESTARTED).map(dt => ZonedDateTimeHelper.getZDT(dt))
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
