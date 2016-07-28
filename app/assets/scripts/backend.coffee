###*
# Major view initializations which are present on all views elements of the toolkit.
###
# Enables the foundation framework for the Toolkit

Static =
  controller: ->
    { static: m.route.param('static') }
  view: (controller) ->
    $.ajax(
      type: "GET"
      url: "/backend/get/" + controller.static ).done (data) ->
        $('#backend-content').empty().prepend data


#setup routes to start w/ the `#` symbol
m.route.mode = 'search'

#define a route
m.route document.getElementById('backend-content'), '/backend', { '/:static' : Static }
