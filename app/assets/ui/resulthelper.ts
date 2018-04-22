declare var ResultViewHelper: any;
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
let followScroll = function(element: any) {
    try {
        $(element).ready(function() {
            // Highlights the position in the control bar on click
            $("#alignments").floatingScroll("init");
            //smoothscroll
            $("#scrollLinks a").on("click", function(e) {
                $("#scrollLinks a").each(function() {
                    $(this).removeClass("colorToggleBar");
                });
                $(this).addClass("colorToggleBar");
            });
        });
        //  Fixes/Unfixes the control bar at the top
        $(element).on("scroll", function() {
            let top = Number($(document).scrollTop());
            if ($("#visualization").position() != undefined) {
                if (top >= $("#visualization").position().top + 75) {
                    $(".scrollContainer").addClass("fixed").removeClass("scrollContainerWhite");
                    $(".scrollContainerDiv").removeClass("scrollContainerDivWhite");
                    $("#wrap").show();
                    $(".colorAA").show();
                    $(".downloadHHR").hide();
                } else {
                    $(".scrollContainer").removeClass("fixed").addClass("scrollContainerWhite");
                    $(".scrollContainerDiv").addClass("scrollContainerDivWhite");
                    $("#wrap").hide();
                    $(".colorAA").hide();
                    $(".downloadHHR").show();

                }
            }
            // triggers getHits on scroll
            if (top == $(this).height() - $(window).height()) {
                if (!ResultViewHelper.loading) {
                    let limit: number = 0;
                    if ($("#toolnameAccess").val() === "psiblast") {
                        limit = 100;
                    } else {
                        limit = 50;
                    }
                    let end = parseInt(ResultViewHelper.shownHits) + limit;
                    end = end < ResultViewHelper.resultContext.numHits ? end : ResultViewHelper.resultContext.numHits;
                    if (ResultViewHelper.shownHits != end) {
                        ResultViewHelper.showHits(ResultViewHelper.shownHits, end);
                    }
                    ResultViewHelper.shownHits = end; // can be removed. should be handled in ajax
                }
            }
            // Highlights the position in the control bar on scroll
            $("#scrollLinks a").each(function() {
                let currLink = $(this);
                let refElement = $(currLink.attr("name"));
                if (typeof refElement.position() != "undefined") {
                    if (refElement.position().top <= top && refElement.position().top + refElement.height() > top) {
                        $("#scrollLinks a").removeClass("colorToggleBar");
                        currLink.addClass("colorToggleBar");
                    }
                    else {
                        currLink.removeClass("colorToggleBar");
                    }
                }
            });


        });
    } catch (e) {
        console.warn(e);
    }
};

function download(filename: string, text: string) {
    const blob = new Blob([text], {type: "application/octet-stream"});
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
$(document).ready(function() {
    const resultcookie = localStorage.getItem("resultcookie");
    $("#alignment").val(resultcookie);
    localStorage.removeItem("resultcookie");
    $.LoadingOverlay("hide");
});


