package models.results

import better.files._
import models.Constants
import play.twirl.api.Html
import play.api.Logger

import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex

object BlastVisualization extends Constants {



  private val color_regex = """(?:[WYF]+|[LIVM]+|[AST]+|[KR]+|[DE]+|[QN]+|H+|C+|P+|G+)""".r
  private val helix_pattern = """([Hh]+)""".r
  private val sheet_pattern = """([Ee]+)""".r
  private val helix_sheets = """([Hh]+|[Ee]+)""".r("ss")

  private val uniprotReg = """([A-Z0-9]{10}|[A-Z0-9]{6})""".r
  private val scopReg = """([defgh][0-9a-zA-Z\.\_]+)""".r
  private val mmcifReg = """(...._[a-zA-Z])""".r
  private val mmcifShortReg = """([0-9]+)""".r
  private val pfamReg = """(pfam[0-9]+|PF[0-9]+(\.[0-9]+)?)""".r
  private val ncbiReg = """[A-Z]{2}_?[0-9]+\.?\#?([0-9]+)?|[A-Z]{3}[0-9]{5}?\.[0-9]""".r

  private val envNrNameReg = """(env.*|nr.*)""".r
  private val pdbNameReg = """(pdb.*)""".r
  private val uniprotNameReg = """(uniprot.*)""".r
  private val pfamNameReg = """(Pfam.*)""".r

  private val pdbBaseLink = "http://pdb.rcsb.org/pdb/explore.do?structureId="
  private val pdbeBaseLink = "http://www.ebi.ac.uk/pdbe/entry/pdb/"
  private val ncbiBaseLink = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?SUBMIT=y&db=structure&orig_db=structure&term="
  private val ncbiProteinBaseLink = "https://www.ncbi.nlm.nih.gov/protein/"
  private val scopBaseLink = "http://scop.berkeley.edu/sid="
  private val pfamBaseLink = "http://pfam.xfam.org/family/"
  private val cddBaseLink = "http://www.ncbi.nlm.nih.gov/Structure/cdd/cddsrv.cgi?uid="
  private val uniprotBaseLik = "http://www.uniprot.org/uniprot/"


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


