package exports.results.data

import org.scalajs.dom.ext.LocalStorage

import scala.scalajs.js

class LocalStorageDao(id: String) {

  val LocalStorageName = s"showHits-$id"

  def hasContent: Boolean = load().isDefined

  def store(content: js.Any): Unit = {
    LocalStorage.update(LocalStorageName, content.asInstanceOf[String])
  }

  def load(): Option[String] = {
    LocalStorage(LocalStorageName)
  }

  def clear(): Unit = {
    LocalStorage.remove(LocalStorageName)
  }

}
