package de.proteinevolution.models.results

import better.files._

object Ancescon {

  /* Just returns the filecontent as string */
  def readTree(pathToTree: String): String = pathToTree.toFile.lines.mkString("")
}
