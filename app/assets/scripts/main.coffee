###*
# Major view initializations which are present on all views elements of the toolkit.
###
# Enables the foundation framework for the Toolkit
$(document).foundation()

Tools =
  controller: ->
    { toolname: m.route.param('toolname') }
  view: (controller) ->
    $.ajax(
      type: "POST"
      url: "/jobs/new/" + controller.toolname).done (data) ->
        $('#content').empty().append data


Jobs =
  controller: ->
    { job_id: m.route.param('jobid') }
  view: (controller) ->
    $.ajax(
      type: "POST"
      url: "/jobs/get/" + controller.job_id).done (data) ->
        $('#content').empty().append data

#setup routes to start w/ the `#` symbol
m.route.mode = 'hash'


#define a route
m.route document.getElementById('content'), '/', { '/tools/:toolname': Tools, '/jobs/:jobid' : Jobs }
