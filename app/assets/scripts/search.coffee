$ ->
  # TODO Add a confirm signal after clicking of some sort
  $(".jobsearchfield .jobsearchentry").click ->
    route = "jobs/add/" + this.id
    $.ajax(
      url: route
      type: 'POST')