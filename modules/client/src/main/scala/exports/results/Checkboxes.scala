package exports.results

import org.scalajs.dom.Element
import org.scalajs.dom.raw.{Event, HTMLInputElement}
import org.scalajs.jquery.{JQuery, jQuery}

import scala.scalajs.js

/**
  * Checkbox handler for result pages
  */
class Checkboxes(private val outerContainer: JQuery) {

  var checkedValues: js.Dictionary[Boolean] = js.Dictionary()

  def initForContainer(container: JQuery): Unit = {
    populateCheckboxes(container)
    linkSynchronize(container)
  }

  def getChecked: js.Array[Int] = {
    val res: js.Array[Int] = js.Array()
    for ((value, checked) <- checkedValues if checked) res.push(value.toInt) // get only checked values
    res
  }

  def populateCheckboxes(container: JQuery): Unit = {
    for ((value, checked) <- checkedValues) container.find(s"input[type=checkbox][name=alignment_elem][value=$value]").prop("checked", checked)
  }

  def linkSynchronize(container: JQuery): Unit = {
    container.on("change", "input[type=checkbox][name=alignment_elem]", (e: Event) => {
      val currentVal = e.currentTarget.asInstanceOf[HTMLInputElement].value
      val currentState = e.currentTarget.asInstanceOf[HTMLInputElement].checked
      checkedValues(currentVal.toString) = currentState // force string value
      // link checkboxes with same value
      container.find(s"input[type=checkbox][name=alignment_elem][value=$currentVal]").each((_: Int, checkbox: Element) => {
        jQuery(checkbox).prop("checked", currentState)
      })
    })
  }

  def toggleAll(max: Int): Unit = {
    if (outerContainer.find("input[type=checkbox][name=alignment_elem]:checked").length != outerContainer.find("input[type=checkbox][name=alignment_elem]").length) {
      selectAll(max)
    } else {
      deselectAll(max)
    }
  }

  def deselectAll(max: Int): Unit = {
    outerContainer.find("input[type=checkbox][name=alignment_elem]").each(
      (_: Int, el: Element) => {
        jQuery(el).prop("checked", false)
      }
    )
    for (i <- 1 to max) checkedValues(i.toString) = false
  }

  def selectAll(max: Int): Unit = {
    outerContainer.find("input[type=checkbox][name=alignment_elem]").each(
      (_: Int, el: Element) => {
        jQuery(el).prop("checked", true)
      }
    )
    for (i <- 1 to max) checkedValues(i.toString) = true
  }

}
