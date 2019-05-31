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

package exports.extensions

import exports.facades.JQueryPosition
import org.scalajs.dom
import org.scalajs.jquery._

object JQueryExtensions {

  implicit class JQueryToggleText(val self: JQuery) extends AnyVal {
    def toggleText(a: String, b: String): self.type = {
      if (self.text() == b)
        self.text(a)
      else
        self.text(b)
      self
    }
  }

  implicit class JQueryIsOnScreen(val self: JQuery) extends AnyVal {
    def isOnScreen: Boolean = {
      val $window        = jQuery(dom.window)
      val viewportTop    = $window.scrollTop()
      val viewportBottom = viewportTop + $window.height()
      val boundsTop      = self.offset().asInstanceOf[JQueryPosition].top
      val boundsBottom   = boundsTop + self.outerHeight()
      boundsTop <= viewportBottom && boundsBottom >= viewportTop
    }
  }

}
