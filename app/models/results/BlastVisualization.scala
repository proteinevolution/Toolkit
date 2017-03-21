package models.results


/**
 */
import better.files._
import models.Constants
import play.twirl.api.Html
import modules.parsers.HHR._
import modules.tel.env.Env
import play.api.Logger


import scala.collection.mutable.ArrayBuffer

object BlastVisualization extends Constants {

  private val color_regex = """(?:[WYF]+|[LIVM]+|[AST]+|[KR]+|[DE]+|[QN]+|H+|C+|P+|G+)""".r
  private val helix_pattern = """([Hh]+)""".r
  private val sheet_pattern = """([Ee]+)""".r
  private val helix_sheets = """([Hh]+|[Ee]+)""".r("ss")

  private val scopReg = """([defgh][0-9a-zA-Z\.\_]+)""".r
  private val mmcifReg = """(...._[a-zA-Z])""".r
  private val mmcifShortReg = """([0-9]...)""".r
  private val pfamReg = """(pfam[0-9]+|PF[0-9]+(\.[0-9]+)?)""".r
  private val ncbiReg = """[A-Z]{2}_?[0-9]+\.?\#?([0-9]+)?|[A-Z]{3}[0-9]{5}""".r

  private val pdbBaseLink = "http://pdb.rcsb.org/pdb/explore.do?structureId="
  private val pdbeBaseLink = "http://www.ebi.ac.uk/pdbe/entry/pdb/"
  private val ncbiBaseLink = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?SUBMIT=y&db=structure&orig_db=structure&term="
  private val ncbiProteinBaseLink = "https://www.ncbi.nlm.nih.gov/protein/"
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
    var html = ""
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
    else if(db == "mmcif") {
      var idPdb = id.replaceAll("_.*$", "")
      link += generateLink(pdbBaseLink, idPdb, id)
    }
    else if(db == "pfam"){
      var idPfam = id.replaceAll("am.*$||..*", "")
      link += generateLink(pfamBaseLink, idPfam + "#tabview=tab1", id)
    }
    else if(db == "ncbi"){
      link += generateLink(ncbiProteinBaseLink, id, id)
    }
    else{
      link = id
    }

    Html(link)
  }


  def getLinksHHpred(id : String) : Html = {
    val db = identifyDatabase(id)
    var links = new ArrayBuffer[String]()

    var idPdb = id.replaceAll("_.*$", "").toLowerCase
    var idTrimmed = id.substring(1, 5)
    var idCDD = id.replaceAll("PF", "pfam")
    var idNcbi = id.replaceAll("#", ".") + "?report=fasta"
    links +=  "<a data-open=\"templateAlignmentModal\" onclick=\"templateAlignment(\'" + id + "\')\">Template alignment</a>"
    if(db == "scop") {

      links += "<a data-open=\"structureModal\" onclick=\"showStructure(\'" + id + "\')\";\">Template 3D structure</a>"
      links += generateLink(scopBaseLink, id, "SCOP")
      links += generateLink(ncbiBaseLink, idTrimmed, "NCBI")
    }
    else if(db == "mmcif") {
      links += "<a data-open=\"structureModal\" onclick=\"showStructure(\'" + id + "\')\";\">Template 3D structure</a>"
      links += generateLink(pdbeBaseLink, idPdb, "PDBe")
    }
    else if (db == "pfam"){
      idCDD = idCDD.replaceAll("\\..*","")
      links += generateLink(cddBaseLink, idCDD, "CDD")
    }
    else if (db == "ncbi"){
      links += generateLink(ncbiProteinBaseLink, idNcbi, "NCBI Fasta")
    }
    Html(links.mkString(" | "))
  }

  def getLinks(id : String) : Html = {
    val db = identifyDatabase(id)
    var links = new ArrayBuffer[String]()
    var idNcbi = id.replaceAll("#", ".") + "?report=fasta"
    var idPdb = id.replaceAll("_.*$", "").toLowerCase
    var idTrimmed = id.substring(1, 5)
    var idCDD = id.replaceAll("PF", "pfam")
    if(db == "scop") {
      links += generateLink(scopBaseLink, id, "SCOP")
      links += generateLink(ncbiBaseLink, idTrimmed, "NCBI")
    }
    else if(db == "mmcif") {
      links += generateLink(pdbeBaseLink, idPdb, "PDBe")
    }
    else if (db == "pfam"){
      idCDD = idCDD.replaceAll("\\..*","")
      links += generateLink(cddBaseLink, idCDD, "CDD")
    }
    else if (db == "ncbi"){
      links += generateLink(ncbiProteinBaseLink, idNcbi , "NCBI Fasta")
    }

    Html(links.mkString(" | "))
  }

  def generateLink(baseLink : String, id : String, name : String) : String = "<a href='"+baseLink+id+"' target='_blank'>"+name+"</a>"

  def identifyDatabase(id : String) : String = id match {
    case scopReg(_) => "scop"
    case mmcifShortReg(_) => "mmcif"
    case mmcifReg(_) => "mmcif"
    case pfamReg(_,_) => "pfam"
    case ncbiReg(_) => "ncbi"
    case e : String => Logger.info("Struc: ("+e+") could not be matched against any database!");""
  }

  def percentage(str : String) : String = {
    val num = str.toDouble
    val percent = (num * 100).toInt.toString + " %"
    percent
  }

  def calculatePercentage(str1 : String, str2 : String): String ={
    val num1 = str1.toDouble
    val num2 = str2.toDouble
    val percent = ((num1/num2) * 100).toInt.toString + " %"
    percent
  }

  def wrapSequence(seq : String, num : Int) : String = {
    var seqWrapped = ""
    for { i <- 0 to seq.length if i % num == 0}
      if( i + num < seq.length ) {
      seqWrapped += "<tr><td></td><td class=\"sequence\">" + seq.substring(i, (i+num)) + "</td></tr>"
    }else {
      seqWrapped += "<tr><td></td><td class=\"sequence\">" + seq.substring(i) + "</td></tr>"
    }

    BlastVisualization.colorRegexReplacer(seqWrapped)
  }
}

