// load forwarded data into alignment field
$(function() {
    setTimeout(function() {
        const resultcookie = localStorage.getItem("resultcookie");
        if (resultcookie !== null && resultcookie.length < 1) {
            console.warn("WARNING: no forwarding data in storage.");
        }
        $("#alignment").val(resultcookie);
        localStorage.removeItem("resultcookie");
        $.LoadingOverlay("hide");
    }, 200); // todo clean this up and make it more robust
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

