###*
# Major view initializations which are present on all views elements of the toolkit.
###
#@
#  Stores the association between a form element name and the type of the form to be rendered
#

###

        <span class="toolname">@toolModel.toolNameLong</span>
        @if(jobOption.isDefined) {
            <span class="jobinfo">
             <span class="jobid"> JobID: @jobOption.get.jobID </span>

                @defining(DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")) { dateFormatter =>
                    <span class="createdon">Created on: @dateFormatter.print(jobOption.get.dateCreated.get)</span>
                }

            </span>
        } else {
            <span class="jobinfo">
                Submit a new Job
            </span>
        }

###
###
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
###
# Helper functions
renderParameter = (content, moreClasses) ->
  m "div", {class: "parameter #{moreClasses}"}, content

# Configuration
tabulated = (element, isInit) ->
  if not isInit then $(element).tabs()

# On client-side, a tool consists of a bunch of different names and its category
Tool = (data) ->
  this.toolname = m.prop data.toolname
  this.toolnameLong = m.prop data.toolnameLong
  this.toolnameAbbrev = m.prop data.toolnameAbbrev
  this.category = m.prop data.category
  this.params = m.prop data.params   # Parameter except for the alignment, sequences

# Retrieve all data for a particular tool based on the toolname
Tool.get = (toolname) ->
  m.request {method: 'GET', url: "/api/tools/#{toolname}"}

#######################################################################################################################
#######################################################################################################################
# Model for the Job currently loaded
JobModel =
  isJob: false
  jobid: m.prop null
  createdOn: null
  tool: null
  alignmentPresent: false


# Component for the Jobline in the unified JobView
JobLineComponent =
  controller: ->
    toolnameLong: JobModel.tool().toolnameLong
  view: (ctrl) ->
    m "div", {class: "jobline"}, [
      m "span", {class: "toolname"}, ctrl.toolnameLong     # Long toolname in Job Descriptor Line
      m "span", {class: "jobinfo"}, "Submit a new Job"      # TODO Switch JobInfo dependent on new or already existing job
    ]



# Components for generating form input fields. Also allows to encapsulate value validation
ParameterAlignmentComponent =
  controller: ->
    name: "alignment"

  view: (ctrl) ->
    renderParameter [
      m "textarea", {name: ctrl.name, placeholder: "Enter multiple sequence alignment", rows: 10, cols: 70, id: "alignment"}
      m "input", {type: "button", class: "button small alignmentExample", value: "Paste Example"}
    ], "alignmentParameter"  # TODO Should be a controller argument


ParameterSelectComponent =
  controller: (args) ->
    options: args.options
    name: args.name
    id: args.id
    label: args.label

  view: (ctrl) ->
    renderParameter [
      m "label", {for: ctrl.id}, ctrl.label
      m "select", {name: ctrl.name, id: ctrl.id}, ctrl.options.map (entry) ->
        m "option", {value: entry[0]}, entry[1]
    ]

ParameterNumberComponent =
  controller: (args ) ->
    name: args.name
    id: args.id
    label: args.label

  view: (ctrl) ->
    renderParameter [
      m "label", {for: ctrl.id}, ctrl.label
      m "input", {type: "number", id: ctrl.id, name: ctrl.name}
    ]




