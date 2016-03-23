package models.graph

/**
  *
  * Created by lukas on 2/26/16.
  */

abstract class FileState     // Currently, only input files implement states

case object Ready extends FileState     // Input file is ready (supplied by user or converted from another job)
case object Missing extends FileState   // The source of this input file has not been specified yet
case object Locked extends FileState    // The file is locked, it is waiting for automatic completion and the
