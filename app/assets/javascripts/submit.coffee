$ ->

  $('#jobid').prop('readonly', true).hide()
  $('#customjobidwanted').change ->

    if $(this).is(':checked')
      $('#jobid').prop('readonly', false).show()
    else
      $('#jobid').prop('readonly', true).hide()
      $('#jobid').val("")
