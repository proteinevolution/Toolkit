$(function() {
    return $(".jobsearchform").submit(function(event) {
        event.preventDefault();
        return sendMessage({
            "type": "Search",
            "queryString": $(".jobsearch").val()
        });
    });
});