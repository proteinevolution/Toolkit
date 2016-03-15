$ ->
  $(".jobsearchform").submit (event) ->
    event.preventDefault()
    job_id = $(".jobsearch").val()
    route = "jobs/search/" + job_id
    $.ajax(
      url: route
      type: 'POST').done (data) ->
        $("#searchmodal").html(data).foundation('open')