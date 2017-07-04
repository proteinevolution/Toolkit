package models.results


import better.files._
import models.Constants
import models.database.results.AlignmentResult
import play.twirl.api.Html
import play.api.Logger
import models.database.results._
import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex

object BlastVisualization extends Constants {

  private val color_regex = """(?:[WYF]+|[LIVM]+|[AST]+|[KR]+|[DE]+|[QN]+|H+|C+|P+|G+)""".r
  private val helix_pattern = """([Hh]+)""".r
  private val sheet_pattern = """([Ee]+)""".r
  private val helix_sheets = """([Hh]+|[Ee]+)""".r("ss")

  private val uniprotReg = """([A-Z0-9]{10}|[A-Z0-9]{6})""".r
  private val scopReg = """([defgh][0-9a-zA-Z\.\_]+)""".r
  private val mmcifReg = """(...._[0-9a-zA-Z][0-9a-zA-Z]?[0-9a-zA-Z]?[0-9a-zA-Z]?)""".r
  private val mmcifShortReg = """([0-9]+)""".r
  private val pfamReg = """(pfam[0-9]+|PF[0-9]+(\.[0-9]+)?)""".r
  private val ncbiReg = """[A-Z]{2}_?[0-9]+\.?\#?([0-9]+)?|[A-Z]{3}[0-9]{5}?\.[0-9]""".r

  private val envNrNameReg = """(env.*|nr.*)""".r
  private val pdbNameReg = """(pdb.*)""".r
  private val uniprotNameReg = """(uniprot.*)""".r
  private val pfamNameReg = """(Pfam.*)""".r

  private val pdbBaseLink = "http://pdb.rcsb.org/pdb/explore.do?structureId="
  private val pdbeBaseLink = "http://www.ebi.ac.uk/pdbe/entry/pdb/"
  private val ncbiBaseLink =
    "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?SUBMIT=y&db=structure&orig_db=structure&term="
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

  def SSColorReplace(sequence: String): String =
    this.helix_sheets.replaceAllIn(
      sequence, { m =>
        m.group("ss") match {
          case this.helix_pattern(substr) => "<span class=\"ss_e\">" + substr + "</span>"
          case this.sheet_pattern(substr) => "<span class=\"ss_h\">" + substr + "</span>"
        }
      }
    )

  def colorRegexReplacer(sequence: String): String =
    this.color_regex.replaceAllIn(sequence, { m =>
      "<span class=\"aa_" + m.toString().charAt(0) + "\">" + m.toString() + "</span>"
    })

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

  def getSingleLink(id: String): Html = {
    val db = identifyDatabase(id)
    var link = ""
    val idTrimmed = if (id.length > 4) {
      id.slice(1, 5)
    } else {
      id
    }
    val idPfam = id.replaceAll("am.*$||..*", "")
    val idPdb = id.replaceAll("_.*$", "")
    if (db == "scop") {
      link += generateLink(scopBaseLink, id, id)
    } else if (db == "mmcif") {
      link += generateLink(pdbBaseLink, idPdb, id)
    } else if (db == "pfam") {
      link += generateLink(pfamBaseLink, idPfam + "#tabview=tab0", id)
    } else if (db == "ncbi") {
      link += generateLink(ncbiProteinBaseLink, id, id)
    } else if (db == "uniprot") {
      link += generateLink(uniprotBaseLik, id, id)
    } else {
      link = id
    }
    Html(link)
  }

  def getLinks(id: String): Html = {
    val db = identifyDatabase(id)
    var links = new ArrayBuffer[String]()
    var idNcbi = id.replaceAll("#", ".") + "?report=fasta"
    var idPdb = id.replaceAll("_.*$", "").toLowerCase
    val idTrimmed = if (id.length > 4) {
      id.slice(1, 5)
    } else {
      id
    }
    var idCDD = id.replaceAll("PF", "pfam")
    if (db == "scop") {
      links += generateLink(scopBaseLink, id, "SCOP")
      links += generateLink(ncbiBaseLink, idTrimmed, "NCBI")
    } else if (db == "mmcif") {
      links += generateLink(pdbeBaseLink, idPdb, "PDBe")
    } else if (db == "pfam") {
      idCDD = idCDD.replaceAll("\\..*", "")
      links += generateLink(cddBaseLink, idCDD, "CDD")
    } else if (db == "ncbi") {
      links += generateLink(ncbiProteinBaseLink, idNcbi, "NCBI Fasta")
    }

    Html(links.mkString(" | "))
  }

