package exports.results

import org.scalajs.jquery._

import scala.scalajs.js

class HitsSlider(private val container: JQuery) {

  def show(seqLength: Int, start: Int, end: Int): Unit = {
    val tooltip = jQuery("<div id='tooltip'/>")
      .css(js.Dictionary(
        "position" -> "absolute",
        "top" -> -20.toDouble
      ))
      .show()
    val tooltip2 = jQuery("<div id='tooltip2'/>")
      .css(js.Dictionary(
        "position" -> "absolute",
        "top" -> -20.toDouble
      ))
      .show()

    js.Dynamic.global
      .$("#flat-slider")
      .slider(
        js.Dynamic.literal(
          "range" -> true,
          "orientation" -> "horizontal",
          "min" -> 1,
          "max" -> seqLength,
          "step" -> 1,
          "values" -> js.Array(start, end),
          "slide" -> { (_: js.Any, ui: js.Dynamic) => {
            tooltip.text(ui.values.asInstanceOf[js.Array[Int]](0).toString)
            tooltip2.text(ui.values.asInstanceOf[js.Array[Int]](1).toString)
          }
          },
          "change" -> { (_: js.Any, ui: js.Dynamic) => {
            js.Dynamic.global.sliderCoords = js.Dynamic.global.$("#flat-slider").slider("option", "values")
          }
          }
        )
      )
  }

  def resubmit(sequence: String, name: String): Unit = {
    val sliderRange = js.Dynamic.global.$("#flat-slider").slider("option", "values").asInstanceOf[js.Array[Int]]
    val resubmitSeqs = name + "\n" + sequence.substring(sliderRange(0), sliderRange(1)) + "\n"
    container.find("a[href='#tabpanel-Input']").click()
    container.find("#alignment").value(resubmitSeqs)
  }

}
