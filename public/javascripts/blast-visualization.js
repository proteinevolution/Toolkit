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
        top: +30
    }).hide();

    var tooltip2 = $('<div id="tooltip2" />').css({
        position: 'absolute',
        top: +30
    }).hide();



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
    }).find(".ui-slider-handle:first").append(tooltip).hover(function() {
        tooltip.show()
    }, function() {
        tooltip.hide()
    })

    $("#flat-slider").find(".ui-slider-handle:last").append(tooltip2).hover(function() {
        tooltip2.show()
    }, function() {
        tooltip2.hide()
    })
}

/* from maplink overlay go to alignment ref */
(function($) {
    $.fn.goTo = function() {
        $('html, body').animate({
            scrollTop: $(this).offset().top + 'px'
        }, 'fast');
        return this;
    };


})(jQuery);