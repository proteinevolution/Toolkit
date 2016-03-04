#$("#prepare_child").click () ->



$("#childjobselect").change (event) ->
  event.preventDefault()
  $("#wiring").empty()

  # compute tuple
  toolname = $("#childjobselect").val()
  tuples = target_tools[toolname]



  ###
  $("#wiring").append('<select id="outport_' + outport + '"' +  ' name="' + outport  + '" >')

  for inport in outport
    $('#outport_' + i).append('<option value="foo">foo</option>')


  $("#wiring").append('</select>')
  ###





