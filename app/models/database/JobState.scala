package models.database


import play.api.libs.json._
import play.libs.Json
import reactivemongo.bson.{BSONInteger, BSONReader, BSONWriter}

/**
  * Created by lukas on 1/20/16.
  * Object which describes the job's status
  */



object JobState {
  abstract class JobState(val no: Int)

  case object PartiallyPrepared extends JobState(0)
  case object Prepared extends JobState(1)
  case object Queued extends JobState(2)
  case object Running extends JobState(3)
  case object Error extends JobState(4)
  case object Done extends JobState(5)
  case object Submitted extends JobState(6)


  implicit object JobStateReads extends Reads[JobState] {
    override def reads(json: JsValue) : JsResult[JobState] = json match {
      case obj: JsObject => try {
        JsSuccess((obj \ "status").as[Int] match {
          case 0 => PartiallyPrepared
          case 1 => Prepared
          case 2 => Queued
          case 3 => Running
          case 4 => Error
          case 5 => Done
          case 6 => Submitted
          case _ => Error
        })
      } catch {
        case cause: Throwable => JsError(cause.getMessage)
      }
      case _ => JsError("expected.jsobject")
    }
  }

  implicit object JobStateWrites extends Writes[JobState] {
    def writes(jobState: JobState) = jobState match {
      case PartiallyPrepared => JsNumber(0)
      case Prepared          => JsNumber(1)
      case Queued            => JsNumber(2)
      case Running           => JsNumber(3)
      case Error             => JsNumber(4)
      case Done              => JsNumber(5)
      case Submitted         => JsNumber(6)
    }
  }

  /**
    * Object containing the reader for the job state
    */
  implicit object JobStateReader extends BSONReader[BSONInteger, JobState] {
    def read(state: BSONInteger) = {
      state match {
        case BSONInteger(0) => PartiallyPrepared
        case BSONInteger(1) => Prepared
        case BSONInteger(2) => Queued
        case BSONInteger(3) => Running
        case BSONInteger(4) => Error
        case BSONInteger(5) => Done
        case BSONInteger(6) => Submitted
        case _ => Error
      }
    }
  }

  /**
    * Object containing the writer for the job state
    */
  implicit object JobStateWriter extends BSONWriter[JobState, BSONInteger] {
    def write(state : JobState)  = {
      state match {
        case PartiallyPrepared => BSONInteger(0)
        case Prepared          => BSONInteger(1)
        case Queued            => BSONInteger(2)
        case Running           => BSONInteger(3)
        case Error             => BSONInteger(4)
        case Done              => BSONInteger(5)
        case Submitted         => BSONInteger(6)
      }
    }
  }
}



