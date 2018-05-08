package exports.results

import org.scalajs.jquery._

import scala.scalajs.js

import exports.facades.JQueryPlugin.jqPlugin

class HitsSlider(private val container: JQuery) {

  def show(seqLength: Int, start: Int, end: Int): Unit = {
    val flatSlider: JQuery = container
      .find("#flat-slider")
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
      .on(
        "slide",
        (_: js.Any, ui: js.Dynamic) => {
          tooltipLeft.text(ui.values.asInstanceOf[js.Array[Int]](0).toString)
          tooltipRight.text(ui.values.asInstanceOf[js.Array[Int]](1).toString)
        }
      )
      .on("slidechange", (_: js.Any, ui: js.Dynamic) => {
        js.Dynamic.global.sliderCoords = flatSlider.slider("option", "values")
      })

    tooltipLeft
      .appendTo(flatSlider.find(".ui-slider-handle:first"))
      .show()

    tooltipRight
      .appendTo(flatSlider.find(".ui-slider-handle:last"))
      .show()
  }

  def resubmit(sequence: String, name: String): Unit = {
    val sliderRange  = js.Dynamic.global.$("#flat-slider").slider("option", "values").asInstanceOf[js.Array[Int]]
    val resubmitSeqs = name + "\n" + sequence.substring(sliderRange(0), sliderRange(1)) + "\n"
    container.find("a[href='#tabpanel-Input']").click()
    container.find("#alignment").value(resubmitSeqs)
  }

}
