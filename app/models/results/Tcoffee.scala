package models.results

import models.Constants
import play.api.Logger

import scala.io.Source

/**
  * Created by lzimmermann on 10/29/16.
  */
object Tcoffee extends Constants {




  def colored(mainID: String) : String = {

    val outfile = s"$jobPath$mainID/results/alignment.score_html"

    val text = Source.fromFile(outfile).getLines().mkString

    val regex = """(?s)<body>(.*?)</body>""".r

    lazy val docwithoutstyles = regex.findFirstIn(text).get

    docwithoutstyles
  }

  /* returns the clustal output to be embedded in biojs msa via twirl */

  def alnviz(jobID: String) : Iterator[String] = {
    Logger.info("Reading file: " + "$jobPath$jobID/results/alignment.clustalw_aln" )
    Source.fromFile(s"$jobPath$jobID/results/alignment.clustalw_aln").getLines()
  }
}
