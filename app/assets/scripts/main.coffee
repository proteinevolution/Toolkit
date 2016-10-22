#######################################################################################################################
#######################################################################################################################
# Model for the Job currently loaded
# TODO Refactor this model
window.JobModel =
  isJob: m.prop false
  jobid: m.prop null
  jobstate: m.prop null
  createdOn: m.prop null
  tool: m.prop null
  alignmentPresent: false
  views:  m.prop false
  paramValues : {}
  defaultValues:
    "num_iter": 1
    "evalue": 10
    "inclusion_ethresh": 0.001
    "gap_open": 11
    "gap_ext": 1
    "desc": 500
    "matrix": "BLOSUM62"

  getTool: (toolname) ->
    m.request {method: 'GET', url: "/api/tools/#{toolname}"}

  getJob: (mainID) ->
    m.request {method: 'GET', url: "/api/jobs/#{mainID}"}

  getParamValue: (param) ->

    # Prefer the alignment from the local storage, if found
    resultcookie = localStorage.getItem("resultcookie")
    if resultcookie
      JobModel.paramValues["alignment"] = resultcookie
      localStorage.removeItem("resultcookie")
    val = JobModel.paramValues[param]
    defVal = JobModel.defaultValues[param]
    if val
      val
    else if defVal
      defVal
    else
      ""



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
  '/:section/:argument': Toolkit



# Mount the JobViewComponent into the Client-side application via associated routed
#m.route document.getElementById('content'), '/',
#  '/:static' : StaticRoute,
#  '/tools/:toolname': m.component JobViewComponent, {isJob: false}
#  '/jobs/:mainid': m.component JobViewComponent, {isJob : true}





# Miscellaneous code that is present across the whole web application
window.onfocus = ->
  titlenotifier.reset();
window.onclick = ->
  titlenotifier.reset();
