$ ->
  # Top bar interaction
  # Tabs for the sign in / up /reset password interaction
  $( "#tabs" ).tabs()
  # toggle the auth dropdown for the link
  $("#auth-link").click ->
    $("#auth-dropdown").foundation('toggle')

  # AJAX submission of the Login form
  $("#signin-form").on 'submit', (event) ->
    event.preventDefault()
    form_data = $("#signin-form").serialize()
    $.ajax(
      data: form_data
      url: "signin/submit/"
      method: 'POST').done (json) ->
        checkAuthResponse(json)

  # AJAX submission of the Registry form
  $("#signup-form").on 'submit', (event) ->
    event.preventDefault()
    #don't send if the button is disabled!
    if (!$("#signup-submit").hasClass("disabled"))
      form_data = $("#signup-form").serialize()
      $.ajax(
        data: form_data
        url: "signup/submit/"
        method: 'POST').done (json) ->
          checkAuthResponse(json)

  # swap values on the accept ToS checkbox on click
  $("#accepttos").change (event) ->
    if $("#accepttos").val() == "true"
      $("#accepttos").val("false")
    else
      $("#accepttos").val("true")

  # Sign Up form dynamic evaluation
  $("#signup-form").change (event) ->
    # tos button checked?
    buttonDisabled = $("#accepttos").val() == "false"
    # any other items unedited?
    $("#signup-form").find(':input').each ->
      if (!this.value && this.type != "submit") then buttonDisabled = true

    # any invalid items?
    $(".is-invalid-input").each ->
      buttonDisabled = true

    # disable the button
    if buttonDisabled
      $("#signup-submit").addClass('disabled')
    else
      $("#signup-submit").removeClass('disabled')

  # checks if the process failed and displays the error message above the form
  # or puts the miniprofile into view
  checkAuthResponse = (json) ->
    # TODO add a closeable notice for the message?
    if (json.successful)
      $("#auth-link").html(json.user.name_login)
      $("#auth-dropdown").html(json.message)
      $.ajax(
        url: "/miniprofile"
        method: 'GET').done (data) ->
          $("#auth-dropdown").html(data)
    else
      # TODO add the error message to the view

