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

//Example of single protein sequence; Human histone H2A; this protein is very well known
const singleProtSeq: string = ">AAN59974.1 histone H2A [Homo sapiens]\nMSGRGKQGGKARAKAKTRSSRAGLQFPVGRVHRLLRKGNYAERVGAGAPVYLAAVLEYLTAEILELAGNAARDNKKTRIIPRHLQLAIRNDEELNKLLGKVTIAQGGVLPNIQAVLLPKKTESHHKAKGK";

//Example of single protein sequence; Human histone H4; this protein is very well known; for pairwise comparison mode of HHpred
const singleProtSeq2: string = ">NP_778224.1 histone H4 [Homo sapiens]\nMSGRGKGGKGLGKGGAKRHRKVLRDNIQGITKPAIRRLARRGGVKRISGLIYEETRGVLKVFLENVIRDAVTYTEHAKRKTVTAMDVVYALKRQGRTLYGFGG";

//Example of single DNA sequence; Human histone H2A
const singleDNASeq: string = ">AY131993.1:374-766 Homo sapiens histone H2A (HIST1H2AM) gene, complete cds\nATGTCTGGACGTGGCAAGCAGGGCGGCAAGGCTCGCGCCAAGGCCAAAACCCGCTCCTCTAGAGCTGGGCTCCAATTTCCTGTAGGACGAGTGCACCGCCTGCTCCGCAAGGGCAACTACGCTGAGCGGGTCGGGGCCGGCGCGCCGGTTTACCTGGCGGCGGTGCTGGAGTACCTAACTGCCGAGATCCTGGAGCTGGCGGGCAACGCAGCCCGCGACAACAAAAAGACCCGCATCATCCCGCGCCACTTGCAGCTGGCCATCCGCAACGACGAGGAGCTCAACAAGCTGCTTGGTAAAGTTACCATCGCTCAGGGCGGTGTTCTGCCTAACATCCAGGCCGTACTGCTCCCCAAGAAGACTGAGAGCCACCACAAAGCTAAGGGCAAGTAA";

//Example of multiple, unaligned protein sequences; histone H2A from different organisms
const multiProtSeq: string = ">AAN59974.1 histone H2A [Homo sapiens]\nMSGRGKQGGKARAKAKTRSSRAGLQFPVGRVHRLLRKGNYAERVGAGAPVYLAAVLEYLTAEILELAGNAARDNKKTRIIPRHLQLAIRNDEELNKLLGKVTIAQGGVLPNIQAVLLPKKTESHHKAKGK\n>NP_001005967.1 histone 2, H2a [Danio rerio]\nMSGRGKTGGKARAKAKSRSSRAGLQFPVGRVHRLLRKGNYAERVGAGAPVYLAAVLEYLTAEILELAGNAARDNKKTRIIPRHLQLAVRNDEELNKLLGGVTIAQGGVLPNIQAVLLPKKTEKPAKSK\n>NP_001027366.1 histone H2A [Drosophila melanogaster]\nMSGRGKGGKVKGKAKSRSDRAGLQFPVGRIHRLLRKGNYAERVGAGAPVYLAAVMEYLAAEVLELAGNAARDNKKTRIIPRHLQLAIRNDEELNKLLSGVTIAQGGVLPNIQAVLLPKKTEKKA\n>NP_175517.1 histone H2A 10 [Arabidopsis thaliana]\nMAGRGKTLGSGSAKKATTRSSKAGLQFPVGRIARFLKKGKYAERVGAGAPVYLAAVLEYLAAEVLELAGNAARDNKKTRIVPRHIQLAVRNDEELSKLLGDVTIANGGVMPNIHNLLLPKKTGASKPSAEDD\n>NP_001263788.1 Histone H2A [Caenorhabditis elegans]\nMSGRGKGGKAKTGGKAKSRSSRAGLQFPVGRLHRILRKGNYAQRVGAGAPVYLAAVLEYLAAEVLELAGNAARDNKKTRIAPRHLQLAVRNDEELNKLLAGVTIAQGGVLPNIQAVLLPKKTGGDKEIRLSNLPKQ\n>NP_009552.1 histone H2A [Saccharomyces cerevisiae S288C]\nMSGGKGGKAGSAAKASQSRSAKAGLTFPVGRVHRLLRRGNYAQRIGSGAPVYLTAVLEYLAAEILELAGNAARDNKKTRIIPRHLQLAIRNDDELNKLLGNVTIAQGGVLPNIHQNLLPKKSAKTAKASQEL\n>XP_641587.1 histone H2A [Dictyostelium discoideum AX4]\nMSETKPASSKPAAAAKPKKVIPRVSRTGEPKSKPESRSARAGITFPVSRVDRLLREGRFAPRVESTAPVYLAAVLEYLVFEILELAHNTCSISKKTRITPQHINWAVGNDLELNSLFQHVTIAYGGVLPTPQQSTGEKKKKPSKKAAEGSSQIY";

