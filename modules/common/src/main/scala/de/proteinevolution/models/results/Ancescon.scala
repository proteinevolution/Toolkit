package de.proteinevolution.models.results

import better.files._

object Ancescon {

  def readTree(pathToTree: String): String = pathToTree.toFile.lines.mkString("")

}
