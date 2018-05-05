package exports.results.resultviews

import exports.facades.{ JQueryPosition, ResultContext }
import exports.results.{ Checkboxes, HitsSlider, ScrollUtil }
import org.scalajs.jquery._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

abstract class ResultView(container: JQuery, jobID: String, tempShownHits: Int, val resultContext: ResultContext) {

  @JSExport
  var shownHits: Int = if (tempShownHits > resultContext.numHits) resultContext.numHits else tempShownHits

  @JSExport
  var loading: Boolean = false

  protected val checkboxes: Checkboxes = new Checkboxes(container)
  protected val hitsSlider: HitsSlider = new HitsSlider(container)
  protected val scrollUtil: ScrollUtil = new ScrollUtil(this)

  def init(): Unit

  def bindEvents(): Unit

  def showHits(start: Int, End: Int, successCallback: (js.Any, js.Any, JQueryXHR) => Unit = null): Unit

  // run init
  init()

  @JSExport
  def getSelectedValues: js.Array[Int] = {
    checkboxes.getChecked
  }

  @JSExport
  def scrollToHit(id: Int): Unit = {
    val elem =
      if (container.find("#tool-tabs").hasClass("fullscreen"))
        "#tool-tabs"
      else
        "html, body"
    if (id > js.Dynamic.global.shownHits.asInstanceOf[Int]) {
      js.Dynamic.global.$.LoadingOverlay("show")
      showHits(
        shownHits,
        id,
        (_: js.Any, _: js.Any, _: JQueryXHR) => {
          js.Dynamic.global.shownHits = id
          jQuery(elem).animate(
            js.Dictionary(
              "scrollTop" -> (container
                .find(".aln[value='" + id + "']")
                .offset()
                .asInstanceOf[JQueryPosition]
                .top
                .asInstanceOf[Double] - 100)
            ),
            1,
            "swing",
            null
          )
        }
      )
      jQuery(elem)
        .animate(js.Dynamic.literal(
                   "scrollTop" -> (container
                     .find(".aln[value='" + id + "']")
                     .offset()
                     .asInstanceOf[JQueryPosition]
                     .top - 100.toDouble)
                 ),
                 1)
      js.Dynamic.global.$.LoadingOverlay("hide")
      js.Dynamic.global.shownHits = id
    } else {
      jQuery(elem)
        .animate(js.Dynamic.literal(
                   "scrollTop" -> (container
                     .find(".aln[value='" + id + "']")
                     .offset()
                     .asInstanceOf[JQueryPosition]
                     .top - 100.toDouble)
                 ),
                 1)
    }

  }

  def scrollToSection(name: String): Unit = {
    val elem =
      if (container.find("#tool-tabs").hasClass("fullscreen"))
        "#tool-tabs"
      else
        "html, body"
    val _pos = container.find("#" + name).offset().asInstanceOf[JQueryPosition].top
    val pos =
      if (container.find("#tool-tabs").hasClass("fullscreen"))
        jQuery(elem).scrollTop()
      else
        25.toDouble
    jQuery(elem)
      .animate(js.Dynamic.literal("scrollTop" -> (_pos + pos)), "fast")
  }

}