//Example of aligned protein sequences; histone H2A from different organisms
const msaProtSeq: string = ">AAN59974.1 histone H2A [Homo sapiens]\nMSG------------------RGKQGG-KARAKAKTRSSRAGLQFPVGRVHRLLRKGNYAERVGAGAPVYLAAVLEYLTAEILELAGNAARDNKKTRIIPRHLQLAIRNDEELNKLLGKVTIAQGGVLPNIQAVLLPKKTESHHKAKGK-----\n>NP_001005967.1 histone 2, H2a [Danio rerio]\nMSG------------------RGKTGG-KARAKAKSRSSRAGLQFPVGRVHRLLRKGNYAERVGAGAPVYLAAVLEYLTAEILELAGNAARDNKKTRIIPRHLQLAVRNDEELNKLLGGVTIAQGGVLPNIQAVLLPKKTEKPAKSK-------\n>NP_001027366.1 histone H2A [Drosophila melanogaster]\nMSG------------------RGK-GG-KVKGKAKSRSDRAGLQFPVGRIHRLLRKGNYAERVGAGAPVYLAAVMEYLAAEVLELAGNAARDNKKTRIIPRHLQLAIRNDEELNKLLSGVTIAQGGVLPNIQAVLLPKKTEKKA----------\n>NP_175517.1 histone H2A 10 [Arabidopsis thaliana]\nMAG------------------RGKTLGSGSAKKATTRSSKAGLQFPVGRIARFLKKGKYAERVGAGAPVYLAAVLEYLAAEVLELAGNAARDNKKTRIVPRHIQLAVRNDEELSKLLGDVTIANGGVMPNIHNLLLPKKTGASKPSAEDD----\n>NP_001263788.1 Histone H2A [Caenorhabditis elegans]\nMSG------------------RGKGGKAKTGGKAKSRSSRAGLQFPVGRLHRILRKGNYAQRVGAGAPVYLAAVLEYLAAEVLELAGNAARDNKKTRIAPRHLQLAVRNDEELNKLLAGVTIAQGGVLPNIQAVLLPKKTGGDKEIRLSNLPKQ\n>NP_009552.1 histone H2A [Saccharomyces cerevisiae S288C]\nMSG------------------GKGGKAGSAAKASQSRSAKAGLTFPVGRVHRLLRRGNYAQRIGSGAPVYLTAVLEYLAAEILELAGNAARDNKKTRIIPRHLQLAIRNDDELNKLLGNVTIAQGGVLPNIHQNLLPKKSAKTAKASQEL----\n>XP_641587.1 histone H2A [Dictyostelium discoideum AX4]\nMSETKPASSKPAAAAKPKKVIPRVSRTGEPKSKPESRSARAGITFPVSRVDRLLREGRFAPRVESTAPVYLAAVLEYLVFEILELAHNTCSISKKTRITPQHINWAVGNDLELNSLFQHVTIAYGGVLPTPQQSTGEKKKKPSKKAAEGSSQIY";

