package modules.tel.runscripts

import java.nio.file.Path

/**
  * The context in which a runscript is being executed
  *
  * Created by lzimmermann on 07.12.16.
  */
class ExecutionContext(val workingDirectory : Path)

object ExecutionContext {

  def apply(workingDirectory : Path) = new ExecutionContext(workingDirectory)
}