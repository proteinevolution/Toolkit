package de.proteinevolution.results.results

import java.nio.file.{ Files, Paths }

import play.twirl.api.Html

import better.files._

object HHrepID {

  def getResult(jobID: String, filePath: String): Html = {
    val headerLine = """(Results for repeats type )([A-Z])(:)""".r
    val seqLine    = """([\S]+\s+[\S]+\s+[\S]+\s+[\S]+\s)([\S]+)""".r
    val imagePath  = s"/results/files/$jobID/query_"
    val data = (for { in <- File(filePath).newInputStream.autoClosed } yield
      in.lines.toList.map {
        case wholeMatch @ headerLine(_, m, _) =>
          "<h5>" + wholeMatch + "</h5>" + "<span class='hhrepImage'>" +
          s"<img hspace='14' src='$imagePath$m.png'>" + "</div><br />"
        case wholeMatch @ seqLine(_, m) =>
          "<pre class='sequence hhrepidview'>" + wholeMatch.replace(m, Common.colorRegexReplacer(m)) + "</pre>"
        case "" => "<br />"
        case m  => "<pre class='sequence hhrepidview'>" + m + "</pre>"
      }).get()
    Html(data.mkString(""))
  }

  def existsResult(filePath: String): Boolean = {
    Files.exists(Paths.get(filePath))
  }

}
