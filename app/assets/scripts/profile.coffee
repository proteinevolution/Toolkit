$ ->
  # AJAX submission of the Profile form
  $("#profile-edit-form").on 'submit', (event) ->
    event.preventDefault()
    form_data = $("#profile-edit-form").serialize()
    $.ajax(
      data: form_data
      url: "profile/submit/userData/"
      method: 'POST').done (json) ->
        alert json.message

  # AJAX submission of the Profile form
  $("#profile-passwort-edit-form").on 'submit', (event) ->
    event.preventDefault()
    form_data = $("#profile-edit-form").serialize()
    $.ajax(
      data: form_data
      url: "profile/submit/password/"
      method: 'POST').done (json) ->
        alert json.message