$ ->
  $(".jobsearchform").submit (event) ->
    event.preventDefault()
    job_id = $(".jobsearch").val()
    route = "jobs/search/" + job_id
    $.ajax(
      url: route
      type: 'POST').done (data) ->
        $("#searchmodal").html(data).foundation('open')

  $("#showjobs").click ->
    route = "jobs/show/12345"
    $.ajax(
      url: route
      type: 'GET').done (data) ->
        $("#searchmodal").html(data).foundation('open')