package models.database.jobs

import play.api.libs.json._
import reactivemongo.bson.{BSONInteger, BSONReader, BSONWriter}

/**
  * Created by lukas on 1/20/16.
  * Object which describes the job's status
  */



object JobDeletionFlag {

  sealed trait JobDeletionFlag

  case object Error         extends JobDeletionFlag
  case object OwnerRequest  extends JobDeletionFlag
  case object PublicRequest extends JobDeletionFlag
  case object Moderation    extends JobDeletionFlag
  case object Automated     extends JobDeletionFlag

  implicit object JobStateReads extends Reads[JobDeletionFlag] {
    override def reads(json: JsValue) : JsResult[JobDeletionFlag] = json match {
      case obj: JsObject => try {
        JsSuccess((obj \ "status").as[Int] match {
          case 0 => Error
          case 1 => OwnerRequest
          case 2 => PublicRequest
          case 3 => Moderation
          case 4 => Automated
          case _ => Error
        })
      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }
      case _ => JsError("expected.jsobject")
    }
  }

  implicit object JobStateWrites extends Writes[JobDeletionFlag] {
    def writes(jobState: JobDeletionFlag) : JsNumber = jobState match {
      case Error         => JsNumber(0)
      case OwnerRequest  => JsNumber(1)
      case PublicRequest => JsNumber(2)
      case Moderation    => JsNumber(3)
      case Automated     => JsNumber(4)
    }
  }

  /**
    * Object containing the reader for the job state
    */
  implicit object JobStateReader extends BSONReader[BSONInteger, JobDeletionFlag] {
    def read(state: BSONInteger) = {
      state match {
        case BSONInteger(0) => Error
        case BSONInteger(1) => OwnerRequest
        case BSONInteger(2) => PublicRequest
        case BSONInteger(3) => Moderation
        case BSONInteger(4) => Automated
        case _              => Error
      }
    }
  }

  /**
    * Object containing the writer for the job state
    */
  implicit object JobStateWriter extends BSONWriter[JobDeletionFlag, BSONInteger] {
    def write(state : JobDeletionFlag) : BSONInteger  = {
      state match {
        case Error         => BSONInteger(0)
        case OwnerRequest  => BSONInteger(1)
        case PublicRequest => BSONInteger(2)
        case Moderation    => BSONInteger(3)
        case Automated     => BSONInteger(4)
      }
    }
  }
}



