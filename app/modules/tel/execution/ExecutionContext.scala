package modules.tel.execution

import java.io.{FileInputStream, FileOutputStream, ObjectInputStream, ObjectOutputStream}

import better.files.File

import scala.collection.mutable

/**
  * An Execution Context represent the environment in which a runscript can be executed. Only TEL
  * can produce ExecutionContexts, as TEL knows about the current set context of the Execution
  *
  * @param root
  */
class ExecutionContext(val root: File, reOpen: Boolean = false) {
  // Root directory of the Execution Context
  private val repFileBase = root./("params")
  if (!reOpen) repFileBase.createDirectories()
  // Parameter directory of the Execution Context
  private val serializedParameters = root./("sparam")

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

  /**
    * Writes the parameters to the ExecutionContext folder
    * @param params
    */
  def writeParams(params: Map[String, String]): Unit = {
    val oos = new ObjectOutputStream(new FileOutputStream(serializedParameters.pathAsString))
    oos.writeObject(params)
    oos.close()
  }

  /**
    * Reload the parameters for a job when the EC is gone
    * @return
    */
  def reloadParams: Map[String, String] = {
    val ois = new ObjectInputStream(new FileInputStream(serializedParameters.pathAsString))
    val x   = ois.readObject().asInstanceOf[Map[String, String]]
    ois.close()
    x
  }

  /** Accepts an execution which is subsequently registered in this Execution Context
    * The working directory is created within the executionContext. Currently, the names
    * of the working directories of subsequent executions are just incremented.
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

  def apply(root: File, reOpen: Boolean = false): ExecutionContext = {
    if (root.exists && !reOpen) {
      throw FileAlreadyExists("ExecutionContext cannot be created because the root File already exists")
    } else {
      new ExecutionContext(root, reOpen)
    }
  }
}
