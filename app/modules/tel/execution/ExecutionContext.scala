package modules.tel.execution

import better.files.File

import scala.collection.mutable

/**
  * An Execution Context represent the environment in which a runscript can be executed. Only TEL
  * can produce ExecutionContexts, as TEL knows about the current set context of the Execution
  *
  * @param root
  */
class ExecutionContext(val root: File) {

  private val repFileBase = root./("params").createDirectories()

  // a Queue of executable files for this execution Context
  private val executionQueue = mutable.Queue[RegisteredExecution]()
  private val execNumbers    = Iterator.from(0, 1)

  /**
   Registers a new file in this ExecutionContext with a certain name and content.
   A preexisting file with the same name will be overridden
    */
  def getFile(name: String, content: String): File = {

    val x = repFileBase./(name)
    x.delete(swallowIOExceptions = true)
    x.write(content)
    x
  }

  /** Accepts an execution which is subsequently registered in this Execution Context
    * The working direcotry is created within the executionContext. Currently, the names
    * of the working direcotries of subsequent executions are just incremented.
    * @param execution
    */
  def accept(execution: PendingExecution): Unit = {

    executionQueue.enqueue(execution.register(root./(execNumbers.next().toString).createDirectories()))
  }

  def executeNext: RegisteredExecution = {

    executionQueue.dequeue()
    //RunnableExecution(Process(x.run.pathAsString, x.run.parent.toJava),x.delete.map(f => Process(f.pathAsString)))
  }
  def hasMoreExecutions: Boolean = executionQueue.nonEmpty
}

object ExecutionContext {

  case class FileAlreadyExists(msg: String) extends IllegalArgumentException(msg)

  def apply(root: File): ExecutionContext = {

    if (root.exists) {
      throw FileAlreadyExists("ExecutionContext cannot be created because the root File already exists")

    } else {
      new ExecutionContext(root)
    }
  }
}
