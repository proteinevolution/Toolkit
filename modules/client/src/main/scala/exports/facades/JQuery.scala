package exports.facades

import scala.scalajs.js

@js.native
trait JQuery extends js.Object {
  def tooltipster(settings: js.Dictionary[js.Any]): js.Dynamic = js.native
  def foundation(action: String = null): js.Dynamic            = js.native
  def LoadingOverlay(action: String): js.Dynamic               = js.native
}
