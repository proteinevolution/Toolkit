function inArray(needle, haystack) {
    var length = haystack.length;
    for(var i = 0; i < length; i++) {
        if(haystack[i] == needle) return true;
    }
    return false;
}



//Highlighting thingy
var previous_highlight;

function gcv_highlight(id)
{
  if(previous_highlight)
	gcv_fade(previous_highlight);
  document.getElementById("l"+id).style.backgroundColor="#aaffaa";
  previous_highlight=id;
}
function gcv_fade(id)
{
    document.getElementById("l"+id).style.backgroundColor="transparent";
}
function cb(e)
{
    if (!e) var e = window.event;
    e.cancelBubble = true;
    if (e.stopPropagation) e.stopPropagation();
}

//Infobox thingy (although it says legend. It was late when I coded this...)
//ToDo: rename
function gcv_legend_on(gi,gene_name,descr,complement,start,end,prev_end,next_start)
{
    document.getElementById("gcv_infobox_gene").innerHTML="gi|"+gi+" "+(gene_name==gi?"":gene_name);
    document.getElementById("gcv_infobox_descr").innerHTML=descr;
    document.getElementById("gcv_infobox_location").innerHTML=start+(complement==1?" <- ":" -> ")+end+" ("+(end-start)+" bp / "+((end-start)/3)+" aa)";
    document.getElementById("gcv_infobox_inter").innerHTML=(prev_end!=-1?((start-prev_end)+" bp <| "):"")+gene_name+(next_start!=-1?(" |> "+(next_start-end)+" bp"):"");
    
    return overlib("<table style=width:400px><tr><td><b>Gene:</b></td><td>"+"gi|"+gi+" "+(gene_name==gi?"":gene_name)+"</td></tr><tr><td><b>Description:</b></td><td>"+descr+"</td></tr><tr><td><b>Location:</b></td><td>"+start+(complement==1?" <- ":" -> ")+end+" ("+(end-start)+" bp / "+((end-start)/3)+" aa)</td></tr><tr><td><b>Intergenic Distances:</b></td><td>"+(prev_end!=-1?((start-prev_end)+" bp <| "):"")+gene_name+(next_start!=-1?(" |> "+(next_start-end)+" bp"):"")+"</td></tr></table>",HAUTO,VAUTO,WIDTH,400);
}

function gcv_legend_off()
{
    return nd();
}

function gcv_show_box(id,s,e)
{
  var box=document.getElementById("box"+id);
  var w = (e-s);
  w = (w<16)?16:w;
  box.style.display="block";
  box.style.width=w+"px";
  box.style.left=s+"px";

}
function gcv_hide_box(id)
{
  document.getElementById("box"+id).style.display="none";
}


//Selection thingy

var gi_array = Array();

function gcv_select(gi)
{
  if (!inArray(gi, gi_array))
  {
    gi_array.push(gi);
  }

  gcv_write_array();

}


function gcv_delete(index)
{
  gi_array.splice(index,1);

  gcv_write_array();
}


function gcv_write_array()
{
  if(inx>0)
    scroll_start();
  var temp = "<table>";
  for (var i=0; i < gi_array.length; i++ )
  {
    temp += "<tr><td>"+gi_array[i]+"</td><td><a onClick=\"gcv_delete("+i+")\">X</a></td></tr>";
  }
  document.getElementById("gi_list").innerHTML = temp + "</table><a class=\"gcv_button\" style=\"display:block;text-align:center\" href=\"/gcview/?gi="+gi_array.join('x')+"&parentjob="+window.location.href.split("/").reverse()[0]+"\">Search again</a><a class=\"gcv_button\" style=\"display:block;text-align:center\" href=\"/gi2seq/?gi="+gi_array.join('x')+"&parentjob="+window.location.href.split("/").reverse()[0]+"\">Retrieve Sequence</a>";
}


var scrolling;
var inx = -10;
function scroll_start()
{
scrolling=window.setInterval("do_scroll()",20);
}

function do_scroll()
{
var position=parseInt(document.getElementById("gi_list_container").style.right)+inx;
document.getElementById("gi_list_container").style.right=position+"px";
if ((inx < 0 && position <=-140) || (inx >0 && position >= 20))
        {
        inx=inx*-1;
        if(inx>0)
        {
          var show="show";
          var hide="hide";
        }
        else
        {
          var show="hide";
          var hide="show";
        }
        document.getElementById("gcv_handle_"+show).style.display= "inline";
        document.getElementById("gcv_handle_"+hide).style.display= "none";
        window.clearInterval(scrolling);
        }
}

function gcv_demo_data()
{
  document.getElementById("sequence_input").value="16128329\n16128328\n49176012";
  document.getElementById("informat").value="gi";
}

