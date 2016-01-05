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

function loadPage(list, url) {
	var hmm = $(list).options[$(list).selectedIndex].value;
	url = url + "?page=" + hmm	
	location.href = url;
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

function toggle_search(){

	if( $('search_btn').value=="Hide search section" ){
		Effect.Fade('search_options');
		$('search_btn').value=$('search_btn_value').value;
	}else{
		Effect.Appear('search_options');
		$('search_btn').value="Hide search section";
	}
}

function toggle_pdb(){

	if( $('pdb_btn').value=="Hide PDB-file" ){
		Effect.Fade('pdb');
		$('pdb_btn').value="Show PDB-file";
	}else{
		Effect.Appear('pdb');
		$('pdb_btn').value="Hide PDB-file";
	}
}

function pasteOmpA() {
	$('sequence_input').value = ">gi|117623167|ref|YP_852080.1| OmpA [Escherichia coli APEC O1]\nMKDLTVLSRWRYSWRILDDNEAQKMKKTAIAIAVALAGFATVAQAAPKDNTWYTGAKLGWSQYHDTGFINNNGPTHENQLGAGAFGGYQVNPYVGFEMGYDWLGRMPYKGSVENGAYKAQGVQLTAKLGYPITDDLDVYTRLGGMVWRADTKSNVYGKNHDTGVSPVFAGGVEYAITPEIATRLEYQWTNNIGDAHTIGTRPDNGMLSLGVSYRFGQGEAAPVVAPAPAPAPEVQTKHFTLKSDVLFTFNKATLKPEGQAALDQLYSQLSNLDPKDGSVVVLGYTDRIGSDAYNQALSERRAQSVVDYLISKGIPADKISARGMGESNPVTGNTCDNVKQRAALIDCLAPDRRVEIEVKGIKDVVTQPQA";
}

function pasteYadA() {
	$('sequence_input').value = ">gi|48607|emb|CAA32086.1| YadA [Yersinia enterocolitica]\nMTKDFKISVSAALISALFSSPYAFADDYDGIPNLTAVQISPNADPALGLEYPVRPPVPGAGGLNASAKGIHSIAIGATAEAAKGAAVAVGAGSIATGVNSVAIGPLSKALGDSAVTYGAASTAQKDGVAIGARASTSDTGVAVGFNSKADAKNSVAIGHSSHVAANHGYSIAIGDRSKTDRENSVSIGHESLNRQLTHLAAGTKDTDAVNVAQLKKEIEKTQENTNKRSAELLANANAYADNKSSSVLGIANNYTDSKSAETLENARKEAFAQSKDVLNMAKAHSNSVARTTLETAEEHANSVARTTLETAEEHANKKSAEALASANVYADSKSSHTLKTANSYTDVTVSNSTKKAIRESNQYTDHKFRQLDNRLDKLDTRVDKGLASSAALNSLFQPYGVGKVNFTAGVGGYRSSQALAIGSGYRVNENVALKAGVAYAGSSDVMYNASFNIEW";
}

function deselect(){
	var number = parseInt($('checkboxes').value, 10);
	for(i = 0; i < number; i++) {
		$('hit_checkbox'+i).checked=false;
		$('hit_checkbox'+(number+i)).checked=false;
  }
}

function select(){
	var number = parseInt($('checkboxes').value, 10);
	for(i = 0; i < number; i++) {
		$('hit_checkbox'+i).checked=true;
		$('hit_checkbox'+(number+i)).checked=true;
	}
}

function select_first(first){
	var number = parseInt($('checkboxes').value, 10);
	for(i = 0; i < first; i++) {
		$('hit_checkbox'+i).checked=true;
		$('hit_checkbox'+(number+i)).checked=true;
	}
	for(i = first; i < number; i++) {
		$('hit_checkbox'+i).checked=false;
		$('hit_checkbox'+(number+i)).checked=false;
	}
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

function appearForward(val)
{
	var coloring = 'color_button';
	var image = 'hitlist_img';
	var create = 'createmodel';
	var qt = "View_QT";
    
	if (Element.getStyle(val, 'display') == "none") {
		if ($(coloring) != null) {
			new Effect.Fade(coloring);
		}
		new Effect.Appear(create);
		new Effect.Fade(image);
		if ($(qt) != null) {
			new Effect.Fade(qt);
		}
		checkbox_createmodel(true);
	} else {
		new Effect.Fade(val);
		if ($(coloring) != null) {
			new Effect.Appear(coloring);
		}
		new Effect.Appear(image);
		if ($(qt) != null) {
			new Effect.Appear(qt);
		}
		checkbox_createmodel(false);
	}
}

function checkbox_createmodel(mode)
{
	boxes = $('createmodel_disabled_Checkboxes').value;
//	if (boxes == "") return;
	checkbox_idx = boxes.split(',');
	number = parseInt(($('checkboxes').value),10);
	for(i=0; i<checkbox_idx.length-1; ++i){
		idx = parseInt(checkbox_idx[i],10) - 1;
		if (mode == true) {
			$('hit_checkbox'+idx).disabled=true;
			$('hit_checkbox'+idx).checked=false;
			$('hit_checkbox'+(number+idx)).disabled=true;
			$('hit_checkbox'+(number+idx)).checked=false;
		} else {
			$('hit_checkbox'+idx).disabled=false;
			$('hit_checkbox'+(number+idx)).disabled=false; 
		}
	}
	// Check only first (not disabled) checkbox 
	if (mode == true) {
		var check = false;
		for(i=0; i<number; ++i) {
			if ($('hit_checkbox'+i).disabled == false) {
				if (check == false) {
					$('hit_checkbox'+i).checked = true;
					$('hit_checkbox'+(number+i)).checked = true;
					check = true;
				} else {
					$('hit_checkbox'+i).checked = false;
					$('hit_checkbox'+(number+i)).checked = false;
				}
			}
		}
	}	
}

