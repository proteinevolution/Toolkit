/* Data table helpers */

/* REGEX FOR DB IDENTIFICATION*/
const uniprotReg = "^([A-Z0-9]{10}|[A-Z0-9]{6})$";
const scopReg = "^([defgh][0-9a-zA-Z\.\_]+)$";
const ecodReg = "^(ECOD_)";
const mmcifReg = "^(...._[a-zA-Z])$";
const mmcifShortReg = "^([0-9]+)$";
const pfamReg = "^(pfam[0-9]+&|^PF[0-9]+(\.[0-9]+)?)$";
const ncbiReg = "^([A-Z]{2}_?[0-9]+\.?\#?([0-9]+)?|[A-Z]{3}[0-9]{5}?\.[0-9])$";


function download(filename: string, text: string) {
    let a: any = document.createElement("a");
    document.body.appendChild(a);
    a.style = "display: none";
    let blob = new Blob([text], {type: "octet/stream"}),
        url = window.URL.createObjectURL(blob);
    a.href = url;
    a.download = filename;
    a.click();
    window.URL.revokeObjectURL(url);
    $.LoadingOverlay("hide");
}


/* Slider */
function slider_show(sequence_length: number, start: number, end: number) {

    let tooltip = $('<div id="tooltip" />').css({
        position: 'absolute',
        top: -20
    }).show();

    let tooltip2 = $('<div id="tooltip2" />').css({
        position: 'absolute',
        top: -20

    }).show();

    let $flatSlider = $("#flat-slider");

    $flatSlider.slider({
        range: true,
        orientation: 'horizontal',
        min: 1,
        max: sequence_length,
        step: 1,
        values: [start, end],
        slide: function (event, ui) {
            tooltip.text(ui.values[0]);
            tooltip2.text(ui.values[1]);
        },
        change: function (event, ui) {
            $flatSlider.slider("option", "values");
        }
    });

    tooltip.text(start);
    tooltip2.text(end);

    $flatSlider.slider({}).find(".ui-slider-handle:first").append(tooltip);

    $flatSlider.slider({}).find(".ui-slider-handle:last").append(tooltip2);

}


function getSliderRange() {

    return $('#flat-slider').slider("option", "values");
}


function resubmitSection(sequence: string, name: string) {
    let sliderRange = getSliderRange();
    let resubmitSeqs = [];

    resubmitSeqs.push(name + '\n');
    resubmitSeqs.push(sequence.substr(sliderRange[0], sliderRange[1]) + '\n');

    $('a[href="#tabpanel-Input"]').click();
    $('#alignment').val(resubmitSeqs.join(''));
}


/**
 * forward the given forwardData to the given tool
 * @param tool
 * @param forwardData
 */
function forward(tool: string, forwardData: string) {
    if (forwardData == "") {
        alert("No sequence(s) selected!");
        $.LoadingOverlay("hide");
        return;
    }
    try {
        localStorage.setItem("resultcookie", forwardData);
        window.location.href = "/#/tools/" + tool;
    } catch (e) {
        if (isQuotaExceeded(e)) {
            // Storage full, maybe notify user or do some clean-up
            $.LoadingOverlay("hide");
            alert("File is too big to be forwarded. Please download the file and use the upload function of the selected tool.")
        }

    }
}


function isQuotaExceeded(e: any) {
    let quotaExceeded = false;
    if (e) {
        if (e.code) {
            switch (e.code) {
                case 22:
                    quotaExceeded = true;
                    break;
                case 1014:
                    // Firefox
                    if (e.name === 'NS_ERROR_DOM_QUOTA_REACHED') {
                        quotaExceeded = true;
                    }
                    break;
            }
        } else if (e.number === -2147024882) {
            // Internet Explorer 8
            quotaExceeded = true;
        }
    }
    return quotaExceeded;
}

