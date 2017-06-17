package models.results
import better.files._
import models.results.Tcoffee.jobPath
import play.twirl.api.Html

/**
  * Created by lzimmermann on 21.01.17.
  */
object FileLoader {

  def loadFile(filepath: String): String = {
    filepath.toFile.contentAsString
  }

  def loadHTML(filepath: String): Html = {
    Html(s"$jobPath/$filepath".toFile.contentAsString.filter(_ >= ' '))
  }
}
