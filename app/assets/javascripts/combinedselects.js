function onSimpleClick(event, otherselect) {
    if (!event.shiftKey && !event.ctrlKey) {
	var elements = document.getElementById(otherselect).selectedOptions;
	for (var i = 0; i < elements.length; ) {
	    elements[i].selected = false;
	}
    }

    return true;
}