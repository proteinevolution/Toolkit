
declare var loading : boolean;
declare var shownHits : number;
declare var showMore : number;
declare var numHits : number;
declare var getHits: any;


// add scrollcontainer highlighting
let followScroll = function(element : any) {

    let expandButton = $('#collapseMe').clone(true);
    expandButton.css('display', 'none');
    $('.scrollContainer').append(expandButton);

    expandButton.on('click',function () {


    });



    $(element).on('scroll', function () {

        let top = $(this).scrollTop();
        if (typeof top !== 'undefined' && top >= $('#visualization').position().top + 75) {
            $('.scrollContainer').addClass('fixed');
            expandButton.addClass('expandButtonOnScroll');
        }
        else {
            $('.scrollContainer').removeClass('fixed');
            expandButton.removeClass('expandButtonOnScroll');

        }

        if ($('#flat-slider').visible()) {
            $("#visualizationScroll").addClass("colorToggle");
            $("#hitlistScroll").removeClass("colorToggle");
            $("#alignmentsScroll").removeClass("colorToggle");
        } else if ($('#alignments').visible(true)) {
            $("#alignmentsScroll").addClass("colorToggle");
            $("#hitlistScroll").removeClass("colorToggle");
            $("#visualizationScroll").removeClass("colorToggle");
        } else if ($('#hitlist').visible(true)) {
            $("#hitlistScroll").addClass("colorToggle");
            $("#alignmentsScroll").removeClass("colorToggle");
            $("#visualizationScroll").removeClass("colorToggle");
        }
        // trigger lazyload for loading alignment
        if ($(this).scrollTop() == $(this).height() - $(window).height()) {
            if (!loading) {
                let end = shownHits + showMore;
                end = end < numHits ? end : numHits;
                if (shownHits != end) {
                    getHits(shownHits, end);
                }
                shownHits = end;
            }
        }
        $("#alignments").floatingScroll('init');
    });

};