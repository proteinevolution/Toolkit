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

package exports.results

import org.scalajs.jquery._

import scala.scalajs.js

import exports.facades.JQueryPlugin.jqPlugin

class HitsSlider(private val container: JQuery) {

  def show(seqLength: Int, start: Int, end: Int): Unit = {
    val flatSlider: JQuery = container.find("#flat-slider")
    val tooltipLeft = jQuery("<div id='tooltipLeft'/>")
      .css(
        js.Dictionary(
          "position" -> "absolute",
          "top"      -> -20.toDouble
        )
      )
      .text(start.toString)

    val tooltipRight = jQuery("<div id='tooltipRight'/>")
      .css(
        js.Dictionary(
          "position" -> "absolute",
          "top"      -> -20.toDouble
        )
      )
      .text(end.toString)

    flatSlider
      .slider(
        js.Dictionary(
          "range"       -> true,
          "orientation" -> "horizontal",
          "min"         -> 1,
          "max"         -> seqLength,
          "step"        -> 1,
          "values"      -> js.Array(start, end)
        )
      )
      .off("slide")
      .on(
        "slide",
        (_: js.Any, ui: js.Dynamic) => {
          tooltipLeft.text(ui.values.asInstanceOf[js.Array[Int]](0).toString)
          tooltipRight.text(ui.values.asInstanceOf[js.Array[Int]](1).toString)
        }
      )
      .off("slidechange")
      .on("slidechange", (_: js.Any, ui: js.Dynamic) => {
        js.Dynamic.global.sliderCoords = flatSlider.slider("option", "values")
      })

    tooltipLeft.appendTo(flatSlider.find(".ui-slider-handle:first")).show()

    tooltipRight.appendTo(flatSlider.find(".ui-slider-handle:last")).show()
  }

  def resubmit(sequence: String, name: String): Unit = {
    val sliderRange  = js.Dynamic.global.$("#flat-slider").slider("option", "values").asInstanceOf[js.Array[Int]]
    val resubmitSeqs = name + "\n" + sequence.substring(sliderRange(0), sliderRange(1)) + "\n"
    container.find("a[href='#tabpanel-Input']").click()
    container.find("#alignment").value(resubmitSeqs)
  }

}
