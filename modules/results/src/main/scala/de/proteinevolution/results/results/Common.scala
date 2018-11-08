package de.proteinevolution.results.results

import play.api.Logger
import play.twirl.api.Html

import scala.collection.mutable.ArrayBuffer

object Common {

  private val logger = Logger(this.getClass)

  private val color_regex     = """(?:[WYF]+|[LIVM]+|[AST]+|[KR]+|[DE]+|[QN]+|H+|C+|P+|G+)""".r
  private val CC_pattern      = """(C+)""".r("C")
  private val TM_pattern      = """(M+)""".r("M")
  private val DO_pattern      = """(D+)""".r("D")
  private val PIHELIX_pattern = """(I+)""".r("I")
  private val helix_pattern   = """([Hh]+)""".r
  private val sheet_pattern   = """([Ee]+)""".r
  private val helix_sheets    = """([Hh]+|[Ee]+)""".r("ss")

  private val uniprotReg    = """([A-Z0-9]{10}|[A-Z0-9]{6})""".r
  private val scopReg       = """([defgh][0-9a-zA-Z\.\_]+)""".r
  private val smartReg      = """(^SM0[0-9]{4})""".r
  private val ncbiCDReg     = """(^[cs]d[0-9]{5})""".r
  private val cogkogReg     = """(^[CK]OG[0-9]{4})""".r
  private val tigrReg       = """(^TIGR[0-9]{5})""".r
  private val prkReg        = """(CHL|MTH|PHA|PLN|PTZ|PRK)[0-9]{5}""".r
  private val mmcifReg      = """(...._[0-9a-zA-Z][0-9a-zA-Z]?[0-9a-zA-Z]?[0-9a-zA-Z]?)""".r
  private val mmcifShortReg = """([0-9]+)""".r
  private val pfamReg       = """(pfam[0-9]+|PF[0-9]+(\.[0-9]+)?)""".r
  private val ncbiReg       = """[A-Z]{2}_?[0-9]+\.?\#?([0-9]+)?|[A-Z]{3}[0-9]{5}?\.[0-9]""".r
  private val ecodReg       = """(ECOD_[0-9]+)_.*""".r

  private val envNrNameReg   = """(env.*|nr.*)""".r
  private val pdbNameReg     = """(pdb.*)""".r
  private val uniprotNameReg = """(uniprot.*)""".r
  private val pfamNameReg    = """(Pfam.*)""".r

  private val pdbBaseLink = "http://www.rcsb.org/pdb/explore/explore.do?structureId="

  private val pdbeBaseLink = "http://www.ebi.ac.uk/pdbe/entry/pdb/"
  private val ncbiBaseLink =
    "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?SUBMIT=y&db=structure&orig_db=structure&term="
  private val ncbiProteinBaseLink = "https://www.ncbi.nlm.nih.gov/protein/"
  private val scopBaseLink        = "http://scop.berkeley.edu/sid="
  private val pfamBaseLink        = "http://pfam.xfam.org/family/"
  private val cddBaseLink         = "http://www.ncbi.nlm.nih.gov/Structure/cdd/cddsrv.cgi?uid="
  private val uniprotBaseLik      = "http://www.uniprot.org/uniprot/"
  private val smartBaseLink       = "http://smart.embl-heidelberg.de/smart/do_annotation.pl?DOMAIN="
  private val ecodBaseLink        = "http://prodata.swmed.edu/ecod/complete/domain/"

  private val emptyRow = "<tr class=\"blank_row\"><td colspan=\"3\"></td></tr>"

  def SSColorReplace(sequence: String): String =
    helix_sheets.replaceAllIn(
      sequence, { m =>
        m.group("ss") match {
          case helix_pattern(substr) => "<span class=\"ss_e\">" + substr + "</span>"
          case sheet_pattern(substr) => "<span class=\"ss_h\">" + substr + "</span>"
        }
      }
    )

