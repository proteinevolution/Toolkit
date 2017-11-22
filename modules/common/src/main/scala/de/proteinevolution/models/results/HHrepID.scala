package de.proteinevolution.models.results

import play.twirl.api.Html

import scala.io.Source
import java.nio.file.{ Files, Paths }

object HHrepID {
  def getResult(jobID: String, filePath: String): Html = {
    val headerLine = """(Results for repeats type )([A-Z])(:)""".r
    val seqLine    = """([\S]+\s+[\S]+\s+[\S]+\s+[\S]+\s)([\S]+)""".r

    val imagePath = s"/files/$jobID/query_"
    val source    = Source.fromFile(filePath)
    val data = try {
      source.getLines().toList.map {
        case wholeMatch @ headerLine(m1, m2, m3) =>
          "<h5>" + wholeMatch + "</h5>" + "<span class='hhrepImage'>" +
          s"<img hspace='14' src='$imagePath$m2.png'>" + "</div><br />"
        case wholeMatch @ seqLine(m1, m2) =>
          "<pre class='sequence hhrepidview'>" + wholeMatch.replace(m2, Common.colorRegexReplacer(m2)) + "</pre>"
        case "" => "<br />"
        case m  => "<pre class='sequence hhrepidview'>" + m + "</pre>"
      }
    } finally { source.close() }
    Html(data.mkString(""))
  }

  def existsResult(jobID: String, filePath: String): Boolean = {

    Files.exists(Paths.get(filePath))
  }

}
