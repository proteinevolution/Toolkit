package modules.tel.execution

import javax.inject.{Inject, Named}

import better.files.File
import com.google.inject.assistedinject.Assisted
import better.files._
import modules.tel.TELRegex

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
case class EngineExecution @Inject() (@Named("enginePath") enginePath : String,
                                      @Assisted("content") content: String,
                                      @Assisted("engine") engine: String)
  extends Execution with TELRegex{

  override def register(file: File): File = {

    // Create engine file and runscript file with appropriate content
    file./("runscript.sh").write(content)
    file./("tool.sh").write(runscriptString.replaceAllIn(enginePath.toFile./(s"$engine.sh").contentAsString, "runscript.sh"))
  }
}




object EngineExecution {

  trait Factory {
    def apply(@Assisted("content") content: String, @Assisted("engine") engine: String): EngineExecution
  }
}