  def Q2DColorReplace(name: String, sequence: String): String =
    name match {
      case "psipred" =>
        helix_sheets.replaceAllIn(
          sequence, { m =>
            m.group("ss") match {
              case helix_pattern(substr) => "<span class=\"ss_h_b\">" + substr + "</span>"
              case sheet_pattern(substr) => "<span class=\"ss_e_b\">" + substr + "</span>"
            }
          }
        )
      case "spider2" =>
        helix_sheets.replaceAllIn(
          sequence, { m =>
            m.group("ss") match {
              case helix_pattern(substr) => "<span class=\"ss_h_b\">" + substr + "</span>"
              case sheet_pattern(substr) => "<span class=\"ss_e_b\">" + substr + "</span>"
            }
          }
        )
      case "psspred" =>
        helix_sheets.replaceAllIn(
          sequence, { m =>
            m.group("ss") match {
              case helix_pattern(substr) => "<span class=\"ss_h_b\">" + substr + "</span>"
              case sheet_pattern(substr) => "<span class=\"ss_e_b\">" + substr + "</span>"
            }
          }
        )
      case "deepcnf" =>
        helix_sheets.replaceAllIn(
          sequence, { m =>
            m.group("ss") match {
              case helix_pattern(substr) => "<span class=\"ss_h_b\">" + substr + "</span>"
              case sheet_pattern(substr) => "<span class=\"ss_e_b\">" + substr + "</span>"
            }
          }
        )
      case "marcoil"     => CC_pattern.replaceAllIn(sequence, "<span class=\"CC_b\">" + "$1" + "</span>")
      case "coils"       => CC_pattern.replaceAllIn(sequence, "<span class=\"CC_b\">" + "$1" + "</span>")
      case "pcoils"      => CC_pattern.replaceAllIn(sequence, "<span class=\"CC_b\">" + "$1" + "</span>")
      case "tmhmm"       => TM_pattern.replaceAllIn(sequence, "<span class=\"CC_m\">" + "$1" + "</span>")
      case "phobius"     => TM_pattern.replaceAllIn(sequence, "<span class=\"CC_m\">" + "$1" + "</span>")
      case "polyphobius" => TM_pattern.replaceAllIn(sequence, "<span class=\"CC_m\">" + "$1" + "</span>")
      case "spotd"       => DO_pattern.replaceAllIn(sequence, "<span class=\"CC_do\">" + "$1" + "</span>")
      case "iupred"      => DO_pattern.replaceAllIn(sequence, "<span class=\"CC_do\">" + "$1" + "</span>")
      case "disopred3"   => DO_pattern.replaceAllIn(sequence, "<span class=\"CC_do\">" + "$1" + "</span>")
      case "pipred"      => PIHELIX_pattern.replaceAllIn(sequence, "<span class=\"ss_pihelix\">" + "$1" + "</span>")

    }

  def colorRegexReplacer(sequence: String): String =
    color_regex.replaceAllIn(sequence, { m =>
      "<span class=\"aa_" + m.toString().charAt(0) + "\">" + m.toString() + "</span>"
    })

  def Highlight(sequence: String): String = {
    "<span class=\"sequenceBold\">" + sequence + "</span>"
  }

  def makeRow(rowClass: String, entries: Array[String]): String = {
    val DOMElement = for (entry <- entries) yield {
      "<td>" + entry.toString + "</td>"
    }
    "<tr class='" + rowClass + "'>" + DOMElement.mkString("") + "</tr>"
  }

  /* GENERATING LINKS FOR HHPRED */

  def getSingleLink(id: String): Html = {
    val db     = identifyDatabase(id)
    val idPfam = id.replaceAll("am.*$||..*", "")
    val idPdb  = id.replaceAll("_.*$", "")
    val link = db match {
      case "scop"    => generateLink(scopBaseLink, id, id)
      case "mmcif"   => generateLink(pdbBaseLink, idPdb, id)
      case "prk"     => generateLink(cddBaseLink, id, id)
      case "ncbicd"  => generateLink(cddBaseLink, id, id)
      case "cogkog"  => generateLink(cddBaseLink, id, id)
      case "tigr"    => generateLink(cddBaseLink, id, id)
      case "pfam"    => generateLink(pfamBaseLink, idPfam + "#tabview=tab0", id)
      case "ncbi"    => generateLink(ncbiProteinBaseLink, id, id)
      case "uniprot" => generateLink(uniprotBaseLik, id, id)
      case "smart"   => generateLink(smartBaseLink, id, id)
      case "ecod"    => val idEcod = id.slice(5, 14); generateLink(ecodBaseLink, idEcod, id)
      case _         => id
    }
    Html(link)
  }

  def getLinks(id: String): Html = {
    val db     = identifyDatabase(id)
    val idNcbi = id.replaceAll("#", ".") + "?report=fasta"
    val idPdb  = id.replaceAll("_.*$", "").toLowerCase
    val idTrimmed = if (id.length > 4) {
      id.slice(1, 5)
    } else {
      id
    }
    val idCDD = id.replaceAll("PF", "pfam").replaceAll("\\..*", "")
    val links = db match {
      case "scop"  => generateLink(scopBaseLink, id, "SCOP") + " | " + generateLink(ncbiBaseLink, idTrimmed, "NCBI")
      case "mmcif" => generateLink(pdbeBaseLink, idPdb, "PDBe")
      case "pfam"  => generateLink(cddBaseLink, idCDD, "CDD")
      case "ncbi"  => generateLink(ncbiProteinBaseLink, idNcbi, "NCBI Fasta")
    }
    Html(links)
  }

