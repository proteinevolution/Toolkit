package models.results

import better.files._

/**
  * Created by lzimmermann on 12.01.17.
  */
object Ancescon {

  /* Just returns the filecontent as string */
  def readTree(pathToTree: String): String = pathToTree.toFile.lines.mkString("")
}
