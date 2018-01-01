package exports.results

import scala.scalajs.js.annotation.{ JSExport, JSExportTopLevel }

@JSExportTopLevel("Forwarding")
class Forwarding {

  @JSExport
  def process(toolName: String, mode: String, evalue: Double) = ???

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
