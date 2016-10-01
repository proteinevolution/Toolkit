function deselectHitsAlignments()
{
  var number = parseInt($('CHECKBOXES').value, 10);
  var checkboxes = hits_alignments_form.elements["hits[]"];
  for(i = 0; i < (2*number); i++) {
    checkboxes[i].checked = false;
  }
  calculate_forwarding();
}

function selectHitsAlignments()
{
  var number = parseInt($('CHECKBOXES').value, 10);
  var checkboxes = hits_alignments_form.elements["hits[]"];
  for(i = 0; i < (2*number); i++) {
    checkboxes[i].checked = true;
  }
  calculate_forwarding();
}

function selectFirstHitsAlignments()
{ 
  var number = parseInt($('CHECKBOXES').value, 10);
  var first = 10;
  var checkboxes = hits_alignments_form.elements["hits[]"];
  for(i = 0; i < (2*number); i++) {
    if (i < first || (i >= number && i < (number+first))) {
      checkboxes[i].checked = true;
    } else {
      checkboxes[i].checked = false;
    }
  }
  calculate_forwarding();
}

function changeSelection(num, block)
{
  var number = parseInt($('CHECKBOXES').value, 10);
  var checkboxes = hits_alignments_form.elements["hits[]"];
  var mode = checkboxes[(block * number)+num].checked;
  for (i = 0; i < 2; i++) {
    checkboxes[(i * number)+num].checked = mode;
  }
}

function appearForward(val)
{
    if (Element.getStyle(val, 'display') == "none") {
	new Effect.Appear(val);
    } else {
	new Effect.Fade(val);
    }
}

function setFwAction(form, dest) 
{
    $(form).action = $(dest).options[$(dest).selectedIndex].value;
}

function setFwActionDirect(form, dest) 
{
	$(form).action = dest
}

function setHook(id, list) 
{ 	
    $(id).value = $(list).options[$(list).selectedIndex].value;
}   

function setElement(id, val) 
{	
    setValue(id, val);	
}

function setAction(id, val) 
{	
    $(id).action = val;
}

function setTarget(id, val) 
{	
    $(id).target = val;
}

function setValue(id, val) 
{	
    $(id).value = val;
}

function getValue(id) 
{	
    return $(id).value;
}

function toggleDisabled(id)
{
    $(id).disabled = !$(id).disabled;
}

// Anchor a single checkbox element and disable other
// element according to Checkbox state (used for multi
// checkboxes
function toogleSelectiveDisabled(id, checkbox)
{ 
 if($(checkbox).checked){
    $(id).disabled = true; 
    $(id).checked = false;  
 }
 else
 {
    $(id).disabled = false;
 } 
}

function toggleTarget(id)
{	
    if ($(id).target == '_self') {		
	$(id).target = '_blank';	
    } else {
	$(id).target = '_self';	
    }  
}

function toggleExport(form, dest)
{	
    if ($(form).target == '_blank') {	
	$(form).target = '_self';
    } else {
	$(form).target = '_blank';	
    }  
    $(form).action = $(dest).options[$(dest).options.selectedIndex].value;
}

