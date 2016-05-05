$ ->
# Top bar interaction
# toggle the auth dropdown for the link
  $("#auth-link").click ->
    $("#auth-dropdown").foundation('toggle')

  # AJAX submission of the Login form
  $("#signin-form").on 'submit', (event) ->
    event.preventDefault()
    form_data = $("#signin-form").serialize()
    route = "signin/submit/"
    $.ajax(
      data: form_data
      url: route
      method: 'POST').done (data) ->
        $("#auth-dropdown").html(data)

  # AJAX submission of the Registry form
  $("#signup-form").on 'submit', (event) ->
    event.preventDefault()
    #don't send if the button is disabled!
    if (!$("#signup-submit").hasClass("disabled"))
      form_data = $("#signup-form").serialize()
      route = "signup/submit/"
      $.ajax(
        data: form_data
        url: route
        method: 'POST').done (data) ->
          $("#auth-dropdown").html(data)

  # swap values on the accept ToS checkbox
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

  closeAuthDropdown = ->
    $("#auth-dropdown").foundation('close')

  updateAccountMenu = ->
    $("#auth-link").html("Account")