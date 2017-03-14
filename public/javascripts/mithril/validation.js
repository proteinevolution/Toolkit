/**
 * Created by snam on 03.03.17.
 */


validation = function(elem, isInit, ctx) {

    if(!isInit) {

        var toolname;
        try { toolname = $("#toolnameAccess").val(); }
        catch(err) {
            toolname = "unknown";
            console.warn("toolname unspecified");
        }

        return $(elem).on("keyup", function (e) {

            //---------------------------------Validation Visitors------------------------------------------//

            // in order to modularize validation we use the visitor pattern

            var mustHave2Visitor = {
                visit : function(alignmentVal) {
                    alignmentVal.fastaStep2 = mustHave2($(elem));
                }
            };

            //---------------------------------------------------------------------------------------------//

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
                     * input must consist of at least 2 seqs
                     */

                    var clustaloTarget = new alignmentVal($(elem));
                    clustaloTarget.accept(mustHave2Visitor);

                    break;

                case "kalign":
                    /** validation model for kalign:
                     * input has to be FASTA
                     * input must consist of at least 2 seqs
                     */

                    var kalignTarget = new alignmentVal($(elem));
                    kalignTarget.accept(mustHave2Visitor);

                    break;

                case "msaprobs":
                    /** validation model for msaprobs:
                     * input has to be FASTA
                     * input must consist of at least 2 seqs
                     */

                    var msaprobsTarget = new alignmentVal($(elem));
                    msaprobsTarget.accept(mustHave2Visitor);

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

// global filter to assert whether the original input has been FASTA
originIsFasta = true;


function mustHave2(el) {

    if(el.validate('fasta') && fasta2json(el.val()).length < 2)
        feedback(false, "must have at least 2 seqs", "error");
    else if(el.validate('fasta') && fasta2json(el.val()).length >= 2 && el.reformat('alignment') && originIsFasta && el.reformat('uniqueids')) {
        feedback(true);
        originIsFasta = true;
    }

}

var alignmentVal = function(el){

    var self = this;

    self.fastaStep2 = function(){
        feedback(true);
        originIsFasta = true;
    };

    self.accept = function (visitor) {
        visitor.visit(self);
    };


    if (!el.validate('fasta') && el.reformat('detect') === '' && el.val().length != 0)
        feedback(false, "this is no fasta!", "error");

    else if (!el.reformat('uniqueids'))
        feedback(false, "FASTA but identifiers are not unique!", "error");

    else if(!el.validate('fasta') && el.reformat('detect') != '' && el.val().length != 0) {
        originIsFasta = false;
        var t = el.reformat('detect');
        feedback(false, "Wrong format found: " + t + ". <b>Auto-transformed to Fasta</b>", "success", t);
        $("#alignment").val(el.reformat('fasta'));
    }

    else if (el.validate('fasta') && el.reformat('alignment') &&  originIsFasta)
        this.fastaStep2();

    else if(el.validate('fasta') && !el.reformat('alignment')){
        feedback(false, "not aligned", "warning");
        $(".submitJob").prop("disabled", false);
    }

    else if (el.val().length === 0)
        valReset();
};