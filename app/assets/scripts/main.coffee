###*
# Major view initializations which are present on all views elements of the toolkit.
###
# Enables the foundation framework for the Toolkit
Tools =
  controller: ->
    { toolName: m.route.param('toolName') }
  view: (controller) ->
    $.ajax(
      type: "POST"
      url: "/tools/form/" + controller.toolName).done (data) ->
        $('#content').empty().append data
        $(document).foundation()
        $("html, body").animate({ scrollTop: 0 }, "fast")
        window.removeEventListener 'resize', listener, false


Jobs =
  controller: ->
    { mainID: m.route.param('mainID') }
  view: (controller) ->
    $.ajax(
      type: "GET"
      url: "/jobs/get/" + controller.mainID).done (data) ->
        $('#content').empty().prepend data
        $(document).foundation()
        $("html, body").animate({ scrollTop: 0 }, "fast")


StaticRoute =
  controller: ->
    { static: m.route.param('static') }
  view: (controller) ->
    $.ajax(
      type: "GET"
      url: "/static/get/" + controller.static ).done (data) ->
        if [
          'sitemap'
          'reformat'
          'alnvizfrontend'
          'patSearch'
          'extractIDs'
        ].indexOf(controller['static']) >= 0
          $('#content').empty().prepend data
        else
          $('body').empty().prepend data
        $(document).foundation()
        $("html, body").animate({ scrollTop: 0 }, "fast")


#setup routes to start w/ the `#` symbol
m.route.mode = 'hash'

#define a route
m.route document.getElementById('content'), '/', { '/tools/:toolName': Tools,'/jobs/:mainID' : Jobs, '/:static' : StaticRoute }


window.onfocus = ->
    titlenotifier.reset();
window.onclick = ->
    titlenotifier.reset();


