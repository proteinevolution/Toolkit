package de.proteinevolution.sys

import de.proteinevolution.sys.ConstructR.Script
import sys.process._

class XeqtR(script: Script) {

  // TODO logging

  def run(): Int = {
    val runnable = script.params.toList.mkString("\n")
    runnable.!
  }

}
