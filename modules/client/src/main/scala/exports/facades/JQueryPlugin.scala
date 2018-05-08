package exports.facades

import scala.language.implicitConversions
import org.scalajs.jquery.{ JQuery, JQueryStatic }

import scala.scalajs.js

object JQueryPlugin {

  @js.native
  sealed trait JQueryStaticPlugin extends JQueryStatic {
    def LoadingOverlay(action: String): JQueryStaticPlugin = js.native
  }

  @js.native
  sealed trait JQueryPlugin extends JQuery {
    def LoadingOverlay(action: String): JQueryPlugin               = js.native
    def tooltipster(settings: js.Dictionary[js.Any]): JQueryPlugin = js.native
    def foundation(action: String = null): JQueryPlugin            = js.native
    def slider(options: js.Dictionary[Any]): JQueryPlugin          = js.native
    def slider(action: String, target: String): JQueryPlugin       = js.native
    def DataTable(config: js.Dictionary[Any]): JQueryPlugin        = js.native
  }

  implicit def jqPlugin(jq: JQuery): JQueryPlugin = jq.asInstanceOf[JQueryPlugin]

  implicit def jqStaticPlugin(jq: JQueryStatic): JQueryStaticPlugin = jq.asInstanceOf[JQueryStaticPlugin]

}
