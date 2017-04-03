/**
 * Created by snam on 03.03.17.
 */

let seqLimit : any;

let validation = function(elem : any, isInit : boolean, ctx : any) : any {

    if(!isInit) {

        let toolname : string;
        try { toolname = $("#toolnameAccess").val(); }
        catch(err) {
            toolname = "unknown";
            console.warn("toolname unspecified");
        }

        return $(elem).on("keyup", function (e) {

            //---------------------------------Validation Visitors------------------------------------------//

            // in order to modularize validation we use the visitor pattern

            /*let mustHave2Visitor = {
                visit : function(alignmentVal : any) {
                    alignmentVal.fastaStep2 = mustHave2($(elem));
                }
            };*/


            //---------------------------------------------------------------------------------------------//

            switch(toolname) {
                case "tcoffee":
                    /** validation model for tcoffee:
                     * Input has to be in FASTA format and may comprise multiple sequences of varying lengths.
                     * Input must include at least two sequences.
                     * ALIGNED FASTA input is allowed.
                     * Sequences should have unique IDs; only the characters directly following the '>' sign, until the
                     * first space, in the header are used as ID.
                     * Limit the maximum number of sequences to 2000.
                     **/

                    let tcoffeeTarget = new alignmentVal($(elem));
                    tcoffeeTarget.basicValidation();

                    if (feedback) {
                        tcoffeeTarget.mustHave2();
                    }
                    seqLimit = 2000;

                    break;

                case "mafft":
                    /** validation model for mafft:
                     * Input has to be in FASTA format and may comprise multiple sequences of varying lengths.
                     * Input must include at least two sequences.
                     * ALIGNED FASTA input is allowed.
                     * Sequences should have unique IDs; only the characters directly following the '>' sign, until the
                     * first space, in the header are used as ID.
                     * Limit the maximum number of sequences to 2000.
                     **/

                    let mafftTarget = new alignmentVal($(elem));
                    mafftTarget.basicValidation();

                    if (feedback) {
                        mafftTarget.mustHave2();
                    }
                    seqLimit = 2000;

                    break;

                case "muscle":
                    /** validation model for muscle:
                     * Input has to be in FASTA format and may comprise multiple sequences of varying lengths.
                     * Input must include at least two sequences.
                     * ALIGNED FASTA input is allowed.
                     * Sequences should have unique IDs; only the characters directly following the '>' sign, until the
                     * first space, in the header are used as ID.
                     * Limit the maximum number of sequences to 2000.
                     **/

                    let muscleTarget = new alignmentVal($(elem));
                    muscleTarget.basicValidation();

                    if (feedback) {
                        muscleTarget.mustHave2();
                    }
                    seqLimit = 2000;

                    break;

                case "clustalo":
                    /** validation model for clustalo:
                     * Input has to be in FASTA format and may comprise multiple sequences of varying lengths.
                     * Input must include at least two sequences.
                     * ALIGNED FASTA input is allowed.
                     * Sequences should have unique IDs; only the characters directly following the '>' sign, until the
                     * first space, in the header are used as ID.
                     * Limit the maximum number of sequences to 2000.
                     **/

                    let clustaloTarget = new alignmentVal($(elem));
                    clustaloTarget.basicValidation();

                    if (feedback) {
                        clustaloTarget.mustHave2();
                    }
                    seqLimit = 2000;

                    break;

                case "kalign":
                    /** validation model for kalign:
                     * Input has to be in FASTA format and may comprise multiple sequences of varying lengths.
                     * Input must include at least two sequences.
                     * ALIGNED FASTA input is allowed.
                     * Sequences should have unique IDs; only the characters directly following the '>' sign, until the
                     * first space, in the header are used as ID.
                     * Limit the maximum number of sequences to 2000.
                     **/

                    let kalignTarget = new alignmentVal($(elem));
                    kalignTarget.basicValidation();

                    if (feedback) {
                        kalignTarget.mustHave2();
                    }
                    seqLimit = 2000;

                    break;

                case "msaprobs":
                    /** validation model for msaprobs:
                     * Input has to be in FASTA format and may comprise multiple sequences of varying lengths.
                     * Input must include at least two sequences.
                     * ALIGNED FASTA input is allowed.
                     * Sequences should have unique IDs; only the characters directly following the '>' sign, until the
                     * first space, in the header are used as ID.
                     * Limit the maximum number of sequences to 2000.
                     **/

                    let msaprobsTarget = new alignmentVal($(elem));
                    msaprobsTarget.basicValidation();

                    if (feedback) {
                        msaprobsTarget.mustHave2();
                    }
                    seqLimit = 2000;

                    break;

                case "hmmer":
                    /** validation model for hmmer:
                     * Input has to be a single FASTA sequence
                     * or aligned FASTA with at least two sequences.
                     * Sequences should have unique IDs; only the characters directly following the '>' sign, until the
                     * first space, in the header are used as ID.
                     */

                    let hmmerTarget = new alignmentVal($(elem));
                    hmmerTarget.basicValidation();

                    if (feedback) {
                        hmmerTarget.sameLengthValidation();
                    }

                    break;

                case "mmseqs2":
                    /** validation model for mmseq2:
                     * Input has to be in FASTA format and may comprise multiple sequences of varying lengths.
                     * ALIGNED FASTA input is allowed.
                     * Input must consist of at least two sequences.
                     * Sequences should have unique IDs; only the characters directly following the '>' sign, until the
                     * first space, in the header are used as ID.
                     * Limit the maximum number of sequences to 20000.
                     */

                    let mmseqs2Target = new alignmentVal($(elem));
                    mmseqs2Target.basicValidation();

                    if (feedback) {
                        mmseqs2Target.mustHave2();
                    }
                    seqLimit = 20000;

                    break;

                case "hhfilter":
                    /** validation model for hhfilter:
                     * Input has to be aligned FASTA.
                     * Input must consist of at least two Sequences.
                     * Sequences should have unique IDs; only the characters directly following the '>' sign, until the
                     * first space, in the header are used as ID.
                     * Limit the maximum number of sequences to 10000.
                     */

                    let hhfilterTarget = new alignmentVal($(elem));
                    hhfilterTarget.basicValidation();

                    if (feedback) {
                        mmseqs2Target.mustHave2();
                    }
                    seqLimit = 10000;

                    break;

                case "clans":
                /** validation model for clans:
                 * Input has to be in FASTA format and may comprise multiple sequences of varying lengths.
                 * Input must include at least two sequences.
                 * ALIGNED FASTA input is allowed.
                 * Sequences should have unique IDs; only the characters directly following the '>' sign, until the
                 * first space, in the header are used as ID.
                 * Limit the maximum number of sequences to 10000.
                 **/

                let clansTarget = new alignmentVal($(elem));
                    clansTarget.basicValidation();

                    if (feedback) {
                        clansTarget.mustHave2();
                    }
                    seqLimit = 10000;

                    break;

                default:
                    console.warn("No tool specified");
            }

            if($(elem).val().length === 0){
                valReset();
            }
        });
    }
};


