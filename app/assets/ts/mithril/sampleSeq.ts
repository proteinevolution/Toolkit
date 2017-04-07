/**
 *
 * this file provides sample sequences to different tools
 *
 */



// final input sequences, not reassignable to variables

/**
 * short description of the sample sequence and why it was used for which tool
 * @type {string}
 */
const sequence1 : string = ">NP_877456#7 putative ATP-dependent DNA ligase [Bacteriophage phiKMV]\nPEITVDGRIVGYVMGKTG-KNVGRVVGYRVELEDGSTVAATGLSEE\n>CAK25951#9 putative ATP-dependent DNA ligase [Bacteriophage LKD16]\nPSLAVEGIVVGFVMGKTG-ANVGKVVGYRVDLEDGTIVSATGLTRD\n>CAK24995#5 putative DNA ligase [Bacteriophage LKA1]   E=4e-40 s/c=1.7\nPGFEADGTVIDYVWGDPDKANANKIVGFRVRLEDGAEVNATGLTQD\n>NP_813751#8 putative DNA ligase [Pseudomonas phage gh-1]   gi|29243565\nPDDNEDGFIQDVIWGTKGLANEGKVIGFKVLLESGHVVNACKISRA\n>YP_249578#6 DNA ligase [Vibriophage VP4]   gi|66473268|gb|AAY46277.1|\nPEGEIDGTVVGVNWGTVGLANEGKVIGFQVLLENGVVVDANGITQE\n>YP_338096#3 ligase [Enterobacteria phage K1F]   gi|72527918|gb|AAZ7297\nPSEEADGHVVRPVWGTEGLANEGMVIGFDVMLENGMEVSATNISRA\n>NP_523305#4 DNA ligase [Bacteriophage T3]   gi|118769|sp|P07717|DNLI_B\nPECEADGIIQGVNWGTEGLANEGKVIGFSVLLETGRLVDANNISRA\n>YP_91898#2 DNA ligase [Yersinia phage Berlin]   gi|119391784|emb|CAJ\nPECEADGIIQSVNWGTPGLSNEGLVIGFNVLLETGRHVAANNISQT";

/**
 * short description of the sample sequence and why it was used for which tool
 * @type {string}
 */
const sequence2 : string = "foo";



let sampleSeqConfig = function(elem: any, isInit: boolean, ctx: any) : any {


    let $a = $('#alignment');

    if(!isInit) {
        let toolname: string;
        try {
            toolname = $("#toolnameAccess").val();
        }
        catch (err) {
            toolname = "unknown";
            console.warn("toolname unspecified");
        }

        return $(elem).on("click", function (e) {

            switch(toolname) {
                case "tcoffee":
                    $a.val(sequence1);
                    break;

                case "mafft":
                    $a.val("TEST2");
                    break;

                case "muscle":
                    $a.val(sequence1);

                    break;

                case "clustalo":
                    $a.val(sequence1);

                    break;

                case "kalign":
                    $a.val(sequence1);

                    break;

                case "msaprobs":
                    $a.val(sequence1);
                    break;

                case "hmmer":
                    $a.val(sequence1);
                    break;

                case "hhblits":
                    $a.val(sequence1);
                    break;

                case "hhpred":
                    $a.val(sequence1);
                    break;

                case "psiblast":
                    $a.val(sequence1);
                    break;

                case "patsearch":
                    $a.val(sequence1);
                    break;

                case "mmseqs2":
                    $a.val(sequence1);
                    break;

                case "hhfilter":
                    $a.val(sequence1);
                    break;

                case "clans":
                    $a.val(sequence1);
                    break;

                default:
                    console.warn("implement me");
            }

        })

    }

};

