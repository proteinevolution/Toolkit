$ ->
  # TODO Add a confirm signal after clicking of some sort
  $(".jobsearchfield .jobsearchentry").click ->
    divobject = this
    route = "jobs/add/" + divobject.id
    $.ajax(
      url: route
      type: 'POST').done (data) ->
        divobject.style.backgroundColor = "#22FF22"