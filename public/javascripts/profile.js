$(function() {
    $("#profile-edit-form").on('submit', function(event) {
        var form_data;
        event.preventDefault();
        form_data = $("#profile-edit-form").serialize();
        return $.ajax({
            data: form_data,
            url: "profile/submit/userData/",
            method: 'POST'
        }).done(function(json) {
            $("#auth-alert").html(json.message);
            return $("#auth-alert").fadeIn();
        });
    });
    return $("#profile-password-edit-form").on('submit', function(event) {
        var form_data;
        event.preventDefault();
        form_data = $("#profile-password-edit-form").serialize();
        return $.ajax({
            data: form_data,
            url: "profile/submit/password/",
            method: 'POST'
        }).done(function(json) {
            $("#auth-alert").html(json.message);
            return $("#auth-alert").fadeIn();
        });
    });
});