  def getSingleLinkDB(db: String, id: String): Html = {
    val idPfam = id.replaceAll("am.*$||..*", "")
    val idPdb  = id.replaceAll("_.*$", "")
    val link = db match {
      case envNrNameReg(_)   => generateLink(ncbiProteinBaseLink, id, id)
      case pdbNameReg(_)     => generateLink(pdbBaseLink, idPdb, id)
      case uniprotNameReg(_) => generateLink(uniprotBaseLik, id, id)
      case pfamNameReg(_)    => generateLink(pfamBaseLink, idPfam + "#tabview=tab0", id)
      case _                 => id
    }
    Html(link)
  }

  def getLinksDB(db: String, id: String): Html = {
    val idNcbi = id.replaceAll("#", ".") + "?report=fasta"
    val idPdb  = id.replaceAll("_.*$", "").toLowerCase
    val idCDD  = id.replaceAll("PF", "pfam").replaceAll("\\..*", "")
    val links = db match {
      case envNrNameReg(_)   => generateLink(ncbiProteinBaseLink, idNcbi, "NCBI Fasta")
      case pdbNameReg(_)     => generateLink(pdbeBaseLink, idPdb, "PDBe")
      case pfamNameReg(_)    => generateLink(cddBaseLink, idCDD, "CDD")
      case uniprotNameReg(_) => ""
    }
    Html(links)
  }

  def getSingleLinkHHBlits(id: String): Html = {
    Html(generateLink(uniprotBaseLik, id, id))
  }

  def getLinksHHBlits(jobID: String, id: String): Html = {
    Html(
      s"<a data-open='templateAlignmentModal' onclick='new TemplateAlignment(${'"'}hhblits${'"'}).get(${'"'}$jobID${'"'},${'"'}$id${'"'})'>Template alignment</a>"
    )
  }

  def getLinksHHpred(jobID: String, id: String): Html = {
    val db    = identifyDatabase(id)
    val links = new ArrayBuffer[String]()
    val idPdb = id.replaceAll("_.*$", "").toLowerCase
    val idTrimmed = if (id.length > 4) {
      id.slice(1, 5)
    } else {
      id
    }
    val idCDD  = id.replaceAll("PF", "pfam")
    val idNcbi = id.replaceAll("#", ".") + "?report=fasta"
    links += s"<a data-open='templateAlignmentModal' onclick='new TemplateAlignment(${'"'}hhpred${'"'}).get(${'"'}$jobID${'"'},${'"'}$id${'"'})'>Template alignment</a>"
    db match {
      case "scop" =>
        links += s"<a class='structureModalOpenBtn' data-structure-id='$id'>Template 3D structure</a>"
        links += generateLink(pdbBaseLink, idTrimmed, "PDB")
        links += generateLink(ncbiBaseLink, idTrimmed, "NCBI")
      case "ecod" =>
        val idPdbEcod = id.slice(16, 20)
        links += s"<a class='structureModalOpenBtn' data-structure-id='$id'>Template 3D structure</a>"
        links += generateLink(pdbBaseLink, idPdbEcod, "PDB")
      case "mmcif" =>
        links += s"<a class='structureModalOpenBtn' data-structure-id='$id'>Template 3D structure</a>"
        links += generateLink(pdbeBaseLink, idPdb, "PDBe")
      case "pfam" =>
        val idCDDPfam = idCDD.replaceAll("\\..*", "")
        links += generateLink(cddBaseLink, idCDDPfam, "CDD")
      case "ncbi" =>
        links += generateLink(ncbiProteinBaseLink, idNcbi, "NCBI Fasta")
      case _ => ()
    }
    Html(links.mkString(" | "))
  }

  def getLinksHmmer(id: String): Html = {
    val db     = identifyDatabase(id)
    val idNcbi = id.replaceAll("#", ".") + "?report=fasta"
    val link = db match {
      case "ncbi" => generateLink(ncbiProteinBaseLink, idNcbi, "NCBI Fasta")
      case _      => ""
    }
    Html(link)
  }

  def generateLink(baseLink: String, id: String, name: String): String =
    "<a href='" + baseLink + id + "' target='_blank'>" + name + "</a>"

  def identifyDatabase(id: String): String = id match {
    case scopReg(_)       => "scop"
    case mmcifShortReg(_) => "mmcif"
    case mmcifReg(_)      => "mmcif"
    case prkReg(_)        => "prk"
    case ncbiCDReg(_)     => "ncbicd"
    case cogkogReg(_)     => "cogkog"
    case tigrReg(_)       => "tigr"
    case smartReg(_)      => "smart"
    case pfamReg(_, _)    => "pfam"
    case uniprotReg(_)    => "uniprot"
    case ecodReg(_)       => "ecod"
    case ncbiReg(_)       => "ncbi"
    case e: String =>
      logger.info("Struc: (" + e + ") could not be matched against any database!")
      ""
  }

