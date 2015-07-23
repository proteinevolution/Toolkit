
function tfooterhelp()
{
var th3=0; // Height of the Table
var th=0; // Height of the body
var th2=0; // Height of the Window

// The Routine for Firefox and Mozilla
if (navigator.appCodeName == "Mozilla")
{
  th=document.all.content.offsetHeight;
  th2=window.innerHeight;

  th3= th2-th-150;
}
if (navigator.appName == "Opera") // If check has to be fixed
{
  //The Routine for Opera
  th=document.all.content.offsetHeight;
  th2=window.innerHeight;
  th3= th2-th-160;
} 
if (navigator.appName == "Microsoft Internet Explorer") // If check has to be fixed
{
  //The Routine for The Internet Explorer
  th=document.all.content.offsetHeight;
  th2=document.body.clientHeight;
 
  th3= th2-th-50;
}


// Writing of the table and Div of the Footer
if(th < th2)
{
  document.write('<table width="750px"');
  document.write('height="');
  document.write(th3);
  document.write('px">');

  document.write('<tr valign="bottom"><td align="center" height="');
  document.write(th3-30);
  document.write('px"></td></tr>');
}
else
{
  document.write('<table width="750px">');
}

document.write('<tr top-border="1px" valign="bottom"><td valign="bottom"><div id="footer"><span style="float: right; margin-right: 3px;">Release-0.0.1 (Rails)</span>&#169; 2005, <a href="http://www.eb.tuebingen.mpg.de/dept1/home.html">Dep. of Protein Evolution at the Max-Planck Institute for Developmental Biology</a>, T&#252;bingen</div></td></tr>');
document.write('</table>');
}

function tfooterApp()
{
var th3=0; // Height of the Table
var th=0; // Height of the body
var th2=0; // Height of the Window

//document.write(navigator.appName);

// The Routine for Firefox and Mozilla
if (navigator.appCodeName == "Mozilla")
{
  
  th=document.all.content.offsetHeight;
  
  th2=window.innerHeight;
  th3= th2-th-150;
  
}
if (navigator.appName == "Opera") // If check has to be fixed
{
  //The Routine for Opera
  th=document.all.content.offsetHeight;
  th2=window.innerHeight;
  th3= th2-th-160;
} 
if (navigator.appName == "Microsoft Internet Explorer") // If check has to be fixed
{
  
  //The Routine for The Internet Explorer
  th=document.all.content.offsetHeight;
  th2=document.body.clientHeight;

  th3= th2-th-150;
}


// Writing of the table and Div of the Footer
if(th < th2)
{
  document.write('<table width="750px"');
  document.write('height="');
  document.write(th3);
  document.write('px">');

  document.write('<tr valign="bottom"><td align="center" height="');
  document.write(th3-30);
  document.write('px"></td></tr>');
}
else
{
  document.write('<table width="730px">');
}

document.write('<tr top-border="1px" valign="bottom"><td valign="bottom"><div id="footer"><span style="float: right; margin-right: 3px;">Release-0.0.1 (Rails)</span>&#169; 2005, <a href="http://www.eb.tuebingen.mpg.de/dept1/home.html">Dep. of Protein Evolution at the Max-Planck Institute for Developmental Biology</a>, T&#252;bingen</div></td></tr>');
document.write('</table>');

}