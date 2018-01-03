package exports.results

import org.querki.jquery.{ $, JQueryAjaxSettings, JQueryXHR }
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }

@JSExportTopLevel("Forwarding")
class Forwarding {

  @JSExport
  def process(toolName: String, mode: String, evalue: Double): Unit = {

    js.Dynamic.global.$.LoadingOverlay("show")

    $.ajax(
      js.Dynamic
          .literal(
            url = s"/results/forwardAlignment",
            success = { (data: js.Any, textStatus: js.Any, jqXHR: JQueryXHR) =>
              // TODO
            },
            error = { (jqXHR: JQueryXHR, textStatus: js.Any, errorThrow: js.Any) =>
              dom.console.log(s"jqXHR=$jqXHR,text=$textStatus,err=$errorThrow")
              js.Dynamic.global.$.LoadingOverlay("hide")
            },
            `type` = "POST"
          )
          .asInstanceOf[JQueryAjaxSettings]
    )

  }

}

/*
function psiblast_forward(selectedTool, boolSelectedHits, boolEvalue, evalue, boolFullLength){
    // full seq is retrieved
    var data = {};
    data.checkboxes = checkboxes.removeDuplicates();
    var route = null;
    var filename = generateFilename();
    $.LoadingOverlay("show");
    if(boolFullLength) {
        if(boolEvalue) {
            data.filename = filename;
            data.evalue = evalue;
            route = jsRoutes.controllers.PSIBlastController.evalFull(jobID);
        }else{
            if(data.checkboxes.length === 0){
                $('#forwardModal_@{tool.toolNameShort}').foundation('close');
                $.LoadingOverlay("hide");
                alert("No sequence(s) selected!");
                return false;
            }
            data.filename = filename;
            route = jsRoutes.controllers.PSIBlastController.full(jobID);
        }
    }else{
        if(boolEvalue) {
            data.filename = filename;
            data.evalue = evalue;
            route = jsRoutes.controllers.PSIBlastController.alnEval(jobID);
        }else{
            if(data.checkboxes.length === 0){
                $('#forwardModal_@{tool.toolNameShort}').foundation('close');
                $.LoadingOverlay("hide");
                alert("No sequence(s) selected!");
                return false;
            }
            data.filename = filename;
            route = jsRoutes.controllers.PSIBlastController.aln(jobID);
        }
    }
    $.ajax({
        url: route.url,
        contentType: 'application/json',
        data: JSON.stringify(data),
        method: route.method,
        error: function(){
            $.LoadingOverlay("hide");
        }
    }).done(function (data_) {
        forwardPath(selectedTool, 'files/'+jobID+'/'+filename+'.fa');
    })
}

 */
