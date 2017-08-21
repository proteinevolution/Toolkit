package models.database.jobs

import models.database.jobs.JobDeletionFlag.JobDeletionFlag
import java.time.ZonedDateTime

import util.ZonedDateTimeHelper
import play.api.libs.json._
import reactivemongo.bson._

/**
  * Created by astephens on 23.10.16.
  */
case class JobDeletion(deletionFlag: JobDeletionFlag,
                       deletionDate: Option[ZonedDateTime],
                       fileRemovalDateO: Option[ZonedDateTime] = None) {
  // assume a deletion should be 2 Months after the deletion date unless explicitly given TODO make this configurable
  val fileRemovalDate: Option[ZonedDateTime] = fileRemovalDateO.orElse(deletionDate.map(_.plusMonths(2)))
}

object JobDeletion {

  val DELETIONFLAG     = "flag"
  val DELETIONDATE     = "date"
  val FILEDELETIONDATE = "delDate"

  implicit object JobDeletionReads extends Reads[JobDeletion] {
    // TODO this is unused at the moment, as there is no convertion of JSON needed.
    override def reads(json: JsValue): JsResult[JobDeletion] = json match {
      case obj: JsObject =>
        try {
          val deletionFlag = (obj \ DELETIONFLAG).asOpt[String]
          val deletionDate = (obj \ DELETIONDATE).asOpt[String]
          JsSuccess(JobDeletion(deletionFlag = JobDeletionFlag.Error, deletionDate = Some(new ZonedDateTime())))
        } catch {
          case cause: Throwable => JsError(cause.getMessage)
        }
      case _ => JsError("expected.jsobject")
    }
  }

  implicit object JobDeletionWrites extends Writes[JobDeletion] {
    def writes(jobDeletion: JobDeletion): JsObject = Json.obj(
      DELETIONFLAG     -> jobDeletion.deletionFlag,
      DELETIONDATE     -> jobDeletion.deletionDate,
      FILEDELETIONDATE -> jobDeletion.fileRemovalDate
    )
  }

  implicit object Reader extends BSONDocumentReader[JobDeletion] {
    def read(bson: BSONDocument): JobDeletion = {
      JobDeletion(
        deletionFlag = bson.getAs[JobDeletionFlag](DELETIONFLAG).getOrElse(JobDeletionFlag.Error),
        deletionDate = bson
          .getAs[BSONDateTime](DELETIONDATE).map(dt => ZonedDateTimeHelper.getZDT(dt)),
        fileRemovalDateO = bson
          .getAs[BSONDateTime](FILEDELETIONDATE).map(dt => ZonedDateTimeHelper.getZDT(dt))
      )
    }
  }

  implicit object Writer extends BSONDocumentWriter[JobDeletion] {
    def write(jobDeletion: JobDeletion): BSONDocument = BSONDocument(
      DELETIONFLAG     -> jobDeletion.deletionFlag,
      DELETIONDATE     -> BSONDateTime(jobDeletion.deletionDate.fold(-1L)(_.toInstant.toEpochMilli)),
      FILEDELETIONDATE -> BSONDateTime(jobDeletion.fileRemovalDate.fold(-1L)(_.toInstant.toEpochMilli))
    )
  }
}
