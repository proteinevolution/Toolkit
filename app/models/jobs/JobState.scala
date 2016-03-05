package models.jobs

import slick.lifted.MappedTo

/**
  * Created by lukas on 1/20/16.
  */
abstract class JobState(val no: Int)

case object PartiallyPrepared extends JobState(0) // first int is only for passing the state via websockets
case object Prepared extends JobState(1)
case object Queued extends JobState(2)
case object Running extends JobState(3)
case object Error extends JobState(4)
case object Done extends JobState(5)
case object Submitted extends JobState(6)
