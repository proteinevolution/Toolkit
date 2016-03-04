package models.graph

import models.jobs.UserJob

import scala.collection.mutable.ArrayBuffer

/**
  *
  * Created by lukas on 2/26/16.
  */

abstract class FileState
case object Ready extends FileState
case object Missing extends FileState
case object Waiting extends FileState
case object NeedsAdaption extends FileState




object File {

  def apply(filename : String, userJob: UserJob) = new File(filename, userJob)
}

class File(val filename : String, userJob : UserJob) {

  private var state : FileState  = Missing

  // All files that depend on the state of this file
  val children : ArrayBuffer[File] = ArrayBuffer.empty

  def changeState(newState : FileState): Unit = {


    state = newState match {

      case Waiting => Waiting
      case Missing => Missing  // TODO Change state to missing is not really supported
      case Ready =>

        userJob.countReady()
        children.foreach(_.changeState(NeedsAdaption))
        Ready

      // Todo Implement me, we need to trigger the adaption process here
      case NeedsAdaption =>




        NeedsAdaption
    }
  }
}