// load forwarded data into alignment field
$(document).ready(function () {
    let resultcookie = localStorage.getItem("resultcookie");
    $('#alignment').val(resultcookie);
    localStorage.removeItem("resultcookie");
    $.LoadingOverlay("hide");
});


function identifyDatabase(id: string) {
    if (id == null)
        return null;
    if (id.match(scopReg))
        return "scop";
    else if (id.match(ecodReg))
        return "ecod";
    else if (id.match(mmcifShortReg))
        return "mmcif";
    else if (id.match(mmcifReg))
        return "mmcif";
    else if (id.match(pfamReg))
        return "pfam";
    else if (id.match(ncbiReg))
        return "ncbi";
    else if (id.match(uniprotReg))
        return "uniprot";
    else
        return null;
}


/* draw hits */
function drawHits(id: string, hits: Array<any>) {
    let s = Snap("#" + id);
    // Get maximum of query_end with map and reduce
    document.getElementById(id).setAttribute("viewBox", "0 0 " + hits.map(function (hit: any) {
        return hit.query_end;
    }).reduce(function (a, b) {
        return Math.max(a, b);
    }) + " " + hits.length);
    for (let i = 0; i < hits.length; ++i) {
        let hit = hits[i];
        let diff = hit.query_end - hit.query_begin;
        let r = s.rect(hit.query_begin, i, diff, 0.5, 0.5, 0.5);
        let text = s.text(diff / 2, i + 0.4, hit.struc);
        text.attr({
            'font-size': 0.5,
            'fill': 'white',
            'font-weight': 'bold'
        });
        let colors = calcColor(hit.prob).map(function (val) {
            return val * 255
        });
        r.attr({
            fill: Snap.rgb(colors[0], colors[1], colors[2])
        });
    }
}


function calcColor(prob: number) {
    let red, grn, blu;
    if (prob > 40) {
        let signif = ((prob - 40) / 60);
        let col = 4 * signif;
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
        let signif = Math.pow((prob / 40), 3);
        red = 0.2 * (1 - signif);
        grn = 0.2 * (1 - signif);
        blu = 0.2 + 0.8 * signif;
    }
    return [red, grn, blu];
}


function scrollToElem(num: any) {
    num = parseInt(num);
    let elem = $('#tool-tabs').hasClass("fullscreen") ? '#tool-tabs' : 'html, body';
    if (num > shownHits) {
        $.LoadingOverlay("show");
        getHits(shownHits, num, wrapped, colorAAs).done(function () {
            let pos = $('.aln"][value=' + num + ']').offset().top;
            $(elem).animate({
                scrollTop: pos - 100
            }, 1)
        }).then(function () {
            $.LoadingOverlay("hide");
        });
        shownHits = num;
    } else {
        let pos = $('.aln[value=' + num + ']').offset().top;
        $(elem).animate({
            scrollTop: pos - 100
        }, 1)
    }
}

function scrollToSection(name: string) {
    let elem = $('#tool-tabs').hasClass("fullscreen") ? '#tool-tabs' : 'html, body';
    let pos = $('#tool-tabs').hasClass("fullscreen") ? $('#' + name).offset().top + $(elem).scrollTop() : $('#' + name).offset().top + 25;
    $(elem).animate({
        scrollTop: pos
    }, 'fast');

}

// select all checkboxes
function selectAllHelper(name: string) {
    $('input:checkbox.' + name + '[name="alignment_elem"]').each(function () {
        $(this).prop('checked', true);
    });

}

function deselectAll(name: string) {
    $('input:checkbox.' + name + '').prop('checked', false);
}

function selectFromArray(checkboxes: Array<number>) {
    for(var currentVal= 1; currentVal <= numHits + 1; currentVal++){
        $('input:checkbox[value=' + currentVal + '][name="alignment_elem"]').prop('checked', checkboxes.indexOf(currentVal) != -1);
    }
}