# Associates each parameter name with the respective component
formComponents =
  "alignment" : (toolargs) -> [ParameterAlignmentComponent, {}]
  "alignment_format": (toolargs) -> [
                      ParameterSelectComponent
                    ,
                      options: toolargs
                      name: "alignment_format"
                      id: "alignment_format"
                      label: "Select Alignment format"
  ]
  "standarddb": (toolargs) -> [
                      ParameterSelectComponent
                    ,
                      options: toolargs
                      name: "standarddb"
                      id: "standarddb"
                      label: "Select Standard Database"
  ]

  "matrix": (toolargs) -> [
                      ParameterSelectComponent
                    ,
                      options: toolargs
                      name: "matrix"
                      id: "matrix"
                      label: "Scoring Matrix"
  ]
  "num_iter": (toolargs) -> [
                       ParameterNumberComponent
                    ,
                       name: "num_iter"
                       id: "num_iter"
                       label: "Number of Iterations"
  ]

  "evalue": (toolargs) -> [
                      ParameterNumberComponent
                    ,
                      name: "evalue"
                      id: "evalue"
                      label: "E-Value"
  ]
  "gap_open": (toolargs) -> [
                      ParameterNumberComponent
                    ,
                      name: "gap_open"
                      id: "gap_open"
                      label: "Gap Open penalty"
  ]
  "gap_ext":  (toolargs) -> [
                      ParameterNumberComponent
                    ,
                      name: "gap_ext"
                      id: "gap_ext"
                      label: "Gap Extension penalty"
  ]
  "desc": (toolargs) -> [
                      ParameterNumberComponent
                    ,
                      name: "desc"
                      id: "desc"
                      label: "Number of alignments and descriptions"
  ]


# Encompasses the individual sections of a Job, usually rendered as tabs
JobTabsComponent =
  controller: ->
    params = JobModel.tool().params
    # Determine whether the parameters contain an alignment
    params: params
    alignmentPresent: params[0][1][0][0] is "alignment"

  view: (ctrl) ->
    m "div", {class: "tool-tabs", id: "tool-tabs", config: tabulated}, [

       m "ul", ctrl.params.map (param) ->
         m "li",  {id: "tab-#{param[0]}"},  m "a", {href: "#tabpanel-#{param[0]}"}, param[0]

       # Generate views for all Parameter groups
      m "form", {id: "jobform"},
         ctrl.params.map (paramGroup) ->
           elements = paramGroup[1].filter((paramElem) -> paramElem[0] != "alignment")
           split = elements.length / 2
           m "div", {class: "tabs-panel", id: "tabpanel-#{paramGroup[0]}"}, [

             if ctrl.alignmentPresent and paramGroup[0] is "Alignment"
               comp = formComponents["alignment"]([])
               m.component comp[0], comp[1]

             # Show everything except for the alignment in the parameters div
             m "div", {class: "parameters"},

               [
                 m "div", elements.slice(0,split).map (paramElem) ->
                   comp = formComponents[paramElem[0]](paramElem[1])
                   m.component comp[0], comp[1]

                 m "div", elements.slice(split,elements.length).map (paramElem) ->
                   comp = formComponents[paramElem[0]](paramElem[1])
                   m.component comp[0], comp[1]
               ]
             m.component(JobSubmissionComponent)
           ]
    ]



JobSubmissionComponent =
  controller: ->
    jobid: JobModel.jobid

  view: (ctrl) ->
    m "div", {class: "submitbuttons"}, [
        m "input", {type: "submit", class: "success button small submitJob", value: "Submit Job"}  #TODO
        m "input", {type: "reset", class: "alert button small resetJob", value: "Reset"}
        m "input", {type: "text", class: "jobid", placeholder: "Custom JobID", onchange: m.withAttr("value", JobModel.jobid), value: JobModel.jobid()}
    ]




# The component mounted in "content" which displays the tool submission form and the result pages
JobViewComponent =

  controller: ->
    # Update tool information in Model
    JobModel.tool = Tool.get(m.route.param("toolname"))

  view: (ctrl) ->
    m "div", {id: "jobview"}, [

      m.component(JobLineComponent),
      m.component(JobTabsComponent)
    ]



#setup routes to start w/ the `#` symbol
m.route.mode = 'hash'


# Mount the JobViewComponent into the Client-side application via associated routed
#m.route document.getElementById('content'), '/', { '/tools/:toolname': JobViewComponent, '/jobs/:mainID'   : Jobs, '/:static' : StaticRoute }
m.route document.getElementById('content'), '/', { '/tools/:toolname': JobViewComponent}




# Miscellaneous code that is present across the whole web application
window.onfocus = ->
  titlenotifier.reset();
window.onclick = ->
  titlenotifier.reset();

