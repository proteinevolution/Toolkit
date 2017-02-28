$ ->
  # AJAX submission of the Profile form
  $("#profile-edit-form").on 'submit', (event) ->
    event.preventDefault()
    form_data = $("#profile-edit-form").serialize()
    $.ajax(
      data: form_data
      url: "profile/submit/userData/"
      method: 'POST').done (json) ->
        $("#auth-alert").html(json.message)
        $("#auth-alert").fadeIn()

  # AJAX submission of the Profile form
  $("#profile-password-edit-form").on 'submit', (event) ->
    event.preventDefault()
    form_data = $("#profile-password-edit-form").serialize()
    $.ajax(
      data: form_data
      url: "profile/submit/password/"
      method: 'POST').done (json) ->
        $("#auth-alert").html(json.message)
        $("#auth-alert").fadeIn()