package exports.results

import scala.scalajs.js
import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }
import org.querki.jquery.$

@JSExportTopLevel("HitsSlider")
object HitsSlider {

  @JSExport
  def show(seqLength: Int, start: Int, end: Int): Unit = {
    val tooltip = $("<div id='tooltip' />")
      .css(
        js.Dictionary[js.Any](
          "position" -> "absolute",
          "top"      -> -20.toDouble
        )
      )
      .show()
    val tooltip2 = $("<div id='tooltip2' />")
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
          "values"      -> js.Array(start, end),
          "slide" -> { (_: js.Any, ui: js.Array[String]) =>
            {
              tooltip.text(ui(0))
              tooltip2.text(ui(1))
            }
          },
          "change" -> { (_: js.Any, ui: js.Array[String]) =>
            {
              js.Dynamic.global.sliderCoords = js.Dynamic.global.$("#flat-slider").slider("option", "values")
            }
          }
        )
      )
  }

}
