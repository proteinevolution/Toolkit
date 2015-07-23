// Put your javascript code here!
function adjustMatchModeValues() {
    var hide_mode = false;
    if (hide_mode) {
	var msadiv = $('match_mode_selection');
	var format = $('informat').value;
	var displayState = 'block';
	if (format == 'a3m' || format == 'a2m') {
	    displayState = 'none';
	}

	msadiv.style.display = displayState;
    } else {
	var msainput = $('match_mode');
	var format = $('informat').value;
	var disable = false;
	var visible = 'visible';
	if (format == 'a3m' || format == 'a2m') {
	    disable = true;
	    visible = 'hidden';
	}

	msainput.disabled = disable;	
	msainput.style.visibility = visible;
    }
}

function resetDisplayValues(default_format) {
    // On reset, adjustMatchModeValues() is not triggered by a change of the
    // input mode (the same would apply to change_format). Then we have to
    // toggle the display of the match state assignment according to the
    // default input format.
    // Because the onreset-handler is performed before the reset, we have to
    // reset the relevant values before calling the adjusting function.

    $('informat').value = default_format;
    adjustMatchModeValues();
    return true;
}

function pasteAbrB() {
	$('sequence_input').value = ">gi|113009|sp|P08874|ABRB_BACSU Transition state regulatory protein abrB\nMFMKSTGIVRKVDELGRVVIPIELRRTLGIAEKDALEIYVDDEKIILKKYKPNMTCQVTGEVSDDNLKLA\nGGKLVLSKEGAEQIISEIQNQLQNLK";
}

function pasteP5() {
	$('sequence_input').value = ">gi|1353222|sp|P07582|VLYS_BPPH6 Phage lysis protein (Protein P5)\nMSKDSAFAVQYSLRALGQKVRADGVVGSETRAALDALPENQKKAIVELQALLPKAQSVGNNRVRFTTAEV\nDSAVARISQKIGVPASYYQFLIPIENFVVAGGFETTVSGSFRGLGQFNRQTWDRLRRLGRNLPAFEEGSA\nQLNASLYAIGFLYLENKRAYEASFKGRVFTHEIAYLYHNQGAPAAEQYLTSGRLVYPKQSEAAVAAVAAA\nRNQHVKESWA\n";
}