  def getSingleLinkDB(db: String, id: String): Html = {
    var link = ""
    val idTrimmed = if (id.length > 4) {
      id.slice(1, 5)
    } else {
      id
    }
    val idPfam = id.replaceAll("am.*$||..*", "")
    val idPdb = id.replaceAll("_.*$", "")
    db match {
      case envNrNameReg(_) => link += generateLink(ncbiProteinBaseLink, id, id)
      case pdbNameReg(_) => link += generateLink(pdbBaseLink, idPdb, id)
      case uniprotNameReg(_) => link += generateLink(uniprotBaseLik, id, id)
      case pfamNameReg(_) => link += generateLink(pfamBaseLink, idPfam + "#tabview=tab0", id)
      case _ => link = id
    }
    Html(link)
  }

  def getLinksDB(db: String, id: String): Html = {
    var links = new ArrayBuffer[String]()
    var idNcbi = id.replaceAll("#", ".") + "?report=fasta"
    var idPdb = id.replaceAll("_.*$", "").toLowerCase
    val idTrimmed = if (id.length > 4) {
      id.slice(1, 5)
    } else {
      id
    }
    var idCDD = id.replaceAll("PF", "pfam")

    db match {
      case envNrNameReg(_) => links += generateLink(ncbiProteinBaseLink, idNcbi, "NCBI Fasta")
      case pdbNameReg(_) => links += generateLink(pdbeBaseLink, idPdb, "PDBe")
      case pfamNameReg(_) => {
        idCDD = idCDD.replaceAll("\\..*", "")
        links += generateLink(cddBaseLink, idCDD, "CDD")
      }
      case uniprotNameReg(_) => ""
    }
    Html(links.mkString(" | "))
  }

  def getSingleLinkHHBlits(id: String): Html = {
    var link = ""
    val idPdb = id.replaceAll("_.*$", "")
    link += generateLink(uniprotBaseLik, id, id)
    Html(link)
  }

  def getLinksHHBlits(id: String): Html = {
    Html(
      "<a data-open=\"templateAlignmentModal\" onclick=\"templateAlignment(\'" + id + "\')\">Template alignment</a>"
    )
  }

  def getLinksHHpred(id: String): Html = {
    val db = identifyDatabase(id)
    var links = new ArrayBuffer[String]()

    var idPdb = id.replaceAll("_.*$", "").toLowerCase
    val idTrimmed = if (id.length > 4) {
      id.slice(1, 5)
    } else {
      id
    }
    var idCDD = id.replaceAll("PF", "pfam")
    var idNcbi = id.replaceAll("#", ".") + "?report=fasta"
    links += "<a data-open=\"templateAlignmentModal\" onclick=\"templateAlignment(\'" + id + "\')\">Template alignment</a>"
    if (db == "scop") {
      links += "<a data-open=\"structureModal\" onclick=\"showStructure(\'" + id + "\')\";\">Template 3D structure</a>"
      links += generateLink(pdbBaseLink, idTrimmed, "PDB")
      links += generateLink(ncbiBaseLink, idTrimmed, "NCBI")
    } else if (db == "mmcif") {
      links += "<a data-open=\"structureModal\" onclick=\"showStructure(\'" + id + "\')\";\">Template 3D structure</a>"
      links += generateLink(pdbeBaseLink, idPdb, "PDBe")
    } else if (db == "pfam") {
      idCDD = idCDD.replaceAll("\\..*", "")
      links += generateLink(cddBaseLink, idCDD, "CDD")
    } else if (db == "ncbi") {
      links += generateLink(ncbiProteinBaseLink, idNcbi, "NCBI Fasta")
    }
    Html(links.mkString(" | "))
  }

  def getLinksHmmer(id: String): Html = {
    val db = identifyDatabase(id)
    var links = new ArrayBuffer[String]()
    var idNcbi = id.replaceAll("#", ".") + "?report=fasta"
    if (db == "ncbi") {
      links += generateLink(ncbiProteinBaseLink, idNcbi, "NCBI Fasta")
    }
    Html(links.mkString(" | "))
  }

  def generateLink(baseLink: String, id: String, name: String): String =
    "<a href='" + baseLink + id + "' target='_blank'>" + name + "</a>"

