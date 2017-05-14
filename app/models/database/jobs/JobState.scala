package models.database.jobs

import play.api.libs.json._
import reactivemongo.bson.{BSONInteger, BSONReader, BSONWriter}

/**
  * Created by lukas on 1/20/16.
  * Object which describes the job's status
  */
sealed trait JobState

/*
 A Pending Job has parameters which have not been supplied yet. This might happen for file download
 and when a job depends on the successful execution of another job.
 */
// Job State which is set to save a job without submitting it
case object Prepared extends JobState
// Job State which is set when the job is submitted to the cluster but has to wait in the queue
case object Queued extends JobState
// Job State which is set when the job is being executed
case object Running extends JobState
// Job State which is set when the job has reached an error state
case object Error extends JobState
// Job State which is set when the job has completed successfully
case object Done extends JobState
// Job State which is set when the job was successfully sent to the server
case object Submitted extends JobState
// Job State which is set when the job was validated by the hash search but a different job was found
case object Pending extends JobState

object JobState {
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
      case Prepared  => JsNumber(1)
      case Queued    => JsNumber(2)
      case Running   => JsNumber(3)
      case Error     => JsNumber(4)
      case Done      => JsNumber(5)
      case Submitted => JsNumber(6)
      case Pending   => JsNumber(7)
    }
  }

  /**
    * Object containing the reader for the job state
    */
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
        case _              => Error
      }
    }
  }

  /**
    * Object containing the writer for the job state
    */
  implicit object JobStateWriter extends BSONWriter[JobState, BSONInteger] {
    def write(state: JobState): BSONInteger = {
      state match {
        case Prepared  => BSONInteger(1)
        case Queued    => BSONInteger(2)
        case Running   => BSONInteger(3)
        case Error     => BSONInteger(4)
        case Done      => BSONInteger(5)
        case Submitted => BSONInteger(6)
        case Pending   => BSONInteger(7)
      }
    }
  }
}
