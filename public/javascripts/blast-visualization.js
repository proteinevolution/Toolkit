/* Data table helpers */



function toggleAliColor(element) {
    if(element.value == "letters") {
        for(var i = 0; i < aas.length; i++) {
            var aa = aas[i];
            aa.style.color = aa_color_font.get(aa.className);
            aa.style.backgroundColor = "white"
        }
    } else if(element.value == "background") {
        for(var i = 0; i < aas.length; i++) {
            var aa = aas[i];
            aa.style.backgroundColor = aa_color_background.get(aa.className);
            aa.style.color = "black"
        }
    } else {
        for(var i = 0; i < aas.length; i++) {
            aas[i].style.color = "black";
            aas[i].style.backgroundColor = "white";
        }
    }
}

function toggleSS(element) {

    if(element.checked) {
        for (var i = 0; i < ss_helices.length; i++) {
            ss_helices[i].style.color="#D00000";
        }
        for (var i = 0; i < ss_extended.length; i++) {
            ss_extended[i].style.color="#0000D0";
        }
    } else {
        for (var i = 0; i < ss_helices.length; i++) {
            ss_helices[i].style.color="black";
        }
        for (var i = 0; i < ss_extended.length; i++) {
            ss_extended[i].style.color="black";
        }
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
function makeRowColspan(entries, num, HTMLelement) {

    var row = document.createElement("tr");
    for(var i = 0; i < entries.length; i++ ) {
        var entry = document.createElement(HTMLelement);
        entry.setAttribute("padding", "0")
        entry.setAttribute("colspan",num);
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
    })

    tooltip.text(start);
    tooltip2.text(end);

    $("#flat-slider").slider({}).find(".ui-slider-handle:first").append(tooltip)

    $("#flat-slider").slider({}).find(".ui-slider-handle:last").append(tooltip2)

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



function resubmitSection(hits, names) {
    if(hits.length < 1) {
        alert("No sequences in selected slider range!");
        return
    }
    var sliderRange = getSliderRange();
    var resubmitSeqs = new Array();

    //for (var i =0 ; i < hits.length; i ++){

    // to resubmit only the first sequence
    for (var i =0 ; i < 1; i ++){
        resubmitSeqs.push(names[i] + '\n')
        resubmitSeqs.push(hits[i].substr(sliderRange[0] - 1, sliderRange[1] - 1) + '\n')
    }
    $('#tool-tabs').tabs('option', 'active', $('#tool-tabs').tabs('option', 'active') -2);
    $('#alignment').val(resubmitSeqs.join(''));
}


//TODO: works only with timeout. needs to be replaced?
// parameter: all pages from dataTable  example: allPages = hitlist.fnGetNodes()


// links two checkboxes with id 'hits'
setTimeout(function() {

    var allPages = hitlist.fnGetNodes()

    // listens to alignment
    $('input:checkbox.hitCheckbox').click(function (e) {
        var currentVal = $(this).val();
        var currentState = $(this).prop('checked');
        // alignment
        $('input[value=' + currentVal + ']').each(function () {
            $(this).prop('checked', currentState);
        })

        $(allPages).find('input[value='+currentVal+']').prop('checked', currentState);

    });

    // listens to dataTable
    $(allPages).find('input[type="checkbox"]').click(function (e) {
        var currentVal = $(this).val();
        var currentState = $(this).prop('checked');
        // alignment
        $('input[value=' + currentVal + ']').each(function () {
            $(this).prop('checked', currentState);
        })
    });


},1000);



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
        if($('input:checkbox.hitCheckbox').prop('checked')) {
            $(allPages).find('input[type="checkbox"]').prop('checked', true);
        }else {
            $(allPages).find('input[type="checkbox"]').prop('checked', false);
        }
    }