  def identifyDatabase(id: String): String = id match {
    case scopReg(_) => "scop"
    case mmcifShortReg(_) => "mmcif"
    case mmcifReg(_) => "mmcif"
    case pfamReg(_, _) => "pfam"
    case ncbiReg(_) => "ncbi"
    case uniprotReg(_) => "uniprot"
    case e: String => Logger.info("Struc: (" + e + ") could not be matched against any database!"); ""
  }

  def percentage(str: String): String = {
    val num = str.toDouble
    val percent = (num * 100).toInt.toString + "%"
    percent
  }

  def calculatePercentage(num1_ : Int, num2_ : Int): String = {
    val num1 = num1_.toDouble
    val num2 = num2_.toDouble
    val percent = ((num1 / num2) * 100).toInt.toString + "%"
    percent
  }

  def wrapSequence(seq: String, num: Int): String = {
    var seqWrapped = ""
    for {i <- 0 to seq.length if i % num == 0} if (i + num < seq.length) {
      seqWrapped += "<tr><td></td><td class=\"sequence\">" + seq.slice(i, (i + num)) + "</td></tr>"
    } else {
      seqWrapped += "<tr><td></td><td class=\"sequence\">" + seq.substring(i) + "</td></tr>"
    }

    seqWrapped
  }

  def getCheckbox(num: Int): String = {
    "<input type=\"checkbox\" value=\"" + num + "\" name=\"alignment_elem\" class=\"checkbox\"><a onclick=\"scrollToElem(" + num + ")\">" + num + "</a>"
  }

  def addBreak(description: String): String = {
    description.replaceAll("(\\S{40})", "$1</br>");
  }

  def insertMatch(seq: String, length: Int, hitArr: List[Int]): String = {
    var newSeq = ""
    for (starPos <- hitArr) {
      val endPos = starPos + length
      newSeq += seq.slice(0, starPos) + "<span class=\"patternMatch\">" + seq.slice(starPos, endPos) + "</span>" + seq
        .substring(endPos)

    }
    newSeq.replaceAll("""\s""", "")
    newSeq
  }

  def clustal(alignment: AlignmentResult, begin: Int, breakAfter: Int, color: Boolean): String = {
    if (begin >= alignment.alignment.head.seq.length) {
      return ""
    } else {
      var string = alignment.alignment.map { elem =>
        "<tr>" +
          "<td>" +
          "<input type=\"checkbox\" value=\"" + elem.num + "\" name=\"alignment_elem\" class=\"checkbox\"><b>" +
          "</b><td>" +
          "<b><span class='clustalAcc'>" + elem.accession.take(20) + "</span></b><br />" +
          "</td>" +
          "</td>" +
          "<td class=\"sequence\">" + {
          if (color) colorRegexReplacer(elem.seq.slice(begin, Math.min(begin + breakAfter, elem.seq.length))) else elem.seq.slice(begin, Math.min(begin + breakAfter, elem.seq.length))
        } +
          "</td>" +
          "</tr>"
      }
        return {
          string.mkString + "<tr class=\"blank_row\"><td colspan=\"3\"></td></tr><tr class=\"blank_row\"><td colspan=\"3\"></td></tr>" + clustal(alignment, begin + breakAfter, breakAfter, color)
        }
    }
  }

  def hmmerHitWrapped(hit: HmmerHSP, charCount: Int, breakAfter: Int, beginQuery: Int, beginTemplate: Int): String ={
    if (charCount >= hit.hit_len){
      return ""
    }
    else {
      val query = hit.query_seq.slice(charCount, Math.min(charCount + breakAfter, hit.query_seq.length))
      val midline = hit.midline.slice(charCount, Math.min(charCount + breakAfter, hit.midline.length))
      val template = hit.hit_seq.slice(charCount, Math.min(charCount + breakAfter, hit.hit_seq.length))
      val queryEnd = lengthWithoutDashDots(query)
      val templateEnd = lengthWithoutDashDots(template)
      if (beginQuery == beginQuery + queryEnd) {
        return ""
      } else {
        return {
          "<tr class='sequence'><td></td><td>Q " + (beginQuery + 1) + "</td><td>" + query + "   " + (beginQuery + queryEnd) + "</td></tr>" +
            "<tr class='sequence'><td></td><td></td><td>" + midline + "</td></tr>" +
            "<tr class='sequence'><td></td><td>T " + (beginTemplate  + 1) + "</td><td>" + template + "   " + (beginTemplate + templateEnd) + "</td></tr>" +
            "<tr class=\"blank_row\"><td colspan=\"3\"></td></tr>" + "<tr class=\"blank_row\"><td colspan=\"3\"></td></tr>" +
            hmmerHitWrapped(hit, charCount + breakAfter, breakAfter, beginQuery + queryEnd, beginTemplate + templateEnd)
        }
      }
    }
  }

