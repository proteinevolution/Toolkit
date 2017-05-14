package models.database.jobs

import org.joda.time.DateTime
import play.api.libs.json._
import reactivemongo.bson.{BSONDateTime, BSONDocument, BSONDocumentReader, BSONDocumentWriter, BSONObjectID}
import reactivemongo.play.json._

/**
  * Created by snam on 23.01.17.
  */
case class JobAnnotation(mainID: BSONObjectID, // ID of the Job in the System
                         jobID: String,
                         content: String,
                         dateCreated: Option[DateTime]) // Creation time of the Job

object JobAnnotation {
  // Constants for the JSON object identifiers
  val IDDB        = "_id" //              ID in MongoDB
  val DATECREATED = "dateCreated" //              created on field
  val CONTENT     = "content"
  val JOBID       = "jobID" //              ID for the job

  //implicit val format: Format[Job] = Json.format[Job]

  implicit object JsonReader extends Reads[JobAnnotation] {
    // TODO this is unused at the moment, as there is no convertion of JSON -> Job needed.
    override def reads(json: JsValue): JsResult[JobAnnotation] = json match {
      case obj: JsObject =>
        try {
          val mainID      = (obj \ IDDB).asOpt[String]
          val jobID       = (obj \ JOBID).asOpt[String]
          val content     = (obj \ CONTENT).asOpt[String]
          val dateCreated = (obj \ DATECREATED).asOpt[String]

          JsSuccess(
            JobAnnotation(mainID = BSONObjectID.generate(),
                          jobID = "",
                          content = "",
                          dateCreated = Some(new DateTime())))
        } catch {
          case cause: Throwable => JsError(cause.getMessage)
        }
      case _ => JsError("expected.jsobject")
    }
  }

  implicit object JobAnnotationWrites extends Writes[JobAnnotation] {
    def writes(jobAnnotation: JobAnnotation): JsObject = Json.obj(
      IDDB        -> jobAnnotation.mainID,
      JOBID       -> jobAnnotation.jobID,
      CONTENT     -> jobAnnotation.content,
      DATECREATED -> BSONDateTime(jobAnnotation.dateCreated.fold(-1L)(_.getMillis))
    )
  }

  /**
    * Object containing the writer for the Class
    */
  implicit object Reader extends BSONDocumentReader[JobAnnotation] {
    def read(bson: BSONDocument): JobAnnotation = {
      JobAnnotation(
        mainID = bson.getAs[BSONObjectID](IDDB).getOrElse(BSONObjectID.generate()),
        jobID = bson.getAs[String](JOBID).getOrElse("Error loading Job Name"),
        content = bson.getAs[String](CONTENT).getOrElse(""),
        dateCreated = bson.getAs[BSONDateTime](DATECREATED).map(dt => new DateTime(dt.value))
      )
    }
  }

  /**
    * Object containing the writer for the Class
    */
  implicit object Writer extends BSONDocumentWriter[JobAnnotation] {
    def write(jobAnnotation: JobAnnotation): BSONDocument = BSONDocument(
      IDDB        -> jobAnnotation.mainID,
      JOBID       -> jobAnnotation.jobID,
      CONTENT     -> jobAnnotation.content,
      DATECREATED -> BSONDateTime(jobAnnotation.dateCreated.fold(-1L)(_.getMillis))
    )
  }
}
