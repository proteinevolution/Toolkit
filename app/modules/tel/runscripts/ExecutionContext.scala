package modules.tel.runscripts

import better.files._

/**
  * The context in which a runscript is being executed
  *
  * Created by lzimmermann on 07.12.16.
  */
class ExecutionContext(val workingDirectory : File) extends Cleanable {

  /*
   * Definition of subdirectories (TODO Get them from configuration)
   */
  val children = Map(

    "params" -> "params"
  )
  for(child <- children.valuesIterator) {
    workingDirectory./(child).createDirectories()
  }

  /**
    * Returns subdirectory of execution context
    * @param name
    * @return
    */
  def /(name: String): File = workingDirectory./(children(name))


  /**
    * Cleaning of an execution Context will at least remove the working directory
    */
  def clean(): Unit = {

    for(child <- children.valuesIterator) {
      workingDirectory./(child).delete(swallowIOExceptions = true)
    }
    workingDirectory.delete(swallowIOExceptions = true)
  }
}

object ExecutionContext {

  def apply(workingDirectory : File): ExecutionContext = {

    if(workingDirectory.isDirectory) {
      new ExecutionContext(workingDirectory)
    }
    else {
      throw NotADirectoryException(s"Provided file ${workingDirectory.toString} is not a directory." )
    }
  }
}
case class NotADirectoryException(msg : String) extends IllegalArgumentException(msg)