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

import org.scalajs.dom.raw.{ Event, HTMLInputElement }
import org.scalajs.jquery.JQuery

import scala.collection.mutable
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

/**
 * Checkbox handler for result pages
 */
class Checkboxes(private val outerContainer: JQuery) {

  val checkedValues: mutable.Map[String, Boolean] = mutable.Map[String, Boolean]()

  def initForContainer(container: JQuery): Unit = {
    populateCheckboxes(container)
    linkSynchronize(container)
  }

  def getChecked: js.Array[Int] = checkedValues.filter(_._2).keys.map(_.toInt).toJSArray

  def populateCheckboxes(container: JQuery): Unit = {
    for ((value, checked) <- checkedValues)
      container.find(s"input[type=checkbox][name=alignment_elem][value=$value]").prop("checked", checked)
  }

  def linkSynchronize(container: JQuery): Unit = {
    container
      .off("change", "input[type=checkbox][name=alignment_elem]")
      .on(
        "change",
        "input[type=checkbox][name=alignment_elem]",
        (e: Event) => {
          val currentVal   = e.currentTarget.asInstanceOf[HTMLInputElement].value
          val currentState = e.currentTarget.asInstanceOf[HTMLInputElement].checked
          checkedValues(currentVal.toString) = currentState // force string value
          // link checkboxes with same value
          container.find(s"input[type=checkbox][name=alignment_elem][value=$currentVal]").prop("checked", currentState)
        }
      )
  }

  def toggleAll(max: Int, $button: JQuery): Unit = {
    if ($button.text().trim == "Select all".trim) {
      deselectAll(max)
    } else {
      selectAll(max)
    }
  }

  def deselectAll(max: Int): Unit = {
    outerContainer.find("input[type=checkbox][name=alignment_elem]").prop("checked", false)
    (1 to max).foreach(i => checkedValues.update(i.toString, false))
  }

  def selectAll(max: Int): Unit = {
    outerContainer.find("input[type=checkbox][name=alignment_elem]").prop("checked", true)
    (1 to max).foreach(i => checkedValues.update(i.toString, true))
  }

}
