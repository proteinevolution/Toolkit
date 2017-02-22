package models.results
import javax.inject.Inject

import models.Constants
import better.files._
import models.results.Tcoffee.jobPath
import modules.CommonModule
import play.api.Logger
import play.modules.reactivemongo.ReactiveMongoApi
import play.twirl.api.Html

import scala.io.Source

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
