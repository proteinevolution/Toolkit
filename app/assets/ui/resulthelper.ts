// load forwarded data into alignment field
$(function() {
    const resultcookie = localStorage.getItem("resultcookie");
    $("#alignment").val(resultcookie);
    localStorage.removeItem("resultcookie");
    $.LoadingOverlay("hide");
});
