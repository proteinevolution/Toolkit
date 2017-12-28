package de.proteinevolution.tools.results

import java.nio.file.{ Files, Paths }
import play.twirl.api.Html
import scala.io.Source

object HHrepID {
  def getResult(jobID: String, filePath: String): Html = {
    val headerLine = """(Results for repeats type )([A-Z])(:)""".r
    val seqLine    = """([\S]+\s+[\S]+\s+[\S]+\s+[\S]+\s)([\S]+)""".r

    val imagePath = s"/files/$jobID/query_"
    val source    = Source.fromFile(filePath)
    val data = try {
      source.getLines().toList.map {
        case wholeMatch @ headerLine(_, m, _) =>
          "<h5>" + wholeMatch + "</h5>" + "<span class='hhrepImage'>" +
          s"<img hspace='14' src='$imagePath$m.png'>" + "</div><br />"
        case wholeMatch @ seqLine(_, m) =>
          "<pre class='sequence hhrepidview'>" + wholeMatch.replace(m, Common.colorRegexReplacer(m)) + "</pre>"
        case "" => "<br />"
        case m  => "<pre class='sequence hhrepidview'>" + m + "</pre>"
      }
    } finally { source.close() }
    Html(data.mkString(""))
  }

  def existsResult(filePath: String): Boolean = {
    Files.exists(Paths.get(filePath))
  }

}
