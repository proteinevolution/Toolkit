package de.proteinevolution.results.results

import better.files._

object Ancescon {

  def readTree(pathToTree: String): String = pathToTree.toFile.lines.mkString("")

}
