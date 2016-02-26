package models.graph

/**
  *
  * Created by lukas on 2/26/16.
  */

abstract class FileState
case object Ready extends FileState
case object Missing extends FileState
case object Waiting extends FileState

case class File(val filename : String, val state : FileState)



