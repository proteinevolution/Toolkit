/* Data table helpers */


function toggleAliColor(str) {
    var i;
    if(str == "letters") {
        for(i = 0; i < aas.length; i++) {
            var aa = aas[i];
            aa.style.color = aa_color_font.get(aa.className);
            aa.style.backgroundColor = "white"
        }
            $("#letterColor").addClass( "colorSpan" );
            $("#backgroundColor").removeClass("colorSpan");

    } else if(str == "background") {
        for(i = 0; i < aas.length; i++) {
            aa = aas[i];
            aa.style.backgroundColor = aa_color_background.get(aa.className);
            aa.style.color = "black";
        }
            $("#backgroundColor").addClass( "colorSpan" );
            $("#letterColor").removeClass("colorSpan");
    } else {
        for(i = 0; i < aas.length; i++) {
            aas[i].style.color = "black";
            aas[i].style.backgroundColor = "white";
        }
            $("span").removeClass("colorSpan");
    }
}


function toggleSS(bool) {
    var i;

    if(!bool) {

        for (i = 0; i < ss_helices.length; i++) {
            ss_helices[i].style.color = "black";
        }
        for (i = 0; i < ss_extended.length; i++) {
            ss_extended[i].style.color = "black";
        }
            $("#onlySS").removeClass( "colorSpan" );
    }
    if(bool){

        for (i = 0; i < ss_helices.length; i++) {
            ss_helices[i].style.color = "#D00000";
        }
        for (i = 0; i < ss_extended.length; i++) {
            ss_extended[i].style.color = "#0000D0";
        }
        $("#onlySS").addClass("colorSpan");
    }


}

// Makes a table row with the specified content
function makeRow(entries) {

    var row = document.createElement("tr");
    for(var i = 0; i < entries.length; i++ ) {
        var entry = document.createElement("td");
        entry.innerHTML = entries[i];
        row.appendChild(entry);
    }
    return row;
}
// Makes a table row with colspan=num
function makeRowColspan(entries, num, HTMLElement) {

    var row = document.createElement("tr");
    for(var i = 0; i < entries.length; i++ ) {
        var entry = document.createElement(HTMLElement);
        entry.setAttribute("padding", "0");
        entry.setAttribute("colspan",num);
        entry.innerHTML = entries[i];
        row.appendChild(entry);
    }
    return row;
}

// Makes a table row with colspan=num
function makeRowDiffColspan(entries, num, HTMLElement) {

    var row = document.createElement("tr");
    for(var i = 0; i < entries.length; i++ ) {
        var entry = document.createElement(HTMLElement);
        entry.setAttribute("padding", "0");
        entry.setAttribute("colspan",num[i]);
        entry.innerHTML = entries[i];
        row.appendChild(entry);
    }
    return row;
}


/* Slider */
function slider_show(sequence_length, start, end) {

    var tooltip = $('<div id="tooltip" />').css({
        position: 'absolute',
        top: -20
    }).show();

    var tooltip2 = $('<div id="tooltip2" />').css({
        position: 'absolute',
        top: -20

    }).show();



    $("#flat-slider").slider({
        range: true,
        orientation: 'horizontal',
        min: 1,
        max: sequence_length,
        step: 1,
        values: [start, end],
        slide: function(event, ui) {
            tooltip.text(ui.values[0]);
            tooltip2.text(ui.values[1]);
        },
        change: function(event, ui) {
            var sliderCoords =  $('#flat-slider').slider("option", "values");
        }
    });

    tooltip.text(start);
    tooltip2.text(end);

    $("#flat-slider").slider({}).find(".ui-slider-handle:first").append(tooltip);

    $("#flat-slider").slider({}).find(".ui-slider-handle:last").append(tooltip2);

}

/* from maplink overlay go to alignment ref */
(function($) {
    $.fn.goTo = function() {
        if (!$("#alignments").parent(".is-active").length) {
            $("#accordion").foundation('down',$("#alignments"));
        }
        $('html, body').animate({
            scrollTop: $(this).offset() + 'px'
        }, 'fast');
        return this;
    };
})(jQuery);





