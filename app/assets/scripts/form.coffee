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
  $('#jobID').prop('readonly', true).hide()
  $('#provideJobID').change ->
    if $(this).is(':checked')
      $('#jobid').prop('type', 'text').prop('readonly', false).show()
    else
      $('#jobid').prop('type', 'hidden').prop('readonly', true).hide()
      $('#jobid').val("")

  $('#jobID').bind 'input propertychange', ->
    value = $(this).val()
    if value.match jobidPattern
      $(this).css('background-color', 'white')
      submissionAllowed = true
    else
      $(this).css('background-color', 'rgba(255, 0, 0, 0.3)').
      submissionAllowed = false

  # If the user hits the ID button
  $("#provideJobID").click (event) ->
    if $('#jobID').is(':visible')
      $('#jobID').prop('type', 'text').prop('readonly', false).hide()
      $('#jobID').val("")
    else
      $('#jobID').prop('type', 'text').prop('readonly', false).show()

  #handles all types of submission
  submitJob = (start) ->
    submitRoute = jsRoutes.controllers.Tool.submit(toolname, start, if job != null then job.mainID else null)

    $.ajax(
      url: submitRoute.url
      type: "POST"
      data: $(".jobForm").serialize()
      error: (jqXHR, textStatus, errorThrown) -> alert errorThrown
    ).done (json) ->
      if(json.jobSubmitted)
        if (json.identicalJobs)
          #TODO refer to the alert div box here
          alert "job submitted but there was an identical job"
        #TODO maybe link to the job page here
      else
        alert "job NOT submitted"

  #event binding
  #handles starting of an already prepared job
  $("#startJob").bind 'click', (event) ->
    if(job != null)
      alert "works" #todo it does not ... at the moment... ;)
      sendMessage("type":"StartJob", "mainID":job.mainID)
    else
      alert "there is no Main ID for this job. Are you sure this job is submitted?"

  # Handles the behavior when the submit or prepare button is pressed in a job form
  $("#submitJob").bind 'click', (event) ->
    submitJob(true)
  $("#prepareJob").bind 'click', (event) ->
    submitJob(false)

  # Bind the validation to the form
  $("#alignment").bind 'input propertychange', (event) ->
    revalidate
  revalidate
  #TODO validation does nothing at the moment. had to take out the disable on the buttons.

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