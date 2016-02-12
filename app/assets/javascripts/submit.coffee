$ ->
  # Variables in Scope of the Input form

  # JobIDs have to obey this regular expression
  jobidPattern = /// ^
   ([\w-]+)
   $ ///i

  # Whether Job submission is currently possible
  submissionAllowed = true

  # List all the reasons why job Submission is currently not allowed



  $('#jobid').prop('readonly', true).hide()
  $('#customjobidwanted').change ->

    if $(this).is(':checked')
      $('#jobid').prop('readonly', false).show()
      $('#jobidnotif').show()
    else
      $('#jobid').prop('readonly', true).hide()
      $('#jobid').val("")
      $('#jobidnotif').hide()


  $('#jobid').bind 'input propertychange', ->

    value = $(this).val()
    if value.match jobidPattern
      $('#jobidnotif').text ""
      submissionAllowed = true
    else
      $('#jobidnotif').text "This Job ID is invalid."
      submissionAllowed = false


  # Handles the behavior when the submit button is pressed in a job form
  $(".jobform").submit (event) ->
    event.preventDefault()
    submitRoute = jsRoutes.controllers.Tool.submit(toolname)

    $.ajax
      url: submitRoute.url
      type: submitRoute.type
      data: $(".jobform").serialize()
      #dataType: "json"
      error: (jqXHR, textStatus, errorThrown) ->

        alert errorThrown
      success: (data, textStatus, jqXHR) ->
      #$('body').append "Successful AJAX call: #{data}"


  $(".jobformclear").click (event) ->
    $('.jobform').trigger("reset")
    $('#jobid').prop('readonly', true).hide().val("")








###

  $('input').bind('input propertychange', function() {
    $('#output').html($(this).val());
});
  helpers =

    check_empty: (bag) ->

      _num_errors = 0
      _errors 	= []

      for item in bag

        id			= item.prop('id') || 0
        name		= item.prop('name') || 0
        value 		= item.val() || 0
        error_msg 	= item.error_msg || 0

        if !value or value is ""

          _num_errors++

          if (name or id)
            _error = {}
            _error.name = name
            _error.id = id
            _error.msg = if error_msg then error_msg else "#{name} cannot be left blank."

            _errors.push _error

      if _num_errors > 0
        _errors
      else
        true

$('form').submit () ->

  check_blank = [
    $('[name="firstname"]')
    $('[name="lastname"]')
    $('[name="email"]')
  ]

  errors = helpers.check_empty(check_blank)

  console.dir errors

  return false
###