  def percentage(str: String): String = (str.toDouble * 100).toInt.toString + "%"

  def calculatePercentage(num1_ : Int, num2_ : Int): String =
    ((num1_.toDouble / num2_.toDouble) * 100).toInt.toString + "%"

  def wrapSequence(seq: String, num: Int): String = {
    (0 to seq.length)
      .filter(_ % num == 0)
      .map {
        case x if x + num < seq.length => makeRow("sequence", Array("", seq.slice(x, x + num)))
        case x                         => makeRow("sequence", Array("", seq.substring(x)))
      }
      .mkString("")
  }

  def getCheckbox(num: Int): String = {
    "<div class=\"nowrap\"><input type=\"checkbox\" data-id=\"" + num + "\" value=\"" + num +
    "\" name=\"alignment_elem\" class=\"checkbox\"><a onclick=\"Toolkit.resultView.scrollToHit(" + num + ")\">" +
    num + "</a></div>"
  }

  def getAddScrollLink(num: Int): String = {
    "<a onclick=\"Toolkit.resultView.scrollToHit(" + num + ")\">" + num + "</a>"
  }

  def addBreak(description: String): String = {
    description.replaceAll("(\\S{40})", "$1</br>")
  }

  def addBreakHHpred(description: String): String = {
    val index = description.indexOfSlice("; Related PDB entries")
    if (index > 1)
      description.slice(0, index).replaceAll("(\\S{40})", "$1</br>")
    else
      description.replaceAll("(\\S{40})", "$1</br>")
  }

  def insertMatch(seq: String, length: Int, hitArr: List[Int]): String = {
    val inserted = for (starPos <- hitArr) yield {
      seq.slice(0, starPos) + "<span class=\"patternMatch\">" + seq.slice(starPos, starPos + length) + "</span>" + seq
        .substring(starPos + length)
    }
    inserted.mkString("")
  }

  def clustal(alignment: AlignmentResult, begin: Int, breakAfter: Int, color: Boolean): String = {
    if (begin >= alignment.alignment.head.seq.length) {
      ""
    } else {
      val string = alignment.alignment.map { elem =>
        "<tr>" +
        "<td>" +
        "<input type=\"checkbox\" data-id=\"" + elem.num + "\" value=\"" + elem.num + "\" name=\"alignment_elem\" class=\"checkbox\"><b>" +
        "</b><td>" +
        "<b><span class='clustalAcc'>" + elem.accession.take(20) + "</span></b><br />" +
        "</td>" +
        "</td>" +
        "<td class=\"sequence\">" + {
          if (color) colorRegexReplacer(elem.seq.slice(begin, Math.min(begin + breakAfter, elem.seq.length)))
          else elem.seq.slice(begin, Math.min(begin + breakAfter, elem.seq.length))
        } +
        "</td>" +
        "</tr>"
      }
      string.mkString + emptyRow + emptyRow + clustal(alignment, begin + breakAfter, breakAfter, color)
    }
  }

  def hmmerHitWrapped(hit: HmmerHSP, charCount: Int, breakAfter: Int, beginQuery: Int, beginTemplate: Int): String = {
    if (charCount >= hit.hit_len) {
      ""
    } else {
      val query       = hit.query_seq.slice(charCount, Math.min(charCount + breakAfter, hit.query_seq.length))
      val midline     = hit.midline.slice(charCount, Math.min(charCount + breakAfter, hit.midline.length))
      val template    = hit.hit_seq.slice(charCount, Math.min(charCount + breakAfter, hit.hit_seq.length))
      val queryEnd    = lengthWithoutDashDots(query)
      val templateEnd = lengthWithoutDashDots(template)
      if (beginQuery == beginQuery + queryEnd) {
        ""
      } else {
        makeRow("sequence", Array("", "Q " + (beginQuery + 1), query + "   " + (beginQuery + queryEnd))) +
        makeRow("sequence", Array("", "", midline)) +
        makeRow("sequence", Array("", "T " + (beginTemplate + 1), template + "   " + (beginTemplate + templateEnd))) +
        emptyRow + emptyRow +
        hmmerHitWrapped(hit, charCount + breakAfter, breakAfter, beginQuery + queryEnd, beginTemplate + templateEnd)
      }
    }
  }

