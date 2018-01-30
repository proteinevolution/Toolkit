
declare var loading : boolean;
declare var shownHits : any;
declare var showMore : any;
declare var numHits : any;
declare var jobID: any;
declare var ResultViewHelper: any;
declare var colorAAs: boolean;
declare var wrapped: boolean;
declare var JobModel: any;
let count = 0;


/**
 * Helpers for all Search Tools
 * 1. Highlights the position in the control bar
 * 2. Fixes/Unfixes the control bar at the top
 * 3. triggers getHits on scroll
 *
 */
// add scrollcontainer highlighting
let followScroll = function(element : any) {
    try {
    $(element).ready(function () {
        // Highlights the position in the control bar on click
        $("#alignments").floatingScroll('init');
        //smoothscroll
        $('#scrollLinks a').on('click', function (e) {
            $('a').each(function () {
                $(this).removeClass('colorToggleBar');
            });
            $(this).addClass('colorToggleBar');
        });
    });
    //  Fixes/Unfixes the control bar at the top
    $(element).on("scroll", function(){
        let top = Number($(document).scrollTop());
        if($('#visualization').position() != undefined) {
            if (top >= $('#visualization').position().top + 75) {
                $('.scrollContainer').addClass('fixed');
                $('.scrollContainer').removeClass('scrollContainerWhite');
                $('.scrollContainerDiv').removeClass('scrollContainerDivWhite');
                $('#wrap').show();
                $(".colorAA").show();
                $(".downloadHHR").hide();
            } else {
                $('.scrollContainer').removeClass('fixed');
                $('.scrollContainer').addClass('scrollContainerWhite');
                $('.scrollContainerDiv').addClass('scrollContainerDivWhite');
                $('#wrap').hide();
                $(".colorAA").hide();
                $(".downloadHHR").show();

            }
        }
        // triggers getHits on scroll
        if (top == $(this).height() - $(window).height()) {
            if (!loading) {
		let limit: number = 0;
		if($("#toolnameAccess").val() === "psiblast")
                	limit = 100;
		else 
			limit = 50;
		let end = parseInt(shownHits) + limit;
                end = end < numHits ? end : numHits;
                if (shownHits != end) {
                ResultViewHelper.showHits(shownHits, end, wrapped, false, numHits, jobID);
                }
                shownHits = end;
            }
        }
        // Highlights the position in the control bar on scroll
        $('#scrollLinks a').each(function () {
            let currLink = $(this);
            let  refElement = $(currLink.attr("name"));
            if(typeof refElement.position() != "undefined") {
                if (refElement.position().top <= top && refElement.position().top + refElement.height() > top) {
                    $('#scrollLinks a').removeClass("colorToggleBar");
                    currLink.addClass("colorToggleBar");
                }
                else {
                    currLink.removeClass("colorToggleBar");
                }
            }
        });


    });
    } catch(e) { console.warn(e); }
};

function download(filename, text) {
    const blob = new Blob([text], {type: "octet/stream"});
    if (window.navigator.msSaveOrOpenBlob) {
        window.navigator.msSaveBlob(blob, filename);
    } else {
        const a = document.createElement("a");
        a.href = window.URL.createObjectURL(blob);
        a.download = filename;
        // Append anchor to body.
        document.body.appendChild(a);
        a.click();
        window.URL.revokeObjectURL(a.href);
        // Remove anchor from body
        a.remove();
    }
    $.LoadingOverlay("hide");
}

// load forwarded data into alignment field
$(document).ready(function () {
    const resultcookie = localStorage.getItem("resultcookie");
    $('#alignment').val(resultcookie);
    localStorage.removeItem("resultcookie");
    $.LoadingOverlay("hide");
});

function hitlistBaseFunctions(){
    $(document).ready(function() {
        // add tooltipster to visualization
        $('#blastviz').find('area').tooltipster({
            theme: ['tooltipster-borderless', 'tooltipster-borderless-customized'],
            position: 'bottom',
            animation: 'fade',
            contentAsHTML: true,
            debug: false,
            maxWidth: $(this).innerWidth() * 0.6
        });
        $.LoadingOverlay("hide");
        followScroll(document);

        // add slider val
        $('.slider').on('moved.zf.slider', function () {
            $('#lefthandle').html($('#hidden1').val()).css({
                'color': 'white',
                'font-weight': 'bold',
                'padding-left': '2px'
            });
            $('#righthandle').html($('#hidden2').val()).css({
                'color': 'white',
                'font-weight': 'bold',
                'padding-left': '2px'
            });
        });
    });
}


