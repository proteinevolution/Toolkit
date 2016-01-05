function setMoreHomologs() {
	if ($('moreHomologs').value == 'true') {
		Effect.Fade('psiblast_options');
		$('moreHomologs').value = 'false';
		$('seqCentered').checked = false;
	} else {
		Effect.Appear('psiblast_options');
		$('seqCentered').checked = true;
		$('moreHomologs').value = 'true';
	}
}


function check_pdb(){
	if($('pdb_file').value!=''){
		$('solv_acc').disabled = true; 
		$('solv_acc').checked = false;
	}else{
		$('solv_acc').disabled = false;	
		$('solv_acc').checked = true;	
	}
}


function setPdb() {
	if ($('pdb_check').checked) {
		Effect.Fade('pdb_options');
		$('pdb_check').checked = true;
		$('pdb_check').disabled = false;
	} else {
		Effect.Appear('pdb_options');
		$('pdb_check').checked = false;
		$('pdb_check').disabled = false;
	}
}

function pasteExample() {    $('sequence_input').value = ">1a0i241-349\nPENEADGIIQGLVWGTKGLANEGKVIGFEVLLESGRLVNATNISRALMDEFTETVKEATLSQWGFFDACTINPYDGWACQISYMEETPDGSLRHPSFVMF\nR\n>gi|119637753|ref|YP_91898#2 DNA ligase [Yersinia phage Berlin]   gi|119391784|emb|CAJ\nPECEADGIIQSVNWGTPGLSNEGLVIGFNVLLETGRHVAANNISQTLMEELTANAKEHGE-----------DYYNGWACQVAYMEETSDGSLRHPSFVMF\nR\n>gi|77118174|ref|YP_338096#3 ligase [Enterobacteria phage K1F]   gi|72527918|gb|AAZ7297\nPSEEADGHVVRPVWGTEGLANEGMVIGFDVMLENGMEVSATNISRALMSEFTENVKSDP------------DYYKGWACQITYMEETPDGSLRHPSFDQW\nR\n>gi|17570796|ref|NP_523305#4 DNA ligase [Bacteriophage T3]   gi|118769|sp|P07717|DNLI_B\nPECEADGIIQGVNWGTEGLANEGKVIGFSVLLETGRLVDANNISRALMDEFTSNVK-----------AHGEDFYNGWACQVNYMEATPDGSLRHPSFEKF\nR\n>gi|114796457|emb|CAK24995#5 putative DNA ligase [Bacteriophage LKA1]   E=4e-40 s/c=1.7\nPGFEADGTVIDYVWGDPDKANANKIVGFRVRLEDGAEVNATGLTQDQMACYTQSY-HATAYEVGI----TQTIYIGRACRVSGMERTKDGSIRHPHFDGF\nR\n>gi|68299729|ref|YP_249578#6 DNA ligase [Vibriophage VP4]   gi|66473268|gb|AAY46277.1|\nPEGEIDGTVVGVNWGTVGLANEGKVIGFQVLLENGVVVDANGITQEQMEEYTNLVYKTG------HDDC----FNGRPVQVKYMEKTPKGSLRHPSFQRW\nR\n>gi|33300828|ref|NP_877456#7 putative ATP-dependent DNA ligase [Bacteriophage phiKMV]\nPEITVDGRIVGYVMGKTGK-NVGRVVGYRVELEDGSTVAATGLSEEHIQLLTCAYLNA-------HIDEAMPNY-GRIVEVSAMERSAN-TLRHPSFSRF\nR\n>gi|29366706|ref|NP_813751#8 putative DNA ligase [Pseudomonas phage gh-1]   gi|29243565\nPDDNEDGFIQDVIWGTKGLANEGKVIGFKVLLESGHVVNACKISRALMDEFTDT--ETRLPG----------YYKGHTAKVTFMERYPDGSLRHPSFDSF\nR\n>gi|114796395|emb|CAK25951#9 putative ATP-dependent DNA ligase [Bacteriophage LKD16]\nPSLAVEGIVVGFVMGKTG-ANVGKVVGYRVDLEDGTIVSATGLTRDRIEMLT------TEAELLGGADHPGMADLGRVVEVTAMERSAN-TLRHPKFSRF\nR"    $('inputmode_sequence').checked = false;    $('inputmode_alignment').checked = true;    $('maxpsiblastit').value = 1;}