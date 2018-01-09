package exports

import org.scalajs.dom
import org.scalajs.dom.raw.Node

import scala.scalajs.js.annotation._

@JSExportTopLevel("TitleNotification")
object TitleNotification {

  private val title: Node = dom.document.getElementsByTagName("title").item(0)
  private val normalTitle = title.textContent
  private var titleCounter = 0

  @JSExport
  def set(n: Int): Unit = {
    if (titleCounter < Int.MaxValue) {
      titleCounter += n
      title.textContent = s"($titleCounter) " + normalTitle
    }
    else
      title.textContent = s"($titleCounter +) " + normalTitle
  }

  @JSExport
  def reset(): Unit = {
    titleCounter = 0
    title.textContent = normalTitle
  }

} 