  def psiblastHitWrapped(hit: PSIBlastHSP, charCount: Int, breakAfter: Int, beginQuery: Int, beginTemplate: Int): String ={
    if (charCount >= hit.hit_len){
      return ""
    }
    else {
      val query = hit.query_seq.slice(charCount, Math.min(charCount + breakAfter, hit.query_seq.length))
      val midline = hit.midline.slice(charCount, Math.min(charCount + breakAfter, hit.midline.length))
      val template = hit.hit_seq.slice(charCount, Math.min(charCount + breakAfter, hit.hit_seq.length))
      val queryEnd = lengthWithoutDashDots(query)
      val templateEnd = lengthWithoutDashDots(template)
      if (beginQuery == beginQuery + queryEnd) {
        return ""
      } else {
        return {
          "<tr class='sequence'><td></td><td>Q " + beginQuery + "</td><td>" + query + "  " + (beginQuery + queryEnd - 1) + "</td></tr>" +
            "<tr class='sequence'><td></td><td></td><td>" + midline + "</td></tr>" +
            "<tr class='sequence'><td></td><td>T " + beginTemplate + "</td><td>" + template + "  " + (beginTemplate + templateEnd - 1) + "</td></tr>" +
            "<tr class=\"blank_row\"><td colspan=\"3\"></td></tr>" + "<tr class=\"blank_row\"><td colspan=\"3\"></td></tr>" +
            psiblastHitWrapped(hit, charCount + breakAfter, breakAfter, beginQuery + queryEnd, beginTemplate + templateEnd)
        }
      }
    }
  }


  def lengthWithoutDashDots(str : String): Int ={
    str.length-str.count(char =>  char == '-' ||  char == ".")
  }

  def hhblitsHitWrapped(hit: HHBlitsHSP, charCount: Int, breakAfter: Int, beginQuery: Int, beginTemplate: Int): String ={
    if (charCount >= hit.length){
      return ""
    }
    else {
      val query = hit.query.seq.slice(charCount, Math.min(charCount + breakAfter, hit.query.seq.length))
      val queryCons = hit.query.consensus.slice(charCount, Math.min(charCount + breakAfter, hit.query.consensus.length))
      val midline = hit.agree.slice(charCount, Math.min(charCount + breakAfter, hit.agree.length))
      val templateCons = hit.template.consensus.slice(charCount, Math.min(charCount + breakAfter, hit.template.consensus.length))
      val template = hit.template.seq.slice(charCount, Math.min(charCount + breakAfter, hit.template.seq.length))
      val queryEnd = lengthWithoutDashDots(query)
      val templateEnd = lengthWithoutDashDots(template)
      if (beginQuery == beginQuery + queryEnd) {
        return ""
      } else {
        return {
          "<tr class='sequence'><td></td><td>Q " +  hit.query.accession + "</td><td>" + beginQuery + "</td><td>" + query + "  " + (beginQuery + queryEnd - 1) + " (" + hit.query.ref + ")" + "</td></tr>" +
            "<tr class='sequence'><td></td><td>Q Consensus " + "</td><td>" + beginQuery + "</td><td>" + queryCons + "  " + (beginQuery + queryEnd - 1) + " (" + hit.query.ref + ")" + "</td></tr>" +
            "<tr class='sequence'><td></td><td></td><td></td><td>" + midline + "</td></tr>" +
            "<tr class='sequence'><td></td><td>T Consensus " + "</td><td>" + beginTemplate + "</td><td>" + templateCons + "  " + (beginTemplate + templateEnd - 1) + " (" + hit.template.ref + ")" + "</td></tr>" +
            "<tr class='sequence'><td></td><td>T " + hit.template.accession + "</td><td>" + beginTemplate + "</td><td>" + template + "  " + (beginTemplate + templateEnd - 1) + " (" + hit.template.ref + ")" + "</td></tr>" +
            "<tr class=\"blank_row\"><td colspan=\"3\"></td></tr>" + "<tr class=\"blank_row\"><td colspan=\"3\"></td></tr>" +
            hhblitsHitWrapped(hit, charCount + breakAfter, breakAfter, beginQuery + queryEnd, beginTemplate + templateEnd)
        }
      }
    }
  }

