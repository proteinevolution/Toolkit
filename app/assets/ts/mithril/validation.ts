/**
 * Created by snam on 03.03.17.
 */
/// <reference path="parametercomponents.ts"/>

let seqLimit : any;
let charLimitPerSeq : any;
let modellerIsValid : boolean = false;
let samccIsValid : boolean = false;

let validation = function(elem : any, isInit : boolean, ctx : any) : any {
    if (!isInit) {
        let toolname: string;
        try {
            toolname = $("#toolnameAccess").val();
        }
        catch (err) {
            toolname = "unknown";
            console.warn("toolname unspecified");
        }
            if (toolname == 'hhpred')
                elem = $("[name='alignment']");

            let path = window.location.href;
            let url = path.split("/");

            if (url[url.length - 2] != 'jobs') {

                $('#pasteButton').on('click', function () {
                    m.redraw(true);
                    setTimeout(function () {
                        validationProcess($(elem), toolname)
                    }, 200);
                    $(elem).focus();
                });
                $('.inputDBs').on('change', function () {
                    validationProcess($(elem), toolname)
                });
            }
        validationProcess($(elem), toolname);
        return $(elem).on("input", function (e) {
            validationProcess($(elem), toolname);
        });
    }
};



let validationProcess = function(elem: any,toolname: string) {
    //---------------------------------Validation Visitors------------------------------------------//

    // in order to modularize validation we use the visitor pattern

    /*let mustHave2Visitor = {
     visit : function(alignmentVal : any) {
     alignmentVal.fastaStep2 = mustHave2($(elem));
     }
     };*/


    //---------------------------------------------------------------------------------------------//
    switch (toolname) {
        case "tcoffee":
            /** validation model for tcoffee:
             * Input has to be in FASTA format and may comprise multiple sequences of varying lengths.
             * Input must include at least two sequences.
             * ALIGNED FASTA input is allowed.
             * Sequences should have unique IDs; only the characters directly following the '>' sign, until the
             * first space, in the header are used as ID.
             * Limit the maximum number of sequences to 500.
             **/

            seqLimit = 500;


            let tcoffeeTarget = new alignmentVal($(elem));

            if (tcoffeeTarget.basicValidation("no")) {
                tcoffeeTarget.mustHave2();
            }

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


            seqLimit = 2000;

            let mafftTarget = new alignmentVal($(elem));

            if (mafftTarget.basicValidation("no")) {
                mafftTarget.mustHave2();
            }

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

            seqLimit = 2000;

            let muscleTarget = new alignmentVal($(elem));

            if (muscleTarget.basicValidation("no")) {
                muscleTarget.mustHave2();
            }

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

            seqLimit = 2000;

            let clustaloTarget = new alignmentVal($(elem));


            if (clustaloTarget.basicValidation("no")) {
                clustaloTarget.mustHave2();
            }

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

            seqLimit = 2000;

            let kalignTarget = new alignmentVal($(elem));


            if (kalignTarget.basicValidation("no")) {
                kalignTarget.mustHave2();
            }

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

            seqLimit = 2000;

            let msaprobsTarget = new alignmentVal($(elem));

            if (msaprobsTarget.basicValidation("no")) {
                msaprobsTarget.mustHave2();
            }

            break;

        case "hmmer":
            /** validation model for hmmer:
             * Input has to be a single FASTA sequence
             * or aligned FASTA with at least two sequences.
             * Sequences should have unique IDs; only the characters directly following the '>' sign, until the
             * first space, in the header are used as ID.
             */

            charLimitPerSeq = 3000;
            seqLimit = 10000;

            let hmmerTarget = new alignmentVal($(elem));

            if (hmmerTarget.basicValidation("yes")) {
                hmmerTarget.sameLengthValidation();

                if(hmmerTarget.hasTwo()) {
                    $("#max_hhblits_iter").val("0");
                    $("#max_hhblits_iter").niceSelect('update');
                }
                else {
                    $("#max_hhblits_iter").val("1");
                    $("#max_hhblits_iter").niceSelect('update');
                }
            }

            break;

        case "hhblits":
            /** validation model for hhblits:
             * Input has to be a single FASTA sequence
             * or aligned FASTA with at least two sequences.
             * Sequences should have unique IDs; only the characters directly following the '>' sign, until the
             * first space, in the header are used as ID.
             */
            charLimitPerSeq = 3000;
            seqLimit = 10000;

            let hhblitsTarget = new alignmentVal($(elem));

            if (hhblitsTarget.basicValidation("yes")) {
                hhblitsTarget.sameLengthValidation();
            }

            break;

        case "hhpred":
            /** validation model for hhpred:
             * Input has to be a single FASTA sequence
             * or aligned FASTA with at least two sequences.
             * Sequences should have unique IDs; only the characters directly following the '>' sign, until the
             * first space, in the header are used as ID.
             */

            charLimitPerSeq = 3000; // TODO: why was the charLimit defined after it's usage?
            seqLimit = 10000;

            let hhpredTarget = new alignmentVal($(elem));


            if (hhpredTarget.basicValidation("yes")) {
                hhpredTarget.sameLengthValidation();
                hhpredTarget.hhMaxDB();

                if(hhpredTarget.hasTwo()) {
                    $("#msa_gen_max_iter").val("0");
                    $("#msa_gen_max_iter").niceSelect('update');
                }
                else {
                    $("#msa_gen_max_iter").val("4");
                    $("#msa_gen_max_iter").niceSelect('update');
                }
            }

            break;

        case "hhomp":
            /** validation model for hhomp:
             * Input has to be a single FASTA sequence
             * or aligned FASTA with at least two sequences.
             * only the characters directly following the '>' sign, until the
             * first space, in the header are used as ID.
             */

            charLimitPerSeq = 3000; // TODO: why was the charLimit defined after it's usage?
            seqLimit = 10000;

            let hhompTarget = new alignmentVal($(elem));


            if (hhompTarget.basicValidation("yes")) {
                hhompTarget.sameLengthValidation();

                if(hhompTarget.hasTwo()) {
                    $("#msa_gen_max_iter").val("0");
                    $("#msa_gen_max_iter").niceSelect('update');
                }
                else {
                    $("#msa_gen_max_iter").val("4");
                    $("#msa_gen_max_iter").niceSelect('update');
                }
            }

            break;

        case "psiblast":
            /** validation model for psiblast:
             * Input has to be a single FASTA sequence
             * or aligned FASTA with at least two sequences.
             * Sequences should have unique IDs; only the characters directly following the '>' sign, until the
             * first space, in the header are used as ID.
             */
            charLimitPerSeq = 10000;
            seqLimit = 5000;

            let psiblastTarget = new alignmentVal($(elem));

            if (psiblastTarget.basicValidation("yes")) {
                psiblastTarget.sameLengthValidation();
            }

            break;

        case "patsearch":
            /** validation model for patsearch:
             * Input has to be a single line without spaces, and the first character may not be '>'
             */

            let patsearchTarget = new alignmentVal($(elem));
            patsearchTarget.patternSearchValidation();

            break;

        case "aln2plot":
            charLimitPerSeq = 10000;
            seqLimit = 2000;

            let aln2plotTarget = new alignmentVal($(elem));


            if (aln2plotTarget.basicValidation("yes")) {
                if (aln2plotTarget.sameLengthValidation())
                    aln2plotTarget.mustHave2();
            }


            break;

        case "frpred":

            charLimitPerSeq = 3000;
            seqLimit = 2000;

            let frpredTarget = new alignmentVal($(elem));


            if (frpredTarget.basicValidation("yes")) {
                frpredTarget.sameLengthValidation();
            }

            break;

        case "hhrepid":

            charLimitPerSeq = 3000;
            seqLimit = 10000;

            let hhrepidTarget = new alignmentVal($(elem));

            if (hhrepidTarget.basicValidation("yes")) {
                hhrepidTarget.sameLengthValidation();

                if(hhrepidTarget.hasTwo()) {
                    $("#msa_gen_max_iter").val("0");
                    $("#msa_gen_max_iter").niceSelect('update');
                }
                else {
                    $("#msa_gen_max_iter").val("4");
                    $("#msa_gen_max_iter").niceSelect('update');
                }
            }
            break;


        case "pcoils":

            charLimitPerSeq = 6000;
            seqLimit = 2000;

            let pcoilsTarget = new alignmentVal($(elem));

            if (pcoilsTarget.basicValidation("yes")) {
                pcoilsTarget.sameLengthValidation();
            }

            break;

        case "repper":

            charLimitPerSeq = 10000;
            seqLimit = 2000;

            let repperTarget = new alignmentVal($(elem));

            if (repperTarget.basicValidation("yes")) {
                repperTarget.sameLengthValidation();
            }

            break;

        case "marcoil":

            charLimitPerSeq = 10000;
            seqLimit = 2000;

            let marcoilTarget = new alignmentVal($(elem));

            if (marcoilTarget.basicValidation("yes")) {
                marcoilTarget.mustHave1();
            }

            break;

        case "tprpred":

            charLimitPerSeq = 10000;


            let tprpredTarget = new alignmentVal($(elem));

            if (tprpredTarget.basicValidation("yes")) {
                tprpredTarget.mustHave1();
            }

            break;

        case "ali2d":

            charLimitPerSeq = 3000;
            seqLimit = 100;

            let ali2dTarget = new alignmentVal($(elem));

            if (ali2dTarget.basicValidation("yes")) {
                if (ali2dTarget.sameLengthValidation())
                    ali2dTarget.mustHave2();
            }

            break;

        case "quick2d":

            charLimitPerSeq = 3000;
            seqLimit = 2000;

            let quick2dTarget = new alignmentVal($(elem));

            if (quick2dTarget.basicValidation("yes")) {
                quick2dTarget.sameLengthValidation();

                if(quick2dTarget.hasTwo()) {
                    $("#quick_iters").val("0");
                    $("#quick_iters").niceSelect('update');
                } else {
                    $("#quick_iters").val("3");
                    $("#quick_iters").niceSelect('update');
                }
            }

            break;

        case "modeller":
            let modellerTarget = new alignmentVal($(elem));
            modellerTarget.modellerValidation();

            break;

        case "samcc":

            let samccTarget = new alignmentVal($(elem));
            samccTarget.samccValidation();

            break;

        case "ancescon":

            charLimitPerSeq = 3000;
            seqLimit = 2000;

            let ancesconTarget = new alignmentVal($(elem));

            if (ancesconTarget.basicValidation("yes")) {
                if (ancesconTarget.sameLengthValidation())
                    ancesconTarget.mustHave2();
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

            charLimitPerSeq = 30000;
            seqLimit = 20000;

            let mmseqs2Target = new alignmentVal($(elem));

            if (mmseqs2Target.basicValidation("yes")) {
                mmseqs2Target.mustHave2();
            }

            break;

        case "phyml":

            seqLimit = 100;

            let phymlTarget = new alignmentVal($(elem));

            if (phymlTarget.basicValidation("yes")) {
                if (phymlTarget.sameLengthValidation())
                    phymlTarget.mustHave2();
            }


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

            charLimitPerSeq = 20000;
            seqLimit = 10000;

            let clansTarget = new alignmentVal($(elem));

            if (clansTarget.basicValidation("yes")) {
                clansTarget.mustHave2();
            }

            break;

        case "sixframe":

            let sixframetranslationTarget = new alignmentVal($(elem));
            sixframetranslationTarget.DNAvalidation();

            break;

        case "backtrans":

            let backtransTarget = new alignmentVal($(elem));

            if (backtransTarget.basicValidation("yes")) {
                backtransTarget.mustHave1();
            }

            break;

        case "hhfilter":
            /** validation model for hhfilter:
             * Input has to be aligned FASTA.
             * Input must consist of at least two Sequences.
             * Sequences should have unique IDs; only the characters directly following the '>' sign, until the
             * first space, in the header are used as ID.
             * Limit the maximum number of sequences to 10000.
             */
            charLimitPerSeq = 3000;
            seqLimit = 10000;

            let hhfilterTarget = new alignmentVal($(elem));

            if (hhfilterTarget.basicValidation("yes")) {
                if(hhfilterTarget.sameLengthValidation())
                    hhfilterTarget.mustHave2();
            }

            break;

        case "retseq":
            let retseqTarget = new alignmentVal($(elem));
            retseqTarget.retSeqValidation();

            break;

        case "seq2id":
            /** validation model for seq2id: TODO is this model appropriate (copied from hhfilter)?
             * Input has to be aligned FASTA.
             * Input must consist of at least two Sequences.
             * Sequences should have unique IDs; only the characters directly following the '>' sign, until the
             * first space, in the header are used as ID.
             * Limit the maximum number of sequences to 10000.
             */

            let seq2idTarget = new alignmentVal($(elem));

            seq2idTarget.seq2IDvalidation();

            break;

        default:
            console.warn("No tool specified");
            break;
    }

    if ($(elem).val().length === 0) {
        valReset();
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
        $(".submitJob").prop("disabled", true);
        $v.css("display", "block").html(msg).addClass(type);
    }
    else if(valid){
        $(".submitJob").prop("disabled", false);
        $v.css("display", "block").html(msg).addClass(type);
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

    // Limit HHpred DB
    hhMaxDB(): boolean{
        if ($("#hhsuitedb").val().length + $("#proteomes").val().length > 4) {
            feedback(false, "Only 4 databases may be selected at a time!", "error");
            return false;
        }else{
            return true;
        }
    }

    basicValidation(checkNucleotide: string): boolean {

        if($("#fileUpload").val() !== "") {
            feedback(true, "Uploaded file", "success");
            return true;
        }
	    else if (this.elem.val() !== "" && !this.elem.validate('fasta') && this.elem.reformat('detect') !== '') {
            originIsFasta = false;
            let t = this.elem.reformat('detect');
            feedback(false, t + " format found:  <b>Auto-transformed to Fasta</b>", "success", t);
            $("#alignment").val(this.elem.reformat('fasta'));
            return true;
        }
        else if (this.elem.val() !== "" && !this.elem.validate('fasta')) {
            feedback(false, "Invalid characters!", "error");
            return false;
        }
        else if(!this.elem.reformat('PROTEINLETTERS')){
            feedback(false, "Invalid characters!", "error");
            return false;
        }
        else if(checkNucleotide === "yes" && !this.elem.reformat('PROTEIN')){
            feedback(false, "Input contains nucleotide sequence(s). Expecting protein sequence(s).", "error");
            return false;
        }
        else if(checkNucleotide === "no" && !this.elem.reformat('PROTEIN')){
            feedback(true, "Nucleotide FASTA.", "success");
        }
        else if ((/^\n$/m.test(this.elem.reformat('extractheaders')))) {
            feedback(false, "Empty header!", "error");
            return false;
        }

        else if (this.elem.reformat('maxseqnumber', seqLimit)) {
            feedback(false, "Input contains more than " + seqLimit + " sequences!", "error");
            return false;
        }

        else if (!this.elem.reformat('maxseqlength', charLimitPerSeq)) {
            feedback(false, "Input exceeds maximum allowed sequence length of " + charLimitPerSeq + "!", "error");
            return false;
        }

        else if (!this.elem.reformat('maxlength', 20000000)) {
            feedback(false, "Input contains over twenty million characters!", "error");
            return false;
        }

        else if (this.elem.reformat('dashes')) {
            feedback(false, "Sequence contains only dots/dashes!", "error");
            return false;
        }

        else if (!this.elem.reformat('uniqueids')) {
            feedback(true, "FASTA but identifiers are not unique!", "warning");
            return true;
        }
	    else if (this.elem.val() === "") {
            feedback(false);
            valReset();
        } 

        else feedback(true, "<b>Protein FASTA</b>", "success");

        return true;

    }

    sameLengthValidation(): boolean {
        if($("#fileUpload").val() !== "") {
            feedback(true, "Uploaded file", "success");
            return true;
        }
        else if (!this.elem.reformat('samelength')) {
            feedback(false, "Invalid MSA! Sequences should have the same length.", "error");
            return false;
        }
        return true;

    }

    mustHave2() : boolean {
        if($("#fileUpload").val() !== "") {
            feedback(true, "Uploaded file", "success");
            return true;
        }
        else if(this.elem.validate('fasta') && this.elem.reformat('numbers') < 2) {
            feedback(false, "Must have at least two sequences!", "error");
            return false;
        }
        return true;
    }

    mustHave1() : boolean {
        if($("#fileUpload").val() !== "") {
            feedback(true, "Uploaded file", "success");
            return true;
        }
        else if (this.elem.validate('fasta') && this.elem.reformat('numbers') > 1){
            feedback(false, "Input must be a single protein sequence!", "error");
            return false;
        }
        return true;
    }

    hasTwo() : boolean {

        if($("#fileUpload").val() !== "") {
            feedback(true, "Uploaded file", "success");
            return true;
        }
        else if (this.elem.reformat('numbers') > 1){
            return true;
        }
       return false;
    }


    DNAvalidation(): any {

        console.log(this.elem.reformat('DNA'));
        if($("#fileUpload").val() !== "") {
            feedback(true, "Uploaded file", "success");
            return true;
        }
        if (!this.elem.validate('fasta')) {
            feedback(false, "This is no FASTA!", "error");
            return false;
        }

        else if (this.elem.validate('fasta') && this.elem.reformat('numbers') > 1){
            feedback(false, "Input must be a single DNA sequence!", "error");
            return false;
        }

        else if ((/^\n$/m.test(this.elem.reformat('extractheaders')))){
            feedback(false, "Empty header!", "error");
            return false;
        }

        else if (!this.elem.reformat('maxlength', 10000)){
            feedback(false, "Input contains over 10,000 characters!", "error");
            return false;
        }

        else if(!this.elem.reformat('DNA')){
            feedback(false, "Invalid characters! Expecting a DNA sequence.", "error");
            return false;
        }

        else if (this.elem.val() == "") {
            feedback(false);
            valReset();
        }

        else feedback(true, "<b>Nucleotide FASTA</b>", "success");
        return true;
    }

    seq2IDvalidation(): any {
        if($("#fileUpload").val() !== "") {
            feedback(true, "Uploaded file", "success");
        }
        else if (!this.elem.validate('fasta') && this.elem.reformat('detect') != '') {
            originIsFasta = false;
            let t = this.elem.reformat('detect');
            feedback(false, t + " format found:  <b>Auto-transformed to Fasta</b>", "success", t);
            $("#alignment").val(this.elem.reformat('fasta'));
        }

        else if(this.elem.reformat('extractheaders') === false)
            feedback(false, "At least one header required!", "error");

        else if ((/^\n$/m.test(this.elem.reformat('extractheaders'))))
            feedback(false, "Empty header!", "error");

        else if (!this.elem.reformat('maxlength', 10000000))
            feedback(false, "Input contains over two million characters!", "error");

        else if (this.elem.reformat('maxheadernumber', 20000))
            feedback(false, "Input contains over 20,000 headers!", "error");

        else if (this.elem.reformat('extractheaders') !== "")
            feedback(true, "Valid input", "success");

        else if (this.elem.val() == "") {
            feedback(false);
            valReset();
        }
    }

    patternSearchValidation(): any {
        if($("#fileUpload").val() !== "") {
            feedback(true, "Uploaded file", "success");
        }
        else if (!this.elem.reformat('line')) {
            feedback(false, "Please enter a valid regular expression/PROSITE grammar!", "error");
        }
        else if (/\s/.test(this.elem.val()))
            feedback(false, "Input must not contain spaces!", "error");

        else if (!this.elem.reformat('maxlength', 100))
            feedback(false, "Input contains over 100 characters!", "error");

        else if (this.elem.reformat('line'))
            feedback(true, "Valid input", "success");

        else if (this.elem.val() == ""){
            feedback(false);
            valReset();
        }
    }

    modellerValidation(): any {
        modellerIsValid = false;
        if($("#fileUpload").val() !== "") {
            feedback(true, "Uploaded file", "success");
            modellerIsValid = true;
        }
        else if (!this.elem.validate('pir'))
            feedback(false, "MODELLER only works with PIR alignments forwarded by HHpred.", "error");

        else if (!this.elem.reformat('star'))
            feedback(false, "Every sequence must end with a star!", "error");

        else if (!this.elem.reformat('samelength'))
            feedback(false, "Sequences should have the same length!", "error");

        else if (this.elem.reformat('numbers') < 2)
            feedback(false, "Must have at least two sequences!", "error");

        else if (this.elem.val() == "") {
            feedback(false);
            valReset();
        }
        else if(!ParameterModellerKeyComponent.keyStored){
            feedback(false, "Please enter your MODELLER-key!", "error");
        }

        else {
            feedback(true, "Valid PIR alignment.", "success");
            modellerIsValid = true;
        }
    }

    samccValidation(): any {

        samccIsValid = false;
        if($("#fileUpload").val() !== "") {
            feedback(true, "Uploaded file", "success");
            samccIsValid = true;
        }
        else if(!this.elem.reformat('atoms'))
            feedback(false, "Must contain at least 28 PDB ATOM records", "error");
        else if (this.elem.val() == "") {
            feedback(false);
            valReset();
        }

        else {
            $("#validOrNot").css("display", "block").html("Valid input").removeClass("alert").addClass("success");
            samccIsValid = true;
        }
    }

    //retseq validation is only a stub
    retSeqValidation(): any {
        if($("#fileUpload").val() !== "") {
            feedback(true, "Uploaded file", "success");
        }
        else if(this.elem.val() != "")
            feedback(true, "Valid input", "success");

        else if (this.elem.val() == "") {
            feedback(false);
            valReset();
        }
    }
}
