/* Data table helpers */

/* BASELINKS */

var pdbBaseLink = "http://pdb.rcsb.org/pdb/explore.do?structureId=";
var pdbeBaseLink = "http://www.ebi.ac.uk/pdbe/entry/pdb/";
var ncbiBaseLink = "http://www.ncbi.nlm.nih.gov/entrez/query.fcgi?SUBMIT=y&db=structure&orig_db=structure&term=";
var ncbiProteinBaseLink = "https://www.ncbi.nlm.nih.gov/protein/";
var scopBaseLink = "http://scop.berkeley.edu/sid=";
var pfamBaseLink = "http://pfam.xfam.org/family/";
var cddBaseLink = "http://www.ncbi.nlm.nih.gov/Structure/cdd/cddsrv.cgi?uid=";
var uniprotBaseLik = "http://www.uniprot.org/uniprot/";


/* REGEX FOR DB IDENTIFICATION*/
var uniprotReg = "^([A-Z0-9]{10}|[A-Z0-9]{6})$";
var scopReg = "^([defgh][0-9a-zA-Z\.\_]+)$";
var mmcifReg = "^(...._[a-zA-Z])$";
var mmcifShortReg = "^([0-9]+)$";
var pfamReg = "^(pfam[0-9]+&|^PF[0-9]+(\.[0-9]+)?)$";
var ncbiReg = "^([A-Z]{2}_?[0-9]+\.?\#?([0-9]+)?|[A-Z]{3}[0-9]{5}?\.[0-9])$";


function download(filename, text){
    var pom = document.createElement('a');
    pom.setAttribute('href', 'data:text/plain;charset=utf-8,' + encodeURIComponent(text));
    pom.setAttribute('download', filename);

    if (document.createEvent) {
        var event = document.createEvent('MouseEvents');
        event.initEvent('click', true, true);
        pom.dispatchEvent(event);
    }
    else {
        pom.click();
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
        console.log(currentVal)
        var currentState = $(this).prop('checked');
        // alignment
        $('input[value=' + currentVal + ']').each(function () {
            $(this).prop('checked', currentState);
        })
    });


},2000);


/**
 *  This function preprocesses data for forwarding
 * @param selectedTool
 * @param boolSelectedHits
 * @param boolEvalue
 * @param evalue
 * @returns {*}
 */

function preprocessingForward(selectedTool, boolSelectedHits,boolEvalue, evalue, sliderRegion){
    var sliderRange = getSliderRange();
    if(boolSelectedHits) {
        // get checked checkboxes
        var jsonData = getCheckboxesData();
    } else if (boolEvalue) {
        // first deselect all previous selected checkboxes
        _deselectAll();
        // now select all Better than given evalue
        var jsonData = checkAllBetterThan(evalue);
    }
    if(sliderRegion) {
        jsonData = applySliderRange(jsonData, sliderRange);
    }
    return jsonData;
}


function getCheckedCheckboxes(className ){
    var numList = [];
    $('input:checkbox.' + className + ":checked").each(function () {
        numList.push($(this).val());
    });
    return numList;
}


/**
 *  this function applies a range on jsData containing seqs
 * @param jsonData
 * @returns {*}
 */

function applySliderRange(jsonData, sliderRange){
    // get slider range
    //slider range is applied
    jsonData.seqs = jsonData.seqs.map(function(e) {return e.substr(sliderRange[0] - 1, sliderRange[1]) + '\n'});
    return jsonData;
}

// parameter: all pages from dataTable example: allPages = hitlist.fnGetNodes()
// select all checkboxes
    function selectAll(checkboxName) {
        // alignment
        $('input:checkbox.'+checkboxName).each(function () {
            $(this).prop('checked', true);
        });

    }

    function selectAllDatatable(allPages) {
        // dataTable
        $(allPages).find('input[type="checkbox"]').prop('checked', true);
    }

    function deselectAll(checkboxName, allPages){
        $('input:checkbox.'+checkboxName).prop('checked', false);
        $(allPages).find('input[type="checkbox"]').prop('checked', false);
    }

    /*Select top ten Checkboxes*/
    function selectTopTen(checkboxName){

        for(var i=0 ; i < 10; i++){
            $('input:checkbox.'+checkboxName+'[value="' + i + '"]').prop('checked', true);
        }
    }

/* FORWARDING */

// parameter: tool (String)
// forwards all checked identifier and sequences to tool
function forward(tool, forwardData){
    localStorage.setItem("resultcookie", forwardData);
    window.location.href = "/#/tools/" + tool;
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
    if(!id)
        return;
    var db = identifyDatabase(id);
    var idTrimmed = id.substring(1, 5);
    var idPfam = id.replace("am.*$||..*", "");
    var idPdb = id.replace("_.*$", "");

    switch(db){
      case 'scop':
          return generateLink(pdbBaseLink, idTrimmed,id);
          break;
      case 'mmcif':
          return generateLink(pdbBaseLink, idPdb,id);
          break;
      case 'pfam':
          return generateLink(pfamBaseLink, idPfam+"#tabview=tab0",id);
          break;
      case 'ncbi':
          return generateLink(ncbiProteinBaseLink, id,id);
          break;
      case 'uniprot':
          return generateLink(uniprotBaseLik,id,id);
          break;
      default:
          return null;
  }
}

function getLinks(id){
    var db = identifyDatabase(id);
    var links = [];
    var idNcbi = id.replace("#", ".") + "?report=fasta";
    var idPdb = id.replace("_.*$", "").toString().toLowerCase();
    var idTrimmed = id.substring(1, 5);
    var idCDD = id.replace("PF", "pfam");
    switch(db){
        case 'scop':
            links.push(generateLink(scopBaseLink, id, "SCOP"));
            links.push(generateLink(ncbiProteinBaseLink, idTrimmed,"NCBI"));
            break;
        case 'pfam':
            links.push(generateLink(cddBaseLink, idCDD, "CDD"));
            break;
        case 'mmcif':
            links.push(generateLink(pdbeBaseLink, idCDD, "PDBe"));
            break;
        case 'ncbi':
            links.push(generateLink(ncbiProteinBaseLink, idNcbi,"NCBI Fasta"));
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
    if(id.match(scopReg))
        return "scop";
    else if(id.match(mmcifShortReg))
        return "mmcif";
    else if(id.match(mmcifReg))
        return "mmcif";
    else if(id.match(pfamReg))
        return "pfam";
    else if(id.match(ncbiReg))
        return "ncbi";
    else if(id.match(uniprotReg))
        return "uniprot";
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


function scrollToElem(num){

    if (!$("#alignments").parent(".is-active").length) {
        $("#accordion").foundation('down',$("#alignments"));
    }
    var offsetParent = $("#alignments").offset().top;
    var pos = $('input[name=templates][value='+num+']').offset().top;
    console.log(pos+ " " + offsetParent)
    $('html, body').animate({
        scrollTop: pos+offsetParent-1400 + 'px'
    }, 'fast');

}
