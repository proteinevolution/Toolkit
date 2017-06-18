/**
 * Created by snam on 03.03.17.
 */
/// <reference path="parametercomponents.ts"/>

let seqLimit : any;
let charLimitPerSeq : any;
let modellerIsValid : boolean = false;
let samccIsValid : boolean = false;



let validation = function(elem : any, isInit : boolean, ctx : any) : any {

        if(!isInit) {

            let toolname : string, placeholder :  string;
            try { toolname = $("#toolnameAccess").val(); }
            catch(err) {
                toolname = "unknown";
                console.warn("toolname unspecified");
            }


            let linebreak = function(elem : any, placeholder : string) {

                let pastedContent = $(elem).val();
                if (pastedContent == '') {
                    if (toolname == 'hhpred')
                        elem = $("[name='alignment']");

                    let path = window.location.href;
                    let url = path.split("/");

                    if (url[url.length - 2] != 'jobs') {
                        $(elem).val(placeholder).css('color', 'grey');
                        $(elem).focus(function () {
                            if ($(elem).val() === placeholder) {
                                $(elem).attr('value', '').css('color', '#0a0a0a');
                                m.redraw(true);
                            }
                        });
                        $('#pasteButton').on('click', function () {
                            $(elem).css('color', '#0a0a0a');
                            m.redraw(true);
                            setTimeout(function () {
                                validationProcess($(elem), toolname)
                            }, 200);
                            $(elem).focus();
                        });
                        $('.inputDBs').on('change', function () {
                            setTimeout(function () {
                                validationProcess($(elem), toolname)
                            }, 200);
                        });
                        $(elem).blur(function () {
                            if ($(elem).val() === '') {
                                $(elem).attr('value', placeholder).css('color', 'grey');
                                m.redraw(true);
                            }
                        });
                    }
                } else {
                    m.redraw(true);
                    setTimeout(function () {
                        $(elem).css('color', '#0a0a0a');
                        $(elem).val(pastedContent);
                        validationProcess($(elem), toolname)
                    }, 200);
                    $(elem).focus();
                }
            };


            // Placeholder overrides

            switch(toolname) {

                case "hhblits":
                    $(elem).attr("placeholder", "Enter a protein sequence/multiple sequence alignment in FASTA/CLUSTAL format.");
                    break;

                case "hhpred":
                    placeholder = "Enter a protein sequence/multiple sequence alignment in FASTA/CLUSTAL format. \n\nTo create a structural model of your query protein, run a HHpred search with it against the PDB_mmCIF70 database, select the top-scoring template(s) and click on 'Create model using selection'. This will generate a PIR file that can be subsequently submitted to MODELLER.";
                    linebreak($(elem), placeholder);
                    break;

                case "hmmer":
                    $(elem).attr("placeholder", "Enter a protein sequence/multiple sequence alignment in FASTA/CLUSTAL format.");
                    break;

                case "psiblast":
                    $(elem).attr("placeholder", "Enter a protein sequence/multiple sequence alignment in FASTA/CLUSTAL format.");
                    break;

                case "patsearch":
                    $(elem).attr("placeholder", "Enter a PROSITE grammar/regular expression");
                    break;

                case "clustalo":
                    $(elem).attr("placeholder", "Enter up to 2000 protein sequences in FASTA format");
                    break;

                case "kalign":
                    $(elem).attr("placeholder", "Enter up to 2000 protein sequences in FASTA format");
                    break;

                case "mafft":
                    $(elem).attr("placeholder", "Enter up to 2000 protein sequences in FASTA format");
                    break;

                case "msaprobs":
                    $(elem).attr("placeholder", "Enter up to 2000 protein sequences in FASTA format");
                    break;

                case "muscle":
                    $(elem).attr("placeholder", "Enter up to 2000 protein sequences in FASTA format");
                    break;

                case "tcoffee":
                    $(elem).attr("placeholder", "Enter up to 500 protein sequences in FASTA format");
                    break;

                case "aln2plot":
                    $(elem).attr("placeholder", "Enter a protein multiple sequence alignment with up to 2000 sequences in FASTA/CLUSTAL format");
                    break;

                case "frpred":
                    $(elem).attr("placeholder", "Enter a protein sequence/multiple sequence alignment with up to 2000 sequences in FASTA/CLUSTAL format");
                    break;

                case "hhrepid":
                    $(elem).attr("placeholder", "Enter a protein sequence/multiple sequence alignment with up to 2000 sequences in FASTA/CLUSTAL format");
                    break;

                case "marcoil":
                    $(elem).attr("placeholder", "Enter a protein sequence in FASTA format");
                    break;

                case "pcoils":
                    $(elem).attr("placeholder", "Enter a protein sequence/multiple sequence alignment with up to 2000 sequences in FASTA/CLUSTAL format");
                    break;

                case "repper":
                    $(elem).attr("placeholder", "Enter a protein sequence/multiple sequence alignment with up to 2000 sequences in FASTA/CLUSTAL format");
                    break;

                case "tprpred":
                    $(elem).attr("placeholder", "Enter a protein sequence in FASTA format");
                    break;

                case "ali2d":
                    placeholder = "Enter a protein multiple sequence alignment with up to 100 sequences in FASTA/CLUSTAL format.\n\n\nPlease note: Runtime of ~30 mins for N=100 sequences of length L=200. Scales as N*L.";
                    linebreak($(elem), placeholder);
                    break;

                case "quick2d":
                    $(elem).attr("placeholder", "Enter a protein sequence/multiple sequence alignment with up to 2000 sequences in FASTA/CLUSTAL format");
                    break;

                case "modeller":
                    placeholder = "Please note: MODELLER is configured to work with PIR alignments forwarded by HHpred. \n\nRun a HHpred search with your query, select the top-scoring templates and click on 'Create model using selection'. This will generate a PIR file that can be subsequently submitted to MODELLER. \n\nTo obtain a key for MODELLER go to: http://salilab.org/modeller/registration.shtml.";
                    linebreak($(elem), placeholder);
                    break;

                case "samcc":
                    placeholder = "Enter PDB coordinates of a four-helical bundle.\n\nNote: The definitions for helices below need to be entered according to their sequential position in the bundle (it is not relevant whether this done clockwise or counterclockwise, and whether one starts with the N-terminal helix or any other one), and not in their order from N- to C-terminus. For helices in anti-parallel orientation, the residue range should be given with the larger residue number before the smaller one.";
                    linebreak($(elem), placeholder);
                    break;

                case "ancescon":
                    $(elem).attr("placeholder", "Enter a protein multiple sequence alignment with up to 2000 sequences in FASTA/CLUSTAL format");
                    break;

                case "clans":
                    $(elem).attr("placeholder", "Enter protein sequences with up to 10000 sequences in FASTA format");
                    break;

                case "mmseqs2":
                    $(elem).attr("placeholder", "Enter up to 20000 protein sequences in FASTA format");
                    break;

                case "phyml":
                    $(elem).attr("placeholder", "Enter a protein multiple sequence alignment with up to 100 sequences in FASTA/CLUSTAL format");
                    break;

                case "sixframe":
                    $(elem).attr("placeholder", "Enter a DNA sequence in FASTA format");
                    break;

                case "backtrans":
                    $(elem).attr("placeholder", "Enter a protein sequence");
                    break;

                case "hhfilter":
                    $(elem).attr("placeholder", "Enter a protein multiple sequence alignment with up to 2000 sequences in FASTA/CLUSTAL format");
                    break;

                case "retseq":
                    $(elem).attr("placeholder", "Enter a newline separated list of identifiers and choose the corresponding database");
                    break;

                case "seq2id":
                    $(elem).attr("placeholder", "Enter protein sequences (or their headers) in FASTA format");
                    break;

                default:
                    break;

            }




            return $(elem).on("input", function (e) {

                validationProcess(elem, toolname);
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
            tcoffeeTarget.basicValidation();

            if (tcoffeeTarget.basicValidation()) {
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
            mafftTarget.basicValidation();

            if (mafftTarget.basicValidation()) {
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
            muscleTarget.basicValidation();

            if (muscleTarget.basicValidation()) {
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
            clustaloTarget.basicValidation();

            if (clustaloTarget.basicValidation()) {
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
            kalignTarget.basicValidation();

            if (kalignTarget.basicValidation()) {
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
            msaprobsTarget.basicValidation();

            if (msaprobsTarget.basicValidation()) {
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

            seqLimit = 999999;

            let hmmerTarget = new alignmentVal($(elem));
            hmmerTarget.basicValidation();

            if (hmmerTarget.basicValidation()) {
                hmmerTarget.sameLengthValidation();
            }

            break;

        case "hhblits":
            /** validation model for hhblits:
             * Input has to be a single FASTA sequence
             * or aligned FASTA with at least two sequences.
             * Sequences should have unique IDs; only the characters directly following the '>' sign, until the
             * first space, in the header are used as ID.
             */

            seqLimit = 999999;

            let hhblitsTarget = new alignmentVal($(elem));
            hhblitsTarget.basicValidation();

            if (hhblitsTarget.basicValidation()) {
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

            charLimitPerSeq = 30000; // TODO: why was the charLimit defined after it's usage?
            seqLimit = 999999;

            let hhpredTarget = new alignmentVal($(elem));
            hhpredTarget.basicValidation();

            if (hhpredTarget.basicValidation()) {
                hhpredTarget.sameLengthValidation();
                hhpredTarget.hhMaxDB();

            }

            break;

        case "psiblast":
            /** validation model for psiblast:
             * Input has to be a single FASTA sequence
             * or aligned FASTA with at least two sequences.
             * Sequences should have unique IDs; only the characters directly following the '>' sign, until the
             * first space, in the header are used as ID.
             */

            seqLimit = 999999;

            let psiblastTarget = new alignmentVal($(elem));
            psiblastTarget.basicValidation();

            if (psiblastTarget.basicValidation()) {
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

            seqLimit = 2000;

            let aln2plotTarget = new alignmentVal($(elem));
            aln2plotTarget.basicValidation();

            if (aln2plotTarget.basicValidation()) {
                aln2plotTarget.sameLengthValidation();
                if (aln2plotTarget.sameLengthValidation())
                    aln2plotTarget.mustHave2();
            }


            break;

        case "frpred":

            seqLimit = 2000;

            let frpredTarget = new alignmentVal($(elem));
            frpredTarget.basicValidation();

            if (frpredTarget.basicValidation()) {
                frpredTarget.sameLengthValidation();
            }

            break;

        case "hhrepid":

            seqLimit = 2000;

            let hhrepidTarget = new alignmentVal($(elem));
            hhrepidTarget.basicValidation();

            if (hhrepidTarget.basicValidation()) {
                hhrepidTarget.sameLengthValidation();
            }

            break;

        case "pcoils":

            seqLimit = 2000;

            let pcoilsTarget = new alignmentVal($(elem));
            pcoilsTarget.basicValidation();

            if (pcoilsTarget.basicValidation()) {
                pcoilsTarget.sameLengthValidation();
            }

            break;

        case "repper":

            seqLimit = 2000;

            let repperTarget = new alignmentVal($(elem));
            repperTarget.basicValidation();

            if (repperTarget.basicValidation()) {
                repperTarget.sameLengthValidation();
            }

            break;

        case "marcoil":

            seqLimit = 2000;

            let marcoilTarget = new alignmentVal($(elem));
            marcoilTarget.basicValidation();

            if (marcoilTarget.basicValidation()) {
                marcoilTarget.mustHave1();
            }

            break;

        case "tprpred":

            let tprpredTarget = new alignmentVal($(elem));
            tprpredTarget.basicValidation();

            if (tprpredTarget.basicValidation()) {
                tprpredTarget.mustHave1();
            }

            break;

        case "ali2d":

            seqLimit = 100;

            let ali2dTarget = new alignmentVal($(elem));
            ali2dTarget.basicValidation();

            if (ali2dTarget.basicValidation()) {
                ali2dTarget.sameLengthValidation();
                if (ali2dTarget.sameLengthValidation())
                    ali2dTarget.mustHave2();
            }

            break;

        case "quick2d":

            seqLimit = 2000;

            let quick2dTarget = new alignmentVal($(elem));
            quick2dTarget.basicValidation();

            if (quick2dTarget.basicValidation()) {
                quick2dTarget.sameLengthValidation();
            }

            break;

        case "modeller":

            let modellerTarget = new alignmentVal($(elem));
            modellerTarget.modellerValidation();
            if(ParameterModellerKeyComponent.keyStored){
                $(".submitJob").attr("disabled", false);
            }
            break;

        case "samcc":

            let samccTarget = new alignmentVal($(elem));
            samccTarget.samccValidation();

            break;

        case "ancescon":

            seqLimit = 2000;

            let ancesconTarget = new alignmentVal($(elem));
            ancesconTarget.basicValidation();

            if (ancesconTarget.basicValidation()) {
                ancesconTarget.sameLengthValidation();
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

            seqLimit = 20000;

            let mmseqs2Target = new alignmentVal($(elem));
            mmseqs2Target.basicValidation();

            if (mmseqs2Target.basicValidation()) {
                mmseqs2Target.mustHave2();
            }

            break;

        case "phyml":

            seqLimit = 100;

            let phymlTarget = new alignmentVal($(elem));
            phymlTarget.basicValidation();

            if (phymlTarget.basicValidation()) {
                phymlTarget.sameLengthValidation();
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

            seqLimit = 10000;

            let clansTarget = new alignmentVal($(elem));
            clansTarget.basicValidation();

            if (clansTarget.basicValidation()) {
                clansTarget.mustHave2();
            }

            break;

        case "sixframe":

            let sixframetranslationTarget = new alignmentVal($(elem));
            sixframetranslationTarget.DNAvalidation();

            break;

        case "backtrans":

            let backtransTarget = new alignmentVal($(elem));
            backtransTarget.basicValidation();

            if (backtransTarget.basicValidation()) {
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

            seqLimit = 10000;

            let hhfilterTarget = new alignmentVal($(elem));
            hhfilterTarget.basicValidation();

            if (hhfilterTarget.basicValidation()) {
                hhfilterTarget.mustHave2();
            }

            break;

        case "retseq":
            let retseqTarget = new alignmentVal($(elem));
            retseqTarget.retSeqValidation();

            break;

        case "seq2id":
            /** validation model for hhfilter:
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
        if ($("#hhsuitedb").val().length + $("#proteomes").val().length > 6) {
            feedback(false, "Only 6 databases may be selected at a time!", "error");
            return false;
        }else{
            return true;
        }
    }

    basicValidation(): boolean {

        if (this.elem.val() !== "" && !this.elem.validate('fasta') && this.elem.reformat('detect') !== '') {
            originIsFasta = false;
            let t = this.elem.reformat('detect');
            feedback(false, t + " format found:  <b>Auto-transformed to Fasta</b>", "success", t);
            $("#alignment").val(this.elem.reformat('fasta'));
            return true;
        }

        else if (this.elem.val() !== "" && !this.elem.validate('fasta')) {
            feedback(false, "This is no Fasta!", "error");
            return false;
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
            feedback(false, "Input contains more than " + charLimitPerSeq + " chars in a sequence!", "error");
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
            feedback(true, "Fasta but identifiers are not unique!", "warning");
            return true;
        }

        else if (this.elem.val() === "") {
            feedback(false);
            valReset();
        }

        else feedback(true, "Found format: <b>Fasta</b>", "success");

        return true;

    }

    sameLengthValidation(): boolean {

        if (!this.elem.reformat('samelength')) {
            feedback(false, "Sequences should have the same length!", "error");
            return false;
        }
        return true;

    }

    mustHave2() : boolean {

        if(this.elem.validate('fasta') && this.elem.reformat('numbers') < 2) {
            feedback(false, "Must have at least two sequences!", "error");
            return false;
        }
        return true;
    }

    mustHave1() : boolean {

        if (this.elem.validate('fasta') && this.elem.reformat('numbers') > 1){
            feedback(false, "Must have single sequence!", "error");
            return false;
        }
        return true;
    }

    DNAvalidation(): any {

        if (!this.elem.validate('fasta'))
            feedback(false, "This is no Fasta!", "error");

        else if (this.elem.validate('fasta') && this.elem.reformat('numbers') > 1)
            feedback(false, "Must have single sequence!", "error");

        else if ((/^\n$/m.test(this.elem.reformat('extractheaders'))))
            feedback(false, "Empty header!", "error");

        else if (!this.elem.reformat('maxlength', 10000))
            feedback(false, "Input contains over 10,000 characters!", "error");

        else if(!this.elem.reformat('DNA'))
            feedback(false, "Illegal characters used!", "error");

        else if (this.elem.val() == "") {
            feedback(false);
            valReset();
        }

        else feedback(true, "Found format: <b>Fasta</b>", "success");
    }

    seq2IDvalidation(): any {

        if (!this.elem.validate('fasta') && this.elem.reformat('detect') != '') {
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

        if (!this.elem.reformat('line'))
            feedback(false, "Input has to be one line!", "error");

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

        if (!this.elem.validate('pir'))
            feedback(false, "This is no pir!", "error");

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

        else {
            feedback(false, "Valid input", "success");
            modellerIsValid = true;
        }
    }

    samccValidation(): any {

        samccIsValid = false;

        if(!this.elem.reformat('atoms'))
            feedback(false, "Must contain at least 28 ATOM records", "error");

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
        if(this.elem.val() != "")
            feedback(true, "Valid input", "success");

        else if (this.elem.val() == "") {
            feedback(false);
            valReset();
        }
    }
}
