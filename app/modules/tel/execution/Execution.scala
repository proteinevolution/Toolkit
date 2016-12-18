package modules.tel.execution

import javax.inject.{Inject, Named}

import better.files.File
import com.google.inject.assistedinject.Assisted
import better.files._
import modules.tel.TELRegex
import java.nio.file.attribute.PosixFilePermission


// A registered execution can declare a file to specify how the execution can be stopped once started
case class RegisteredExecution(run: File, destroy: Option[File]) {

  run.setPermissions(Set(PosixFilePermission.OWNER_EXECUTE,
    PosixFilePermission.OWNER_READ,
    PosixFilePermission.OTHERS_WRITE))
}

/**
  *
  * Created by lzimmermann on 11.12.16.
  */
abstract class Execution {

  /**
    * Accepts a working directory and returns the executable file
    * The input directory is required to exist
    */
  def register(file: File): RegisteredExecution
}

/*
  Implementations
 */

// Execution which just requires the content of the file
case class LocalExecution(content: String) extends Execution {

  override def register(file: File): RegisteredExecution = {

    // Local execution does not require a delete file
    RegisteredExecution(file./("tool.sh").write(content), None)
  }
}

// An execution which executes embeds the actual content in an engine File
case class EngineExecution @Inject() (@Named("enginePath") enginePath : String,
                                      @Assisted("content") content: String,
                                      @Assisted("engine") engine: String)
  extends Execution with TELRegex{

  override def register(file: File): RegisteredExecution = {

    // Create engine file and runscript file with appropriate content
    file./("runscript.sh").write(content)
    file./("tool.sh").write(runscriptString.replaceAllIn(enginePath.toFile./(s"$engine.sh").contentAsString, "runscript.sh"))

    val run = file./("tool.sh").write(runscriptString.replaceAllIn(enginePath.toFile./(s"$engine.sh").contentAsString, "runscript.sh"))
    // Assumption that the runfile produces a delete.sh script in the same directory
    val delete =  Some(file./("delete.sh"))
    RegisteredExecution(run, delete)
  }
}

object EngineExecution {

  trait Factory {
    def apply(@Assisted("content") content: String, @Assisted("engine") engine: String): EngineExecution
  }
}






