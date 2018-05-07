package exports.results

import exports.facades.JQueryPosition
import exports.results.resultviews.ResultView
import org.scalajs.dom
import org.scalajs.dom.raw.{HTMLElement, HTMLInputElement}
import org.scalajs.jquery._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

class ScrollUtil(resultView: ResultView) {

  @JSExport
  def followScroll($elem: JQuery): Unit = {
    try {
      val $scrollLinks        = $elem.find("#scrollLinks a")
      val $visualization      = $elem.find("#visualization")
      val $scrollContainer    = $elem.find(".scrollContainer")
      val $scrollContainerDiv = $elem.find(".scrollContainerDiv")
      val $wrap               = $elem.find("#wrap")
      val $colorAA            = $elem.find(".colorAA")
      val $downloadHHR        = $elem.find(".downloadHHR")

      $elem.ready(() => {
        // Highlights the position in the control bar on click
        js.Dynamic.global.$("#alignments").floatingScroll("init")
        //smoothscroll
        $scrollLinks.on("click", { link: HTMLElement =>
          {
            $scrollLinks.removeClass("colorToggleBar")
            jQuery(link).addClass("colorToggleBar")
          }
        }: js.ThisFunction)
      })

      //  Fixes/Unfixes the control bar at the top
      $elem.on("scroll",
        () => {
          val top = jQuery(dom.document).scrollTop()
          if (!js.isUndefined($visualization.position())) {
            if (top >= $visualization.position().asInstanceOf[JQueryPosition].top + 75) {
              $scrollContainer.addClass("fixed").removeClass("scrollContainerWhite")
              $scrollContainerDiv.removeClass("scrollContainerDivWhite")
              $wrap.show()
              $colorAA.show()
              $downloadHHR.hide()
            } else {
              $scrollContainer.removeClass("fixed").addClass("scrollContainerWhite")
              $scrollContainerDiv.addClass("scrollContainerDivWhite")
              $wrap.hide()
              $colorAA.hide()
              $downloadHHR.show()
            }
          }
          // triggers getHits on scroll
          if (top == $elem.height() - jQuery(dom.window).height()) {
            if (!resultView.loading) {
              val limit: Int =
                if (dom.document.getElementById("toolnameAccess").asInstanceOf[HTMLInputElement].value == "psiblast")
                  100
                else
                  50
              var end = resultView.shownHits + limit
              end = if (end < resultView.resultContext.numHits) end else resultView.resultContext.numHits
              if (resultView.shownHits != end) {
                resultView.showHits(resultView.shownHits, end)
              }
            }
          }
          // Highlights the position in the control bar on scroll
          for (i <- 0 until $scrollLinks.length) {
            val currLink   = jQuery($scrollLinks(i))
            val refElement = jQuery(currLink.attr("name"))
            val refPos     = refElement.position().asInstanceOf[JQueryPosition]
            if (!js.isUndefined(refPos)) {
              if (refPos.top <= top && refPos.top + refElement.height() > top) {
                $scrollLinks.removeClass("colorToggleBar")
                currLink.addClass("colorToggleBar")
              } else {
                currLink.removeClass("colorToggleBar")
              }
            }
          }
        }
      )
    } catch {
      case e: Throwable => dom.console.warn(e.getMessage)
    }
  }

}