function feedback(valid : boolean, msg : string = "unknown validation error", type : string = "error", wrongformat : boolean = false) : void {

    let $v = $("#validOrNot");

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

    let $v = $("#validOrNot");
    $v.hide();

}


//------------------------------ General Validators -------------------------------------------//

/**
 * alignment section specific validations
 * @param el
 */

// global filter to assert whether the original input has been FASTA
let originIsFasta : boolean = true;


/*function mustHave2(el : any) {

    if(el.validate('fasta') && el.reformat('numbers') < 2)
        feedback(false, "must have at least two sequences", "error");
    else if(el.validate('fasta') && el.reformat('numbers') >= 2 && el.reformat('alignment') && originIsFasta && el.reformat('uniqueids')) {
        feedback(true);
        originIsFasta = true;
    }
}
*/

// TODO standard validator for the search section. If search tools differ much in their validations, this one should be kept simple and small


//var searchVal = function(el) { /*...*/ };


// TODO other validators, similar story


//var classVal = function(el) { /*...*/ };
//var utilsVal = function(el) { /*...*/ };
//var secondaryVal = function(el) { /*...*/ };
//var ternaryVal = function(el) { /*...*/ };
//var seqAnalVal = function(el) { /*...*/ };


// this is the standard validator for the alignment section


