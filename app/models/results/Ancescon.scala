package models.results

import better.files._
import models.Constants

/**
  * Created by lzimmermann on 12.01.17.
  */
object Ancescon extends Constants {

  /* Just returns the filecontent as string */
  def readTree(pathToTree: String): String = pathToTree.toFile.lines.mkString("")
}