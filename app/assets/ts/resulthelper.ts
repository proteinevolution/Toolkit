
declare var loading : boolean;
declare var shownHits : any;
declare var showMore : any;
declare var numHits : any;
declare var getHits: any;
declare var colorAAs: boolean;
declare var wrapped: boolean;
let count = 0;

// add scrollcontainer highlighting
let followScroll = function(element : any) {
    try {
    $(element).ready(function () {
        $("#alignments").floatingScroll('init');
        //smoothscroll
        $('#scrollLinks a').on('click', function (e) {
            $('a').each(function () {
                $(this).removeClass('colorToggleBar');
            });
            $(this).addClass('colorToggleBar');
        });
    });

    $(element).on("scroll", function(){
        let top = Number($(document).scrollTop());
        if($('#visualization').position() != undefined) {
            if (top >= $('#visualization').position().top + 75) {
                $('.scrollContainer').addClass('fixed');
                $('.scrollContainer').removeClass('scrollContainerWhite');
                $('.scrollContainerDiv').removeClass('scrollContainerDivWhite');
                $('#wrap').show();
                $(".colorAA").show();
            } else {
                $('.scrollContainer').removeClass('fixed');
                $('.scrollContainer').addClass('scrollContainerWhite');
                $('.scrollContainerDiv').addClass('scrollContainerDivWhite');
                $('#wrap').hide();
                $(".colorAA").hide();

            }
        }
        // trigger lazyload for loading alignment
        if (top == $(this).height() - $(window).height()) {
            if (!loading) {
                let end = parseInt(shownHits) + parseInt(showMore);
                end = end < numHits ? end : numHits;
                if (shownHits != end) {
                    getHits(shownHits, end,wrapped,colorAAs).then(function () {
                    });
                }
                shownHits = end;
            }
        }
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

