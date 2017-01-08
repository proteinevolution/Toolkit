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

  def hitlist(jobID: String) = {

    val outfile = s"$jobPath$jobID/results/hhsearch.hhr"

    lazy val Matchlist = HeaderParser.fromString().get.Match_columns
    lazy val Query = HeaderParser.fromString().get.Query
    lazy val Neff = HeaderParser.fromString().get.Neff
    lazy val Searched_HMMs = HeaderParser.fromString().get.Searched_HMMs
    lazy val No_of_Seqs = HeaderParser.fromString().get.No_of_Seqs
    lazy val Date = HeaderParser.fromString().get.Date
    lazy val Command = HeaderParser.fromString().get.Command

    println("Query: " + Query + " Matchlist: " + Matchlist + " Neff " + Neff + "Searched_HMMs: " + Searched_HMMs +
    " No_of_Seqs: " + No_of_Seqs + " Date : " + Date + " Command: " + Command)

    Query

  }

}
