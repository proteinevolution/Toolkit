function toggleFields(val)
{
	if (val == "linkage") {
		$('minLinks').disabled = false;

		$('stdev').disabled = true;
		$('globalaverage').disabled = true;
		$('offset').disabled = true;
	} else {
		if (val == "convex") {
			$('stdev').disabled = false;
			
			$('minLinks').disabled = true;
			$('globalaverage').disabled = true;
			$('offset').disabled = true;
		} else {
			$('globalaverage').disabled = false;
			$('offset').disabled = false;
			
			$('minLinks').disabled = true;
			$('stdev').disabled = true;
		}
	}
}

var xoffset=82;
var maxval=1;
var graphwidth=605;
var graphoffset=75;
var graphheight=400;

function init_clans() {
	$('divline').style.top = "275px";
}

function init_cluster() {
	$('divline').style.top = "313px";
}

function init(){
    img = $('graph');
    img.addEventListener('click',setxcoord,false);
    maxvalobj = $('maxval');
    maxval = maxvalobj.value;
    cutoffline =  $('divline');
    cutoffline.style.left = xoffset+"px";
}

function setxcoord(e){
   var cutoff = $('xcutoff');
	var posx = 0;
	var posy = 0;
	if (!e){
	    var e = window.event;
	}
	if (e.layerX || e.layerY){
		posx = e.layerX;
		posy = e.layerY;
	}else if (e.clientX || e.clientY){
		posx = e.clientX + document.body.scrollLeft;
		posy = e.clientY + document.body.scrollTop;
		cutoff.value="enter manually";
	}
	posx=posx-xoffset;
	if(posx<0){
	    posx=0;
	}else if(posx>graphwidth){
	    posx=graphwidth;
	}
	cutoff.value = (posx/graphwidth)*maxval;
	cutoffline = $('divline');
	cutoffline.style.left = (posx+xoffset)+"px";
}

function pasteExample()
{
  $('sequence_input').value = "1a0i241-349\nPENEADGIIQGLVWGTKGLANEGKVIGFEVLLESGRLVNATNISRALMDEFTETVKEATLSQWGFFDACTINPYDGWACQISYMEETPDGSLRHPSFVMF\nR\n>gi|119637753|ref|YP_91898#2 DNA ligase [Yersinia phage Berlin]   gi|119391784|emb|CAJ\nPECEADGIIQSVNWGTPGLSNEGLVIGFNVLLETGRHVAANNISQTLMEELTANAKEHGEDYYNGWACQVAYMEETSDGSLRHPSFVMF\nR\n>gi|77118174|ref|YP_338096#3 ligase [Enterobacteria phage K1F]   gi|72527918|gb|AAZ7297\nPSEEADGHVVRPVWGTEGLANEGMVIGFDVMLENGMEVSATNISRALMSEFTENVKSDPDYYKGWACQITYMEETPDGSLRHPSFDQW\nR\n>gi|17570796|ref|NP_523305#4 DNA ligase [Bacteriophage T3]   gi|118769|sp|P07717|DNLI_B\nPECEADGIIQGVNWGTEGLANEGKVIGFSVLLETGRLVDANNISRALMDEFTSNVKAHGEDFYNGWACQVNYMEATPDGSLRHPSFEKF\nR\n>gi|114796457|emb|CAK24995#5 putative DNA ligase [Bacteriophage LKA1]   E=4e-40 s/c=1.7\nPGFEADGTVIDYVWGDPDKANANKIVGFRVRLEDGAEVNATGLTQDQMACYTQSYHATAYEVGITQTIYIGRACRVSGMERTKDGSIRHPHFDGF\nR\n>gi|68299729|ref|YP_249578#6 DNA ligase [Vibriophage VP4]   gi|66473268|gb|AAY46277.1|\nPEGEIDGTVVGVNWGTVGLANEGKVIGFQVLLENGVVVDANGITQEQMEEYTNLVYKTGHDDCFNGRPVQVKYMEKTPKGSLRHPSFQRW\nR\n>gi|33300828|ref|NP_877456#7 putative ATP-dependent DNA ligase [Bacteriophage phiKMV]\nPEITVDGRIVGYVMGKTGKNVGRVVGYRVELEDGSTVAATGLSEEHIQLLTCAYLNAHIDEAMPNYGRIVEVSAMERSANTLRHPSFSRF\nR\n>gi|29366706|ref|NP_813751#8 putative DNA ligase [Pseudomonas phage gh-1]   gi|29243565\nPDDNEDGFIQDVIWGTKGLANEGKVIGFKVLLESGHVVNACKISRALMDEFTDTETRLPGYYKGHTAKVTFMERYPDGSLRHPSFDSF\nR\n>gi|114796395|emb|CAK25951#9 putative ATP-dependent DNA ligase [Bacteriophage LKD16]\nPSLAVEGIVVGFVMGKTGANVGKVVGYRVDLEDGTIVSATGLTRDRIEMLTTEAELLGGADHPGMADLGRVVEVTAMERSANTLRHPKFSRF\nR"
}