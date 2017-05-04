import co.technius.scalajs.mithril.{Component, MithrilComponent, MithrilProp, m}

import scala.scalajs.js
import js.Dynamic.{global => g}
import scala.util.Random

/**
  * Created by snam on 02.05.17.
  */

object ErrorComponent extends Component {

  override val controller: js.Function = (args: Any) => new ErrorComponentCtrl(args)

  val view: js.Function = (ctrl: ErrorComponentCtrl) => js.Array(

    m("input", js.Dynamic.literal("id" -> "hidden404", "type" -> "hidden", "value" -> ctrl.notFoundRandomized)),
    m("div", js.Dynamic.literal("id" -> "404wrapper", "style" -> "display: block; margin: auto; margin-top: 100px;"),
      m("div",js.Dynamic.literal("id" -> "404msa", "config" -> g.call404))),
    m("p",js.Dynamic.literal("id" -> "subtitle404", "style" -> "position: absolute; margin-top: 400px; margin-left: 45%;"), s"${ctrl.errorMessage()}")


  )

}


private[this] class ErrorComponentCtrl(args : Any) {


  val errorMessage : MithrilProp[String] = m.prop("Page not found.")



  val aminoAcids : List[Char] = List('A', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'V', 'W', 'Y')



  val notFound : String = """>AAN59974.1 histQne H2A [HQmQ sapiens]
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


  val notFoundRandomized : String = notFound.map(c => if (c == 'G' || c == 'Q' || c == 'A') Stream.continually(Random.alphanumeric).take(100).head.take(100).filter(x => aminoAcids.contains(x)).head else c)

}
