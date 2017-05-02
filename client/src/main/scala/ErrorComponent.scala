import co.technius.scalajs.mithril.{Component, MithrilProp, m}

import scala.scalajs.js

/**
  * Created by snam on 02.05.17.
  */

object ErrorComponent extends Component {

  override val controller: js.Function = () => new ErrorComponentCtrl()

  val view: js.Function = (ctrl: ErrorComponentCtrl) => js.Array(

    m("div", "HALLO"),
    m("span", s"Hi, ${ctrl.errorID()}!"),
    m("span", s"Hi, ${ctrl.errorMessage()}!")
  )

}


private[this] class ErrorComponentCtrl {

  val errorID : MithrilProp[Int] = m.prop(404)
  val errorMessage : MithrilProp[String] = m.prop("Page not Found")

}
