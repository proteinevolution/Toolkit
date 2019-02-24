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

package exports.results

import exports.extensions.Vanilla.{ ExtendedLink, ExtendedWindow }
import org.scalajs.dom
import org.scalajs.dom.raw.{ Blob, BlobPropertyBag }

import scala.scalajs.js
import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }

@JSExportTopLevel("DownloadHelper")
object DownloadHelper {

  @JSExport
  def download(filename: String, text: String): Unit = {
    val blob      = new Blob(js.Array(text), BlobPropertyBag("application/octet-stream"))
    val extWindow = dom.window.asInstanceOf[ExtendedWindow]
    if (extWindow.navigator.msSaveOrOpenBlob.toOption.getOrElse(false)) {
      extWindow.navigator.msSaveBlob(blob, filename)
    } else {
      val a = dom.document.createElement("a").asInstanceOf[ExtendedLink]
      a.href = extWindow.URL.createObjectURL(blob)
      a.download = filename
      // Append anchor to body.
      dom.document.body.appendChild(a)
      a.click()
      extWindow.URL.revokeObjectURL(a.href)
      // Remove anchor from body
      a.remove()
    }
    js.Dynamic.global.$.LoadingOverlay("hide")
  }

}