function getSliderRange() {

    return $('#flat-slider').slider("option", "values");
}



function resubmitSection(sequence, name) {
    var sliderRange = getSliderRange();
    var resubmitSeqs = [];

    resubmitSeqs.push(name + '\n');
    resubmitSeqs.push(sequence.substr(sliderRange[0] - 1, sliderRange[1]) + '\n');

    $('#tool-tabs').tabs('option', 'active', $('#tool-tabs').tabs('option', 'active') -2);
    $('#alignment').val(resubmitSeqs.join(''));
}


//TODO: works only with timeout. needs to be replaced?
// links two checkboxes with id 'hits'
setTimeout(function() {

    var allPages;


    // listens to alignment
    $('input:checkbox.hitCheckbox').click(function (e) {
        allPages = hitlist.fnGetNodes();
        var currentVal = $(this).val();
        var currentState = $(this).prop('checked');
        // alignment
        $('input[value=' + currentVal + ']').each(function () {
            $(this).prop('checked', currentState);
        });

        $(allPages).find('input[value='+currentVal+']').prop('checked', currentState);

    });

    // listens to dataTable
    $(allPages).find('input[type="checkbox"]').click(function (e) {
        allPages = hitlist.fnGetNodes();
        var currentVal = $(this).val();
        var currentState = $(this).prop('checked');
        // alignment
        $('input[value=' + currentVal + ']').each(function () {
            $(this).prop('checked', currentState);
        })
    });


},2000);



// parameter: all pages from dataTable example: allPages = hitlist.fnGetNodes()
// select all checkboxes
    function selectAll(allPages) {

        // alignment
        $('input:checkbox.hitCheckbox').each(function () {
            var checked = !$(this).data('checked');
            $('input:checkbox.hitCheckbox').prop('checked', checked);
            $(this).data('checked', checked);

        });
        // dataTable
        if ($('input:checkbox.hitCheckbox').prop('checked')) {
            $(allPages).find('input[type="checkbox"]').prop('checked', true);
        } else {
            $(allPages).find('input[type="checkbox"]').prop('checked', false);
        }
    }

/* FORWARDING */

// parameter: tool (String)
// forwards all checked identifier and sequences to tool
function forward(tool, forwardData){
    localStorage.setItem ( "resultcookie", forwardData ) ;
    window.location.href = "/#/tools/" + tool ;

}

// load forwarded data into alignment field
$(document).ready(function() {

    // for getting full alignment
    //curl -X GET 'https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=protein&id=NP_877456,NP_877456&rettype=fasta'
    var resultcookie = localStorage.getItem("resultcookie");
    $('#alignment').val(resultcookie);
    localStorage.removeItem("resultcookie");
});


/* GENERATING LINKS FOR HHPRED */


function getSingleLink(id){
  var db = identifyDatabase(id);
    var pdb = 'http://pdb.rcsb.org/pdb/explore.do?structureId=';
    var ncbi = 'http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?SUBMIT=y&db=structure&orig_db=structure&term=';
    var ebi = 'http://www.ebi.ac.uk/pdbe-srv/view/entry/';
    var pubmed = 'http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?CMD=search&db=pubmed&term=';
    var scop = 'http://scop.berkeley.edu/sid=';
    var scopLineage = 'http://scop.berkeley.edu/sccs=';
    var pfam = 'http://pfam.xfam.org/family?acc=';
    var cdd = 'http://www.ncbi.nlm.nih.gov/Structure/cdd/cddsrv.cgi?uid=';


    switch(db){
      case 'scop':
          var idTrimmed = id.substr(1,4);
          return generateLink(pdb, idTrimmed,id);
          break;
      case 'mmcif':
          var idPdb = id.replace(/\_.*$/ , "");
          return generateLink(pdb, idPdb,id);
          break;
      case 'pfam':
          var idPfam = id.replace(/am.*$/ , "");
          return generateLink(pfam, idPfam+"#tabview=tab1",id);
          break;
      default:
          return null;
  }
}


