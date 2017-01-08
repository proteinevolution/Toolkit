package models.results


/**
 */
import better.files._
import models.Constants
import play.twirl.api.Html
import modules.parsers.HHR._

object HHpred extends Constants {

  /**
    * Renders file content as plain HTML. Can be used for scripts that produce HTML from the old Toolkit
    * @param filepath
    * @return
    */
  def html(filepath: String): Html = {
    Html(s"$jobPath/$filepath".toFile.contentAsString)
  }

  def hitlist(jobID: String) : Option[HHR.Header] = {

    val outfile = s"$jobPath$jobID/results/hhsearch.hhr"

    lazy val headerObj = HeaderParser.fromFile(outfile)

    headerObj

  }

}
