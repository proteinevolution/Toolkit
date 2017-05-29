
declare var loading : boolean;
declare var shownHits : any;
declare var showMore : any;
declare var numHits : any;
declare var getHits: any;
declare var colorAAs: boolean;

let count = 0;

// add scrollcontainer highlighting
let followScroll = function(element : any) {



    $(document).ready(function () {
        $("#alignments").floatingScroll('init');
        //smoothscroll
        $('#scrollLinks a').on('click', function (e) {
            e.preventDefault();
            $(element).off("scroll");

            $('a').each(function () {
                $(this).removeClass('colorToggleBar');
            });
            $(this).addClass('colorToggleBar');
        });
    });

    $(element).on("scroll", function(){
    try {
        let top = $(element).scrollTop();
        if (typeof top !== 'undefined') {
            if (top >= $('#visualization').position().top + 75) {
                // detached = $('#collapseMe').detach();
                // $('.scrollContainer').append(detached);
                $('.scrollContainer').addClass('fixed');
                $('.scrollContainer').removeClass('scrollContainerWhite');
                $('.scrollContainerDiv').removeClass('scrollContainerDivWhite');
            } else {
                // detached = $('#collapseMe').detach();
                // $('.ui-widget-header').append(detached);
                $('.scrollContainer').removeClass('fixed');
                $('.scrollContainer').addClass('scrollContainerWhite');
                $('.scrollContainerDiv').addClass('scrollContainerDivWhite');
            }
        }
        // trigger lazyload for loading alignment
        if (top == $(this).height() - $(window).height()) {
            if (!loading) {
                let end = parseInt(shownHits) + parseInt(showMore);
                end = end < numHits ? end : numHits;
                if (shownHits != end) {
                    getHits(shownHits, end, colorAAs).then(function () {
                    });
                }
                shownHits = end;
            }
        }

        $('#scrollLinks a').each(function () {
            let currLink = $(this);
            let  refElement = $(currLink.attr("name"));
            if (refElement.position().top <= top && refElement.position().top + refElement.height() > top) {
                $('#scrollLinks a').removeClass("colorToggleBar");
                currLink.addClass("colorToggleBar");
            }
            else {
                currLink.removeClass("colorToggleBar");
            }
        });

    } catch (e) {
        console.warn(e);
    }

    });

};

