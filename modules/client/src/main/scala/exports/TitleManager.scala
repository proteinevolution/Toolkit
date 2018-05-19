package exports

import de.proteinevolution.models.ToolName
import org.scalajs.dom
import org.scalajs.dom.raw.Node
import org.scalajs.jquery.jQuery

import scala.collection.mutable
import scala.scalajs.js
import scala.scalajs.js.annotation._

@JSExportTopLevel("TitleManager")
object TitleManager {

  private val title: Node        = dom.document.getElementsByTagName("title").item(0)
  private val basicTitle: String = title.textContent
  private var hasAlert: Boolean  = false
  private val moreTitlesDictionary: mutable.Map[String, String] = js
    .Dictionary(
      "jobmanager" -> "Jobmanager",
      "404"        -> "404"
    )
    .withDefaultValue("")
  private var titlePrefix = ""

  jQuery(dom.window).on("hashchange", () => {
    updateTitle()
  })

  // init on load
  updateTitle()

  @JSExport
  def updateTitle(overrideHash: js.Array[String] = new js.Array()): Unit = {
    var newTitle: String = basicTitle
    hashToPrefix(overrideHash)
    // set toolname
    if (titlePrefix != "") {
      newTitle = s"$titlePrefix | $newTitle"
    }
    if (hasAlert) {
      newTitle = s"(*) $newTitle"
    }
    title.textContent = newTitle
  }

  @JSExport
  def hashToPrefix(overrideHash: js.Array[String]): Unit = {
    var hashFragments: Array[String] = overrideHash.toArray
    if (hashFragments.isEmpty)
      hashFragments = dom.document.location.hash.replace("#", "").replaceFirst("/", "").split("/")
    // find prefix
    titlePrefix = hashFragments(0) match {
      case "tools" => ToolName(hashFragments(1)).toString
      case "jobs"  => ToolName(js.Dynamic.global.JobListComponent.currentTool.asInstanceOf[String]).toString
      case _       => moreTitlesDictionary(hashFragments(0))
    }
  }

  @JSExport
  def setAlert(): Unit = {
    hasAlert = true
    updateTitle()
  }

  @JSExport
  def clearAlert(): Unit = {
    hasAlert = false
    updateTitle()
  }

}
