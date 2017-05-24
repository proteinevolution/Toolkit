
declare var loading : boolean;
declare var shownHits : any;
declare var showMore : any;
declare var numHits : any;
declare var getHits: any;
declare var colorAAs: boolean;

let count = 0;

// add scrollcontainer highlighting
let followScroll = function(element : any) {
    
    $(element).on('scroll', function () {
    let detached;

        try {

            let top = $(element).scrollTop();
            if(typeof top !== 'undefined'){
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

            if ($('#flat-slider').visible()) {
                $("#visualizationScroll").addClass("colorToggleBar");
                $("#hitlistScroll").removeClass("colorToggleBar");
                $("#alignmentsScroll").removeClass("colorToggleBar");
            } else if ($('#alignments').visible(true)) {
                $("#alignmentsScroll").addClass("colorToggleBar");
                $("#hitlistScroll").removeClass("colorToggleBar");
                $("#visualizationScroll").removeClass("colorToggleBar");
            } else if ($('#htb').visible(true)) {
                $("#hitlistScroll").addClass("colorToggleBar");
                $("#alignmentsScroll").removeClass("colorToggleBar");
                $("#visualizationScroll").removeClass("colorToggleBar");
            }
            // trigger lazyload for loading alignment
            if ($(this).scrollTop() == $(this).height() - $(window).height()) {
                if (!loading) {
                    let end = parseInt(shownHits) + parseInt(showMore);
                    end = end < numHits ? end : numHits;
                    if (shownHits != end) {
                            getHits(shownHits, end, colorAAs).then(function () {
                                // hide loadHits
                                if (shownHits == numHits) {
                                    $('#loadHits').hide();
                                }
                            });
                    }else{
                        $('#loadHits').hide();
                    }
                    shownHits = end;
                }
            }
            $("#alignments").floatingScroll('init');

        } catch(e) { console.warn(e); }
    });

};