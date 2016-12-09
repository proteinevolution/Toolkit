package modules.tel.runscripts

import better.files.File


class ExecutionContext(val root: File) {

  private val repFileBase = root./("params").createDirectories()

  /*
   Creates a new file in this ExecutionContext with a certain name and content.
   A preexisting file with the same name will be overridden
   */
  def getFile(name: String, content: String) : File = {

    val x = repFileBase./(name)
    x.delete(swallowIOExceptions = true)
    x.write(content)
    x
  }
}
object ExecutionContext {

  def apply(root: File): ExecutionContext ={

    if(root.exists) {

      throw FileAlreadyExists("Directory for Execution context already exists.")
    }
    else {
      new ExecutionContext(root)
    }
  }
}

case class NotADirectoryException(msg : String) extends IllegalArgumentException(msg)
case class FileAlreadyExists(msg: String) extends IllegalArgumentException(msg)