//Example of aligned protein sequences in A3M format; histone H2A from different organisms
const a3mProtSeq: string = "#A3M#\n>AAN59974.1 histone H2A [Homo sapiens]\nMSGRGKQGGKARAKAKTRSSRAGLQFPVGRVHRLLRKGNYAERVGAGAPVYLAAVLEYLTAEILELAGNAARDNKKTRIIPRHLQLAIRNDEELNKLLGK\nVTIAQGGVLPNIQAVLLPKKTESHHKAKGK\n>NP_001005967.1 histone 2, H2a [Danio rerio]\nMSGRGKTGGKARAKAKSRSSRAGLQFPVGRVHRLLRKGNYAERVGAGAPVYLAAVLEYLTAEILELAGNAARDNKKTRIIPRHLQLAVRNDEELNKLLGGVTIAQGGVLPNIQAVLLPKKTEKPAKSK--\n>NP_001027366.1 histone H2A [Drosophila melanogaster]\nMSGRGK-GGKVKGKAKSRSDRAGLQFPVGRIHRLLRKGNYAERVGAGAPVYLAAVMEYLAAEVLELAGNAARDNKKTRIIPRHLQLAIRNDEELNKLLSGVTIAQGGVLPNIQAVLLPKKTEKKA-----\n>NP_175517.1 histone H2A 10 [Arabidopsis thaliana]\nMAGRGKTLGsGSAKKATTRSSKAGLQFPVGRIARFLKKGKYAERVGAGAPVYLAAVLEYLAAEVLELAGNAARDNKKTRIVPRHIQLAVRNDEELSKLLGDVTIANGGVMPNIHNLLLPKKTGASKPSAEDd\n>NP_001263788.1 Histone H2A [Caenorhabditis elegans]\nMSGRGKGGKaKTGGKAKSRSSRAGLQFPVGRLHRILRKGNYAQRVGAGAPVYLAAVLEYLAAEVLELAGNAARDNKKTRIAPRHLQLAVRNDEELNKLLAGVTIAQGGVLPNIQAVLLPKKTGGDKEIRLSnlpkq\n>NP_009552.1 histone H2A [Saccharomyces cerevisiae S288C]\nMSGGKGGKAgSAAKASQSRSAKAGLTFPVGRVHRLLRRGNYAQRIGSGAPVYLTAVLEYLAAEILELAGNAARDNKKTRIIPRHLQLAIRNDDELNKLLGNVTIAQGGVLPNIHQNLLPKKSAKTAKASQEl\n>XP_641587.1 histone H2A [Dictyostelium discoideum AX4]\nMSEtkpasskpaaaakpkkviPRVSRTgEPKSKPESRSARAGITFPVSRVDRLLREGRFAPRVESTAPVYLAAVLEYLVFEILELAHNTCSISKKTRITPQHINWAVGNDLELNSLFQHVTIAYGGVLPTPQQSTGEKKKKPSKKAAEGssqiy\n";

//Example of a pattern; Walker A (P-loop) motif
const patternProt: string = "G-x(4)-GK-[TS]";

//Example of a protein with a coiled coil segment; GCN4
const coiledcoilSeq: string = ">sp|P03069.1|GCN4_YEAST RecName: Full=General control protein GCN4; AltName: Full=Amino acid biosynthesis regulatory protein\nMSEYQPSLFALNPMGFSPLDGSKSTNENVSASTSTAKPMVGQLIFDKFIKTEEDPIIKQDTPSNLDFDFALPQTATAPDAKTVLPIPELDDAVVESFFSSSTDSTPMFEYENLEDNSKEWTSLFDNDIPVTTDDVSLADKAIESTEEVSLVPSNLEVSTTSFLPTPVLEDAKLTQTRKVKKPNSVVKKSHHVGKDDESRLDHLGVVAYNRKQRSIPLSPIVPESSDPAALKRARNTEAARRSRARKLQRMKQLEDKVEELLSKNYHLENEVARLKKLVGER";

//Example of a protein with TPR motifs; Human Tetratricopeptide repeat protein 5
const tprSeq: string = ">sp|Q8N0Z6|TTC5_HUMAN Tetratricopeptide repeat protein 5 OS=Homo sapiens GN=TTC5 PE=1 SV=2\nMMADEEEEVKPILQKLQELVDQLYSFRDCYFETHSVEDAGRKQQDVQKEMEKTLQQMEEVVGSVQGKAQVLMLTGKALNVTPDYSPKAEELLSKAVKLEPELVEAWNQLGEVYWKKGDVAAAHTCFSGALTHCRNKVSLQNLSMVLRQLRTDTEDEHSHHVMDSVRQAKLAVQMDVHDGRSWYILGNSYLSLYFSTGQNPKISQQALSAYAQAEKVDRKASSNPDLHLNRATLHKYEESYGEALEGFSRAAALDPAWPEPRQREQQLLEFLDRLTSLLESKGKVKTKKLQSMLGSLRPAHLGPCSDGHYQSASGQKVTLELKPLSTLQPGVNSGAVILGKVVFSLTTEEKVPFTFGLVDSDGPCYAVMVYNIVQSWGVLIGDSVAIPEPNLRLHRIQHKGKDYSFSSVRVETPLLLVVNGKPQGSSSQAVATVASRPQCE";