  def psiblastHitWrapped(
      hit: PSIBlastHSP,
      charCount: Int,
      breakAfter: Int,
      beginQuery: Int,
      beginTemplate: Int
  ): String = {
    if (charCount >= hit.hit_len) {
      ""
    } else {
      val query       = hit.query_seq.slice(charCount, Math.min(charCount + breakAfter, hit.query_seq.length))
      val midline     = hit.midLine.slice(charCount, Math.min(charCount + breakAfter, hit.midLine.length))
      val template    = hit.hit_seq.slice(charCount, Math.min(charCount + breakAfter, hit.hit_seq.length))
      val queryEnd    = lengthWithoutDashDots(query)
      val templateEnd = lengthWithoutDashDots(template)
      if (beginQuery == beginQuery + queryEnd) {
        ""
      } else {
        makeRow("sequence", Array("", "Q " + beginQuery, query + "   " + (beginQuery + queryEnd - 1))) +
        makeRow("sequence", Array("", "", midline)) +
        makeRow("sequence", Array("", "T " + beginTemplate, template + "   " + (beginTemplate + templateEnd - 1))) +
        emptyRow + emptyRow +
        psiblastHitWrapped(hit, charCount + breakAfter, breakAfter, beginQuery + queryEnd, beginTemplate + templateEnd)
      }
    }
  }

  def lengthWithoutDashDots(str: String): Int = {
    str.length - str.count(char => char == '-') - str.count(char => char == '.')
  }

  def hhblitsHitWrapped(
      hit: HHBlitsHSP,
      charCount: Int,
      breakAfter: Int,
      beginQuery: Int,
      beginTemplate: Int
  ): String = {
    if (charCount >= hit.length) {
      ""
    } else {
      val query = hit.query.seq.slice(charCount, Math.min(charCount + breakAfter, hit.query.seq.length))
      val queryCons =
        hit.query.consensus.slice(charCount, Math.min(charCount + breakAfter, hit.query.consensus.length))
      val midline = hit.agree.slice(charCount, Math.min(charCount + breakAfter, hit.agree.length))
      val templateCons =
        hit.template.get.consensus.slice(charCount, Math.min(charCount + breakAfter, hit.template.get.consensus.length))
      val template =
        hit.template.get.seq.slice(charCount, Math.min(charCount + breakAfter, hit.template.get.seq.length))
      val queryEnd    = lengthWithoutDashDots(query)
      val templateEnd = lengthWithoutDashDots(template)
      if (beginQuery == beginQuery + queryEnd) {
        ""
      } else {
        makeRow(
          "sequence",
          Array("", "Q " + beginQuery, query + "  " + (beginQuery + queryEnd - 1) + " (" + hit.query.ref + ")")
        ) +
        makeRow("sequence", Array("", "", queryCons)) +
        makeRow("sequence", Array("", "", midline)) +
        makeRow("sequence", Array("", "", templateCons)) +
        makeRow("sequence",
                Array("",
                      "T " + beginTemplate,
                      template + "  " + (beginTemplate + templateEnd - 1) + " (" + hit.template.get.ref + ")")) +
        emptyRow + emptyRow +
        hhblitsHitWrapped(hit, charCount + breakAfter, breakAfter, beginQuery + queryEnd, beginTemplate + templateEnd)
      }
    }
  }

