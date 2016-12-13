package modules.tel.execution

import javax.inject.{Inject, Named}

import better.files.File

/**
  *
  * Created by lzimmermann on 11.12.16.
  */
trait Execution {

  /**
    * Accepts a working directory and returns the executable file
    * The input directory is required to exist
    */
  def register(file: File): File
}


/*
  Implementations
 */

// Execution which just requires the content of the file
case class LocalExecution(content: String) extends Execution {

  override def register(file: File): File = file./("tool.sh").write(content)
}

// An execution which executes embeds the actual content in an engine File
case class EngineExecution @Inject() (@Named("enginePath") enginePath : String, content: String)



