package modules.tel.runscripts

import better.files.File

/**
  * An Execution Context represent the environment in which a runscript can be executed. Only TEL
  * can produce ExecutionContexts, as TEL knows about the current set context of the Execution
  *
  * @param root
  */
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
case class NotADirectoryException(msg : String) extends IllegalArgumentException(msg)
case class FileAlreadyExists(msg: String) extends IllegalArgumentException(msg)



