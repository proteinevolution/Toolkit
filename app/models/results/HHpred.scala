package models.results


/**
 */
import better.files._
import models.Constants
import play.twirl.api.Html
import modules.parsers.HHR._
import play.api
import play.api.Logger

import scala.collection.mutable.ArrayBuffer

object HHpred extends Constants {

  private val color_regex = """(?:[WYF]+|[LIVM]+|[AST]+|[KR]+|[DE]+|[QN]+|H+|C+|P+|G+)""".r
  private val helix_pattern = """([Hh]+)""".r
  private val sheet_pattern = """([Ee]+)""".r
  private val helix_sheets = """([Hh]+|[Ee]+)""".r("ss")

  private val scopReg = """(d[0-9].*)""".r
  private val mmcifReg = """(...._[a-zA-Z])""".r
  private val pfamReg = """(pfam.*)|(PF.*)""".r

  private val pdbBaseLink = "http://pdb.rcsb.org/pdb/explore.do?structureId="
  private val ncbiBaseLink = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?SUBMIT=y&db=structure&orig_db=structure&term="
  private val ebiBaseLink = "http://www.ebi.ac.uk/pdbe-srv/view/entry/"
  private val pubmedBaseLink = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?CMD=search&db=pubmed&term="
  private val scopBaseLink = "http://scop.berkeley.edu/sid="
  private val scopLineageBaseLink = "http://scop.berkeley.edu/sccs="
  private val pfamBaseLink = "http://pfam.xfam.org/family/"
  private val cddBaseLink = "http://www.ncbi.nlm.nih.gov/Structure/cdd/cddsrv.cgi?uid="


  /**
    * Renders file content as plain HTML. Can be used for scripts that produce HTML from the old Toolkit
    *
    * @param filepath
    * @return
    */

  def html(filepath: String): Html = {
    Logger.info("Getting file: " + s"$jobPath/$filepath")
    Html(s"$jobPath/$filepath".toFile.contentAsString)
  }

  def header(jobID: String): HHR.Header = {

    val outfile = s"$jobPath$jobID/results/hhsearch.hhr"

    lazy val headerObj = HeaderParser.fromFile(outfile)

    headerObj

  }


  def hitlist(jobID: String): HHR.HitList = {

    val outfile = s"$jobPath$jobID/results/hhsearch.hhr"

    lazy val hitListObj = HitListParser.fromFile(outfile)

    hitListObj

  }

  def alignments(jobID: String): HHR.Alignments = {

    val outfile = s"$jobPath$jobID/results/hhsearch.hhr"

    lazy val alignmentsObj = AlignmentsParser.fromFile(outfile)

    alignmentsObj

  }



  def SSColorReplace(sequence: String): String = this.helix_sheets.replaceAllIn(sequence, { m =>
    m.group("ss") match {
      case this.helix_pattern(substr) => "<span class=\"aa_h\">" + substr + "</span>"
      case this.sheet_pattern(substr) => "<span class=\"ss_h\">" + substr + "</span>"
    }
  })
  
  def colorRegexReplacer(sequence: String): String= this.color_regex.replaceAllIn(sequence, { m =>
 "<span class=\"aa_"+m.toString().charAt(0)+"\">"+m.toString()+"</span>"})

  def makeRow(rowClass: String, entries: Array[String]): Html = {
    var html = "";
    if (rowClass == null)
      html += "<tr>"
    else
      html += "<tr class='" + rowClass + "'>"
    for (entry <- entries) {
      html += "<td>" + entry + "</td>"
    }
    html += "<tr>"
    Html(html)
  }

  /* GENERATING LINKS FOR HHPRED */


  def getSingleLink(id : String) : Html = {
    val db = identifyDatabase(id)
    var link = ""
    if(db == "scop") {
      var idTrimmed = id.substring (1, 5)
     link += generateLink (pdbBaseLink, idTrimmed, id)
    }
    if(db == "mmcif") {
      var idPdb = id.replaceAll("_.*$", "")
      link += generateLink(pdbBaseLink, idPdb, id)
    }
    if(db == "pfam"){
      var idPfam = id.replaceAll("am.*$", "")
      link += generateLink(pfamBaseLink, idPfam + "#tabview=tab1", id)
    }
    Html(link)
  }


  def getLinks(id : String) : Html = {
    val db = identifyDatabase(id)
    var links = new ArrayBuffer[String]()


    if(db == "scop") {
      var idTrimmed = id.substring(1, 5)
      links += "<a onclick=\"window.open(\'/structure3D/"+idTrimmed+"\', '_blank', " +
        "'location=yes,height=550,width=750,scrollbars=yes,status=yes');\">Show template 3D structure</a>"
      links +=  "<a>Template alignment</a>"
      links += generateLink(scopBaseLink, id, "SCOP")
      links += generateLink(ncbiBaseLink, idTrimmed, "NCBI")
    }
    if(db == "mmcif") {
      var idPdb = id.replaceAll("_.*$", "")
      links += "<a onclick=\"window.open(\'/structure3D/"+idPdb+"\', '_blank'," +
        " 'location=yes,height=550,width=750');\">Show template 3D structure</a>"
      links +=  "<a>Template alignment</a>"
    }
    if (db == "pfam"){
      var idCDD = id.replaceAll("PF", "pfam")
      links +=  "<a>Template alignment</a>"
      idCDD = idCDD.replaceAll("\\..*","")
      links += generateLink(cddBaseLink, idCDD, "CDD")
      links += generateLink(pubmedBaseLink, id, "PubMed")
    }
    Html(links.mkString(" | "))
  }

  def generateLink(baseLink : String, id : String, name : String) : String = "<a href='"+baseLink+id+ "' target='_blank'>"+name+"</a>"

  def identifyDatabase(id : String) : String = id match {
    case scopReg(_) => "scop"
    case mmcifReg(_) => "mmcif"
    case pfamReg(_,_) => "pfam"
    case e : String => Logger.info("Struc: "+e+ "could not be matched against any database!");""
  }
}

