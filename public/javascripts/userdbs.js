function updateUserDB() { 
   if (opener.window.location.href.indexOf('admin_userdbs') > 0) {
   	opener.window.location.reload();
   } else {
   	var dbbox = opener.document.getElementById("userdbs_box"); 
   	if (dbbox.style.display == "none") {
			Element.show(dbbox);
		}
		var dbmenu = opener.document.getElementById("user_dbs"); 
		var i = dbmenu.options.length;
		dbmenu.options[i] = new Option(uploaded_db, uploaded_db_path);
   }
 }