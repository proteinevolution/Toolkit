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
        ].indexOf(controller['static']) >= 0
          $('#content').empty().prepend data
        else
          $('body').empty().prepend data
        $(document).foundation()
        $("html, body").animate({ scrollTop: 0 }, "fast")


#setup routes to start w/ the `#` symbol
m.route.mode = 'hash'

m.route document.getElementById('main-content'), '/',
  '/' : Index
  '/tools/:toolname': m Toolkit, {isJob: false}
  '/jobs/:jobID': m Toolkit, {isJob: true}
  #'/profile/:username': m UserDashboard




# Miscellaneous code that is present across the whole web application
window.onfocus = ->
  titlenotifier.reset();
window.onclick = ->
  titlenotifier.reset();
