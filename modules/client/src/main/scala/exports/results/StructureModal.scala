package exports.results

import exports.facades.JQueryPlugin._
import exports.facades.NGL
import exports.parsers.BioDB
import org.scalajs.dom.raw.{ Blob, Event, HTMLLinkElement, XMLHttpRequest }
import org.scalajs.jquery._

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExportTopLevel

@JSExportTopLevel("StructureModal")
class StructureModal($container: JQuery, $modal: JQuery) {

  $modal.foundation()

  private val accessionStructure = $modal.find("#accessionStructure")
  private val viewport: JQuery   = $modal.find("#viewport")

  // register listener on triggering links dynamically because they will be reloaded
  $container.on(
    "click",
    ".structureModalOpenBtn", { trigger: HTMLLinkElement =>
      {
        $modal.foundation("open")
        showStructure(jQuery(trigger).data("structure-id").toString)
      }
    }: js.ThisFunction
  )

  def showStructure(accession: String): Unit = {
    val fileEnding = BioDB.identify(accession) match {
      case "scop"  => "pdb"
      case "ecod"  => "pdb"
      case "mmcif" => "cif"
    }
    val url = s"/results/getStructure/$accession.$fileEnding"
    accessionStructure.html(s"<h6 class='structureAccession'>3D Structure: $accession</h6>")
    viewport.width(800).height(700).find("canvas").remove()
    val stage = new NGL.Stage(viewport.get(0))
    viewport.LoadingOverlay("show")
    // get blob with ajax
    val xhr = new XMLHttpRequest()
    xhr.onreadystatechange = { _: Event =>
      if (xhr.readyState == 4) {
        xhr.status match {
          case 200 =>
            stage.loadFile(xhr.response.asInstanceOf[Blob],
                           js.Dictionary("binary"                -> true,
                                         "defaultRepresentation" -> true,
                                         "sele"                  -> ":A or :B or DPPC",
                                         "ext"                   -> fileEnding))
          case status =>
            println(s"jqXHR=$xhr,text=${xhr.response},status=$status")
            accessionStructure.append("<div class='fetchError'>Sorry, failed to fetch structure.</div>")
        }
        viewport.LoadingOverlay("hide")
      }
    }
    xhr.open("GET", url)
    xhr.responseType = "blob"
    xhr.send()
  }
}
