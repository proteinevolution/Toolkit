package models.database

import models.database.JobDeletionFlag.JobDeletionFlag
import org.joda.time.DateTime
import play.api.libs.json._
import reactivemongo.bson.{BSONDocumentWriter, BSONDateTime, BSONDocument, BSONDocumentReader}

/**
  * Created by astephens on 23.10.16.
  */
case class JobDeletion(deletionFlag : JobDeletionFlag, deletionDate : Option[DateTime])
object JobDeletion {

  val DELETIONFLAG = "flag"
  val DELETIONDATE = "date"

  implicit object JobDeletionReads extends Reads[JobDeletion] {
    // TODO this is unused at the moment, as there is no convertion of JSON -> Job needed.
    override def reads(json: JsValue): JsResult[JobDeletion] = json match {
      case obj: JsObject => try {
        val deletionFlag = (obj \ DELETIONFLAG).asOpt[String]
        val deletionDate = (obj \ DELETIONDATE).asOpt[String]
        JsSuccess(JobDeletion(
          deletionFlag = JobDeletionFlag.Error,
          deletionDate = Some(new DateTime())))
      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }
      case _ => JsError("expected.jsobject")
    }
  }

  implicit object JobDeletionWrites extends Writes[JobDeletion] {
    def writes (jobDeletion : JobDeletion) : JsObject = Json.obj(
      DELETIONFLAG -> jobDeletion.deletionFlag,
      DELETIONDATE -> jobDeletion.deletionFlag
    )
  }

  implicit object Reader extends BSONDocumentReader[JobDeletion] {
    def read(bson: BSONDocument): JobDeletion = {
      JobDeletion(
        deletionFlag = bson.getAs[JobDeletionFlag](DELETIONFLAG).getOrElse(JobDeletionFlag.Error),
        deletionDate = bson.getAs[BSONDateTime](DELETIONDATE).map(dt => new DateTime(dt.value))
      )
    }
  }

  implicit object Writer extends BSONDocumentWriter[JobDeletion] {
    def write(jobDeletion : JobDeletion) : BSONDocument = BSONDocument(
      DELETIONFLAG -> jobDeletion.deletionFlag,
      DELETIONDATE -> BSONDateTime(jobDeletion.deletionDate.fold(-1L)(_.getMillis))
    )
  }
}
