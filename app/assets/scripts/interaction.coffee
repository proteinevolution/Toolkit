$(".jobsearchform").submit (event) ->
  event.preventDefault()
  job_id = $(".jobsearch").val()
  route = jsRoutes.controllers.Tool.result(job_id)
  $.ajax(
    url: route.url
    type: 'POST').done (data) ->
      $('#content').empty().append data
