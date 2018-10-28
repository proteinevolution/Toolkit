package de.proteinevolution.models.database.jobs

import java.time.ZonedDateTime

import de.proteinevolution.models.util.ZonedDateTimeHelper
import io.circe.generic.JsonCodec
import reactivemongo.bson._

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

  final val SGEID        = "sge_id"
  final val MEMORY       = "memory"
  final val THREADS      = "threads"
  final val HARDRUNTIME  = "hardruntime"
  final val DATESTARTED  = "started"
  final val DATEFINISHED = "finished"

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