function toggleForward(id)
{
    if (Element.getStyle(id, 'display') == "none") {
	new Effect.Appear(id);
    } else {
	new Effect.Fade(id);
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

function openHelpWindow(url) 
{	
    var helpwindow = window.open(url,'helpwindow','width=950,height=700,left=0,top=0,scrollbars=yes,resizable=no'); 	
    helpwindow.focus();
}

function show_sitereq()
{	
/* if (!is_opera7up && !is_nav7up && !is_moz && !is_fx && !is_safari)	{
   $('sitereq').style.display = "block";		
   $('sitereqfull').style.display = "block"; 	
} */	
}

function show_periodicity()
{
    obj.visibility='show';
}

function MM_findObj(n, d) 
{
    var p,i,x;  
    if(!d) d=document; if((p=n.indexOf("?"))>0&&parent.frames.length) {    
	d=parent.frames[n.substring(p+1)].document; 
	n=n.substring(0,p);
    }  
    if(!(x=d[n])&&d.all) 
	x=d.all[n]; 
    for (i=0;!x&&i<d.forms.length;i++) 
	x=d.forms[i][n];  
    for(i=0;!x&&d.layers&&i<d.layers.length;i++) 
	x=MM_findObj(n,d.layers[i].document);  
    if(!x && document.getElementById) 
	x=document.getElementById(n); 
    return x;
}

function MM_showHideLayers() 
{   
    var i,p,v,obj,args=MM_showHideLayers.arguments;  
    for (i=0; i<(args.length-2); i+=3) 
	if ((obj=MM_findObj(args[i]))!=null) { 
	    v=args[i+2];    
	    if (obj.style) { 
		obj=obj.style; 
		v=(v=='show')?'visible':(v='hide')?'hidden':v; 
	    }    
	    obj.visibility=v; 
	}
}

function openUserdbWindow(url) 
{
    var userdbwin = window.open(url,'userdbwindow','width=700,height=360,left=0,top=0,scrollbars=yes,resizable=no');
    userdbwin.focus(); 
}

function deselectjobs()
{
    var number = $(clear_checkboxes).value;
    if (number==1) {
	document.clearrecentjobs.elements["jobid[]"].checked = false;
    } else {
	for(i=0; i<number; i++) {
	    document.clearrecentjobs.elements["jobid[]"][i].checked = false;
	}
    }
}

function selectjobs()
{ 
    
    var number = $(clear_checkboxes).value;
    if (number==1) {
	document.clearrecentjobs.elements["jobid[]"].checked = true;
    } else {
	for(i=0; i<number; i++) {
	    document.clearrecentjobs.elements["jobid[]"][i].checked = true;
	}
    }
}

function sleep(numberMillis) 
{
    var now = new Date();
    var exitTime = now.getTime() + numberMillis;
    while (true) {
	now = new Date();
	if (now.getTime() > exitTime)
	    return;
    }
}

function calculate_forwarding(){
	var number_of_lines_per_sequence = $('NUMBER_OF_LINES_PER_SEQUENCE') == undefined ? 1 : $('NUMBER_OF_LINES_PER_SEQUENCE').value;
	var checked_checkboxes = Math.floor($$('input[type="checkbox"]:checked').length / number_of_lines_per_sequence);
	var total_tools = parseInt($('NUMBER_OF_FORWARDING_ACCEPTORS').value);
	var tool_array = $('destination');
	
	if(checked_checkboxes > 1){
		var enabled_tools = 0;
		for(i = 0; i < total_tools; i++){
			if((parseInt((($(destination))[i]).getAttribute('acceptance')) & parseInt($(EMISSION_TYPE).value)) != 0){
				tool_array[i].disabled = false;
				enabled_tools++;
			} else{
				tool_array[i].disabled = true;
			}
		}
		if(tool_array.options.selectedIndex >= 0 && tool_array[tool_array.options.selectedIndex].disabled){
			for(i = total_tools - 1; i >= 0; i--){
				if(!tool_array[i].disabled){
					tool_array[i].selected = true;
				}
			}
		}
		if(enabled_tools > 0){
			$(forwardbutton).disabled = false;
			tool_array.disabled = false;
		} else{
			$(forwardbutton).disabled = true;
			tool_array.disabled = true;
		}
	} else if(checked_checkboxes == 1){
		var enabled_tools = 0;
		for(i = total_tools - 1; i >= 0; i--){
			if((parseInt((($(destination))[i]).getAttribute('acceptance')) & 1) != 0){
				tool_array[i].disabled = false;
				enabled_tools++;
			} else{
				tool_array[i].disabled = true;
			}
		}
		if(tool_array.options.selectedIndex >= 0 && tool_array[tool_array.options.selectedIndex].disabled){
			for(i = total_tools - 1; i >= 0; i--){
				if(!tool_array[i].disabled){
					tool_array[i].selected = true;
				}
			}
		}
		if(enabled_tools > 0){
			$(forwardbutton).disabled = false;
			tool_array.disabled = false;
		} else{
			$(forwardbutton).disabled = true;
			tool_array.disabled = true;
		}
	} else{
		for(i = 0; i < total_tools; i++){
			tool_array[i].disabled = true;
		}
		tool_array.disabled = true;
		$(forwardbutton).disabled = true;
	}
}





function slider_show(sequence_length, start, end) {

    var tooltip = $('<div id="tooltip" />').css({
        position: 'absolute',
        top: -25
    }).hide();

    var tooltip2 = $('<div id="tooltip2" />').css({
        position: 'absolute',
        top: -25
    }).hide();



    $("#flat-slider").slider({
        range: true,
        orientation: 'horizontal',
        min: 1,
        max: sequence_length,
        step: 1,
        values: [start, end],
        slide: function(event, ui) {
            tooltip.text(ui.values[0]);
            tooltip2.text(ui.values[1]);
        },
        change: function(event, ui) {
            var sliderCoords =  $('#flat-slider').slider("option", "values");


            $('area').each(function (index) {
                var coords = $(this).attr('coords').split(',');

                // calculating length of seq relative to sequence length and the width of the png (800px)
               var left = parseInt((sequence_length/800)*coords[0],10);
                var right = parseInt((sequence_length/800)*coords[2],10);

                if (left >= sliderCoords[0] && right <= sliderCoords[1]) {
                  $('input[type=checkbox][value=index]').prop('checked', true);
                }
            })



        }
    }).find(".ui-slider-handle:first").append(tooltip).hover(function() {
        tooltip.show()
    }, function() {
        tooltip.hide()
    })

    $("#flat-slider").find(".ui-slider-handle:last").append(tooltip2).hover(function() {
        tooltip2.show()
    }, function() {
        tooltip2.hide()
    })
}

/* Forwarding */

function forward(tool) {
    var seqs = "";
    $('input:checkbox.hits').each(function () {
        if(this.checked){
            // get sequences of checked checkboxes
            seqs += $(this).closest('tr').text();
        }
    })
    localStorage.setItem ( "resultcookie", seqs ) ;
    window.location.href = "/#/tools/" + tool ;

}

