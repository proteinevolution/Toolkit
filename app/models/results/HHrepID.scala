package models.results

import models.Constants
import play.twirl.api.Html

import scala.io.Source
import java.nio.file.{Paths, Files}

/**
  * Created by drau on 30.05.17.
  */
object HHrepID extends Constants{
  def getResult(jobID: String) : Html = {
    val headerLine = """(Results for repeats type )([A-Z])(:)""".r
    val seqLine = """([\S]+\s+[\S]+\s+[\S]+\s+[\S]+\s)([\S]+)""".r

    val filePath = s"$jobPath$jobID/results/query.hhrepid"

      val imagePath = s"/files/$jobID/query_"
      val source  = Source.fromFile(filePath)
      val data = try {
        source.getLines().toList.map {
          case wholeMatch@headerLine(m1, m2, m3) => "<h5>" + wholeMatch + "</h5>" + "<div class='hhrepImage'>" + views.html.jobs.resultpanels.image(s"$imagePath$m2.png") + "</div>"
          case wholeMatch@seqLine(m1, m2) => "<pre class='sequence'>" + wholeMatch.replace(m2, BlastVisualization.colorRegexReplacer(m2)) + "</pre>"
          case "" => "<br />"
          case m => "<pre class='sequence'>" + m + "</pre>"
        }
      } finally{source.close()}
      Html(data.mkString(""))
  }

  def existsResult(jobID: String) : Boolean = {
    val filePath = s"$jobPath$jobID/results/query.hhrepid"
    Files.exists(Paths.get(filePath))
  }

}
