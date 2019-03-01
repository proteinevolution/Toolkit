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

package exports.results.resultviews

import exports.extensions.JQueryExtensions
import exports.facades.JQueryPlugin.jqPlugin
import exports.facades.ResultContext
import exports.results.DataTables
import exports.results.models.ResultForm.ShowHitsForm
import org.scalajs.dom
import org.scalajs.dom.ext._
import org.scalajs.dom.raw.{ HTMLDivElement, HTMLInputElement, HTMLLinkElement }
import org.scalajs.jquery.{ jQuery, JQuery, JQueryXHR }
import exports.extensions.JQueryExtensions._
import exports.facades.JQueryPlugin._
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("NormalResultView")
class NormalResultView(
    val container: JQuery,
    val jobID: String,
    val hitsToLoad: Int,
    var wrapped: Boolean,
    var colorAAs: Boolean,
    val resultContext: ResultContext
) extends ResultView {

  override def init(): Unit = {

    initScrollWatch()

    // add slider val
    container
      .find(".slider")
      .off("moved.zf.slider")
      .on(
        "moved.zf.slider",
        () => {
          container
            .find("#lefthandle")
            .html(dom.document.getElementById("hidden1").asInstanceOf[HTMLInputElement].value)
            .css(
              js.Dictionary(
                "color"        -> "white",
                "font-weight"  -> "bold",
                "padding-left" -> "2px"
              )
            )
          container
            .find("#righthandle")
            .html(dom.document.getElementById("hidden2").asInstanceOf[HTMLInputElement].value)
            .css(
              js.Dictionary(
                "color"        -> "white",
                "font-weight"  -> "bold",
                "padding-left" -> "2px"
              )
            )
        }
      )

    if (resultContext.numHits > 0) {
      val wrap = container.find("#wrap")
      if (wrapped) {
        wrap.text("Unwrap Seqs").addClass("colorToggleBar")
      }
      if (colorAAs) {
        container.find(".colorAA").addClass("colorToggleBar")
      }
      bindEvents()
      new DataTables(resultContext.toolName).config(jobID, resultContext.numHits, () => {
        if (resultContext.toolName.equals("hhomp"))
          js.undefined
        else {
          checkboxes.initForContainer(jQuery("#htb"))
        }
      })

      // hide colorAAs button initially
      container.find(".colorAA").hide()

      if (resultContext.toolName.equals("psiblast")) {
        container.find(".selectAllSeqBar").addClass("colorToggleBar").text("Deselect all")
        checkboxes.selectAll(resultContext.belowEvalThreshold - 1)
      }
      showHits(0, hitsToLoad)
      setupBlastVizTooltipster()
      wrap.hide()
    }
  }

  def setupBlastVizTooltipster(): Unit = {
    // add tooltipster to visualization
    val blastVizArea = container.find("#blastviz").find("area")
    blastVizArea.tooltipster(
      js.Dictionary(
        "theme"         -> js.Array("tooltipster-borderless", "tooltipster-borderless-customized"),
        "position"      -> "bottom",
        "animation"     -> "fade",
        "contentAsHTML" -> true,
        "debug"         -> false,
        "maxWidth"      -> blastVizArea.innerWidth() * 0.6
      )
    )
    hitsSlider.show(resultContext.query.seq.length, resultContext.firstQueryStart, resultContext.firstQueryEnd)
  }

  def bindEvents(): Unit = {
    commonBindEvents()
    container
      .find("#wrap")
      .off("click")
      .on("click", () => {
        toggleIsWrapped()
      })
    container
      .find("#resubmitSection")
      .off("click")
      .on("click", () => {
        hitsSlider.resubmit(resultContext.query.seq, '>' + resultContext.query.accession)
      })
    container
      .find(".colorAA")
      .off("click")
      .on("click", () => {
        toggleAlignmentColoring()
      })
    container
      .off("click", "#scrollLinks a")
      .on("click", "#scrollLinks a", { link: HTMLLinkElement =>
        {
          scrollToSection(link.getAttribute("name"))
        }
      }: js.ThisFunction)
    container
      .find("#loadHits")
      .off("click")
      .on("click", () => {
        showHits(shownHits, shownHits + hitsToLoad)
      })
  }

  override def showHits(start: Int, end: Int, successCallback: (js.Any, js.Any, JQueryXHR) => Unit = null): Unit = {
    internalShowHits(
      jobID,
      s"/results/loadHits/$jobID",
      ShowHitsForm(start, end, wrapped, colorAAs),
      container.find("#alignmentTable"),
      successCallback
    )
  }

  def toggleAlignmentColoring(): Unit = {
    colorAAs = !colorAAs
    container.find(".colorAA").toggleClass("colorToggleBar")
    val current = currentIndex()
    jQuery.LoadingOverlay("show")
    scrollToHit(current, forceReload = true)
  }

  def toggleIsWrapped(): Unit = {
    wrapped = !wrapped
    container.find("#wrap").toggleClass("colorToggleBar")
    import JQueryExtensions._
    container.find("#wrap").toggleText("Unwrap Seqs", "Wrap Seqs")
    val current = currentIndex()
    jQuery.LoadingOverlay("show")
    scrollToHit(current, forceReload = true)
  }

  private def currentIndex(): Int = {
    def splitFn: PartialFunction[dom.Node, Int] = {
      case el: dom.Node =>
        if (resultContext.toolName == "hhomp")
          el.asInstanceOf[HTMLDivElement].getAttribute("data-id").toInt
        else
          el.asInstanceOf[HTMLInputElement].value.toInt
    }
    dom.document.querySelectorAll(".aln").iterator.toList.filter(jQuery(_).isOnScreen).collect(splitFn).min
  }

}
