function genomessearch(t){
	data = "genomes_expr="+($('genomes_expr').value)+"&data_type="+t;
	new Ajax.Updater(	'genomes_ids_target',
				'/prot_blast/send_ids', 	
				{asynchronous:true, evalScripts:true, 
				parameters:data,
				onLoading:function(request){ Element.toggle('gsearch_lnk','gsearching'); }, 
				onComplete:function(request){ onsearchcomplete(); } 
				}
	);  
}

function onsearchcomplete(){ 
	var val = $('taxids').value;
	taxids2tree(val);	
	Element.toggle('gsearch_lnk','gsearching');  
	expandselected(); 
}

function collapseall(TREE){
	if(TREE.length>2){
		$(TREE[0]+"gdiv").style.display = 'none';
		$(TREE[0]+"gimg").src= '/images/plus.gif';
		for(var i=2; i<TREE.length; i++){
			collapseall(TREE[i]);	
		}	
	}	
}
	
function expandall(TREE){
	if(TREE.length>2){
		$(TREE[0]+"gdiv").style.display = 'block';
		$(TREE[0]+"gimg").src= '/images/minus.gif';
		for(var i=2; i<TREE.length; i++){
			expandall(TREE[i]);	
		}	
	}	
}

function collapserec(level, TREE){
	if(TREE.length>2){
		if(level>0)
			for(var i=2; i<TREE.length; i++) collapserec(level-1, TREE[i]);
		else{
			$(TREE[0]+"gdiv").style.display = 'none';
			$(TREE[0]+"gimg").src= '/images/plus.gif';
		}
	}
}
	
function expandselected(){
	expandselectedrec(tree[0]);
}

function expandselectedrec(TREE){
	if(TREE.length>2){
		var r=0;
		for(var i=2; i<TREE.length; i++) r+=expandselectedrec(TREE[i]);
		if(r>0){
			$(TREE[0]+"gdiv").style.display = 'block';
			$(TREE[0]+"gimg").src= '/images/minus.gif';
		}else{
			$(TREE[0]+"gdiv").style.display = 'none';
			$(TREE[0]+"gimg").src= '/images/plus.gif';
		}
		return r;				
	}else return TREE[1];
	
		
}


function dump(obj){
	var outstr = "";
	for (var i in obj)
		outstr += i+"\n";
	return outstr;
}

function loadtogglegenomes(t, level){
    if( !$('gtree') ){
	dblist = $("std_dbs");
	for( i=0; i<dblist.length; i++ ) dblist.options[i].selected = false;
	data = "data_type="+t;
	var x = new Ajax.Updater(	'gtree_target', 
				'/prot_blast/send_gtree', 
				{asynchronous:true, evalScripts:true, 	
				parameters:data,
				onLoading:function(request){ Element.toggle('gshow_lnk','gloading_lnk'); }, 
				onComplete:function(request){ Element.toggle('ghide_lnk','gloading_lnk'); collapserec(level, tree[0]);} 
	});
    }else{  
  	Effect.toggle('gtree');
	Effect.toggle('gshow_lnk');
	Effect.toggle('ghide_lnk');
    }
}

function toggleplusminus(obj, hdiv){
	Effect.toggle(hdiv);
	if( obj.src.search(/plus/) != -1) obj.src='/images/minus.gif';
	else obj.src= '/images/plus.gif';			
}

function gettaxids(){
	return gettaxidsrec(tree[0]);
}

function count(){
	return reccount(tree[0]);
}

function reccount(TREE){
	var c=0;
	if(TREE.length>2){
		for(var i=2; i<TREE.length; i++)
			c+=reccount(TREE[i]);	
		return c;
	}else
		if(TREE[1]==1)	
			return 1;		
	return 0;
}

function gettaxidsrec(TREE){
	var ids="";
	if(TREE.length>2){
		for(var i=2; i<TREE.length; i++)
			ids = ids+gettaxidsrec(TREE[i]);	
		return ids;
	}else
		if(TREE[1]==1)	
			return TREE[0]+" ";		
	return "";
}

function taxids2tree(ids){
	deselect();	
	if(ids!=""){
			var id_ar = ids.split(" ");
			for(var i=0; i<id_ar.length; i++)
				if(id_ar[i]!="")findNode(parseInt(id_ar[i]), tree[0], true);
			control(tree);
	}
}


function deselect(){	
//	dblist = $("std_dbs");
//	for( i=0; i<dblist.length; i++ ) dblist.options[i].selected = false;
	deselectrec(tree[0]);
	c=0;	
	$('count_label').innerHTML=c+" Proteomes selected.";
	document.getElementById('taxids').value ="";
}

//recursive unchecking of tree not working.....
function deselectrec(TREE){
	if(TREE.length>2){
		for(var i=2; i<TREE.length; i++)	
			deselectrec(TREE[i]);	
	}
	$(TREE[0]+"gchk").checked=false;
	TREE[1]=0;		
}

function checker(obj){
	var id = parseInt(obj.id);
	findNode(id, tree[0], obj.checked);
	control(tree);
}

function findNode(ID, TREE, STAT){
	if(TREE[0]==ID){
		changeStatus(TREE,STAT);
		return true;
	}else
		if(TREE.length>2)
			for(var i=2; i<TREE.length; i++){
				var res=findNode(ID, TREE[i], STAT);
				if(res) break;
			}
}

function findLeaf(ID, TREE, STAT){
	if(TREE[0]==ID && TREE.length==2){
		changeStatus(TREE,STAT);
		return true;
	}else
		if(TREE.length>2)
			for(var i=2; i<TREE.length; i++){
				var res=findNode(ID, TREE[i], STAT);
				if(res) break;
			}
}

function changeStatus(TREE, b){
	$(TREE[0]+"gchk").checked=b;
	if(b==true)TREE[1]=1;
	else TREE[1]=0;
	if(TREE.length>2)
		for(var i=2; i<TREE.length; i++)
			changeStatus(TREE[i], b);
}

function control(obj){
	var button = $('submitform');
	controller(obj[0]);
        var c = count();
        if(c==1){
		$('count_label').innerHTML=c+" Proteome selected";
	}
        else{	
		$('count_label').innerHTML=c+" Proteomes selected.";
	// If the user has selected more than 900 genomes remind him that system may crash
	}
	if(c>900){
		$('count_label').innerHTML=c+" Proteomes selected.<br><font color=red><b>You have exceeded the Proteome Limit ! 900 Proteomes.</b></font>";
		button.disabled = true;
	        $('gtree').setStyle({background:'#FFF8C6'});
		}
	if(c<900){
	button.disabled =false;
	$('gtree').setStyle({background:'white'});
	}
	}

function controller(TREE){
	if(TREE.length>2){
		var res = true;	
		for(var i=2; i<TREE.length; i++){
			var b=controller(TREE[i]);
			if(b==false) res = false;
		}
		if(res==true){
			$(TREE[0]+"gchk").checked=true;
			TREE[1]=1;
		}else{
			$(TREE[0]+"gchk").checked=false;
			TREE[1]=0;
		}
		return res;
	}else{
		if(TREE[1]==0) return false;
		return true;
	}	
}


