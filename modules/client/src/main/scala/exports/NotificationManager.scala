package exports

import org.scalajs.dom
import org.scalajs.dom.experimental.{ Notification, NotificationOptions }

import scala.language.postfixOps
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