  def hhpredHitWrapped(hit: HHPredHSP, charCount: Int, breakAfter: Int, beginQuery: Int, beginTemplate: Int, color: Boolean): String ={
    if (charCount >= hit.length){
      return ""
    }
    else {
      val querySSDSSP = hit.query.ss_dssp.slice(charCount, Math.min(charCount + breakAfter, hit.query.ss_dssp.length))
      val querySSPRED = hit.query.ss_pred.slice(charCount, Math.min(charCount + breakAfter, hit.query.ss_pred.length))
      val query = hit.query.seq.slice(charCount, Math.min(charCount + breakAfter, hit.query.seq.length))
      val queryCons = hit.query.consensus.slice(charCount, Math.min(charCount + breakAfter, hit.query.consensus.length))
      val midline = hit.agree.slice(charCount, Math.min(charCount + breakAfter, hit.agree.length))
      val templateCons = hit.template.consensus.slice(charCount, Math.min(charCount + breakAfter, hit.template.consensus.length))
      val template = hit.template.seq.slice(charCount, Math.min(charCount + breakAfter, hit.template.seq.length))
      val templateSSDSSP = hit.template.ss_dssp.slice(charCount, Math.min(charCount + breakAfter, hit.template.ss_dssp.length))
      val templateSSPRED = hit.template.ss_pred.slice(charCount, Math.min(charCount + breakAfter, hit.template.ss_pred.length))
      val confidence = hit.confidence.slice(charCount, Math.min(charCount + breakAfter, hit.confidence.length))
      val queryEnd = lengthWithoutDashDots(query)
      val templateEnd = lengthWithoutDashDots(template)

      if (beginQuery == beginQuery + queryEnd) {
        return ""
      } else {

          var html = ""
          if(!querySSPRED.isEmpty) {
          html +=" <tr class='sequence'><td></td><td>Q ss_pred" + "</td><td>" + "</td><td>" + BlastVisualization.SSColorReplace(querySSPRED) + "</td></tr>"
          }
          if(!querySSDSSP.isEmpty) {
            html += "<tr class='sequence'><td></td><td>Q ss_dssp" + "</td><td>" + "</td><td>" + BlastVisualization.SSColorReplace(querySSDSSP) + "</td></tr>"
          }
        html +="<tr class='sequence'><td></td><td>Q " +  hit.query.accession + "</td><td>" + beginQuery + "</td><td>" + {if(color) colorRegexReplacer(query) else query} + "  " + (beginQuery + queryEnd - 1) + " (" + hit.query.ref + ")" + "</td></tr>" +
         "<tr class='sequence'><td></td><td>Q Consensus " + "</td><td>" + beginQuery + "</td><td>" + queryCons + "  " + (beginQuery + queryEnd - 1) + " (" + hit.query.ref + ")" + "</td></tr>" +
            "<tr class='sequence'><td></td><td></td><td></td><td>" + midline + "</td></tr>" +
            "<tr class='sequence'><td></td><td>T Consensus " + "</td><td>" + beginTemplate + "</td><td>" + templateCons + "  " + (beginTemplate + templateEnd - 1) + " (" + hit.template.ref + ")" + "</td></tr>" +
            "<tr class='sequence'><td></td><td>T " + hit.template.accession + "</td><td>" + beginTemplate + "</td><td>" + {if(color) colorRegexReplacer(template) else template} + "  " + (beginTemplate + templateEnd - 1) + " (" + hit.template.ref + ")" + "</td></tr>"
        if(!templateSSDSSP.isEmpty) {
          html += "<tr class='sequence'><td></td><td>T ss_dssp" + "</td><td>" + "</td><td>" + BlastVisualization.SSColorReplace(templateSSDSSP) + "</td></tr>"
        }
        if(!templateSSPRED.isEmpty) {
          html +=" <tr class='sequence'><td></td><td>T ss_pred" + "</td><td>" + "</td><td>" + BlastVisualization.SSColorReplace(templateSSPRED) + "</td></tr>"
        }
        if(!confidence.isEmpty) {
          html +=" <tr class='sequence'><td></td><td>Confidence" + "</td><td>" + "</td><td>" + confidence + "</td></tr>"
        }

        html += "<tr class=\"blank_row\"><td colspan=\"3\"></td></tr>" + "<tr class=\"blank_row\"><td colspan=\"3\"></td></tr>"

            return html + hhpredHitWrapped(hit, charCount + breakAfter, breakAfter, beginQuery + queryEnd, beginTemplate + templateEnd, color)
      }
    }
  }


}
