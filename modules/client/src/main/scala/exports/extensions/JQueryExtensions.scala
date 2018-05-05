package exports.extensions

import org.scalajs.jquery.JQuery

object JQueryExtensions {
  def toggleText(self: JQuery, a: String, b: String): JQuery = {
    if (self.text() == b)
      self.text(a)
    else
      self.text(b)
    self
  }
}