function getCheckedCheckboxes(checkboxes: Array<number>) {
    $('input:checkbox:checked[name="alignment_elem"]').each(function () {
        let num = parseInt($(this).val());
        if (checkboxes.indexOf(num) == -1) {
            checkboxes.push(num)
        }
    });
}


function hitlistBaseFunctions() {
    $(document).ready(function () {
        // add tooltipser to visualization
        $('#blastviz area').tooltipster({
            theme: 'tooltipster-borderless',
            position: 'bottom',
            animation: 'fade',
            contentAsHTML: true,
            debug: false
        });
        $.LoadingOverlay("hide");
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

interface Array<T> {
    removeDuplicates(): any;
}
Array.prototype.removeDuplicates = function () {
    return this.filter(function (item: any, index: number, self: any) {
        return self.indexOf(item) == index;
    });
};

function getsHitsManually() {
    if (!loading) {
        let end = shownHits + showMore;
        end = end < numHits ? end : numHits;
        if (shownHits != end) {
            getHits(shownHits, end, wrapped, colorAAs);
        }
        shownHits = end;
    }
}

function linkCheckboxes(checkboxes: Array<number>) {
    $('input:checkbox').on('change', function (e) {
        let currentVal = $(this).val();
        let currentState = $(this).prop('checked');

        // link checkboxes with same value
        $('input:checkbox[value=' + currentVal + '][name=alignment_elem]').each(function () {
            $(this).prop('checked', currentState);
        });

        if (currentState) {
            // push num of checked checkbox into array
            checkboxes.push(parseInt(currentVal));
            // make sure array contains no duplicates
            checkboxes = checkboxes.filter(function (value, index, array) {
                return array.indexOf(value) == index;
            });
        } else {
            // delete num of unchecked checkbox from array
            checkboxes = checkboxes.filter(function (x) {
                return x != currentVal
            });
        }

    });
}


function generateFilename() {
    return Math.floor(100000 + Math.random() * 900000).toString();
}

/**
 * wraps sequences for search tools
 * for this it empties the table "#alignmentTable"
 * and calls get Hits taking the boolean wrapped as a parameter
 */
function wrap() {
    wrapped = !wrapped;
    let elemArr = $(".aln").toArray();
    let num: any = 1;
    for (let i = 0; i < elemArr.length; i++) {
        if ($(elemArr[i]).isOnScreen()) {
            num = $(elemArr[i]).attr("value");
            break;
        }
    }
    $("#wrap").toggleClass("colorToggleBar");
    $("#wrap").toggleText("Unwrap Seqs", "Wrap Seqs");
    $("#alignmentTable").empty();
    getHits(0, shownHits, wrapped, colorAAs).then(function () {
        //linkCheckboxes(checkboxes); todo
        scrollToElem(num);
    });

}


function colorAA() {
    colorAAs = !colorAAs;
    $.LoadingOverlay("show");
    $(".colorAA").toggleClass("colorToggleBar");
    let elemArr = $(".aln").toArray();
    let num: any = 1;
    for (let i = 0; i < elemArr.length; i++) {
        if ($(elemArr[i]).isOnScreen()) {
            num = $(elemArr[i]).attr("value");
            break;
        }
    }
    $("#alignmentTable").empty();
    getHits(0, shownHits, wrapped, colorAAs).then(function () {
        $.LoadingOverlay("hide");
        //linkCheckboxes(checkboxes); todo
        scrollToElem(num);
    });
}


$.fn.extend({
    toggleText: function (a: string, b: string) {
        return this.text(this.text() == b ? a : b);
    }
});

$.fn.isOnScreen = function () {
    let viewport: any = {};
    viewport.top = $(window).scrollTop();
    viewport.bottom = viewport.top + $(window).height();
    let bounds: any = {};
    bounds.top = this.offset().top;
    bounds.bottom = bounds.top + this.outerHeight();
    return ((bounds.top <= viewport.bottom) && (bounds.bottom >= viewport.top));
};