package models.results


/**
 */
import better.files._
import models.Constants
import play.twirl.api.Html
import modules.parsers.HHR._
import play.api.Logger

object HHpred extends Constants {

  /**
    * Renders file content as plain HTML. Can be used for scripts that produce HTML from the old Toolkit
    * @param filepath
    * @return
    */

  def html(filepath: String): Html = {
    Logger.info("Getting file: " + s"$jobPath/$filepath")
    Html(s"$jobPath/$filepath".toFile.contentAsString)
  }

  def header(jobID: String) : HHR.Header = {

    val outfile = s"$jobPath$jobID/results/hhsearch.hhr"

    lazy val headerObj = HeaderParser.fromFile(outfile)

    headerObj

  }


  def hitlist(jobID: String) : HHR.HitList = {

    val outfile = s"$jobPath$jobID/results/hhsearch.hhr"

    lazy val hitListObj = HitListParser.fromFile(outfile)

    hitListObj

  }

  def alignments(jobID: String) : HHR.Alignments = {

    val outfile = s"$jobPath$jobID/results/hhsearch.hhr"

    lazy val alignmentsObj = AlignmentsParser.fromFile(outfile)

    alignmentsObj

  }

}