//Example of an outer membrane protein (OMP); for HHomp
const ompSeq: string = ">WP_001350588.1 MULTISPECIES: porin OmpA [Escherichia]\nMKKTAIAIAVALAGFATVAQAAPKDNTWYTGAKLGWSQYHDTGFINNNGPTHENQLGAGAFGGYQVNPYVGFEMGYDWLGRMPYKGSVENGAYKAQGVQLTAKLGYPITDDLDVYTRLGGMVWRADTKSNVYGKNHDTGVSPVFAGGVEYAITPEIATRLEYQWTNNIGDAHTIGTRPDNGMLSLGVSYRFGQGEAAPVVAPAPAPAPEVQTKHFTLKSDVLFTFNKATLKPEGQAALDQLYSQLSNLDPKDGSVVVLGYTDRIGSDAYNQALSERRAQSVVDYLISKGIPADKISARGMGESNPVTGNTCDNVKQRAALIDCLAPDRRVEIEVKGIKDVVTQPQA";

//Example of NCBI headers; headers are of H2A proteins
const protHeaders: string = "AAN59974.1\nNP_009552.1\nXP_641587.1\nNP_001005967.1\nNP_001027366.1\nNP_175517.1\nNP_001263788.1\n";

const modellerPIR: string = ">P1;UKNP\nsequence:UKNP:1    :A:132  :A::::\nMSGRGKQGG-KARAKAKTRSSRAGLQFPVGRVHRLLRKGNY-AERVGAGAPVYLAAVLEYLTAEILELAGNAARDNKKTRIIPRHLQLAIRNDEELNKLLGKVTIAQGGVLPNIQAVLLPKKTESHHKAKGK*\n>P1;4WNN\nstructure:4WNN:18  :C:100 :C::Saccharomyces cerevisiae:1.8:\n-----------------SRSAKAGLTFPVGRVHRLLRRGNY-AQRIGSGAPVYLTAVLEYLAAEILELAGNAARDNKKTRIIPRHLQLAIRNDDELNKLLG-------------------------------*\n>P1;4CAY\nstructure:4CAY:2   :A:91  :A::HOMO SAPIENS:1.48:\n-----------------SRSQRAGLQFPVGRIHRHLKSR----GRVGATAAVYSAAILEYLTAEVLELAGNASKDLKVKRITPRHLQLAIRGDEELDSLIK-ATIAGG------------------------*\n>P1;5B0Z\nstructure:5B0Z:15  :C:122 :C::Homo sapiens:1.987:\n------------RAKAKTRSSRAGLQFPVGRVHRLLRKGNY-SERVGAGAPVYLAAVLEYLTAEILELAGNAARDNKKTRIIPRHLQLAIRNDEELNKLLGRVTIAQGGVLPNIQAVLLPK-----------*";

//Example sequence for REPPER
const protyadA: string = ">gi|401465|sp|P31489|YDA1_YEREN Adhesin yadA precursor\nMTKDFKISVSAALISALFSSPYAFADDYDGIPNLTAVQISPNADPALGLEYPVRPPVPGAGGLNASAKGIHSIAIGATAEAAKGAAVAVGAGSIATGVNSVAIGPLSKALGDSAVTYGAASTAQKDGVAIGARASTSDTGVAVGFNSKADAKNSVAIGHSSHVAANHGYSIAIGDRSKTDRENSVSIGHESLNRQLTHLAAGTKDTDAVNVAQLKKEIEKTQENTNKRSAELLANANAYADNKSSSVLGIANNYTDSKSAETLENARKEAFAQSKDVLNMAKAHSNSVARTTLETAEEHANSVARTTLETAEEHANKKSAEALASANVYADSKSSHTLKTANSYTDVTVSNSTKKAIRESNQYTDHKFRQLDNRLDKLDTRVDKGLASSAALNSLFQPYGVGKVNFTAGVGGYRSSQALAIGSGYRVNENVALKAGVAYAGSSDVMYNASFNIEW";


