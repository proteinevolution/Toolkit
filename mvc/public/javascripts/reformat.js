/* global m: false */
// TODO: ensure this targets the current API.


// Intermediate format: JSON

//model
window.reformat = (function () {
    "use strict"



    function readFastaText(fastaText){


        var splittedStrings = fastaText.split(">"),
            result = [],
            i = 1;

        for (; i < splittedStrings.length; i++) {

            result += JSON.stringify(new readFastaLine(splittedStrings[i]));

        }

        return result;
    }


    function readFastaLine(fastaLine) {

        var splittedStrings  = fastaLine.split('\n'),
            result = {},
            i = 1;
        result.name = splittedStrings[0].substr(1, splittedStrings[0].length - 1);
        result.sequence = '';
        for (; i < splittedStrings.length; i++) {
            result.sequence += splittedStrings[i];
        }
        return result;
    }



    function clustalParser() {}


    function writeFasta() {}


    function writeClustal() {}




    return {
        controller: function () {
            this.source = m.prop(">gi|33300828|ref|NP_877456#7 putative ATP-dependent DNA ligase [Bacteriophage phiKMV]\nPEITVDGRIVGYVMGKTG-KNVGRVVGYRVELEDGSTVAATGLSEEHIQLLTCAYLNAHI\nD---EAMPNYGRIVEVSAMERSAN-TLRHPSFSRFR\n>gi|114796395|emb|CAK25951#9 putative ATP-dependent DNA ligase [Bacteriophage LKD16]\nPSLAVEGIVVGFVMGKTG-ANVGKVVGYRVDLEDGTIVSATGLTRDRIEMLTTEAELLGG\nA-DHPGMADLGRVVEVTAMERSAN-TLRHPKFSRFR\n>gi|114796457|emb|CAK24995#5 putative DNA ligase [Bacteriophage LKA1]   E=4e-40 s/c=1.7\nPGFEADGTVIDYVWGDPDKANANKIVGFRVRLEDGAEVNATGLTQDQMACYTQSYHATAY\nEVGITQTIYIGRACRVSGMERTKDGSIRHPHFDGFR\n>gi|29366706|ref|NP_813751#8 putative DNA ligase [Pseudomonas phage gh-1]   gi|29243565\nPDDNEDGFIQDVIWGTKGLANEGKVIGFKVLLESGHVVNACKISRALMDEFTDTETRLPG\n-------YYKGHTAKVTFMERYPDGSLRHPSFDSFR\n>gi|68299729|ref|YP_249578#6 DNA ligase [Vibriophage VP4]   gi|66473268|gb|AAY46277.1|\nPEGEIDGTVVGVNWGTVGLANEGKVIGFQVLLENGVVVDANGITQEQMEEYTNLVYKTGH\nD-----DCFNGRPVQVKYMEKTPKGSLRHPSFQRWR\n>gi|77118174|ref|YP_338096#3 ligase [Enterobacteria phage K1F]   gi|72527918|gb|AAZ7297\nPSEEADGHVVRPVWGTEGLANEGMVIGFDVMLENGMEVSATNISRALMSEFTENVKSDP-\n------DYYKGWACQITYMEETPDGSLRHPSFDQWR\n>gi|17570796|ref|NP_523305#4 DNA ligase [Bacteriophage T3]   gi|118769|sp|P07717|DNLI_B\nPECEADGIIQGVNWGTEGLANEGKVIGFSVLLETGRLVDANNISRALMDEFTSNVKAHGE\nD------FYNGWACQVNYMEATPDGSLRHPSFEKFR\n>gi|119637753|ref|YP_91898#2 DNA ligase [Yersinia phage Berlin]   gi|119391784|emb|CAJ\nPECEADGIIQSVNWGTPGLSNEGLVIGFNVLLETGRHVAANNISQTLMEELTANAKEHGE\nD------YYNGWACQVAYMEETSDGSLRHPSFVMFR\n")
            this.output = m.prop("")

            this.convert = function () {
                var source = this.source()
                return this.output(JSON.stringify(readFastaText(source)))
            }.bind(this)
        },

        view: function (ctrl) {
            return m("div", [
                m("textarea", {
                    autofocus: true,
                    style: {width: "100%", height: "250px", fontFamily: "Droid Sans Mono", fontSize: "0.625em"},
                    onchange: m.withAttr("value", ctrl.source)
                }, ctrl.source()),
                m("button", {class: "button small"}, {onclick: ctrl.convert}, "Convert from FASTA to JSON"),
                m("textarea", {style: {width: "100%", height: "250px", fontFamily: "Droid Sans Mono", fontSize: "0.625em"}},
                    ctrl.output())
            ])
        }
    }
})()