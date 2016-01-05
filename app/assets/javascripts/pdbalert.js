function show_upload_form() {
	  	Effect.Fade('upload_option');
	  	Effect.Appear('upload_form');
}
function hide_upload_form() {
          	Effect.Fade('upload_form');
	  	Effect.Appear('upload_option');
}
function reset_informat() {
		document.getElementById("informat").disabled = true;
}
function alert_possible_alignment (url_seq, url_align) {
		var response = confirm("Your input seems to be an \"Alignment\" but you have selected option \"Independent FASTA Sequence(s)\".\n Press OK if you are sure they are \"Independent FASTA Sequence(s)\"\n Press Cancel if it is an \"Alignment\"");
		if(response == true)
		{
			window.location = url_seq;
		}
		else
		{ 
			window.location = url_align;
		}
}
function redir (url) {
		window.location = url;
}
