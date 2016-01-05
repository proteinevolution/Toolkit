function deselect()
{
  var number = parseInt($('CHECKBOXES').value, 10);
  for(i = 0; i < (2*number); i++) {
    $(fast_hmmer_form).elements["hits[]"][i].checked = false;
  }
}
function select()
{ 
  var number = parseInt($('CHECKBOXES').value, 10);
  for(i = 0; i < (2*number); i++) {
    $(fast_hmmer_form).elements["hits[]"][i].checked = true;
  }
}
function select_first()
{ 
  var number = parseInt($('CHECKBOXES').value, 10);
  var first = 10;
  for(i = 0; i < (2*number); i++) {
    if (i < first || (i >= number && i < (number+first))) {
      $(fast_hmmer_form).elements["hits[]"][i].checked = true;
    } else {
      $(fast_hmmer_form).elements["hits[]"][i].checked = false;
    }
  }
}

function change(num, block)
{
  var number = parseInt($('CHECKBOXES').value, 10);
  var mode = $(fast_hmmer_form).elements["hits[]"][(block * number)+num].checked;
  for (i = 0; i < 2; i++) {
    $(fast_hmmer_form).elements["hits[]"][(i * number)+num].checked = mode;
  }
}