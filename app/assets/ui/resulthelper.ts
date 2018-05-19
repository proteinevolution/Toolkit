// load forwarded data into alignment field on browser reload
$(function() {
    const resultcookie = localStorage.getItem("resultcookie");
    if (!resultcookie || resultcookie.length < 1) {
        console.warn("WARNING: no forwarding data in storage.");
    } else {
        $("#alignment").val(resultcookie);
    }
    localStorage.removeItem("resultcookie");
    $.LoadingOverlay("hide");
});

