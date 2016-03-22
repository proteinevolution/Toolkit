###*
# Provides the necessary scripts for handling form elements of the view, like they are used to submit a
# job from the UI.
###
$ ->
  # Variables in Scope of the Input form
  # JobIDs have to obey this regular expression
  jobidPattern = /// ^
   ([\w-]+)
   $ ///i

  # Whether Job submission is currently possible
  submissionAllowed = true
  # Elements taking care of the input of a custom Job ID
  $('#jobid').prop('readonly', true).hide()
  $('#customjobidwanted').change ->

    if $(this).is(':checked')
      $('#jobid').prop('type', 'text').prop('readonly', false).show()
    else
      $('#jobid').prop('type', 'hidden').prop('readonly', true).hide()
      $('#jobid').val("")

  $('#jobid').bind 'input propertychange', ->

    value = $(this).val()
    if value.match jobidPattern
      $(this).css('background-color', 'white')
      submissionAllowed = true
    else
      $(this).css('background-color', 'rgba(255, 0, 0, 0.3)').
      submissionAllowed = false

  # Handles the behavior when the submit button is pressed in a job form
  $(".jobform").unbind('submit').submit (event) ->
    event.preventDefault()
    submitRoute = jsRoutes.controllers.Tool.submit(toolname, true, newSubmission)

    $.ajax
      url: submitRoute.url
      type: "POST"
      data: $(".jobform").serialize()
      error: (jqXHR, textStatus, errorThrown) -> alert errorThrown


  # Handles the behavior when the submit button is pressed in a job form
  $(".jobprepare").click  ->

    submitRoute = jsRoutes.controllers.Tool.submit(toolname, false, newSubmission)

    $.ajax
      url: submitRoute.url
      type: "POST"
      data: $(".jobform").serialize()
      error: (jqXHR, textStatus, errorThrown) -> alert errorThrown

    m.redraw.strategy("all")

# If one hits the Reset button of the form
  $(".jobformclear").click (event) ->
    $('.jobform').trigger("reset")
    $('#jobid').prop('readonly', true).hide().val("")
    $('#foo').prop('disabled', true);


# If one hits the ID button
  $(".provideid").click (event) ->
    if $('#jobid').is(':visible')
      $('#jobid').prop('type', 'text').prop('readonly', false).hide()
      $('#jobid').val("")
    else
      $('#jobid').prop('type', 'text').prop('readonly', false).show()





###






  // Our mithril model
var Todo = function (data) {
  this.name = m.prop(data.name || '')
  this.done = m.prop(data.done)
}

// Initialize a new validator
var validator = new m.validator({

  // Check model name property
  name: function (name) {
    if (!name) {
      return "Name is required."
    }
  }

})

// Results in "Name is required."
validator.validate(new Todo()).hasError('name')
###



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