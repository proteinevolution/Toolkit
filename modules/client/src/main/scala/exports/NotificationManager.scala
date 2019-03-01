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

package exports

import org.scalajs.dom
import org.scalajs.dom.experimental.{ Notification, NotificationOptions }

import scala.scalajs.js
import scala.scalajs.js.annotation._
import scala.scalajs.js.timers._

@JSExportTopLevel("NotificationManager")
object NotificationManager {

  @JSExport
  def showJobNotification(tag: String, title: String, body: String): Unit = {
    showNotification(
      tag,
      title,
      body,
      () => {
        js.Dynamic.global.m.route("/jobs/" + tag)
        dom.window.parent.focus()
        dom.window.focus()
        TitleManager.clearAlert()
      }
    )
  }

  @JSExport
  def showNotification(tag: String, title: String, body: String, onclick: js.Function0[Any] = () => {
    dom.window.parent.focus()
    dom.window.focus()
    TitleManager.clearAlert()
  }): Unit = {
    if (!js.isUndefined(Notification)) {
      if (Notification.permission == "granted") {
        val options = Map(
          "body" -> body,
          "icon" -> dom.document.getElementById("favicon_link").getAttribute("href")
        ).asInstanceOf[NotificationOptions]
        val n = new Notification(title, options)
        n.onclick = onclick
        setTimeout(5000) {
          n.close()
        }
      } else if (Notification.permission != "denied") {
        Notification.requestPermission((permission: String) => {
          if (permission == "granted") {
            showNotification("perm_granted",
                             "Thank you!",
                             "You will now receive updates on your jobs over notifications.")
          }
        })
      }
    }
  }

}
