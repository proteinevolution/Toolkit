package exports.results

import scala.scalajs.js
import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }
import org.querki.jquery.$

@JSExportTopLevel("HitsSlider")
object HitsSlider {

  @JSExport
  def show(seqLength: Int, start: Int, end: Int): Unit = {
    val tooltip = $("<div id='tooltip'/>")
      .css(
        js.Dictionary[js.Any](
          "position" -> "absolute",
          "top"      -> -20.toDouble
        )
      )
      .show()
    val tooltip2 = $("<div id='tooltip2'/>")
      .css(
        js.Dictionary[js.Any](
          "position" -> "absolute",
          "top"      -> -20.toDouble
        )
      )
      .show()
    js.Dynamic.global
      .$("#flat-slider")
      .slider(
        js.Dynamic.literal(
          "range"       -> true,
          "orientation" -> "horizontal",
          "min"         -> 1,
          "max"         -> seqLength,
          "step"        -> 1,
          "values"      -> js.Array[Int](start, end),
          "slide" -> { (_: js.Any, ui: js.Dynamic) =>
            {
              tooltip.text(ui.values.asInstanceOf[js.Array[Int]](0).toString)
              tooltip2.text(ui.values.asInstanceOf[js.Array[Int]](1).toString)
            }
          },
          "change" -> { (_: js.Any, ui: js.Dynamic) =>
            {
              js.Dynamic.global.sliderCoords = js.Dynamic.global.$("#flat-slider").slider("option", "values")
            }
          }
        )
      )
  }

  @JSExport
  def resubmit(sequence: String, name: String): Unit = {
    val sliderRange  = js.Dynamic.global.$("#flat-slider").slider("option", "values").asInstanceOf[js.Array[Int]]
    val resubmitSeqs = name + "\n" + sequence.substring(sliderRange(0), sliderRange(1)) + "\n"
    $("a[href='#tabpanel-Input']").click()
    $("#alignment").value(resubmitSeqs)
  }

}
