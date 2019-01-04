package de.proteinevolution.util

import better.files._

object FileLoader {

  def loadFile(filepath: String): String = {
    filepath.toFile.contentAsString
  }

}
