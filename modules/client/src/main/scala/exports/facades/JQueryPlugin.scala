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
    def floatingScroll(config: String): JQueryPlugin               = js.native
  }

  implicit def jqPlugin(jq: JQuery): JQueryPlugin = jq.asInstanceOf[JQueryPlugin]

  implicit def jqStaticPlugin(jq: JQueryStatic): JQueryStaticPlugin = jq.asInstanceOf[JQueryStaticPlugin]

}
