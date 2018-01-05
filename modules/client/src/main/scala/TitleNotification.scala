import org.scalajs.dom.raw.Node

import scala.scalajs.js.annotation._
import org.scalajs.dom

import scala.scalajs.js

@JSExportTopLevel("TitleNotification")
object TitleNotification {

  private val title: Node = dom.document.getElementsByTagName("title").item(0)
  private var titleCounter = 0

  @JSExport
  def set(n: Int): Unit = {
    if (titleCounter < Int.MaxValue) {
      titleCounter += n
      title.textContent = s"($titleCounter) " + Globals.siteTitle
    }
    else
      title.textContent = s"($titleCounter +) " + Globals.siteTitle
  }

  @JSExport
  def reset(): Unit = {
    titleCounter = 0
    title.textContent = Globals.siteTitle
  }

  @js.native
  @JSGlobalScope
  object Globals extends js.Object {
    var siteTitle: String = js.native
  }

} 
