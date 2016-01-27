package models

/**
  * Created by lukas on 1/20/16.
  */

abstract class JobState(val no: Int)

case object Pending extends JobState(0)
case object Running extends JobState(1)
case object Error extends JobState(2)
case object Done extends JobState(3)

