function setHydro()
{
	if (document.repper_form.criteria[1].checked) {
		//alert("Standard");
		$('ala').value = 0;
		$('arg').value = 0;
		$('asn').value = 0;
		$('asp').value = 0;
		$('cys').value = 0;
		$('gln').value = 0;
		$('glu').value = 0;
		$('gly').value = 0;
		$('his').value = 0;
		$('ile').value = 1;
		$('leu').value = 1;
		$('lys').value = 0;
		$('met').value = 1;
		$('phe').value = 0;
		$('pro').value = 0;
		$('ser').value = 0;
		$('thr').value = 0;
		$('trp').value = 0;
		$('tyr').value = 0;
		$('val').value = 1;
	} else if (document.repper_form.criteria[0].checked) {
		//alert("Kyte-Doolittle");
		$('ala').value = 1.8;
		$('arg').value = -4.5;
		$('asn').value = -3.5;
		$('asp').value = -3.5;
		$('cys').value = 2.5;
		$('gln').value = -3.5;
		$('glu').value = -3.5;
		$('gly').value = -0.4;
		$('his').value = -3.2;
		$('ile').value = 4.5;
		$('leu').value = 3.8;
		$('lys').value = -3.9;
		$('met').value = 1.9;
		$('phe').value = 2.8;
		$('pro').value = -1.6;
		$('ser').value = -0.8;
		$('thr').value = -0.7;
		$('trp').value = -0.9;
		$('tyr').value = -1.3;
		$('val').value = 4.2;
	} else {
		openWindow("/repper/template/");
		$('ala').value = "";
                $('arg').value = "";
                $('asn').value = "";
                $('asp').value = "";
                $('cys').value = "";
                $('gln').value = "";
                $('glu').value = "";
                $('gly').value = "";
                $('his').value = "";
                $('ile').value = "";
                $('leu').value = "";
                $('lys').value = "";
                $('met').value = "";
                $('phe').value = "";
                $('pro').value = "";
                $('ser').value = "";
                $('thr').value = "";
                $('trp').value = "";
                $('tyr').value = "";
                $('val').value = "";
        }

}

function openWindow (address){
	var MeinFenster = window.open(address, 'helpwindow','width=1000,height=400,left=0,top=0,scrollbars=yes,resizable=yes');
	MeinFenster.focus();
}

function closeRepperWindow (){
//        alert(document.repper_form.template.value);
        var templateContent = document.repper_form.template.value.split("\n");
        for (i=0; i<templateContent.length; i++)
	{
		insertValues(templateContent[i]);
	}
	window.close();
}


function insertValues (inputValue){
	var item=inputValue.split(" ");
	if (item.length==1) {
		item.push("0");
	}
	num = checkValue(item[1]);
	if (item[0]=="A"){
		opener.$('ala').value=num;
//		alert("Item A (ala) found! " + opener.$('ala').value);
	} else if (item[0]=="C"){
		opener.$('cys').value=num;
//                alert("Item C (cys) found! " + opener.$('cys').value);
	} else if (item[0]=="C"){
                opener.$('cys').value=num;
//                alert("Item C (cys) found! " + opener.$('cys').value);
        } else if (item[0]=="D"){
                opener.$('asp').value=num;
//                alert("Item D (asp) found! " + opener.$('asp').value);
        } else if (item[0]=="E"){
                opener.$('glu').value=num;
//                alert("Item E (glu) found! " + opener.$('glu').value);
        } else if (item[0]=="F"){
                opener.$('phe').value=num;
//                alert("Item F (phe) found! " + opener.$('phe').value);
        } else if (item[0]=="G"){
                opener.$('gly').value=num;
//                alert("Item G (gly) found! " + opener.$('gly').value);
        } else if (item[0]=="H"){
                opener.$('his').value=num;
//                alert("Item H (his) found! " + opener.$('his').value);
        } else if (item[0]=="I"){
                opener.$('ile').value=num;
//                alert("Item I (ile) found! " + opener.$('ile').value);
        } else if (item[0]=="K"){
                opener.$('lys').value=num;
//                alert("Item K (lys) found! " + opener.$('lys').value);
        } else if (item[0]=="L"){
                opener.$('leu').value=num;
//                alert("Item L (leu) found! " + opener.$('leu').value);
        } else if (item[0]=="M"){
                opener.$('met').value=num;
//                alert("Item M (met) found! " + opener.$('met').value);
        } else if (item[0]=="N"){
                opener.$('asn').value=num;
//                alert("Item N (asn) found! " + opener.$('asn').value);
        } else if (item[0]=="P"){
                opener.$('pro').value=num;
//                alert("Item P (pro) found! " + opener.$('pro').value);
        } else if (item[0]=="Q"){
                opener.$('gln').value=num;
//                alert("Item Q (gln) found! " + opener.$('gln').value);
        } else if (item[0]=="R"){
                opener.$('arg').value=num;
//                alert("Item R (arg) found! " + opener.$('arg').value);
        } else if (item[0]=="S"){
                opener.$('ser').value=num;
//                alert("Item S (ser) found! " + opener.$('ser').value);
        } else if (item[0]=="T"){
                opener.$('thr').value=num;
//                alert("Item T (thr) found! " + opener.$('thr').value);
        } else if (item[0]=="V"){
                opener.$('val').value=num;
//                alert("Item V (val) found! " + opener.$('val').value);
        } else if (item[0]=="W"){
                opener.$('trp').value=num;
//                alert("Item W (trp) found! " + opener.$('trp').value);
        } else if (item[0]=="Y"){
                opener.$('tyr').value=num;
//                alert("Item Y (tyr) found! " + opener.$('tyr').value);
        } else {
//		alert("Not the right item!");
	}
}




function checkValue(value) {
	var up = 150;
	var down = -150;
	value = value.replace(/,/, ".");
	if (value.startsWith(".")) {
		value = "0" + value;
	}
//	alert(value);
	if (isNaN(value) || value=="" || (value > up) || (value < down)) {
		value = 0;
	}
	//if ((value > up) || (value < down)) {
		//alert(value);
	//	value = 0;
		//alert(value);
	//}
	return value;
}


//function checkPerRange()
//{
//   if (!document.getElementById) return false;
//	var o_winsize = document.getElementById('winsize');
//	var o_maxper = document.getElementById('maxper');

//	if(parseInt(o_maxper.value) > parseInt(o_winsize.value) ) {
//		o_maxper.value = o_winsize.value;
//	} 
//}

function setSequence()
{
	$('sequence_input').value = ">gi|401465|sp|P31489|YDA1_YEREN Adhesin yadA precursor\nMTKDFKISVSAALISALFSSPYAFADDYDGIPNLTAVQISPNADPALGLEYPVRPPVPGAGGLNASAKGI\nHSIAIGATAEAAKGAAVAVGAGSIATGVNSVAIGPLSKALGDSAVTYGAASTAQKDGVAIGARASTSDTG\nVAVGFNSKADAKNSVAIGHSSHVAANHGYSIAIGDRSKTDRENSVSIGHESLNRQLTHLAAGTKDTDAVN\nVAQLKKEIEKTQENTNKRSAELLANANAYADNKSSSVLGIANNYTDSKSAETLENARKEAFAQSKDVLNM\nAKAHSNSVARTTLETAEEHANSVARTTLETAEEHANKKSAEALASANVYADSKSSHTLKTANSYTDVTVS\nNSTKKAIRESNQYTDHKFRQLDNRLDKLDTRVDKGLASSAALNSLFQPYGVGKVNFTAGVGGYRSSQALA\nIGSGYRVNENVALKAGVAYAGSSDVMYNASFNIEW";
}
