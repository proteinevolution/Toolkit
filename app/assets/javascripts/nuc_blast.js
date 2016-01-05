function deselect()
{
  var number = parseInt($('CHECKBOXES').value, 10);
  for(i = 0; i < (2*number); i++) {
    $(nuc_blast_form).elements["hits[]"][i].checked = false;
  }
}
function select()
{ 
  var number = parseInt($('CHECKBOXES').value, 10);
  for(i = 0; i < (2*number); i++) {
    $(nuc_blast_form).elements["hits[]"][i].checked = true;
  }
}
function select_first()
{ 
  var number = parseInt($('CHECKBOXES').value, 10);
  var first = 10;
  for(i = 0; i < (2*number); i++) {
    if (i < first || (i >= number && i < (number+first))) {
      $(nuc_blast_form).elements["hits[]"][i].checked = true;
    } else {
      $(nuc_blast_form).elements["hits[]"][i].checked = false;
    }
  }
}

function change(num, block)
{
  var number = parseInt($('CHECKBOXES').value, 10);
  var mode = $(nuc_blast_form).elements["hits[]"][(block * number)+num].checked;
  for (i = 0; i < 2; i++) {
    $(nuc_blast_form).elements["hits[]"][(i * number)+num].checked = mode;
  }
}

function toggle_blosum(){
	if($('program').value=='blastn') $('matrix').disabled=true;
	else $('matrix').disabled=false;
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