function getLinks(id){
    var db = identifyDatabase(id);
    var links = [];
    var pdb = 'http://pdb.rcsb.org/pdb/explore.do?structureId=';
    var ncbi = 'http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?SUBMIT=y&db=structure&orig_db=structure&term=';
    var ebi = 'http://www.ebi.ac.uk/pdbe-srv/view/entry/';
    var pubmed = 'http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?CMD=search&db=pubmed&term=';
    var scop = 'http://scop.berkeley.edu/sid=';
    var pfam = 'http://pfam.xfam.org/family?acc=';
    var cdd = 'http://www.ncbi.nlm.nih.gov/Structure/cdd/cddsrv.cgi?uid=';


    switch(db){
        case 'scop':
            var idNCBI = id.substr(1,4);
            links.push(generateLink(scop, id, "SCOP"));
            links.push(generateLink(ncbi, idNCBI,"NCBI"));
            break;
        case 'pfam':
            links.push(generateLink(cdd, id, "CDD"));
            links.push(generateLink(pubmed, id,"PubMed"));
            break;
    }
    if(links.length > 0)
        links.unshift("<a onclick='toggleHistogram()' >Histogram</a> | <a>Template alignment</a>");
    return links;
}

function generateLink(baseLink, id, name){

    return ["<a href=\"",baseLink, id, "\" target=\"\_blank\">", name, "</a>"].join('');
}
function identifyDatabase(id){
    if (id == null)
        return null;
    var scop = new RegExp('(d[0-9].....)');
    var mmcif = new RegExp('(...._[a-zA-Z])|(....)');
    var pfam = new RegExp('(pfam*)');
    if(id.match(scop))
        return "scop";
    else if(id.match(mmcif))
        return "mmcif";
    else if(id.match(pfam))
        return "pfam";
    else
        return null;
}



/* Histograms */

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




/* draw hits */
function drawHits(id, hits) {
    var s = Snap("#" + id);
    // Get maximum of query_end with map and reduce
    document.getElementById(id).setAttribute("viewBox", "0 0 " + hits.map(function (hit) {
            return hit.query_end;
        })
            .reduce(function (a, b) {
                return Math.max(a, b);
            }) + " " + hits.length);
    for (var i = 0; i < hits.length; ++i) {
        var hit = hits[i];
        var diff = hit.query_end - hit.query_begin;
        var r = s.rect(hit.query_begin, i, diff, 0.5, 0.5, 0.5);
        var text = s.text(diff / 2, i + 0.4, hit.struc);
        text.attr({
            'font-size': 0.5,
            'fill': 'white',
            'font-weight': 'bold'
        });
        var colors = calcColor(hit.prob).map(function (val) {
            return val * 255
        });
        r.attr({
            fill: Snap.rgb(colors[0], colors[1], colors[2])
        });
    }
}


function calcColor(prob) {
    var red, grn, blu;
    if (prob > 40) {
        var signif = ((prob - 40) / 60);
        var col = 4 * signif;
        if (col > 3) {
            col -= 3.0;
            red = 1;
            grn = 0.7 * (1 - col);
            blu = 0;
        } else if (col > 2) {
            col -= 2.0;
            red = col;
            grn = 0.7 + 0.3 * (1 - col);
            blu = 0;
        } else if (col > 1) {
            col -= 1.0;
            red = 0;
            grn = 1 - 0.3 * (1 - col);
            blu = 1 - col;
        } else {
            red = 0;
            grn = 0.7 * col;
            blu = 1;
        }
    } else {
        signif = Math.pow((prob / 40), 3);
        red = 0.2 * (1 - signif);
        grn = 0.2 * (1 - signif);
        blu = 0.2 + 0.8 * signif;
    }
    return [red, grn, blu];
}

