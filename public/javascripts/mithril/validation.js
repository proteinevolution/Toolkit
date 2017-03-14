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

                    var visitorKalign = {
                        visit : function(alignmentVal) {
                            //let's fix the garage door
                            alignmentVal.fastaStep2 = mustHave2($(elem));
                        }
                    };

                    var target = new alignmentVal($(elem));
                    target.accept(visitorKalign);
                    //console.log(target.fastaStep2);

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

    if(!valid) {
        console.log(msg);
        $(".submitJob").prop("disabled", true);
        $v.css("display", "block").html(msg).addClass(type);
    }
    else if(valid){
        $(".submitJob").prop("disabled", false);
        $v.css("display", "block").html("Found format: <b>Fasta</b>").addClass("success");
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

// global filter to assert whether Fasta was the original input or the product of a conversion
changed = false;


function mustHave2(el) {

    if(el.validate('fasta') && fasta2json(el.val()).length < 2)
        feedback(false, "must have at least 2 seqs", "error");
    if(el.validate('fasta') && fasta2json(el.val()).length >= 2) {
        feedback(true);
        changed = false;
    }

}

var alignmentVal = function(el){

    var self = this;

    self.test = "";

    self.fastaStep2 = function(){
        feedback(true);
        changed = false;
    };

    self.accept = function (visitor) {
        visitor.visit(self);
    };


    self.setFastaStep2 = function(step) {
      self.fastaStep2 = step;
    };


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
            this.fastaStep2();
    }

    if(el.validate('fasta') && !el.reformat('alignment') && el.val().length != 0){
        feedback(false, "not aligned", "warning");
        $(".submitJob").prop("disabled", false); }
    else if (el.val().length === 0)
        valReset();
};