  def hhpredHitWrapped(
      hit: HHPredHSP,
      charCount: Int,
      breakAfter: Int,
      beginQuery: Int,
      beginTemplate: Int,
      color: Boolean
  ): String = {
    if (charCount >= hit.length) {
      ""
    } else {
      val querySSDSSP = hit.query.ss_dssp.map(s => s.slice(charCount, Math.min(charCount + breakAfter, s.length)))
      val querySSPRED = hit.query.ss_pred.map(s => s.slice(charCount, Math.min(charCount + breakAfter, s.length)))
      val query       = hit.query.seq.slice(charCount, Math.min(charCount + breakAfter, hit.query.seq.length))
      val queryCons =
        hit.query.consensus.slice(charCount, Math.min(charCount + breakAfter, hit.query.consensus.length))
      val midline = hit.agree.slice(charCount, Math.min(charCount + breakAfter, hit.agree.length))
      val templateCons =
        hit.template.map(t => t.consensus.slice(charCount, Math.min(charCount + breakAfter, t.consensus.length)))
      val template = hit.template.map(t => t.seq.slice(charCount, Math.min(charCount + breakAfter, t.seq.length)))
      val templateSSDSSP =
        hit.template.map(t => t.ss_dssp.slice(charCount, Math.min(charCount + breakAfter, t.ss_dssp.length)))
      val templateSSPRED =
        hit.template.map(t => t.ss_pred.slice(charCount, Math.min(charCount + breakAfter, t.ss_pred.length)))
      val confidence  = hit.confidence.slice(charCount, Math.min(charCount + breakAfter, hit.confidence.length))
      val queryEnd    = lengthWithoutDashDots(query)
      val templateEnd = lengthWithoutDashDots(template.getOrElse(""))

      if (beginQuery == beginQuery + queryEnd) {
        ""
      } else {
        var html = ""
        if (querySSPRED.get.nonEmpty) {
          html += makeRow("sequence", Array("", "Q ss_pred", "", Common.SSColorReplace(querySSPRED.get)))
        }
        if (querySSDSSP.get.nonEmpty) {
          html += makeRow("sequence", Array("", "Q ss_dssp", "", Common.SSColorReplace(querySSDSSP.get)))
        }
        html += makeRow(
          "sequence",
          Array(
            "",
            "Q " + hit.query.accession,
            beginQuery.toString,
            s"${if (color) colorRegexReplacer(query) else query}  ${beginQuery + queryEnd - 1} (${hit.query.ref})"
          )
        )
        html += makeRow("sequence",
                        Array("",
                              "Q Consensus ",
                              beginQuery.toString,
                              queryCons + "  " + (beginQuery + queryEnd - 1) + " (" + hit.query.ref + ")"))
        html += makeRow("sequence", Array("", "", "", midline))
        html += makeRow(
          "sequence",
          Array("",
                "T Consensus ",
                beginTemplate.toString,
                "%s  %d (%d)".format(templateCons, beginTemplate + templateEnd - 1, hit.template.get.ref))
        )
        html += makeRow(
          "sequence",
          Array(
            "",
            "T " + hit.template.get.accession,
            beginTemplate.toString,
            s"${if (color) colorRegexReplacer(template.getOrElse("")) else template}  ${beginTemplate + templateEnd - 1} (${hit.template.get.ref})"
          )
        )
        if (!templateSSDSSP.get.isEmpty) {
          html += makeRow("sequence", Array("", "T ss_dssp", "", Common.SSColorReplace(templateSSDSSP.get)))
        }
        if (!templateSSPRED.get.isEmpty) {
          html += makeRow("sequence", Array("", "T ss_pred", "", Common.SSColorReplace(templateSSPRED.get)))
        }
        if (!confidence.isEmpty) {
          html += makeRow("sequence", Array("", "Confidence", "", confidence))
        }
        html += emptyRow + emptyRow
        html + hhpredHitWrapped(
          hit,
          charCount + breakAfter,
          breakAfter,
          beginQuery + queryEnd,
          beginTemplate + templateEnd,
          color
        )
      }
    }
  }

