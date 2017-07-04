package models.database.jobs

import org.joda.time.DateTime
import play.api.libs.json._
import reactivemongo.bson.{BSONDateTime, BSONDocument, BSONDocumentReader, BSONDocumentWriter}

/**
  * This class holds jobID and Deletion date for
  * a job that was permanently deleted by the job
  * sweeping routine
  *
  * Created by drau on 04.07.17.
  */
case class DeletedJob(jobID: String, deletionDate: DateTime)

object DeletedJob {
  val JOBID         = "jobID"
  val DELETIONDATE  = "deletionDate"

  implicit object JsonReader extends Reads[DeletedJob] {
    override def reads(json: JsValue): JsResult[DeletedJob] = json match {
      case obj: JsObject =>
        try {
          val jobID         = (obj \ JOBID).asOpt[String].getOrElse("")
          val deletionDate  = (obj \ DELETIONDATE).asOpt[DateTime].getOrElse(new DateTime().minusYears(100))
          JsSuccess(DeletedJob(jobID, deletionDate))
        } catch {
          case cause: Throwable => JsError(cause.getMessage)
        }
      case _ => JsError("expected.jsobject")
    }
  }

  implicit object JsonWriter extends Writes[DeletedJob] {
    override def writes(deletedJob: DeletedJob): JsObject = Json.obj(
      JOBID        -> deletedJob.jobID,
      DELETIONDATE -> deletedJob.deletionDate.getMillis
    )
  }

  implicit object Reader extends BSONDocumentReader[DeletedJob] {
    def read(bson: BSONDocument): DeletedJob = {
      DeletedJob(
        bson.getAs[String](JOBID).getOrElse(""),
        bson.getAs[BSONDateTime](DELETIONDATE).map(dt => new DateTime(dt.value)).getOrElse(new DateTime().minusYears(100))
      )
    }
  }

  implicit object Writer extends BSONDocumentWriter[DeletedJob] {
    def write(deletedJob: DeletedJob): BSONDocument =
      BSONDocument(
        JOBID  -> deletedJob.jobID,
        DELETIONDATE   -> BSONDateTime(deletedJob.deletionDate.getMillis)
      )
  }
}
