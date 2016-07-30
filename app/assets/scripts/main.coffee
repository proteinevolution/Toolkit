###*
# Major view initializations which are present on all views elements of the toolkit.
###
# Enables the foundation framework for the Toolkit
Tools =
  controller: ->
    { toolname: m.route.param('toolname') }
  view: (controller) ->


    $.ajax(
      type: "POST"
      url: "/tools/form/" + controller.toolname).done (data) ->
        $('#content').empty().append data
        $("html, body").animate({ scrollTop: 0 }, "fast")
        window.removeEventListener 'resize', listener, false


Jobs =
  controller: ->
    { job_id: m.route.param('jobid') }
  view: (controller) ->
    $.ajax(
      type: "GET"
      url: "/jobs/get/" + controller.job_id).done (data) ->
        $('#content').empty().prepend data
        $("html, body").animate({ scrollTop: 0 }, "fast")



Static =
  controller: ->
    { static: m.route.param('static') }
  view: (controller) ->
    $.ajax(
      type: "GET"
      url: "/static/get/" + controller.static ).done (data) ->
        $('#content').empty().prepend data
        $("html, body").animate({ scrollTop: 0 }, "fast")



#setup routes to start w/ the `#` symbol
m.route.mode = 'hash'

#define a route
m.route document.getElementById('content'), '/', { '/tools/:toolname': Tools,'/jobs/:jobid' : Jobs, '/:static' : Static }
