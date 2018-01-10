package de.proteinevolution.sys

import de.proteinevolution.sys.ConstructR.Script
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent._
import sys.process._

class XeqtR(script: Script) {

  // TODO logging, validation, error handling
  def run(): List[Future[Int]] = {
    script.params.toList.map { cmd =>
      Future(blocking(cmd.run().exitValue()))
    }
  }

}
