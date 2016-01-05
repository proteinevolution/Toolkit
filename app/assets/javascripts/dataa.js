function expand(thistag){
   styleObj=document.getElementById(thistag).style;
   if(styleObj.display=='none'){styleObj.display='';}
   else {styleObj.display='none';}
}

function expandCollapse() {
for (var i=0; i<expandCollapse.arguments.length; i++) {
var element = document.getElementById(expandCollapse.arguments[i]);
element.style.display = (element.style.display == "none") ? "block" : "none";
	}
}

function toggle_ali(){

	if( $('ali_btn').value=="Hide alignment" ){
		Effect.Fade('ali');
		$('ali_btn').value="Show alignment";
	}else{
		Effect.Appear('ali');
		$('ali_btn').value="Hide alignment";
	}
}

function show_hide_more_options() {
    if ($('more_options_on').value == 'true') {
            Effect.Fade('more_options');
            Effect.Fade('hide_more_options', { duration: 0.0 });
            Effect.Appear('show_more_options', { duration: 0.0 });
           $('more_options_on').value = 'false';

        } else {
        Effect.Appear('more_options');
        Effect.Fade('show_more_options', { duration: 0.0 });
        Effect.Appear('hide_more_options', { duration: 0.0 });
        $('more_options_on').value = 'true';
	    }
    }

function pasteExample() {
    $('sequence_input').value = ">gi|48607|emb|CAA32086.1| YadA [Yersinia enterocolitica]\nMTKDFKISVSAALISALFSSPYAFADDYDGIPNLTAVQISPNADPALGLEYPVRPPVPGAGGLNASAKGI\nHSIAIGATAEAAKGAAVAVGAGSIATGVNSVAIGPLSKALGDSAVTYGAASTAQKDGVAIGARASTSDTG\nVAVGFNSKADAKNSVAIGHSSHVAANHGYSIAIGDRSKTDRENSVSIGHESLNRQLTHLAAGTKDTDAVN\nVAQLKKEIEKTQENTNKRSAELLANANAYADNKSSSVLGIANNYTDSKSAETLENARKEAFAQSKDVLNM\nAKAHSNSVARTTLETAEEHANSVARTTLETAEEHANKKSAEALASANVYADSKSSHTLKTANSYTDVTVS\nNSTKKAIRESNQYTDHKFRQLDNRLDKLDTRVDKGLASSAALNSLFQPYGVGKVNFTAGVGGYRSSQALA\nIGSGYRVNENVALKAGVAYAGSSDVMYNASFNIEW"
}
