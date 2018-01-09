package exports.parsers

import scala.scalajs.js
import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }

@JSExportTopLevel("BioDB")
object BioDB {

  private val uniprotReg = new js.RegExp("^([A-Z0-9]{10}|[A-Z0-9]{6})$")
  private val scopReg = new js.RegExp("^([defgh][0-9a-zA-Z._]+)$")
  private val ecodReg = new js.RegExp("^(ECOD_)")
  private val mmcifReg = new js.RegExp("^(...._[a-zA-Z])$")
  private val mmcifShortReg = new js.RegExp("^([0-9]+)$")
  private val pfamReg = new js.RegExp("^(pfam[0-9]+&|^PF[0-9]+(.[0-9]+)?)$")
  private val ncbiReg = new js.RegExp("^([A-Z]{2}_?[0-9]+.?#?([0-9]+)?|[A-Z]{3}[0-9]{5}?.[0-9])$")

  @JSExport
  def identify(id: String): String = id match {
    case x if scopReg.test(x) => "scop"
    case x if ecodReg.test(x) => "ecod"
    case x if mmcifShortReg.test(x) | mmcifReg.test(x) => "mmcif"
    case x if pfamReg.test(x) => "pfam"
    case x if ncbiReg.test(x) => "ncbi"
    case x if uniprotReg.test(x) => "uniprot"
    case _ => null
  }

}

