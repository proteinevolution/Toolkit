// Put your javascript code here!
function pasteExample() {
    $('sequence_input').value = "[LIVMF](2)-D-E-A-D-[RKEN]-x-[LIVMFYGSTN]";
}

function deselect()
{
  var number = parseInt($('CHECKBOXES').value, 10);
  for(i = 0; i < (2*number); i++) {
    $(patsearch_form).elements["hits[]"][i].checked = false;
  }
  calculate_forwarding();
  //count_checked_checkboxes()
}
function select()
{ 
  var number = parseInt($('CHECKBOXES').value, 10);
  for(i = 0; i < (2*number); i++) {
    $(patsearch_form).elements["hits[]"][i].checked = true;
  }
  calculate_forwarding();
  //count_checked_checkboxes();
}
function select_first()
{ 
  var number = parseInt($('CHECKBOXES').value, 10);
  var first = 10;
  for(i = 0; i < (2*number); i++) {
    if (i < first || (i >= number && i < (number+first))) {
      $(patsearch_form).elements["hits[]"][i].checked = true;
    } else {
      $(patsearch_form).elements["hits[]"][i].checked = false;
    }
  }
 calculate_forwarding();
  //count_checked_checkboxes(); 
}

function change(num, block)
{
  var number = parseInt($('CHECKBOXES').value, 10);
  var mode = $(patsearch_form).elements["hits[]"][(block * number)+num].checked;
  for (i = 0; i < 2; i++) {
    $(patsearch_form).elements["hits[]"][(i * number)+num].checked = mode;
  }
}

function count_checked_checkboxes(){
	
    var checked_checkboxes = $$('input[type="checkbox"]:checked').length;
    var number_of_tools_for_one_seq = parseInt($('NUMBER_OF_TOOLS_FOR_ONE_SEQ').value);
    var number_of_tools_for_more_than_one_seq = parseInt($('NUMBER_OF_TOOLS_FOR_MORE_THAN_ONE_SEQ').value);
    var total_tools = number_of_tools_for_one_seq + number_of_tools_for_more_than_one_seq;
    var tool_array = $('destination');
    
    if(checked_checkboxes > 1){
        for(i = 0; i < number_of_tools_for_more_than_one_seq; i++){
            tool_array[i].disabled = false;
        }
        for(i = number_of_tools_for_more_than_one_seq; i < total_tools; i++){
            tool_array[i].disabled = true;
        }
        if(tool_array.options.selectedIndex >= number_of_tools_for_more_than_one_seq){
            tool_array[0].selected = true;
        }
        $('forwarding_info').innerHTML = "Note: More than one sequence is currently selected. You can forward these sequences to several tools for creating an alignment or classification. " +
                                         "For searching a database, for example with HHBlits, please create an alignment first.";
        $(forwardbutton).disabled = false;
    } else if(checked_checkboxes == 1){
        for(i = 0; i < number_of_tools_for_more_than_one_seq; i++){
            tool_array[i].disabled = true;
        }
        for(i = number_of_tools_for_more_than_one_seq; i < total_tools; i++){
            tool_array[i].disabled = false;
        }
        if(tool_array.options.selectedIndex < number_of_tools_for_more_than_one_seq){
            tool_array[number_of_tools_for_more_than_one_seq].selected = true;
        }
        $('forwarding_info').innerHTML = "Note: One sequence is currently selected. You can forward this sequence to several tools for database searches and sequence analysis. ";
        $(forwardbutton).disabled = false;
    } else{
        for(i = 0; i < total_tools; i++){
            tool_array[i].disabled = true;
        }
        $('forwarding_info').innerHTML = "Note: No sequence is currently selected. Forwarding is therefore not possible.";
        tool_array[tool_array.options.selectedIndex].selected = false;
        $(forwardbutton).disabled = true;
    }
}


