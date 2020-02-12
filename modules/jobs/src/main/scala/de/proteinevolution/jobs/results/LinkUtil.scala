/*
 * Copyright 2018 Dept. Protein Evolution, Max Planck Institute for Developmental Biology
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.proteinevolution.jobs.results

import scala.collection.mutable.ArrayBuffer

object LinkUtil {

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
  private val uniprotBaseLink     = "http://www.uniprot.org/uniprot/"
  private val unirefBaseLink     = "http://www.uniprot.org/uniref/"
  private val smartBaseLink       = "http://smart.embl-heidelberg.de/smart/do_annotation.pl?DOMAIN="
  private val ecodBaseLink        = "http://prodata.swmed.edu/ecod/complete/domain/"

  /* GENERATING LINKS FOR HHPRED */

  def getSingleLink(id: String): String = {
    val db     = identifyDatabase(id)
    val idPfam = id.replaceAll("am.*$||..*", "")
    val idPdb  = id.replaceAll("_.*$", "")
    db match {
      case "scop"    => generateLink(scopBaseLink, id, id)
      case "mmcif"   => generateLink(pdbBaseLink, idPdb, id)
      case "prk"     => generateLink(cddBaseLink, id, id)
      case "ncbicd"  => generateLink(cddBaseLink, id, id)
      case "cogkog"  => generateLink(cddBaseLink, id, id)
      case "tigr"    => generateLink(cddBaseLink, id, id)
      case "pfam"    => generateLink(pfamBaseLink, idPfam + "#tabview=tab0", id)
      case "ncbi"    => generateLink(ncbiProteinBaseLink, id, id)
      case "uniprot" => generateLink(uniprotBaseLink, id, id)
      case "smart"   => generateLink(smartBaseLink, id, id)
      case "ecod"    => val idEcod = id.slice(5, 14); generateLink(ecodBaseLink, idEcod, id)
      case _         => id
    }
  }

  def getSingleLinkDB(db: String, id: String): String = {
    val idPfam = id.replaceAll("am.*$||..*", "")
    val idPdb  = id.replaceAll("_.*$", "")
    db match {
      case envNrNameReg(_)   => generateLink(ncbiProteinBaseLink, id, id)
      case pdbNameReg(_)     => generateLink(pdbBaseLink, idPdb, id)
      case uniprotNameReg(_) => generateLink(uniprotBaseLink, id, id)
      case pfamNameReg(_)    => generateLink(pfamBaseLink, idPfam + "#tabview=tab0", id)
      case _                 => id
    }
  }

  def getLinksDB(db: String, id: String): String = {
    val idNcbi = id.replaceAll("#", ".") + "?report=fasta"
    val idPdb  = id.replaceAll("_.*$", "").toLowerCase
    val idCDD  = id.replaceAll("PF", "pfam").replaceAll("\\..*", "")
    db match {
      case envNrNameReg(_)   => generateLink(ncbiProteinBaseLink, idNcbi, "NCBI Fasta")
      case pdbNameReg(_)     => generateLink(pdbeBaseLink, idPdb, "PDBe")
      case pfamNameReg(_)    => generateLink(cddBaseLink, idCDD, "CDD")
      case uniprotNameReg(_) => generateLink(uniprotBaseLink, id + ".fasta", "UniProt")
    }
  }

  def displayModellerLink(db: String, proteome: String): Boolean = {
    db == "mmcif70/pdb70" || db == "mmcif30/pdb30" && proteome.isEmpty
  }

  def displayStructLink(id: String): Boolean = {
    val db = identifyDatabase(id)
    db match {
      case "scop"  => true
      case "mmcif" => true
      case "ecod"  => true
      case _       => false
    }
  }

  def getSingleLinkHHBlits(id: String): String = generateLink(unirefBaseLink, id, id)

  def getLinksHHpred(jobID: String, id: String): String = {
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
    db match {
      case "scop" =>
        links += generateLink(pdbBaseLink, idTrimmed, "PDB")
        links += generateLink(ncbiBaseLink, idTrimmed, "NCBI")
      case "ecod" =>
        val idPdbEcod = id.slice(16, 20)
        links += generateLink(pdbBaseLink, idPdbEcod, "PDB")
      case "mmcif" =>
        links += generateLink(pdbeBaseLink, idPdb, "PDBe")
      case "pfam" =>
        val idCDDPfam = idCDD.replaceAll("\\..*", "")
        links += generateLink(cddBaseLink, idCDDPfam, "CDD")
      case "ncbi" =>
        links += generateLink(ncbiProteinBaseLink, idNcbi, "NCBI Fasta")
      case _ => ()
    }
    links.mkString(" | ")
  }

  private def generateLink(baseLink: String, id: String, name: String): String =
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
    case _: String        => ""
  }
}
