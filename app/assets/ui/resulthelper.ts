// load forwarded data into alignment field
$(function() {
    const resultcookie = localStorage.getItem("resultcookie");
    if (resultcookie.length < 1) console.log("WARNING: localStorage is empty.");
    $("#alignment").val(resultcookie);
    localStorage.removeItem("resultcookie");
    $.LoadingOverlay("hide");
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
