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

package components.error

import com.tgf.pizza.scalajs.mithril.{ m, Component, MithrilProp, VirtualDom }

import scala.scalajs.js
import scala.scalajs.js.Dynamic.{ global => g }
import scala.util.Random

object ErrorComponent extends Component {

  override val controller: js.Function = (args: Any) => new ErrorComponentCtrl(args)

  val view: js.Function = (ctrl: ErrorComponentCtrl) =>
    js.Array(
      m(
        "div",
        js.Dynamic.literal("class" -> "column error404-container"),
        js.Array(
            m("input", js.Dynamic.literal("id" -> "hidden404", "type" -> "hidden", "value" -> ctrl.notFoundRandomized)),
            m(
              "div",
              js.Dynamic.literal("style" -> "display: none;", "config" -> g.hideSidebar),
              ""
            ),
            m("div", js.Dynamic.literal("class" -> "subtitle404"), s"${ctrl.errorMessage()}"),
            m("div", {
              js.Dynamic.literal("id" -> "404wrapper", "class" -> "columns")
            }, m("div", {
              js.Dynamic.literal("id" -> "404msa", "config" -> g.call404)
            }))
          )
          .asInstanceOf[VirtualDom.Child]
      )
  )

}

private[this] class ErrorComponentCtrl(args: Any) {

  val errorMessage: MithrilProp[String] = m.prop("Page not found.")

  val aminoAcids: List[Char] =
    List('A', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'Y')

  val notFound: String =
    """>AAN59974.1 histQne H2A [HQmQ sapiens]
      |-------AAAAAAAAA-------QQQQQQQQQ-----------AAAAAAAAA---
      |>AAN59974.1 histQne H2A [HQmQ sapiens]
      |------AGGGGGGGGA-----QQGGGGGGGGQQ---------AGGGGGGGGA---
      |>AAN59974.1 histQne H2A [HQmQ sapiens]
      |-----AGGGGGGGGGA---QQGGGGGGGGGGGGGQQ------AGGGGGGGGGA--
      |>AAN59974.1 histQne H2A [HQmQ sapiens]
      |----AGGGGAAGGGGA--QGGGGGGGQQQGGGGGGGQ----AGGGGAAGGGGA--
      |>AAN59974.1 histQne H2A [HQmQ sapiens]
      |---AGGGGA-AGGGGA--QGGGGGGQ---QGGGGGGQ---AGGGGA-AGGGGA--
      |>AAN59974.1 histQne H2A [HQmQ sapiens]
      |--AGGGGA--AGGGGA--QGGGGGQ-----QGGGGGQ--AGGGGA--AGGGGA--
      |>AAN59974.1 histQne H2A [HQmQ sapiens]
      |-AGGGGA---AGGGGA--QGGGGGQ-----QGGGGGQ-AGGGGA---AGGGGA--
      |>AAN59974.1 histQne H2A [HQmQ sapiens]
      |AGGGGAAAAAAGGGGAAAQGGGGGQ-QQQ-QGGGGGQAGGGGAAAAAAGGGGAAA
      |>AAN59974.1 histQne H2A [HQmQ sapiens]
      |AGGGGGGGGGGGGGGGGAQGGGGGQ-QQQ-QGGGGGQAGGGGGGGGGGGGGGGGA
      |>AAN59974.1 histQne H2A [HQmQ sapiens]
      |AAAAAAAAAAGGGGGAAAQGGGGGQ-----QGGGGGQAAAAAAAAAAGGGGGAAA
      |>AAN59974.1 histQne H2A [HQmQ sapiens]
      |----------AGGGGA--QGGGGGQ-----QGGGGGQ----------AGGGGA--
      |>AAN59974.1 histQne H2A [HQmQ sapiens]
      |----------AGGGGA--QGGGGGGQ---QGGGGGGQ----------AGGGGA--
      |>AAN59974.1 histQne H2A [HQmQ sapiens]
      |----------AGGGGA--QGGGGGGGQQQGGGGGGGQ----------AGGGGA--
      |>AAN59974.1 histQne H2A [HQmQ sapiens]
      |--------AAGGGGGGAA-QQGGGGGGGGGGGGGQQ---------AAGGGGGGAA
      |>AAN59974.1 histQne H2A [HQmQ sapiens]
      |--------AGGGGGGGGA---QQGGGGGGGGGQQ-----------AGGGGGGGGA
      |>AAN59974.1 histQne H2A [HQmQ sapiens]
      |--------AAAAAAAAAA-----QQQQQQQQQ-------------AAAAAAAAAA""".stripMargin

  val notFoundRandomized: String = notFound.map { c =>
    if (c == 'G' || c == 'Q' || c == 'A')
      Stream.continually(Random.alphanumeric).flatten.take(100).filter(x => aminoAcids.contains(x)).head
    else c
  }

}