  def hhompHitWrapped(
      hit: HHompHSP,
      charCount: Int,
      breakAfter: Int,
      beginQuery: Int,
      beginTemplate: Int,
      color: Boolean
  ): String = {
    if (charCount >= hit.length) {
      ""
    } else {
      val querySSCONF = hit.query.ss_conf.slice(charCount, Math.min(charCount + breakAfter, hit.query.ss_conf.length))
      val querySSDSSP = hit.query.ss_dssp.slice(charCount, Math.min(charCount + breakAfter, hit.query.ss_dssp.length))
      val querySSPRED = hit.query.ss_pred.slice(charCount, Math.min(charCount + breakAfter, hit.query.ss_pred.length))
      val query       = hit.query.seq.slice(charCount, Math.min(charCount + breakAfter, hit.query.seq.length))
      val queryCons =
        hit.query.consensus.slice(charCount, Math.min(charCount + breakAfter, hit.query.consensus.length))
      val midline = hit.agree.slice(charCount, Math.min(charCount + breakAfter, hit.agree.length))
      val templateCons =
        hit.template.map(t => t.consensus.slice(charCount, Math.min(charCount + breakAfter, t.consensus.length)))
      val template = hit.template.map(t => t.seq.slice(charCount, Math.min(charCount + breakAfter, t.seq.length)))
      val templateSSDSSP =
        hit.template.map(t => t.ss_dssp.slice(charCount, Math.min(charCount + breakAfter, t.ss_dssp.length)))
      val templateSSPRED =
        hit.template.map(t => t.ss_pred.slice(charCount, Math.min(charCount + breakAfter, t.ss_pred.length)))
      val templateSSCONF =
        hit.template.map(t => t.ss_conf.slice(charCount, Math.min(charCount + breakAfter, t.ss_conf.length)))
      val templateBBPRED =
        hit.template.map(t => t.bb_pred.slice(charCount, Math.min(charCount + breakAfter, t.bb_pred.length)))
      val templateBBCONF =
        hit.template.map(t => t.bb_conf.slice(charCount, Math.min(charCount + breakAfter, t.bb_conf.length)))

      val queryEnd    = lengthWithoutDashDots(query)
      val templateEnd = lengthWithoutDashDots(template.getOrElse(""))

      if (beginQuery == beginQuery + queryEnd) {
        ""
      } else {
        var html = ""
        if (!querySSCONF.isEmpty) {
          html += makeRow("sequence", Array("", "Q ss_conf", "", querySSCONF))
        }
        if (!querySSPRED.isEmpty) {
          html += makeRow("sequence", Array("", "Q ss_pred", "", Common.SSColorReplace(querySSPRED)))
        }
        if (!querySSDSSP.isEmpty) {
          html += makeRow("sequence", Array("", "Q ss_dssp", "", Common.SSColorReplace(querySSDSSP)))
        }
        html += makeRow(
          "sequence",
          Array(
            "",
            "Q " + hit.query.accession,
            beginQuery.toString,
            { if (color) colorRegexReplacer(query) else query } + "  " + (beginQuery + queryEnd - 1) + " (" + hit.query.ref + ")"
          )
        )
        html += makeRow("sequence",
                        Array("",
                              "Q Consensus ",
                              beginQuery.toString,
                              queryCons + "  " + (beginQuery + queryEnd - 1) + " (" + hit.query.ref + ")"))
        html += makeRow("sequence", Array("", "", "", midline))
        html += makeRow(
          "sequence",
          Array("",
                "T Consensus ",
                beginTemplate.toString,
                templateCons + "  " + (beginTemplate + templateEnd - 1) + " (" + hit.template.get.ref + ")")
        )
        html += makeRow(
          "sequence",
          Array(
            "",
            "T " + hit.template.get.accession,
            beginTemplate.toString,
            s"${if (color) colorRegexReplacer(template.getOrElse(""))
            else template}  ${beginTemplate + templateEnd - 1} (${hit.template.get.ref})"
          )
        )
        if (!templateSSDSSP.get.isEmpty) {
          html += makeRow("sequence", Array("", "T ss_dssp", "", Common.SSColorReplace(templateSSDSSP.get)))
        }
        if (!templateSSPRED.get.isEmpty) {
          html += makeRow("sequence", Array("", "T ss_pred", "", Common.SSColorReplace(templateSSPRED.get)))
        }
        if (!templateSSCONF.get.isEmpty) {
          html += makeRow("sequence", Array("", "T ss_conf", "", templateSSCONF.get))
        }
        if (!templateBBPRED.get.isEmpty) {
          html += makeRow("sequence", Array("", "T bb_pred", "", templateBBPRED.get))
        }
        if (!templateBBCONF.get.isEmpty) {
          html += makeRow("sequence", Array("", "T bb_conf", "", templateBBCONF.get))
        }

        html += emptyRow + emptyRow

        html + hhompHitWrapped(hit,
                               charCount + breakAfter,
                               breakAfter,
                               beginQuery + queryEnd,
                               beginTemplate + templateEnd,
                               color)
      }
    }
  }

  def quick2dWrapped(result: Quick2DResult, charCount: Int, breakAfter: Int): String = {
    val length = result.query.seq.length
    if (charCount >= length) {
      ""
    } else {
      var htmlString = ""
      val query      = result.query.seq.slice(charCount, Math.min(charCount + breakAfter, result.query.seq.length))
      val psipred    = result.psipred.seq.slice(charCount, Math.min(charCount + breakAfter, result.query.seq.length))
      val pipred     = result.pipred.seq.slice(charCount, Math.min(charCount + breakAfter, result.query.seq.length))
      val marcoil    = result.marcoil.seq.slice(charCount, Math.min(charCount + breakAfter, result.query.seq.length))
      val coils      = result.coils.seq.slice(charCount, Math.min(charCount + breakAfter, result.query.seq.length))
      val pcoils     = result.pcoils.seq.slice(charCount, Math.min(charCount + breakAfter, result.query.seq.length))
      val tmhmm      = result.tmhmm.seq.slice(charCount, Math.min(charCount + breakAfter, result.query.seq.length))
      val phobius    = result.phobius.seq.slice(charCount, Math.min(charCount + breakAfter, result.query.seq.length))
      val polyphobius =
        result.polyphobius.seq.slice(charCount, Math.min(charCount + breakAfter, result.query.seq.length))
      val spider2   = result.spider2.seq.slice(charCount, Math.min(charCount + breakAfter, result.query.seq.length))
      val spotd     = result.spotd.seq.slice(charCount, Math.min(charCount + breakAfter, result.query.seq.length))
      val iupred    = result.iupred.seq.slice(charCount, Math.min(charCount + breakAfter, result.query.seq.length))
      val disopred3 = result.disopred3.seq.slice(charCount, Math.min(charCount + breakAfter, result.query.seq.length))
      val psspred   = result.psspred.seq.slice(charCount, Math.min(charCount + breakAfter, result.query.seq.length))
      val deepcnf   = result.deepcnf.seq.slice(charCount, Math.min(charCount + breakAfter, result.query.seq.length))

      htmlString += makeRow(
        "sequenceCompact",
        Array("AA_QUERY",
              (charCount + 1).toString,
              Highlight(query) + "&nbsp;&nbsp;&nbsp;&nbsp;" + Math.min(length, charCount + breakAfter))
      )
      if (!psipred.isEmpty) {
        htmlString += makeRow("sequenceCompact",
                              Array("SS_" + result.psipred.name.toUpperCase(),
                                    "",
                                    Q2DColorReplace(result.psipred.name, psipred.replaceAll("C|T|S|B|G", "&nbsp;"))))
      }
      if (!spider2.isEmpty) {
        htmlString += makeRow("sequenceCompact",
                              Array("SS_" + result.spider2.name.toUpperCase(),
                                    "",
                                    Q2DColorReplace(result.spider2.name, spider2.replace("C", "&nbsp;"))))
      }
      if (!psspred.isEmpty) {
        htmlString += makeRow("sequenceCompact",
                              Array("SS_" + result.psspred.name.toUpperCase(),
                                    "",
                                    Q2DColorReplace(result.psspred.name, psspred.replace("C", "&nbsp;"))))
      }
      if (!deepcnf.isEmpty) {
        htmlString += makeRow("sequenceCompact",
                              Array("SS_" + result.deepcnf.name.toUpperCase(),
                                    "",
                                    Q2DColorReplace(result.deepcnf.name, deepcnf.replace("C", "&nbsp;"))))
      }
      if (!pipred.isEmpty) {
        htmlString += makeRow("sequenceCompact",
                              Array("SS_" + result.pipred.name.toUpperCase(),
                                    "",
                                    Q2DColorReplace(result.pipred.name, pipred.replace("x", "&nbsp;"))))
      }
      if (!marcoil.isEmpty) {
        htmlString += makeRow("sequenceCompact",
                              Array("CC_" + result.marcoil.name.toUpperCase(),
                                    "",
                                    Q2DColorReplace(result.marcoil.name, marcoil.replace("x", "&nbsp;"))))
      }
      if (!coils.isEmpty) {
        htmlString += makeRow("sequenceCompact",
                              Array("CC_" + result.coils.name.toUpperCase() + "_W28",
                                    "",
                                    Q2DColorReplace(result.coils.name, coils.replace("x", "&nbsp;"))))
      }
      if (!pcoils.isEmpty) {
        htmlString += makeRow("sequenceCompact",
                              Array("CC_" + result.pcoils.name.toUpperCase() + "_W28",
                                    "",
                                    Q2DColorReplace(result.pcoils.name, pcoils.replace("x", "&nbsp;"))))
      }
      if (!tmhmm.isEmpty) {
        htmlString += makeRow("sequenceCompact",
                              Array("TM_" + result.tmhmm.name.toUpperCase(),
                                    "",
                                    Q2DColorReplace(result.tmhmm.name, tmhmm.replace("x", "&nbsp;"))))
      }
      if (!phobius.isEmpty) {
        htmlString += makeRow("sequenceCompact",
                              Array("TM_" + result.phobius.name.toUpperCase(),
                                    "",
                                    Q2DColorReplace(result.phobius.name, phobius.replace("x", "&nbsp;"))))
      }
      if (!polyphobius.isEmpty) {
        htmlString += makeRow(
          "sequenceCompact",
          Array("TM_" + result.polyphobius.name.toUpperCase(),
                "",
                Q2DColorReplace(result.polyphobius.name, polyphobius.replace("x", "&nbsp;")))
        )
      }
      if (!disopred3.isEmpty) {
        htmlString += makeRow("sequenceCompact",
                              Array("DO_" + result.disopred3.name.toUpperCase(),
                                    "",
                                    Q2DColorReplace(result.disopred3.name, disopred3.replace("O", "&nbsp;"))))
      }
      if (!spotd.isEmpty) {
        htmlString += makeRow("sequenceCompact",
                              Array("DO_" + result.spotd.name.toUpperCase(),
                                    "",
                                    Q2DColorReplace(result.spotd.name, spotd.replace("O", "&nbsp;"))))
      }
      if (!iupred.isEmpty) {
        htmlString += makeRow("sequenceCompact",
                              Array("DO_" + result.iupred.name.toUpperCase(),
                                    "",
                                    Q2DColorReplace(result.iupred.name, iupred.replace("O", "&nbsp;"))))
      }

      htmlString += emptyRow + emptyRow + emptyRow + emptyRow + emptyRow
      htmlString + quick2dWrapped(result, charCount + breakAfter, breakAfter)
    }
  }

}
