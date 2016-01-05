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

function showInfo(label)
{
   $('infolabel').value = label;
}

function clearInfo()
{
   $('infolabel').value = "";
}

function deselect()
{
	var number = parseInt($('checkboxes').value, 10);
	for(i = 0; i < number; i++) {
		$(hhcluster_form).elements["hits[]"][i].checked = false;
	}
}
function select()
{ 
	var number = parseInt($('checkboxes').value, 10);
	for(i = 0; i < number; i++) {
		$(hhcluster_form).elements["hits[]"][i].checked = true;
	}
}
function changeURL(key)
{
    $(key).href = "/hhcluster/makeHhpred?id=" + key;
}