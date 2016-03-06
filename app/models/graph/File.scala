package models.graph

import models.jobs.UserJob
import play.api.Logger


/**
  *
  * Created by lukas on 2/26/16.
  */

abstract class FileState     // Currently, only input files implement states

case object Ready extends FileState     // Input file is ready (supplied by user or converted from another job)
case object Missing extends FileState   // The source of this input file has not been specified yet
case object Locked extends FileState    // The file is locked, it is waiting for automatic completion and the
                                        // The user does not have access to it


abstract class File(val filename : String, val userJob : UserJob) {

  protected var state : FileState = Missing

  def changeState(newState : FileState)
}


object File {

  def in(filename: String, userJob: UserJob) = new Infile(filename, userJob)
}


class Infile(filename : String, userJob: UserJob) extends File(filename, userJob) {


  def changeState(newState : FileState): Unit = {


    state = newState match {

      case Locked =>
        Logger.info("File with name " + filename + " was locked")

        Locked


      case Missing => Missing  // TODO Change state to missing is not really supported
      case Ready =>

        userJob.countReady()
        Ready
    }
  }
}


/*
class Outfile(filename : String, userJob : UserJob) extends File(filename, userJob) {


  /*
  // Adds a new child as an dependency
  def addChild(child : Infile) = {

    children.append(child)
    Logger.info("File was appended")

    // The child state will now completely depend on the parent File state
    state match {

      // if the parent file is ready, we need adaption to the Input file
      case Ready => child.changeState(NeedsAdaption)

      // In all other cases, the child file has to wait
      case _ : FileState => child.changeState(Waiting)

    }
  }
  */


  def changeState(newState : FileState): Unit = {

    state = newState match {

      case Waiting => Waiting
      case Missing => Missing  // TODO Change state to missing is not really supported
      case Ready =>
        Logger.info("Outfile is now ready")

        Ready
    }
  }
 }
 */

