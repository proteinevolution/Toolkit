function appearForward(val)
{
    var coloring = 'color_button';
    var image = 'hitlist_img';
    var create = 'createmodel';
    var merge = 'mergeali';
    var pcoils = 'pcoils';
    var checked = false;

    // Element is currently display = none and shall be shown
    // For pcoils, disabled checkboxes make no sense. 'noPdb' only makes sense with createmodel.
    if (Element.getStyle(val, 'display') == "none" && (val != create || Element.getStyle('noPdb', 'display') == "none")) {
        if ($(coloring) != null) {
            new Effect.Fade(coloring);
        }
        // Remove all Elements that may be currently shown (except of those to display, because Effect.Appear/Fade
        // takes some time and new invocations don't work.
        if (val != pcoils) {
            new Effect.Fade(pcoils);
        }
        if (val != create) {
            new Effect.Fade(create);
            new Effect.Fade('noPdb');
        }
        //new Effect.Fade(merge);
        //new Effect.Fade(image); Same with this line as above. This code should be revisited soon...
        if ($('forward') == null) {
            new Effect.Fade('forward');
        }

        // Special Case Createmodel, active checkbox
        if (val == create) {
            checkbox_createmodel(true);
            checked = select_first_not_disabled();
            if (checked == false) {
                new Effect.Appear('noPdb');
                new Effect.Fade(val);
            } else {
                new Effect.Appear(val);
                new Effect.Fade('noPdb');
            }
        } else {
            checkbox_createmodel(false);
            // Special Case PCoils, have first element selected
            if(val == pcoils) {
                checked = select_first_not_disabled();
            }
            new Effect.Appear(val);
        }
    } else {
        new Effect.Fade(val);
        if (val == create) {
            new Effect.Fade('noPdb');
        }
        if ($(coloring) != null) {
            new Effect.Appear(coloring);
        }
        new Effect.Appear(image);
    }
}

function select_first_not_disabled()
{
    var checked = false;
    var number = parseInt($('checkboxes').value, 10);
    for(i = 0; i < number; i++) {
        if ($('hit_checkbox'+i).disabled == false) {
            if (checked == false) {
                $('hit_checkbox'+i).checked=true;
                $('hit_checkbox'+(number+i)).checked=true;
                checked = true;
            } else {
                $('hit_checkbox'+i).checked=false;
                $('hit_checkbox'+(number+i)).checked=false;
            }
        }
    }
    return checked;
}