  def SSColorReplace(sequence: String): String = this.helix_sheets.replaceAllIn(sequence, { m =>
    m.group("ss") match {
      case this.helix_pattern(substr) => "<span class=\"ss_e\">" + substr + "</span>"
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
    val idTrimmed = if(id.length > 4){ id.substring (1, 5)} else{ id}
    val idPfam = id.replaceAll("am.*$||..*", "")
    val idPdb = id.replaceAll("_.*$", "")
    if(db == "scop") {
      link += generateLink(scopBaseLink, id, id)
    }
    else if(db == "mmcif") {
      link += generateLink(pdbBaseLink, idPdb, id)
    }
    else if(db == "pfam"){
      link += generateLink(pfamBaseLink, idPfam + "#tabview=tab0", id)
    }
    else if(db == "ncbi"){
      link += generateLink(ncbiProteinBaseLink, id, id)
    } else if(db == "uniprot"){
      link += generateLink(uniprotBaseLik,id,id)
    }

    else{
      link = id
    }
    Html(link)
  }


  def getLinks(id : String) : Html = {
    val db = identifyDatabase(id)
    var links = new ArrayBuffer[String]()
    var idNcbi = id.replaceAll("#", ".") + "?report=fasta"
    var idPdb = id.replaceAll("_.*$", "").toLowerCase
    val idTrimmed = if(id.length > 4){ id.substring (1, 5)} else{ id}
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

  def getSingleLinkDB(db: String, id: String): Html = {
    var link = ""
    val idTrimmed = if(id.length > 4){ id.substring (1, 5)} else{ id}
    val idPfam = id.replaceAll("am.*$||..*", "")
    val idPdb = id.replaceAll("_.*$", "")
    db match {
      case envNrNameReg(_) => link += generateLink(ncbiProteinBaseLink, id, id)
      case pdbNameReg(_) => link += generateLink(pdbBaseLink, idPdb, id)
      case uniprotNameReg(_) => link += generateLink(uniprotBaseLik,id,id)
      case pfamNameReg(_) => link += generateLink(pfamBaseLink, idPfam + "#tabview=tab0", id)
      case _ => link = id
    }
    Html(link)
  }

  def getLinksDB(db: String, id: String) : Html ={
    var links = new ArrayBuffer[String]()
    var idNcbi = id.replaceAll("#", ".") + "?report=fasta"
    var idPdb = id.replaceAll("_.*$", "").toLowerCase
    val idTrimmed = if(id.length > 4){ id.substring (1, 5)} else{ id}
    var idCDD = id.replaceAll("PF", "pfam")

    db match {
      case envNrNameReg(_) => links += generateLink(ncbiProteinBaseLink, idNcbi , "NCBI Fasta")
      case pdbNameReg(_) => links += generateLink(pdbeBaseLink, idPdb, "PDBe")
      case pfamNameReg(_) => {
        idCDD = idCDD.replaceAll("\\..*","")
        links += generateLink(cddBaseLink, idCDD, "CDD")
      }
      case uniprotNameReg(_) => ""
    }
    Html(links.mkString(" | "))
  }
  def getSingleLinkHHBlits(id: String) : Html ={
    var link = ""
    val idPdb = id.replaceAll("_.*$", "")
    link += generateLink(uniprotBaseLik,id,id)
    Html(link)
  }
  def getLinksHHBlits(id: String): Html = {
    Html("<a data-open=\"templateAlignmentModal\" onclick=\"templateAlignment(\'" + id + "\')\">Template alignment</a>")
  }

  def getLinksHHpred(id : String) : Html = {
    val db = identifyDatabase(id)
    var links = new ArrayBuffer[String]()

    var idPdb = id.replaceAll("_.*$", "").toLowerCase
    val idTrimmed = if(id.length > 4){ id.substring (1, 5)} else{ id}
    var idCDD = id.replaceAll("PF", "pfam")
    var idNcbi = id.replaceAll("#", ".") + "?report=fasta"
    links +=  "<a data-open=\"templateAlignmentModal\" onclick=\"templateAlignment(\'" + id + "\')\">Template alignment</a>"
    if(db == "scop") {
      links += "<a data-open=\"structureModal\" onclick=\"showStructure(\'" + id + "\')\";\">Template 3D structure</a>"
      links += generateLink (pdbBaseLink, idTrimmed, "PDB")
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

  def getLinksHmmer(id : String) : Html = {
    val db = identifyDatabase(id)
    var links = new ArrayBuffer[String]()
    var idNcbi = id.replaceAll("#", ".") + "?report=fasta"
    if (db == "ncbi"){
      links += generateLink(ncbiProteinBaseLink, idNcbi, "NCBI Fasta")
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
    case uniprotReg(_) => "uniprot"
    case e : String => Logger.info("Struc: ("+e+") could not be matched against any database!");""
    case e : String => Logger.info("Struc: ("+e+") could not be matched against any database!");""
  }

  def percentage(str : String) : String = {
    val num = str.toDouble
    val percent = (num * 100).toInt.toString + " %"
    percent
  }

  def calculatePercentage(num1_ : Int, num2_ : Int): String ={
    val num1 = num1_.toDouble
    val num2 = num2_.toDouble
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

    seqWrapped
  }


  def getCheckbox(num: Int): String ={
    "<input type=\"checkbox\" value=\""+num+"\"><a onclick=\"scrollToElem("+num+")\">"+num+"</a>"
  }

  def insertMatch (seq : String, length : Int, hitArr : List[Int]) : String = {
    var newSeq = ""
    for (starPos <- hitArr){
      val endPos = starPos+length
      newSeq += seq.substring(0, starPos) + "<span class=\"patternMatch\">" + seq.substring(starPos, endPos) + "</span>" + seq.substring(endPos)

    }
    newSeq.replaceAll("""\s""", "")
    newSeq
  }
}

