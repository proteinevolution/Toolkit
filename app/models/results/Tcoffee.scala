package models.results

import models.Constants

import scala.io.Source

/**
  * Created by lzimmermann on 10/29/16.
  */
object Tcoffee extends Constants {




  /* returns the clustal output to be embedded in biojs msa via twirl */

  def alnviz(mainID: String) = {

    val outfile = s"$jobPath$mainID/results/alignment.clustalw_aln"

    Source.fromFile(outfile).getLines()
  }
}