function checkbox_createmodel(mode)
{
    boxes = $('createmodel_disabled_Checkboxes').value;
    if (boxes == "") return;
    checkbox_idx = boxes.split(',');
    number = parseInt(($('checkboxes').value),10);
    for(i=0; i<checkbox_idx.length-1; ++i){
        idx = parseInt(checkbox_idx[i],10);
        if (mode == true) {
            $('hit_checkbox'+idx).checked=false;
            $('hit_checkbox'+idx).disabled=true;
            $('hit_checkbox'+(number+idx)).checked=false;
            $('hit_checkbox'+(number+idx)).disabled=true;
        } else {
            $('hit_checkbox'+idx).disabled=false;
            $('hit_checkbox'+(number+idx)).disabled=false;
        }
    }
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

function deselect()
{
    var number = parseInt($('checkboxes').value, 10);
    for(i = 0; i < number; i++) {
        $('hit_checkbox'+i).checked=false;
        $('hit_checkbox'+(number+i)).checked=false;
    }
}
function select()
{
    var number = parseInt($('checkboxes').value, 10);
    for(i = 0; i < number; i++) {
        $('hit_checkbox'+i).checked=true;
        $('hit_checkbox'+(number+i)).checked=true;
    }
}

function select_first(first)
{
    var number = parseInt($('checkboxes').value, 10);
    if (first > number) {
        first = number;
    }
    for(i = 0; (i < first && i < number); i++) {
        if ($('hit_checkbox'+i).disabled == true) {
            first++;
        } else {
            $('hit_checkbox'+i).checked=true;
            $('hit_checkbox'+(number+i)).checked=true;
        }
    }
    for(i = first; i < number; i++) {
        $('hit_checkbox'+i).checked=false;
        $('hit_checkbox'+(number+i)).checked=false;
    }
}


function change_label(id, labelA, labelB){
// Switching labels by javascript without knowing the context is not useful,
// because labels are not switched i.e. on reset. -kft
// To fix that, the value of the radio button and a template to compare it to
// need to be supplied.
//	if($(id).innerHTML == labelA){
//		   $(id).innerHTML = labelB;
//	}else{
//		  $(id).innerHTML = labelA;
//	}
}

function init_label(id, radioChecked, labelChecked, labelUnchecked){
    alert($(id).innerHTML+"\n"+$(radioChecked).checked);
}


function change(num, block)
{
    var number = parseInt($('checkboxes').value, 10);
    var mode = $('hit_checkbox'+(num-1)).checked;
    $('hit_checkbox'+(number+num-1)).checked=mode;
}

function change_radio(num, block)
{
    var number = parseInt($('checkboxes').value, 10);
    $('hit_checkbox'+(num-1)).checked=true;
    $('hit_checkbox'+(number+num-1)).checked=true;

}

function show_applet() {
    if (!is_opera7up && !is_nav7up && !is_moz && !is_fx && !is_safari && !is_ie6up) {
        $('applet').style.display = "block";
    }
}


function extract_selected(){
    var number = parseInt($('checkboxes').value, 10);
    var selected = "";
    for(i = 0; i < number; i++){
        if($('hit_checkbox'+i).checked) selected += (i+1)+" ";
    }
    $('hits').value=selected;
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

function select_templates(val){

    var number = parseInt($('checkboxes').value, 10);

    if (val == 'single') {
        deselect()
        boxes = $('singleTemp').value;
        boxes = boxes.replace(/ /i, "");
        checkbox_idx = boxes.split(/ /);
        for(j=0; j<=checkbox_idx.length; ++j){
            idx = checkbox_idx[j]-1
            $('hit_checkbox'+idx).checked=true;
            $('hit_checkbox'+(idx+number)).checked=true;
        }
    }
    if (val == 'multi') {
        deselect()
        boxes = $('multiTemp').value;
        boxes = boxes.replace(/ /i, "");
        checkbox_idx = boxes.split(/ /);
        for(j=0; j<=checkbox_idx.length; ++j){
            idx = checkbox_idx[j]-1
            $('hit_checkbox'+idx).checked=true;
            $('hit_checkbox'+(idx+number)).checked=true;
        }
    }
}

function user_selected_templates(){
    $('selectedtemplates_non').checked = true;
}

function toggle_hitlist(){

    if( $('hitlist_btn').value=="Hide bar graph"){
        Effect.Fade('hitlist_img');
        $('hitlist_btn').value="Show graphical overview of hits";
    }else{
        Effect.Appear('hitlist_img');
        $('hitlist_btn').value="Hide bar graph";
    }
}

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

function adjustMoreOptionsDisplay() {
    // because more_options_on is not available on server, visibility has to be adjusted now.
    if ($('more_options_on').value == 'true') {
        $('more_options').style.display = 'block';
        $('show_more_options').style.display = 'none';
        $('hide_more_options').style.display = 'inline';
    } else {
        $('more_options').style.display = 'none';
        $('hide_more_options').style.display = 'none';
        $('show_more_options').style.display = 'inline';
    }
}

function resetDisplayValues(default_show_more_options, default_format) {
    // On reset, we have to toggle the display of the match state assignment according to the default input format.
    // Because the onreset-handler is performed before the reset, we have to reset the relevant values before calling
    // the adjusting functions.

    // if we don't want to close the more options region, if it's open, comment the two lines of code following
    // this comment.
    //$('more_options_on').value = default_show_more_options;
    //adjustMoreOptionsDisplay();

    $('informat').value = default_format;
    adjustMatchModeValues();
    return true;
}

function change_resubmit_form_to_hhblits() {
    var expression = /(.+controller=)hhpred(.+)/;
    expression.exec($(forward_alignment).action);
    $(forward_alignment).action = RegExp.$1 + "hhblits" + RegExp.$2;
}

function pasteExample()
{
    $('sequence_input').value = ">gi|147642904|sp|Q5FVL3.2|FA69B_RAT RecName: Full=Protein FAM69B; AltName: Full=Pancreatitis-induced protein 49; Short=PIP49\nMRRLRRLVHLVLLCPFSKGLQGRLPGLRVKYVLLVWLGIFVGSWMVYVHYSSYSELCRGHVCQVVICDQY\nQKGIISGSVCQDLCELQKVEWRTCLSSAPGQQVYSGLWQDKEVTIKCGIEEALNSKAWPDAVPRRELVLF\nDKPTRGTSIKEFREMTLSFLKANLGDLPSLPALVDQILLMADFNKDSRVSLAEAKSVWALLQRNEFLLLL\nSLQEKEHASRLLGYCGDLYLTESIPHGSWHGAVLLPALRPLLPSVLHRALQQWFGPAWPWRAKIAIGLLE\nFVEELFHGSYGTFYMCETTLANVGYTATYDFKMADLQQVAPEATVRRFLQGRHCEQSSDCIYGRDCRAPC\nDKLMRQCKGDLIQPNLAKVCELLRDYLLPGAPADLYEELGKQLRTCTTLSGLASQVEAHHSLVLSHLKTL\nLWREISNTNYS";
}
