$ ->
  # TODO Add a confirm signal after clicking of some sort (instead of just turning green maybe)
  #$(".jobsearchfield .jobsearchentry").click ->
  #  divobject = this
  #  route = "jobs/add/" + divobject.id
  #  $.ajax(
  #    url: route
  #    type: 'POST').done (data) ->
  #      divobject.style.backgroundColor = "#22FF22"

  # Side bar interaction
  $(".jobsearchform").submit (event) ->
    event.preventDefault()
    sendMessage("type":"Search", "queryString":$(".jobsearch").val())