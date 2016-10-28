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
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.dsl.DSL.Parse._
import net.ruippeixotog.scalascraper.model.Element


object PsiBlast extends Constants {


  val browser = JsoupBrowser()

  def evalues(mainID: String) = {

    // Convention over configuration! No need to configure the result files in some models or to pass the job object to the view

    val path = s"$jobPath$mainID/results/evalues.dat"

    val lines = Source.fromFile(path).getLines().toList

    lines

  }


  def blastviz(mainID: String) = {

    val blastviz = s"$jobPath$mainID/results/blastviz.html"

    Source.fromFile(blastviz).getLines().mkString

  }


  def overview(mainID: String) = {


    val outfile = s"$jobPath$mainID/results/out.psiblastp"
    val doc = browser.parseFile(outfile)

    //var result : String = doc >> text("TITLE")
    var result = ""

    //val metaItems = doc >> elements("b")

    val regex = """(?s)<b>PSIBLAST(.*?)29:2994-3005.""".r


    for (line <- Source.fromFile(outfile).getLines()) {

      if (!line.startsWith("<b>") && !line.startsWith("href"))
        result = result.concat(line + "<br>")
      else
        result = result.concat(line + " ")

    }


    val overviewText = regex.findFirstIn(result)

    overviewText

  }

}

