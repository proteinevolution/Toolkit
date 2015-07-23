function deselect(){
	var number = parseInt($('checkboxes').value, 10);
	for(i = 0; i < number; i++) {
		$('hit_checkbox'+i).checked=false;
		$('hit_checkbox'+(number+i)).checked=false;
  }
  calculate_forwarding();
}

function select(){
	var number = parseInt($('checkboxes').value, 10);
	for(i = 0; i < number; i++) {
		$('hit_checkbox'+i).checked=true;
		$('hit_checkbox'+(number+i)).checked=true;
	}
  calculate_forwarding();
}

function select_first(){
	first = 10
	var number = parseInt($('checkboxes').value, 10);
	for(i = 0; i < first; i++) {
		$('hit_checkbox'+i).checked=true;
		$('hit_checkbox'+(number+i)).checked=true;
	}
	for(i = first; i < number; i++) {
		$('hit_checkbox'+i).checked=false;
		$('hit_checkbox'+(number+i)).checked=false;
	}
  calculate_forwarding();
}

function change(num, block){ 
	var number = parseInt($('checkboxes').value, 10);
	if(block==0){
		var mode = $('hit_checkbox'+num).checked;
		$('hit_checkbox'+(number+num)).checked=mode;
	}else{
		var mode = $('hit_checkbox'+(number+num)).checked;
		$('hit_checkbox'+num).checked=mode;
	}
}


function toggle_seqlen(list)
{
	var dest = $(list).options[$(list).selectedIndex].value;
	if (dest.indexOf('blastclust') != -1 || dest.indexOf('clans') != -1 || dest.indexOf('clustal') != -1 || 
	    dest.indexOf('kalign') != -1 || dest.indexOf('mafft') != -1 || dest.indexOf('muscle') != -1 ||
	    dest.indexOf('patsearch') != -1 || dest.indexOf('probcons') != -1) {
		$('seqlen_slider').disabled = false;		
		$('seqlen_complete').disabled = false;		
	} else {
		$('seqlen_slider').disabled = true;		
		$('seqlen_complete').disabled = true;		
	        if ($('seqlen_complete').checked == true) {
			$('seqlen_slider').checked = true;
		}
	}
}


function show_hide_field(name) {
    if ($(name + '_on').value == 'true') {
	Effect.Fade(name);
 	Effect.Fade('hide_' + name, { duration: 0.0 });
 	Effect.Appear('show_' + name, { duration: 0.0 });
	$(name + '_on').value = 'false';

    } else {
 	Effect.Appear(name);
 	Effect.Fade('show_' + name, { duration: 0.0 });
 	Effect.Appear('hide_' + name, { duration: 0.0 });
 	$(name + '_on').value = 'true';
    }
}
function pasteExample()
{
  $('sequence_input').value = ">gi|4557853|ref|NP_000337.1| transcription factor SOX-9 [Homo sapiens]\nMNLLDPFMKMTDEQEKGLSGAPSPTMSEDSAGSPCPSGSGSDTENTRPQENTFPKGEPDLKKESEEDKFP\nVCIREAVSQVLKGYDWTLVPMPVRVNGSSKNKPHVKRPMNAFMVWAQAARRKLADQYPHLHNAELSKTLG\nKLWRLLNESEKRPFVEEAERLRVQHKKDHPDYKYQPRRRKSVKNGQAEAEEATEQTHISPNAIFKALQAD\nSPHSSSGMSEVHSPGEHSGQSQGPPTPPTTPKTDVQPGKADLKREGRPLPEGGRQPPIDFRDVDIGELSS\nDVISNIETFDVNEFDQYLPPNGHPGVPATHGQVTYTGSYGISSTAATPASAGHVWMSKQQAPPPPPQQPP\nQAPPAPQAPPQPQAAPPQQPAAPPQQPQAHTLTTLSSEPGQSQRTHIKTEQLSPSHYSEQQQHSPQQIAY\nSPFNLPHYSPSYPPITRSQYDYTDHQNSSSYYSHAAGQGTGLYSTFTYMNPAQRPMYTPIADTSGVPSIP\nQTHSPQHWEQPVYTQLTRP";
  $('std_dbs').options.selectedIndex = 2;
  
}

