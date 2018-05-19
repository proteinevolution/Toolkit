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
      val $window = jQuery(dom.window)
      val viewportTop = $window.scrollTop()
      val viewportBottom = viewportTop + $window.height()
      val boundsTop = self.offset().asInstanceOf[JQueryPosition].top
      val boundsBottom = boundsTop + self.outerHeight()
      boundsTop <= viewportBottom && boundsBottom >= viewportTop
    }
  }

}
