package de.proteinevolution.models.database.jobs

import play.api.libs.json._
import reactivemongo.bson.{ BSONInteger, BSONReader, BSONWriter }

object JobState {

  sealed trait JobState

  case object Prepared extends JobState

  case object Queued extends JobState

  case object Running extends JobState

  case object Error extends JobState

  case object Done extends JobState

  case object Submitted extends JobState

  case object Pending extends JobState

  case object LimitReached extends JobState

  case object Deleted extends JobState

  implicit object JobStateReads extends Reads[JobState] {
    override def reads(json: JsValue): JsResult[JobState] = json match {
      case obj: JsObject =>
        try {
          JsSuccess((obj \ "status").as[Int] match {
            case 1 => Prepared
            case 2 => Queued
            case 3 => Running
            case 4 => Error
            case 5 => Done
            case 6 => Submitted
            case 7 => Pending
            case 8 => LimitReached
            case 9 => Deleted
            case _ => Error
          })
        } catch {
          case cause: Throwable => JsError(cause.getMessage)
        }
      case _ => JsError("expected.jsobject")
    }
  }

  implicit object JobStateWrites extends Writes[JobState] {
    def writes(jobState: JobState): JsNumber = jobState match {
      case Prepared     => JsNumber(1)
      case Queued       => JsNumber(2)
      case Running      => JsNumber(3)
      case Error        => JsNumber(4)
      case Done         => JsNumber(5)
      case Submitted    => JsNumber(6)
      case Pending      => JsNumber(7)
      case LimitReached => JsNumber(8)
      case Deleted      => JsNumber(9)
    }
  }

  implicit object JobStateReader extends BSONReader[BSONInteger, JobState] {
    def read(state: BSONInteger): JobState with Product with Serializable = {
      state match {
        case BSONInteger(1) => Prepared
        case BSONInteger(2) => Queued
        case BSONInteger(3) => Running
        case BSONInteger(4) => Error
        case BSONInteger(5) => Done
        case BSONInteger(6) => Submitted
        case BSONInteger(7) => Pending
        case BSONInteger(8) => LimitReached
        case BSONInteger(9) => Deleted
        case _              => Error
      }
    }
  }

  implicit object JobStateWriter extends BSONWriter[JobState, BSONInteger] {
    def write(state: JobState): BSONInteger = {
      state match {
        case Prepared     => BSONInteger(1)
        case Queued       => BSONInteger(2)
        case Running      => BSONInteger(3)
        case Error        => BSONInteger(4)
        case Done         => BSONInteger(5)
        case Submitted    => BSONInteger(6)
        case Pending      => BSONInteger(7)
        case LimitReached => BSONInteger(8)
        case Deleted      => BSONInteger(9)
      }
    }
  }

}
