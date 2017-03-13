/**
 * Created by snam on 03.03.17.
 */


validation = function(elem, isInit, ctx) {

    if(!isInit) {

        var toolname = $("#toolnameAccess").val();

        return $(elem).on("keyup", function (e) {

            switch(toolname) {
                case "tcoffee":
                    /** validation model for tcoffee:
                     * input has to be FASTA
                     */
                    alignmentVal($(elem));

                    break;

                case "mafft":
                    /** validation model for mafft:
                     * input has to be FASTA
                     */
                    alignmentVal($(elem));

                    break;

                case "muscle":
                    /** validation model for muscle:
                     * input has to be FASTA
                     */
                    alignmentVal($(elem));

                    break;

                case "clustalo":
                    /** validation model for clustalo:
                     * input has to be FASTA
                     */
                    alignmentVal($(elem));

                    break;

                case "kalign":
                    /** validation model for kalign:
                     * input has to be FASTA
                     */
                    alignmentVal($(elem));

                    break;

                case "msaprobs":
                    /** validation model for msaprobs:
                     * input has to be FASTA
                     */
                    alignmentVal($(elem));

                    break;

                default:
                    console.warn("no tool specified");
            }

            if($(elem).val().length === 0){
                valReset();
            }

        });
    }
};


function feedback(valid, msg, type, wrongformat) {

    var $v = $("#validOrNot");

    type = type || "success";
    if(type == "error")
        type = "alert";

    //remove trailing foundation classes
    $v.removeClass("alert warning secondary primary success");
    var t;

    if(!valid) {
        console.log(msg);
        $(".submitJob").prop("disabled", true);
        $v.css("display", "block").html(msg).addClass(type);
    }
    else if(valid){
        $v.css("display", "block").html("Found format: Fasta").addClass("success");
    }
    else if(wrongformat) {
        $(".submitJob").prop("disabled", false);
        $v.css("display", "block").html(msg).addClass(type);
    }
    else {
        console.log(msg);
        $(".submitJob").prop("disabled", false);
        $v.hide();
    }

}

function valReset(){

    var $v = $("#validOrNot");
    $v.hide();

}


//------------------------------ General Validators -------------------------------------------//

/**
 * alignment section specific validations
 * @param el
 */

changed = false;

function alignmentVal(el){


    if(!el.validate('fasta') && el.reformat('detect') === '' && el.val().length != 0)
        feedback(false, "this is no fasta!", "error");
    else if (el.val().length === 0)
        valReset();
    else if(!el.validate('fasta') && el.reformat('detect') != '' && el.val().length != 0) {
        var t = el.reformat('detect');
        feedback(false, "Wrong format found: " + t + ". <b>Auto-transformed to Fasta</b>", "success", t);
        $("#alignment").val(el.reformat('fasta'));
        changed = true;
    } else if (el.validate('fasta') && changed == false) {
            feedback(true);
            changed = false;
    }

    if(el.validate('fasta') && !el.reformat('alignment') && el.val().length != 0)
        feedback(false, "not aligned", "warning");
    else if (el.val().length === 0)
        valReset();
}