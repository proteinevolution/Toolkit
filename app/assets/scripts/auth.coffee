$ ->
  # Top bar interaction
  # Tabs for the sign in / up /reset password interaction
  $( "#auth-tabs" ).tabs()
  # toggle the auth dropdown for the link
  #$("#auth-link").click ->
   # $("#auth-dropdown").foundation('toggle')

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
  $("#acceptToS").change (event) ->
    if $("#acceptToS").val() == "true"
      $("#acceptToS").val("false")
    else
      $("#acceptToS").val("true")

  # Sign Up form dynamic evaluation
  $("#signup-form").change (event) ->
    # tos button checked?
    buttonDisabled = $("#acceptToS").val() == "false"
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
    if (json.successful)
      #change the "sign in" to the user login name
      $("#auth-link-text").html(json.user.nameLogin)
      $("#overlay-content").html(json.message)

      m.startComputation()
      Job.reloadList()
      m.endComputation()

      setTimeout(loadMiniProfile,1000)
      location.reload()
    else
      # add the error message to the view
      $("#auth-alert").html(json.message)
      $("#auth-alert").fadeIn()

  loadMiniProfile = () ->
    closeNav()
    $.ajax(
      url: "/miniprofile"
      method: 'GET').done (data) ->
        $("#overlay-content").html(data)

  # Remove message by clicking on it
  $("#auth-alert").on 'click', (event) ->
    $("#auth-alert").fadeOut()

  # Open the modal with the profile when the profile link is clicked
  $("#profile-open").on 'click', (event) ->
    $.ajax(
      url: "/profile"
      method: 'GET').done (data) ->
        $("#modal").html(data).foundation('open')
