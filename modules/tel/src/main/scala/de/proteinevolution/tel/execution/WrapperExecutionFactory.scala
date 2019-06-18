/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.proteinevolution.tel.execution

import javax.inject.{ Inject, Named, Singleton }
import better.files._
import de.proteinevolution.tel.TELRegex
import java.nio.file.attribute.PosixFilePermission

import de.proteinevolution.tel.execution.WrapperExecutionFactory.{
  PendingExecution,
  RegisteredExecution,
  RunningExecution
}

import scala.sys.process.Process

@Singleton
class WrapperExecutionFactory @Inject()(@Named("wrapperPath") wrapperPath: String) extends TELRegex {

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
  def getInstance(content: String, env: Map[String, String]): PendingExecution = {

    val register = { file: File =>
      val runscript = (file / "runscript.sh").write(content)
      runscript.setPermissions(filePermissions)

      val run = { () =>
        // Start the wrapper
        val wrapper = file / "wrapper.sh"

        wrapper.write(
          envString.replaceAllIn(
            runscriptString.replaceAllIn(wrapperPath.toFile.contentAsString, runscript.pathAsString),
            m => env.getOrElse(m.group("constant"), "")
          )
        )
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

object WrapperExecutionFactory {

  sealed trait Execution
  case class PendingExecution(register: File => RegisteredExecution) extends Execution
  case class RegisteredExecution(run: () => RunningExecution)        extends Execution
  case class RunningExecution(terminate: () => Boolean)              extends Execution

}
