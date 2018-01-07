import org.scalajs.dom
import org.scalajs.dom.raw.Node
import org.scalajs.jquery.jQuery

import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.js.annotation._

@JSExportTopLevel("TitleManager")
object TitleManager {

  private val title: Node = dom.document.getElementsByTagName("title").item(0)
  private val basicTitle: String = title.textContent
  private var titleCounter = 0
  private val toolTitlesDictionary = js.Dictionary(
    "hhblits" -> "HHblits",
    "hhpred" -> "HHpred",
    "hmmer" -> "HMMER",
    "patsearch" -> "PatternSearch",
    "psiblast" -> "PSI-BLAST",
    "alnviz" -> "AlignmentViewer",
    "clustalo" -> "ClustalΩ",
    "kalign" -> "Kalign",
    "mafft" -> "MAFFT",
    "msaprobs" -> "MSAProbs",
    "muscle" -> "MUSCLE",
    "tcoffee" -> "T-Coffee",
    "aln2plot" -> "Aln2Plot",
    "hhrepid" -> "HHrepID",
    "marcoil" -> "MARCOIL",
    "pcoils" -> "PCOILS",
    "repper" -> "REPPER",
    "tprpred" -> "TPRpred",
    "ali2d" -> "Ali2D",
    "hhomp" -> "HHomp",
    "quick2d" -> "Quick2D",
    "modeller" -> "MODELLER",
    "samcc" -> "SamCC",
    "ancescon" -> "ANCESCON",
    "clans" -> "CLANS",
    "mmseqs2" -> "MMseqs2",
    "phyml" -> "PhyML",
    "sixframe" -> "6Frame",
    "backtrans" -> "BackTranslator",
    "formatseq" -> "FormatSeq",
    "hhfilter" -> "HHfilter",
    "retseq" -> "RetrieveSeq",
    "seq2id" -> "Seq2ID",
    "reformat" -> "Reformat"
  ).withDefaultValue("")
  private val moreTitlesDictionary = js.Dictionary(
    "jobmanager" -> "Jobmanager",
    "404" -> "404"
  ).withDefaultValue("")
  private var titlePrefix = ""

  // Bind hashchange
  jQuery(dom.window).on("hashchange", () => {
    updateTitle()
  })

  // init on load
  updateTitle()

  @JSExport
  def resetCounter(): Unit = {
    setCounter(0)
  }

  @JSExport
  def setCounter(n: Int): Unit = {
    if (titleCounter < Int.MaxValue) {
      titleCounter = n
    }
    updateTitle()
  }

  @JSExport
  def updateTitle(overrideHash: js.Array[String] = new js.Array()): Unit = {
    var newTitle: String = basicTitle
    hashToPrefix(overrideHash)
    // set toolname
    if (titlePrefix != "") {
      newTitle = s"$titlePrefix | $newTitle"
    }
    // set counter
    if (titleCounter == 0) {}
    else if (titleCounter < Int.MaxValue) {
      newTitle = s"($titleCounter) $newTitle"
    }
    else {
      newTitle = s"($titleCounter +) $newTitle"
    }
    title.textContent = newTitle
  }

  @JSExport
  def hashToPrefix(overrideHash: js.Array[String]): Unit = {
    var hashFragments: Array[String] = overrideHash.toArray
    if (hashFragments.isEmpty) hashFragments = dom.document.location.hash.replace("#", "").replaceFirst("/", "").split("/")
    // find prefix
    titlePrefix = hashFragments(0) match {
      case "tools" => toolTitlesDictionary(hashFragments(1))
      case "jobs" => toolTitlesDictionary(js.Dynamic.global.JobListComponent.currentTool.asInstanceOf[String])
      case _ => moreTitlesDictionary(hashFragments(0))
    }
  }

} 
