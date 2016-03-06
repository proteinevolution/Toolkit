$("#forward").submit (event) ->
  event.preventDefault()

  data = $("#forward").serializeArray()
  data.map (link) ->
    link["out"] = parseInt(link["name"])
    link["in"] = parseInt(link["value"])
    delete link["name"]
    delete link["value"]
    return link

  obj = { 'parent_job_id' : parent_job_id, 'toolname' : $("#childjobselect").val() , 'links' : data }

  $.ajax
    url: "/jobs/addChild"
    type: "POST"
    data: JSON.stringify obj
    contentType: 'application/json; charset=utf-8',
    dataType: "json"


$("#childjobselect").change (event) ->
  event.preventDefault()
  $("#forward-fieldset").empty()

  # compute tuple
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



      $("#forward-fieldset").append('<label for="outport_' + i +  '">Forward ' + portname + ' to</label>')
      $("#forward-fieldset").append('<select id="outport_' + i + '"' +  ' name="' + i + '" >')

      for inport in ports[i]

        port = inport[0]

        $('#outport_' + i).append('<option value="' +  port + '">'  + port +  '</option>')

      $("#forward-fieldset").append('</select>')

    i++
