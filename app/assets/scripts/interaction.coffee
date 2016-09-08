$ ->
  # Side bar interaction
  $(".jobsearchform").submit (event) ->
    event.preventDefault()
    sendMessage("type":"Search", "queryString":$(".jobsearch").val())
