#$("#prepare_child").click () ->



$("#childjobselect").change (event) ->
  event.preventDefault()
  $("#wiring").empty()

  # compute tuple
  toolname = $("#childjobselect").val()
  tuples = target_tools[toolname]

  ports = []
  for tuple in tuples

    if (typeof ports[tuple[0]]) == "undefined"
      ports[tuple[0]] = [[tuple[1], [tuple[2]]]]
    else
      ports[tuple[0]].push [[tuple[1], [tuple[2]]]]

  i = 0
  while i < ports.length

    if (typeof ports[i]) != "undefined"

      # Get name of this port as classname
      portname = ports[i][0][1]
      alert portname


      $("#wiring").append('<select id="outport_' + i + '"' +  ' name="outport_' + i + '" >')

      for inport in ports[i]
        $('#outport_' + i).append('<option value="foo">foo</option>')


      $("#wiring").append('</select>')

    i++



