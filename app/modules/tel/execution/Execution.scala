package modules.tel.execution

import javax.inject.{Inject, Named}

import better.files.File
import com.google.inject.assistedinject.Assisted
import better.files._
import modules.tel.TELRegex
import java.nio.file.attribute.PosixFilePermission


// A registered execution can declare a file to specify how the execution can be stopped once started
case class RegisteredExecution(run: File, delete: Option[File]) {

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

// An execution which executes embeds the actual content in an engine File
case class WrapperExecution @Inject()(@Named("wrapperPath") wrapperPath : String,
                                      @Assisted("content") content: String)
  extends Execution with TELRegex {

  override def register(file: File): RegisteredExecution = {

    // Create engine file and runscript file with appropriate content
    (file / "runscript.sh").write(content)
    // Assumption that the runfile produces a delete.sh script in the same directory
    RegisteredExecution((file / "wrapper.sh").write(runscriptString.replaceAllIn(wrapperPath.toFile.contentAsString,
      "runscript.sh")), Some(file / "delete.sh"))
  }
}

object WrapperExecution {

  trait Factory {
    def apply(@Assisted("content") content: String): WrapperExecution
  }
}






