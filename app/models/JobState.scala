package models

/**
  * Created by lukas on 1/16/16.
  */
abstract class JobState

object JobPending extends JobState
object JobRunning extends JobState
object JobError extends JobState
object JobDone extends JobState
