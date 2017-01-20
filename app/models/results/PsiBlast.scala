package models.results

/**
 *
 * Created by snam on 18.10.16.
 *
 * Tool model for parsing result files
 *
 */
import models.Constants
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import scala.io.Source
import modules.parsers.FASTA
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.model.Element
import better.files._

object PsiBlast extends Constants {


  val browser = JsoupBrowser()


  /* reads the evalues.dat file from the job directory and returns the evalues as a list */

  def evalues(mainID: String) : List[String] = {

    val path = s"$jobPath$mainID/results/out.psiblastp_evalues"
    Source.fromFile(path).getLines().toList
  }


  /* returns the blastviz html which is still generated by a perl script (TODO) */

  def blastviz(mainID: String) : String = {
    s"$jobPath$mainID/results/blastviz.html".toFile.contentAsString
  }



  /* returns blast references which can be hidden in the result view */

  def overview(mainID: String) : Option[String] = {


    val outfile = s"$jobPath$mainID/results/out.psiblastp"

    var result = ""

    val regex = """(?s)<b>PSIBLAST(.*?)Sequences producing significant alignments:""".r


    for (line <- Source.fromFile(outfile).getLines()) {

      if (!line.startsWith("<b>") && !line.startsWith("href"))
        result = result.concat(line + "<br>")
      else
        result = result.concat(line + " ")

    }

    lazy val overviewText = regex.findFirstIn(result)
    overviewText

  }

  /* embeds the file which was produced on the cluster */

  def alignhits_alt(mainID: String) : String = {

    val outfile = s"$jobPath$mainID/results/out.psiblastp_alignment"

    var result = ""

    for (line <- Source.fromFile(outfile).getLines()) {
      result = result.concat(line + "<br>")

    }
    result
  }

  /* embeds the file which was produced on the cluster */

  def ov_alt(mainID: String) : String = {

    s"$jobPath$mainID/results/out.psiblastp_overview".toFile.contentAsString
  }

  /* returns blast alignhits with links */

  def alignhits(mainID: String) : Option[String] = {

    val outfile = s"$jobPath$mainID/results/out.psiblastp"
    //val doc = browser.parseFile(outfile)
    var index = 0
    var index2 =0
    val regex = """(?s)</a><a title="(.*?)</PRE>""".r

    var result = "<PRE>"

    for (line <- Source.fromFile(outfile).getLines()) {

      if(line.startsWith("<a") || line.startsWith("</a><a")) {
        val indexAdd = index + 1
        result = result.concat(s"<input type='checkbox' style='margin: 9px; padding: 9px;'  class='hits' value= '$index'> $indexAdd $line<br>")
        index = index + 1
      }


      else if(line.startsWith("><a")) {
        val index2add = index2 + 1
        result = result.concat(s"<input type='checkbox' style='margin: 5px; padding: 5px;'  class='hits' value= '$index2'><b>$line</b><br>")
        index2 = index2 + 1
      }


      else
        result = result.concat(line + " <br>")

    }

    lazy val alignHits = regex.findFirstIn(result)

    alignHits

  }

  /* returns fasta blast output */

  def fastaAlignment(mainID: String) : List[FASTA.Entry] = {

    val outfile = s"$jobPath$mainID/results/out.align"

    lazy val fastaList = FASTA.fromFile(outfile)

    fastaList

  }





  /* returns the clustal output to be embedded in biojs msa via twirl */

  def alnviz(mainID: String) : Iterator[String] = {

    val outfile = s"$jobPath$mainID/results/out.align_clu"

    Source.fromFile(outfile).getLines()

  }

}