interface ToolkitValidator {

    accept(visitor : Visitor) : any;

}


interface Visitor {

    visit(validator :  ToolkitValidator) : any;
}


class alignmentVal implements ToolkitValidator {

    elem: any;

    constructor(elem: any) {
        this.elem = elem;
    }


    accept(visitor: Visitor): void {

        visitor.visit(this);
    }


    basicValidation(): any {

        if (!this.elem.validate('fasta') && this.elem.reformat('detect') === '' && this.elem.val().length != 0)
            feedback(false, "This is no fasta!", "error");

        else if (this.elem.reformat('maxseqnumber', seqLimit))
            feedback(false, "Input contains too many sequences!", "error");

        else if (!this.elem.reformat('maxlength', 1000000))
            feedback(false, "Input too large!", "error");

        else if (!this.elem.reformat('uniqueids'))
            feedback(false, "FASTA but identifiers are not unique!", "error");

        else if (this.elem.reformat('dashes'))
            feedback(false, "Sequence contains only dots/dashes!", "error");

        else if (!this.elem.validate('fasta') && this.elem.reformat('detect') != '' && this.elem.val().length != 0) {
            originIsFasta = false;
            let t = this.elem.reformat('detect');
            feedback(false, "Wrong format found: " + t + ". <b>Auto-transformed to Fasta</b>", "success", t);
            $("#alignment").val(this.elem.reformat('fasta'));
        }

        else if (this.elem.validate('fasta') && this.elem.reformat('alignment')) {
            feedback(true);
        }

        else if (this.elem.val().length === 0)
            valReset();

    }

    sameLengthValidation(): any {

        if (!this.elem.reformat('samelength'))
            feedback(false, "Sequences should have the same length!", "error");
    }

    mustHave2() : any {

        if(this.elem.validate('fasta') && this.elem.reformat('numbers') < 2)
        feedback(false, "Must have at least two sequences!", "error");
    }

    mustHave1() : any {

        if(this.elem.validate('fasta') && this.elem.reformat('numbers') > 1){
            feedback(false, "Must have single sequence!", "error");
        }
    }

    DNAvalidation(): any {

        if(this.elem.validate('fasta') && this.elem.reformat('numbers') > 1)
            feedback(false, "Must have single sequence!", "error");

        else if (!this.elem.reformat('maxlength', 10000))
            feedback(false, "Input too large!", "error");

        else if(this.elem.reformat('type') != "DNA")
            feedback(false, "Illegal characters used!", "error");
    }
}




    /*
     var multiSeqVal - function(el) {

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

     else if (el.reformat('maxseqnumber', seqLimit)) {
     feedback(false, "Input contains too many sequences!", "error");
     }
     else if (!el.reformat('maxlength',1000000))
     feedback(false, "Input too large!", "error");

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
     feedback(true);
     $(".submitJob").prop("disabled", false);
     }

     else if (el.val().length === 0)
     valReset();
    }
}
*/




/*
 let seqoralignmentVal = function(el){

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

 else if(el.validate('fasta') && !el.reformat('alignment') && fasta2json(el.val()).length > 1){
 feedback(false, "not aligned FASTA", "error");
 $(".submitJob").prop("disabled", true);
 }
 else if(el.validate('fasta') && !el.reformat('alignment') && fasta2json(el.val()).length == 1){
 this.fastaStep2();
 }

 else if (el.val().length === 0)
 valReset();
 }; */