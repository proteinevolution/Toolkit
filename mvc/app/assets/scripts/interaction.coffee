$ ->
  # Side bar interaction
  $(".jobsearchform").submit (event) ->
    event.preventDefault()
    job_id = $(".jobsearch").val()
    route = "jobs/search/" + job_id
    $.ajax(
      url: route
      type: 'POST').done (data) ->
        $("#searchmodal").html(data).foundation('open')

  $("#showjobs").click ->
    route = "jobs/show/12345"
    $.ajax(
      url: route
      type: 'GET').done (data) ->
        $("#searchmodal").html(data).foundation('open')

  # Top bar interaction

  $("#auth-link").click ->
    $("#auth-dropdown").foundation('toggle')

  $("#signup-form").on 'submit', (event) ->
    event.preventDefault()
    form_data = $("#signup-form").serialize()
    route = "signup/submit"
    $.ajax(
      data: form_data
      url: route
      method: 'POST').done (data) ->
        $("#auth-dropdown").html(data)
        $("#auth-link").html("Account")

  $("#signup-form").change (event) ->
    buttonDisabled = false
    $("#signup-form").find(':input').each ->
      if (!this.value && this.type != "submit") then buttonDisabled = true

    $(".is-invalid-input").each ->
      buttonDisabled = true

    if buttonDisabled
      $("#signup-submit").addClass('disabled')
    else
      $("#signup-submit").removeClass('disabled')