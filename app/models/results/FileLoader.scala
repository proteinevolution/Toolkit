package models.results

import better.files._

/**
  * Created by lzimmermann on 21.01.17.
  */
object FileLoader {

  def loadFile(filepath: String): String = {
    filepath.toFile.contentAsString
  }
}
