package exports.results.resultviews

import exports.extensions.JQueryExtensions
import exports.facades.JQueryPlugin._
import exports.facades.{JQueryPosition, ResultContext}
import exports.results.models.ResultForm
import exports.results.{Checkboxes, ForwardingModal, HitsSlider, ScrollUtil}
import org.scalajs.dom.raw.HTMLLinkElement
import org.scalajs.jquery._
import upickle.default.write

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

trait ResultView {

  def container: JQuery

  def jobID: String

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
  protected val forwardingModal: ForwardingModal = new ForwardingModal(container, resultContext.toolName, jobID)

  def init(): Unit

  def bindEvents(): Unit = {
    // common events
    container
      .find(".selectAllSeqBar")
      .off("click")
      .on("click", { link: HTMLLinkElement => {
          val $link = jQuery(link)
          $link.toggleClass("colorToggleBar")
          JQueryExtensions.toggleText($link, "Select all", "Deselect all")
          checkboxes.toggleAll(resultContext.numHits)
        }
        }: js.ThisFunction
      )
  }

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

      jQuery
        .ajax(
          js.Dictionary(
            "url" -> route,
            "data" -> write(data),
            "contentType" -> "application/json",
            "type" -> "POST"
          )
            .asInstanceOf[JQueryAjaxSettings]
        )
        .done((data: js.Any, textStatus: js.Any, jqXHR: JQueryXHR) => {
          resultContainer.append(data)
          shownHits = end
          if (shownHits != resultContext.numHits)
            container.find("#loadHits").show()
          checkboxes.initForContainer(jQuery("#jobview"))
          jQuery("#alignments").floatingScroll("init")
          if (successCallback != null) {
            import scala.scalajs.js.timers._
            setTimeout(200) {
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
  def scrollToHit(id: Int, forceReload: Boolean = false): Unit = {
    val elem =
      if (container.find("#tool-tabs").hasClass("fullscreen"))
        "#tool-tabs"
      else
        "html, body"
    var reload = forceReload
    if (id > shownHits) {
      shownHits = id
      reload = true
    }
    if (reload) { // reload hits if forced or requested hit not loaded
      container.find("#alignmentTable").empty()
      showHits(
        0,
        shownHits,
        (_: js.Any, _: js.Any, _: JQueryXHR) => {
          jQuery(elem).animate(
            js.Dictionary(
              "scrollTop" -> (container
                .find(".aln[data-id=" + id + "]")
                .offset()
                .asInstanceOf[JQueryPosition]
                .top - 100D)
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
              .find(".aln[data-id=" + id + "]")
              .offset()
              .asInstanceOf[JQueryPosition]
              .top - 100D)
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
        25D
    jQuery(elem)
      .animate(js.Dynamic.literal("scrollTop" -> (_pos + pos)), "fast")
  }

}
