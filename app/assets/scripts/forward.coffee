$("#prepare_child").click () ->

  toolname = $("#childjobselect").val()

  tuples = target_tools[toolname]

  if tuples.length != 1
    alert "Ambiguities are not yet implemented"
    return
  # TODO Implement me
