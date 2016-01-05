// Put your javascript code here!
function deselect()
{
  var number = parseInt($('checkboxes').value, 10);
  for(i = 0; i < (2*number); i++) {
    $(hhrep_form).elements["hits[]"][i].checked = false;
  }
}
function select()
{ 
  var number = parseInt($('checkboxes').value, 10);
  for(i = 0; i < (2*number); i++) {
    if ($(hhrep_form).elements["hits[]"][i].disabled != true) {
      $(hhrep_form).elements["hits[]"][i].checked = true;
    }
  }
}
function select_first(num)
{ 
  var number = parseInt($('checkboxes').value, 10);
  var first = num;
  for(i = 0; i < (2*number); i++) {
    if (i < first || (i >= number && i < (number+first))) {
      if ($(hhrep_form).elements["hits[]"][i].disabled != true) {
        $(hhrep_form).elements["hits[]"][i].checked = true;
      }
    } else {
      $(hhrep_form).elements["hits[]"][i].checked = false;
    }
  }
}

function select5()
{ 
	select_first(5);
}

function select10()
{ 
	select_first(10);
}

function select15()
{ 
	select_first(15);
}

function change(num, block)
{
  var number = parseInt($('checkboxes').value, 10);
  var mode;
  if (block == 2) {
    if ($(hhrep_form).elements["hits[]"][num-1].checked == false) {
      mode = true;
    } else {
      mode = false;
    }
  } else {
    mode = $(hhrep_form).elements["hits[]"][(block * number)+num-1].checked;
  }
  for (i = 0; i < 2; i++) {
    $(hhrep_form).elements["hits[]"][(i * number)+num-1].checked = mode;
  }
}

function pasteTPR() {
	$('sequence_input').value = ">gi|17942835|pdb|1HZ4|A Chain A, Crystal Structure Of Transcription Factor Malt Domain Iii\nGHEIKDIREDTMHAEFNALRAQVAINDGNPDEAERLAKLALEELPPGWFYSRIVATSVLGEVLHCKGELT\nRSLALMQQTEQMARQHDVWHYALWSLIQQSEILFAQGFLQTAWETQEKAFQLINEQHLEQLPMHEFLVRI\nRAQLLWAWARLDEAEASARSGIEVLSSYQPQQQLQCLAMLIQCSLARGDLDNARSQLNRLENLLGNGKYH\nSDWISNANKVRVIYWQMTGDKAAAANWLRHTAKPEFANNHFLQGQWRNIARAQILLGEFEPAEIVLEELN\nENARSLRLMSDLNRNLLLLNQLYWQAGRKSDAQRVLLDALKLANRTGFISHFVIEGEAMAQQLRQLIQLN\nTLPELEQHRAQRILREINQHHGA";
}

function pasteTIM() {
	$('sequence_input').value = ">gi|5821872|pdb|1BQC|A Chain A, Beta-Mannanase From Thermomonospora Fusca\nATGLHVKNGRLYEANGQEFIIRGVSHPHNWYPQHTQAFADIKSHGANTVRVVLSNGVRWSKNGPSDVANV\nISLCKQNRLICMLEVHDTTGYGEQSGASTLDQAVDYWIELKSVLQGEEDYVLINIGNEPYGNDSATVAAW\nATDTSAAIQRLRAAGFEHTLVVDAPNWGQDWTNTMRNNADQVYASDPTGNTVFSIHMYGVYSQASTITSY\nLEHFVNAGLPLIIGEFGHDHSDGNPDEDTIMAEAERLKLGYIGWSWSGNGGGVEYLDMVYNFDGDNLSPW\nGERIFYGPNGIASTAKEAVIFG";
}

function pasteOMPW() {
	$('sequence_input').value = ">gi|71159606|sp|P0A915|OMPW_ECOLI Outer membrane protein W precursor\nMKKLTVAALAVTTLLSGSAFAHEAGEFFMRAGSATVRPTEGAGGTLGSLGGFSVTNNTQLGLTFTYMATD\nNIGVELLAATPFRHKIGTRATGDIATVHHLPPTLMAQWYFGDASSKFRPYVGAGINYTTFFDNGFNDHGK\nEAGLSDLSLKDSWGAAGQVGVDYLINRDWLVNMSVWYMDIDTTANYKLGGAQQHDSVRLDPWVFMFSAGY\nRF";
}

function imgOn(imgName, imgSrc) {
	if (document.images) {
		document[imgName].src = imgSrc;
	}
}

function imgOff(imgName, imgSrc) {
	if (document.images) {
		document[imgName].src = imgSrc;
	}
}

function show_applet(){	var obj = document.getElementById("applet");	if (!is_opera7up && !is_nav7up && !is_moz && !is_fx && !is_safari && !is_ie6up)	{		obj.style.display = "block";	}	}

function setReloadAction() {
	$('hhrep_form').action = $('action_val').value;
}

function appearForwardHHM(val)
{ 
	if (Element.getStyle(val, 'display') == "none") {
		new Effect.Appear(val);
	} else {
		new Effect.Fade(val);
	}
}
