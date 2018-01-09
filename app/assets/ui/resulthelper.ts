
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
