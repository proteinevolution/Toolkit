$ ->
  $(".jobsearchfield .jobsearchentry").click ->
    route = "jobs/add/" + this.id
    $.ajax(
      url: route
      type: 'POST')