const sampleSeqConfig = function(textAreaId: string) {
    return function(elem: any, isInit: boolean, ctx: any): any {
        if (!isInit) {
            const $a = $(textAreaId);
            let toolname: string;
            try {
                toolname = $("#toolnameAccess").val();
            }
            catch (err) {
                toolname = "unknown";
                console.warn("toolname unspecified");
            }

            return $(elem).on("click", function(e) {
                switch (toolname) {

                    case "formatseq":
                        $a.val(a3mProtSeq);
                        break;

                    case "hhblits":
                        $a.val(singleProtSeq);
                        break;

                    case "hhpred":
                        if(textAreaId == "#alignment_two")
                            $a.val(singleProtSeq2);
                        else
                            $a.val(singleProtSeq);
                        break;

                    case "hmmer":
                        $a.val(singleProtSeq);
                        break;

                    case "psiblast":
                        $a.val(singleProtSeq);
                        break;

                    case "patsearch":
                        $a.val(patternProt);
                        break;

                    case "tcoffee":
                        $a.val(multiProtSeq);
                        break;

                    case "mafft":
                        $a.val(multiProtSeq);
                        break;

                    case "muscle":
                        $a.val(multiProtSeq);
                        break;

                    case "clustalo":
                        $a.val(multiProtSeq);
                        break;

                    case "kalign":
                        $a.val(multiProtSeq);
                        break;

                    case "msaprobs":
                        $a.val(multiProtSeq);
                        break;

                    case "aln2plot":
                        $a.val(msaProtSeq);
                        break;

                    case "frpred":
                        $a.val(singleProtSeq);
                        break;

                    case "hhrepid":
                        $a.val(tprSeq);
                        break;

                    case "hhomp":
                        $a.val(ompSeq);
                        break;

                    case "deepcoil":
                        $a.val(coiledcoilSeq);
                        break;

                    case "marcoil":
                        $a.val(coiledcoilSeq);
                        break;

                    case "pcoils":
                        $a.val(coiledcoilSeq);
                        break;

                    case "repper":
                        $a.val(protyadA);
                        break;

                    case "tprpred":
                        $a.val(tprSeq);
                        break;

                    case "ali2d":
                        $a.val(msaProtSeq);
                        break;

                    case "quick2d":
                        $a.val(singleProtSeq);
                        break;

                    case "ancescon":
                        $a.val(msaProtSeq);
                        break;

                    case "clans":
                        $a.val(multiProtSeq);
                        break;

                    case "modeller":
                        $a.val(modellerPIR);
                        break;

                    case "samcc":
                        $(document).ready(function() {
                            //fetch text file
                            $.get("assets/images/Examples/samcc.txt", function(data) {
                                let samccText = "";
                                //split on new lines
                                let lines = data.split("\n");

                                for (let i = 0; i < lines.length; i++) {
                                    samccText += lines[i] + "\n";
                                }
                                $a.val(samccText);
                            });
                        });
                        $("#samcc_helixone").val("a;A;2;30");
                        $("#samcc_helixtwo").val("a;B;2;30");
                        $("#samcc_helixthree").val("a;C;2;30");
                        $("#samcc_helixfour").val("a;D;2;30");
                        break;

                    case "mmseqs2":
                        $a.val(multiProtSeq);
                        break;

                    case "phyml":
                        $a.val(msaProtSeq);
                        break;

                    case "sixframe":
                        $a.val(singleDNASeq);
                        break;

                    case "backtrans":
                        $a.val(singleProtSeq);
                        break;

                    case "hhfilter":
                        $a.val(msaProtSeq);
                        break;

                    case "seq2id":
                        $a.val(multiProtSeq);
                        break;

                    case "retseq":
                        $a.val(protHeaders);
                        break;
                }

                //localStorage.setItem('alignmentcontent', $a.val());

            });

        }

    };
};
