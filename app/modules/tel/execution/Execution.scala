package modules.tel.execution

import javax.inject.{Inject, Named, Singleton}

import better.files.File
import better.files._
import modules.tel.TELRegex
import java.nio.file.attribute.PosixFilePermission

import modules.tel.env.Env
import play.api.Logger

import scala.sys.process.Process

sealed trait Execution
case class PendingExecution(register: File => RegisteredExecution) extends Execution
case class RegisteredExecution(run: () => RunningExecution)        extends Execution
case class RunningExecution(terminate: () => Boolean)              extends Execution

@Singleton
class WrapperExecutionFactory @Inject()(@Named("wrapperPath") wrapperPath: String, env: Env) extends TELRegex {

  private final val filePermissions = Set(
    PosixFilePermission.OWNER_EXECUTE,
    PosixFilePermission.OWNER_READ,
    PosixFilePermission.OWNER_WRITE,
    PosixFilePermission.GROUP_EXECUTE,
    PosixFilePermission.GROUP_READ,
    PosixFilePermission.GROUP_WRITE
  )

  // Accept the content of a runscript and used the Wrapper script to produce the Registered Execution
  // One might offer different Methods to create a Pending Execution to avoid the need to pass the content
  // of the Runscript directly as String
  def getInstance(content: String): PendingExecution = {

    val register = { file: File =>
      val runscript = (file / "runscript.sh").write(content)
      runscript.setPermissions(filePermissions)

      val run = { () =>
        // Start the wrapper
        val wrapper = file / "wrapper.sh"

        wrapper.write(
          envString.replaceAllIn(
            runscriptString.replaceAllIn(wrapperPath.toFile.contentAsString, runscript.pathAsString),
            m => env.get(m.group("constant"))))
        wrapper.setPermissions(filePermissions)
        val proc = Process(wrapper.pathAsString, file.toJava).run()

        val terminate = { () =>
          val deletionFile = file / "delete.sh"
          if (deletionFile.exists) {

            deletionFile.setPermissions(filePermissions)
            Process(deletionFile.pathAsString, file.toJava).run()
          }
          proc.destroy()
          true
        }
        RunningExecution(terminate)
      }
      RegisteredExecution(run)
    }
    PendingExecution(register)
  }
}
