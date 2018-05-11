package exports.results.resultviews

import exports.facades.{ JQueryPosition, ResultContext }
import exports.results.{ Checkboxes, HitsSlider, ScrollUtil }
import org.scalajs.jquery._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport
import exports.facades.JQueryPlugin._
import exports.results.models.ResultForm
import upickle.default.write

trait ResultView {

  def container: JQuery

  def tempShownHits: Int

  def resultContext: ResultContext

  @JSExport
  var shownHits: Int = if (tempShownHits > resultContext.numHits) resultContext.numHits else tempShownHits

  @JSExport
  var loading: Boolean = false

  protected val checkboxes: Checkboxes = new Checkboxes(container)
  protected val hitsSlider: HitsSlider = new HitsSlider(container)
  @JSExport
  protected val scrollUtil: ScrollUtil = new ScrollUtil(this)

  def init(): Unit

  def bindEvents(): Unit

  def showHits(start: Int, End: Int, successCallback: (js.Any, js.Any, JQueryXHR) => Unit = null): Unit

  protected def internalShowHits(jobID: String,
                                 route: String,
                                 data: ResultForm,
                                 resultContainer: JQuery,
                                 start: Int,
                                 end: Int,
                                 successCallback: (js.Any, js.Any, JQueryXHR) => Unit): Unit = {
    if (start <= resultContext.numHits && end <= resultContext.numHits) {

      loading = true
      container.find("#loadingHits").show()
      container.find("#loadHits").hide()

      jQuery.LoadingOverlay("show")
      jQuery
        .ajax(
          js.Dictionary(
              "url"         -> route,
              "data"        -> write(data),
              "contentType" -> "application/json",
              "type"        -> "POST"
            )
            .asInstanceOf[JQueryAjaxSettings]
        )
        .done((data: js.Any, textStatus: js.Any, jqXHR: JQueryXHR) => {
          resultContainer.append(data)
          shownHits = end
          if (shownHits != resultContext.numHits)
            container.find("#loadHits").show()
          checkboxes.initForContainer(resultContainer)
          jQuery("#alignments").floatingScroll("init")
          if (successCallback != null) {
            import scala.scalajs.js.timers._
            setTimeout(300) {
              successCallback(data, textStatus, jqXHR)
            }
          }
        })
        .fail((jqXHR: JQueryXHR, textStatus: js.Any, errorThrow: js.Any) => {
          println(s"jqXHR=$jqXHR,text=$textStatus,err=$errorThrow")
          resultContainer.append("Error loading Data.")
        })
        .always(() => {
          loading = false
          container.find("#loadingHits").hide()
          jQuery.LoadingOverlay("hide")
        })
    }
  }

  // run init
  init()

  @JSExport
  def getSelectedValues: js.Array[Int] = {
    checkboxes.getChecked
  }

  @JSExport
  def scrollToHit(id: Int, reload: Boolean = false): Unit = {
    val elem =
      if (container.find("#tool-tabs").hasClass("fullscreen"))
        "#tool-tabs"
      else
        "html, body"
    if (reload) {
      if (id > shownHits) shownHits = id
      container.find("#alignmentTable").empty()
      showHits(
        0,
        shownHits,
        (_: js.Any, _: js.Any, _: JQueryXHR) => {
          jQuery(elem).animate(
            js.Dictionary(
              "scrollTop" -> (container
                .find("input.aln[value=" + id + "]")
                .offset()
                .asInstanceOf[JQueryPosition]
                .top
                .asInstanceOf[Double] - 100.toDouble)
            ),
            1,
            "swing",
            null
          )
        }
      )
    } else {
      jQuery(elem)
        .animate(
          js.Dynamic.literal(
            "scrollTop" -> (container
              .find("input.aln[value=" + id + "]")
              .offset()
              .asInstanceOf[JQueryPosition]
              .top
              .asInstanceOf[Double] - 100.toDouble)
          ),
          1
        )
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
        jQuery(elem).scrollTop().toDouble
      else
        25.toDouble
    jQuery(elem)
      .animate(js.Dynamic.literal("scrollTop" -> (_pos + pos)), "fast")
  }

}
