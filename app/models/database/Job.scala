package models.database

import org.joda.time.DateTime
import reactivemongo.bson.{

BSONDateTime, BSONDocument, BSONObjectID

}

/** ?
  *
  * @param main_id
  * @param job_type
  * @param parent_id
  * @param job_id
  * @param user_id
  * @param status
  * @param tool
  * @param stat_id
  * @param creationDate
  * @param updateDate
  * @param viewDate
  *
  * Maps MySQL schema with some fields renamed (e.g. type is a reserved word in Scala)
  * +------------+------------------+------+-----+-------------------+-----------------------------+
    | Field      | Type             | Null | Key | Default           | Extra                       |
    +------------+------------------+------+-----+-------------------+-----------------------------+
    | main_id    | int(10) unsigned | NO   | PRI | NULL              | auto_increment              |
    | type       | varchar(50)      | YES  |     | NULL              |                             |
    | parent_id  | int(11)          | YES  |     | NULL              |                             |
    | job_id     | varchar(100)     | YES  |     | NULL              |                             |
    | user_id    | int(11)          | YES  |     | NULL              |                             |
    | status     | char(1)          | YES  |     | NULL              |                             |
    | tool       | varchar(100)     | YES  |     | NULL              |                             |
    | stat_id    | int(11)          | YES  |     | NULL              |                             |
    | created_on | timestamp        | NO   |     | CURRENT_TIMESTAMP |                             |
    | updated_on | timestamp        | NO   |     | CURRENT_TIMESTAMP | on update CURRENT_TIMESTAMP |
    | viewed_on  | timestamp        | NO   |     | CURRENT_TIMESTAMP | on update CURRENT_TIMESTAMP |
    +------------+------------------+------+-----+-------------------+-----------------------------+
  *
  *
  */


case class Job(
                    main_id: Option[Int],
                    job_type: String,
                    parent_id: Int,
                    job_id: String,
                    user_id: Int,
                    status: String,
                    tool: String,
                    stat_id: Int,
                    creationDate: Option[DateTime],
                    updateDate: Option[DateTime],
                    viewDate: Option[DateTime])


object Job {
  import play.api.libs.json._


  implicit object JobWrites extends OWrites[Job] {
    def writes(job: Job): JsObject = Json.obj(
      "main_id" -> job.main_id,
      "job_type" -> job.job_type,
      "parent_id" -> job.parent_id,
      "job_id" -> job.job_id,
      "user_id" -> job.user_id,
      "status" -> job.status,
      "tool" -> job.tool,
      "stat_id" -> job.stat_id,
      "creationDate" -> job.creationDate.fold(-1L)(_.getMillis),
      "updateDate" -> job.updateDate.fold(-1L)(_.getMillis),
      "viewDate" -> job.viewDate.fold(-1L)(_.getMillis))
  }

  implicit object JobReads extends Reads[Job] {
    def reads(json: JsValue): JsResult[Job] = json match {
      case obj: JsObject => try {
        val main_id = (obj \ "main_id").asOpt[Int]
        val job_type = (obj \ "job_type").as[String]
        val parent_id = (obj \ "user_id").as[Int]
        val job_id = (obj \ "job_id").as[String]
        val user_id = (obj \ "user_id").as[Int]
        val status = (obj \ "status").as[String]
        val tool = (obj \ "tool").as[String]
        val stat_id = (obj \ "stat_id").as[Int]
        val creationDate = (obj \ "creationDate").asOpt[Long]
        val updateDate = (obj \ "updateDate").asOpt[Long]
        val viewDate = (obj \ "viewDate").asOpt[Long]

        JsSuccess(Job(main_id, job_type, parent_id, job_id,
          user_id, status, tool, stat_id,
          creationDate.map(new DateTime(_)),
          updateDate.map(new DateTime(_)),
          viewDate.map(new DateTime(_))))

      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }

      case _ => JsError("expected.jsobject")
    }
  }
}
