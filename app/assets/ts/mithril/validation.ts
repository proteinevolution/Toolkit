/**
 * Created by snam on 03.03.17.
 */


let seqLimit : any;
let charLimitPerSeq : any;
let modellerIsValid : boolean = false;

let validation = function(elem : any, isInit : boolean, ctx : any) : any {

    if(!isInit) {

        let toolname : string;
        try { toolname = $("#toolnameAccess").val(); }
        catch(err) {
            toolname = "unknown";
            console.warn("toolname unspecified");
        }

        // Placeholder overrides

        switch(toolname) {


            case "hhblits":
                $(elem).attr("placeholder", "Enter a protein sequence or a protein multiple sequence alignment in FASTA or CLUSTAL format.");
                break;

            case "hhpred":
                $(elem).attr("placeholder", "Enter a protein sequence or a protein multiple sequence alignment in FASTA or CLUSTAL format.");
                break;

            case "hmmer":
                $(elem).attr("placeholder", "Enter a protein sequence or a protein multiple sequence alignment in FASTA or CLUSTAL format.");
                break;

            case "psiblast":
                $(elem).attr("placeholder", "Enter a protein sequence or a protein multiple sequence alignment in FASTA or CLUSTAL format.");
                break;

            case "patsearch":
                $(elem).attr("placeholder", "Enter a PROSITE grammar or a regular expression");
                break;

            case "clustalo":
                $(elem).attr("placeholder", "Enter protein sequences (<2000) in FASTA format");
                break;

            case "kalign":
                $(elem).attr("placeholder", "Enter protein sequences (<2000) in FASTA format");
                break;

            case "mafft":
                $(elem).attr("placeholder", "Enter protein sequences (<2000) in FASTA format");
                break;

            case "msaprobs":
                $(elem).attr("placeholder", "Enter protein sequences (<2000) in FASTA format");
                break;

            case "muscle":
                $(elem).attr("placeholder", "Enter protein sequences (<2000) in FASTA format");
                break;

            case "tcoffee":
                $(elem).attr("placeholder", "Enter protein sequences (<500) in FASTA format");
                break;

            case "aln2plot":
                $(elem).attr("placeholder", "Enter a protein multiple sequence alignment (<2000) in FASTA or CLUSTAL format");
                break;

            case "frpred":
                $(elem).attr("placeholder", "Enter a protein sequence or multiple sequence alignment (<2000) in FASTA or CLUSTAL format");
                break;

            case "hhrepid":
                $(elem).attr("placeholder", "Enter a protein sequence or multiple sequence alignment (<2000) in FASTA or CLUSTAL format");
                break;

            case "marcoil":
                $(elem).attr("placeholder", "Enter a protein sequence in FASTA format");
                break;

            case "pcoils":
                $(elem).attr("placeholder", "Enter a protein sequence or multiple sequence alignment (<2000) in FASTA or CLUSTAL format");
                break;

            case "repper":
                $(elem).attr("placeholder", "Enter a protein sequence or multiple sequence alignment (<2000) in FASTA or CLUSTAL format");
                break;

            case "tprpred":
                $(elem).attr("placeholder", "Enter a protein sequence in FASTA format");
                break;

            case "ali2d":
                $(elem).attr("placeholder", "Enter a protein multiple sequence alignment (<2000) in FASTA or CLUSTAL format");
                break;

            case "quick2d":
                $(elem).attr("placeholder", "Enter a protein sequence or multiple sequence alignment (<2000) in FASTA or CLUSTAL format");
                break;

            case "modeller":
                $(elem).attr("placeholder", "Enter a protein multiple sequence alignment [Target sequence + template(s)]. The first sequence must be the target; the other sequences serve as templates. The header of each template should start with a PDB or SCOP identifier (see example).");
                break;

            case "samcc":
                $(elem).attr("placeholder", "Enter PDB coordinates of a four-helical bundle.\n\nNote: The definitions for helices below need to be entered according to their sequential position in the bundle (it is not relevant whether this done clockwise or counterclockwise, and whether one starts with the N-terminal helix or any other one), and not in their order from N- to C-terminus. For helices in anti-parallel orientation, the residue range should be given with the larger residue number before the smaller one.");
                break;

            case "ancescon":
                $(elem).attr("placeholder", "Enter a protein multiple sequence alignment in FASTA or CLUSTAL format");
                break;

            case "clans":
                $(elem).attr("placeholder", "Enter protein sequences (<10000) in FASTA format");
                break;

            case "mmseqs2":
                $(elem).attr("placeholder", "Enter protein sequences (<20000) in FASTA format");
                break;

            case "phyml":
                $(elem).attr("placeholder", "Enter a protein multiple sequence alignment (<100) in FASTA or CLUSTAL format");
                break;

            case "6frametranslation":
                $(elem).attr("placeholder", "Enter a DNA sequence in FASTA format");
                break;

            case "backtrans":
                $(elem).attr("placeholder", "Enter a protein sequence");
                break;

            case "hhfilter":
                $(elem).attr("placeholder", "Enter a protein multiple sequence alignment (<2000) in FASTA or CLUSTAL format");
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




        return $(elem).on("keyup mouseover", function (e) {

            validationProcess(elem, toolname);
        });
    }
};


let validationProcess = function(elem: any,toolname: string){


//    console.log(elem);

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
             * Limit the maximum number of sequences to 500.
             **/

            let tcoffeeTarget = new alignmentVal($(elem));
            tcoffeeTarget.basicValidation();

            if (tcoffeeTarget.basicValidation()) {
                tcoffeeTarget.mustHave2();
            }
            seqLimit = 500;

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

            if (mafftTarget.basicValidation()) {
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

            if (muscleTarget.basicValidation()) {
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

            if (clustaloTarget.basicValidation()) {
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

            if (kalignTarget.basicValidation()) {
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

            if (msaprobsTarget.basicValidation()) {
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

            let hhpredTarget = new alignmentVal($(elem));
            hhpredTarget.basicValidation();

            if (hhpredTarget.basicValidation()) {
                hhpredTarget.sameLengthValidation();
            }

            charLimitPerSeq = 3000;

            break;

        case "psiblast":
            /** validation model for psiblast:
             * Input has to be a single FASTA sequence
             * or aligned FASTA with at least two sequences.
             * Sequences should have unique IDs; only the characters directly following the '>' sign, until the
             * first space, in the header are used as ID.
             */

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

            let aln2plotTarget = new alignmentVal($(elem));
            aln2plotTarget.basicValidation();

            if (aln2plotTarget.basicValidation()) {
                aln2plotTarget.sameLengthValidation();
                if (aln2plotTarget.sameLengthValidation())
                    aln2plotTarget.mustHave2();
            }

            seqLimit = 2000;

            break;

        case "frpred":

            let frpredTarget = new alignmentVal($(elem));
            frpredTarget.basicValidation();

            if (frpredTarget.basicValidation()) {
                frpredTarget.sameLengthValidation();
            }

            seqLimit = 2000;

            break;

        case "hhrepid":

            let hhrepidTarget = new alignmentVal($(elem));
            hhrepidTarget.basicValidation();

            if (hhrepidTarget.basicValidation()) {
                hhrepidTarget.sameLengthValidation();
            }

            seqLimit = 2000;

            break;

        case "pcoils":

            let pcoilsTarget = new alignmentVal($(elem));
            pcoilsTarget.basicValidation();

            if (pcoilsTarget.basicValidation()) {
                pcoilsTarget.sameLengthValidation();
            }

            seqLimit = 2000;

            break;

        case "repper":

            let repperTarget = new alignmentVal($(elem));
            repperTarget.basicValidation();

            if (repperTarget.basicValidation()) {
                repperTarget.sameLengthValidation();
            }

            seqLimit = 2000;

            break;

        case "marcoil":

            let marcoilTarget = new alignmentVal($(elem));
            marcoilTarget.basicValidation();

            if (marcoilTarget.basicValidation()) {
                marcoilTarget.mustHave1();
            }

            seqLimit = 2000;

            break;

        case "tprpred":

            let tprpredTarget = new alignmentVal($(elem));
            tprpredTarget.basicValidation();

            if (tprpredTarget.basicValidation()) {
                tprpredTarget.mustHave1();
            }

            break;

        case "ali2d":

            let ali2dTarget = new alignmentVal($(elem));
            ali2dTarget.basicValidation();

            if (ali2dTarget.basicValidation()) {
                ali2dTarget.sameLengthValidation();
                if (ali2dTarget.sameLengthValidation())
                    ali2dTarget.mustHave2();
            }

            seqLimit = 2000;

            break;

        case "quick2d":

            let quick2dTarget = new alignmentVal($(elem));
            quick2dTarget.basicValidation();

            if (quick2dTarget.basicValidation()) {
                quick2dTarget.sameLengthValidation();
            }

            seqLimit = 2000;

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

            let ancesconTarget = new alignmentVal($(elem));
            ancesconTarget.basicValidation();

            if (ancesconTarget.basicValidation()) {
                ancesconTarget.sameLengthValidation();
                if (ancesconTarget.sameLengthValidation())
                    ancesconTarget.mustHave2();
            }

            seqLimit = 20000;

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

            if (mmseqs2Target.basicValidation()) {
                mmseqs2Target.mustHave2();
            }
            seqLimit = 20000;

            break;

        case "phyml":

            let phymlTarget = new alignmentVal($(elem));
            phymlTarget.basicValidation();

            if (phymlTarget.basicValidation()) {
                phymlTarget.sameLengthValidation();
                if (phymlTarget.sameLengthValidation())
                    phymlTarget.mustHave2();
            }

            seqLimit = 100;

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

            if (clansTarget.basicValidation()) {
                clansTarget.mustHave2();
            }
            seqLimit = 10000;

            break;

        case "6frametranslation":

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

            let hhfilterTarget = new alignmentVal($(elem));
            hhfilterTarget.basicValidation();

            if (hhfilterTarget.basicValidation()) {
                hhfilterTarget.mustHave2();
            }
            seqLimit = 10000;

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

    if($(elem).val().length === 0){
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


    basicValidation(): boolean {

        if (this.elem.val() !== "" && !this.elem.validate('fasta') && this.elem.reformat('detect') === '') {
            feedback(false, "This is no Fasta!", "error");
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

        else if (!this.elem.reformat('maxlength', 2000000)) {
            feedback(false, "Input contains over ten million characters!", "error");
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

        else if (this.elem.val() !== "" && !this.elem.validate('fasta') && this.elem.reformat('detect') !== '') {
            originIsFasta = false;
            let t = this.elem.reformat('detect');
            feedback(false, t + " format found:  <b>Auto-transformed to Fasta</b>", "success", t);
            $("#alignment").val(this.elem.reformat('fasta'));
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

        //console.log($('#samcc_helixone').val());

        if(!this.elem.reformat('atoms'))
            feedback(false, "Must have at least 25 sequences starting with \"ATOM\"");

        else if (this.elem.val() == "") {
            feedback(false);
            valReset();
        }

        else feedback(true, "Valid input", "success");
    }
}
