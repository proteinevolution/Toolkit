function deselect()
{
  var number = parseInt($('CHECKBOXES').value, 10);
  for(i = 0; i < (2*number); i++) {
    $(prot_blast_form).elements["hits[]"][i].checked = false;
  }
  calculate_forwarding();
}
function select()
{ 
  var number = parseInt($('CHECKBOXES').value, 10);
  for(i = 0; i < (2*number); i++) {
    $(prot_blast_form).elements["hits[]"][i].checked = true;
  }
  calculate_forwarding();
}
function select_first()
{ 
  var number = parseInt($('CHECKBOXES').value, 10);
  var first = 10;
  for(i = 0; i < (2*number); i++) {
    if (i < first || (i >= number && i < (number+first))) {
      $(prot_blast_form).elements["hits[]"][i].checked = true;
    } else {
      $(prot_blast_form).elements["hits[]"][i].checked = false;
    }
  }
  calculate_forwarding();
}

function change(num, block)
{
  var number = parseInt($('CHECKBOXES').value, 10);
  var mode = $(prot_blast_form).elements["hits[]"][(block * number)+num].checked;
  for (i = 0; i < 2; i++) {
    $(prot_blast_form).elements["hits[]"][(i * number)+num].checked = mode;
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

function test(){
	alert((($(destination))[0]).getAttribute('acceptance'))
}
function pasteExample()
{
  $('sequence_input').value = ">gi|21229011|ref|NP_634933.1| hypothetical protein MM_2909 [Methanosarcina mazei Go1]\nMTLVIAFIGKNGAVMAGDMREITFEGEKPDREKLEKELYSGSIVTDEEMQKKAEEFGVKITVADCKEKVS\nERNGVLVGEVSSAEGGVVKKRRLYASAGNFAIAELINTEMTLTSQGKGSNFIAFGNEFTKQVANKCFKDN\nWTKKSNLQDAVKILILCMETVARKTASVSKQFMIVQTASNADVLKVVEKDRNC";
  $('std_dbs').options.selectedIndex = 2;  
}
