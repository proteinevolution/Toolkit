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

    $.LoadingOverlay("hide");

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



function getSliderRange() {

    return $('#flat-slider').slider("option", "values");
}



function resubmitSection(sequence, name) {
    var sliderRange = getSliderRange();
    var resubmitSeqs = [];

    resubmitSeqs.push(name + '\n');
    resubmitSeqs.push(sequence.substr(sliderRange[0], sliderRange[1]) + '\n');

    $('#tool-tabs').tabs('option', 'active', $('#tool-tabs').tabs('option', 'active') -2);
    $('#alignment').val(resubmitSeqs.join(''));
}




/* FORWARDING */

// parameter: tool (String)
// forwards all checked identifier and sequences to tool
function forward(tool, forwardData){
    if(forwardData == ""){
        alert("No hits selected!");
        $.LoadingOverlay("hide");
        return;
    }
    localStorage.setItem("resultcookie", forwardData);
    window.location.href = "/#/tools/" + tool;
}

// load forwarded data into alignment field
$(document).ready(function() {
    var resultcookie = localStorage.getItem("resultcookie");
    $('#alignment').val(resultcookie);
    localStorage.removeItem("resultcookie");
    $.LoadingOverlay("hide")
});


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
    var elem = $('#tool-tabs').hasClass("fullscreen") ? '#tool-tabs' : 'html, body';
    if (num > shownHits) {
        $.LoadingOverlay("show");
        getHits(shownHits, num, colorAAs).done(function(data){
            var pos = $('input[name=templates][value=' + num + ']').offset().top;
            $(elem).animate({
                scrollTop: pos - 100
            }, 'fast')
        }).then(function(){
            $.LoadingOverlay("hide");
        });
        shownHits = num;
    }else{
        var pos = $('input[name=templates][value=' + num + ']').offset().top;
        $(elem).animate({
            scrollTop: pos - 100
        }, 'fast')
    }
}

function scrollToSection(name){
    var elem = $('#tool-tabs').hasClass("fullscreen") ? '#tool-tabs' : 'html, body';
    var pos = $('#tool-tabs').hasClass("fullscreen") ? $('#'+name).offset().top + $(elem).scrollTop(): $('#'+name).offset().top;
    $(elem).animate({
        scrollTop: pos-30}, 'fast');
}


// select all checkboxes
function selectAllHelper(name) {
    $('input:checkbox.'+name+'').each(function () {
        $(this).prop('checked', true);
    });

}
function deselectAll(name){
    $('input:checkbox.'+name+'').prop('checked', false);
}
function selectFromArray(checkboxes){
    checkboxes.forEach(function (currentVal) {
        $('input:checkbox[value='+currentVal+']').prop('checked', true);
    })
}

function getCheckedCheckboxes(){
    $('input:checkbox:checked').each(function(){checkboxes.push(parseInt($(this).val()));});
}


function hitlistBaseFunctions(){
    $(document).ready(function() {
        // add tooltipser to visualization
        $('#blastviz area').tooltipster({
            theme: 'tooltipster-borderless',
            position: 'bottom',
            animation: 'fade',
            contentAsHTML: true,
            debug: false
        });
            $.LoadingOverlay("hide");

            // check checkboxes that are stored in array
            // in order to make it work with pagination/lazyload
            selectFromArray(checkboxes);

            $('input:checkbox').on('change',function (e) {
                var currentVal = $(this).val();
                var currentState = $(this).prop('checked');

                // link checkboxes with same value
                $('input:checkbox[value=' + currentVal + ']').each(function () {
                    $(this).prop('checked', currentState);
                });

                if (currentState) {
                    // push num of checked checkbox into array
                    checkboxes.push(currentVal);
                    // make sure array contains no duplicates
                    checkboxes = checkboxes.filter(function (value, index, array) {
                        return array.indexOf(value) == index;
                    });
                } else {
                    // delete num of unchecked checkbox from array
                    checkboxes = checkboxes.filter(function(x){return x != currentVal});
                }

            });

        followScroll(document);

        // add slider val
        $('.slider').on('moved.zf.slider', function () {
            $('#lefthandle').html($('#hidden1').val());
            $('#lefthandle').css({
                'color': 'white',
                'font-weight': 'bold',
                'padding-left': '2px'
            });
            $('#righthandle').html($('#hidden2').val());
            $('#righthandle').css({
                'color': 'white',
                'font-weight': 'bold',
                'padding-left': '2px'
            });
        });
    });
}


Array.prototype.removeDuplicates = function () {
    return this.filter(function (item, index, self) {
        return self.indexOf(item) == index;
    });
};



function selectAll(){
    selectAllBool = !selectAllBool;
    if(selectAllBool) {
        selectAllHelper(checkbox);
        $(".selectAllSeqBar").addClass("colorToggleBar");
        // first empty array
        checkboxes = [];
        // push all checkboxes (1 to num_hits) into array
        _.range(1, numHits+1).forEach(function(num){return checkboxes.push(num)});
    }
    else {
        deselectAll(checkbox);
        $(".selectAllSeqBar").removeClass("colorToggleBar");
        // delete all checkboxes from array
        checkboxes = [];
    }
}


function getsHitsManually(){
    if (!loading) {
        var end = shownHits + showMore;
        end = end < numHits ? end : numHits;
        if (shownHits != end) {
            getHits(shownHits, end);
        }
        shownHits = end;
    }
}

