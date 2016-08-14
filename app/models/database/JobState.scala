package models.database

/**
  * Created by lukas on 1/20/16.
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
}



