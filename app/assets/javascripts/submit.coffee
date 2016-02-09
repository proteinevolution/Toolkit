$ ->
  # Variables in Scope of Input text
  jobidPattern = /// ^
   ([\w-]+)
   $ ///i




  $('#jobid').prop('readonly', true).hide()
  $('#customjobidwanted').change ->

    if $(this).is(':checked')
      $('#jobid').prop('readonly', false).show()
    else
      $('#jobid').prop('readonly', true).hide()
      $('#jobid').val("")


  $('#jobid').bind 'input propertychange', ->
    if $(this).val().match jobidPattern
      $('#jobidnotif').text "success"
    else
      $('#jobidnotif').text ""






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