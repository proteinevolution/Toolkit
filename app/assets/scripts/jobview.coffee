window.JobViewComponent =

  controller: (args) ->
    if args.isJob
      JobModel.getJob(m.route.param("mainid")).then (data) ->    #TODO Introduce a meaningful JobModel
        JobModel.tool = m.prop data.toolitem
        JobModel.jobid = m.prop data.jobID
        JobModel.createdOn = m.prop data.createdOn
        JobModel.jobstate = m.prop data.state
        JobModel.views = m.prop data.views
        JobModel.paramValues = data.paramValues

    else
      JobModel.tool = JobModel.getTool(m.route.param("toolname"))
      JobModel.isJob(false)
      JobModel.jobstate(null)
      JobModel.jobid(null)
      JobModel.views(null)
      JobModel.paramValues = {}

  view: (ctrl) ->
    m "div", {id: "jobview"}, [

      m.component(JobLineComponent),
      m.component(JobTabsComponent)
    ]
##############################################################################


# Component for the Jobline in the unified JobView
JobLineComponent =
  controller: ->
    toolnameLong: JobModel.tool().toolnameLong
    jobid : JobModel.jobid()
    createdOn : JobModel.createdOn()
  view: (ctrl) ->
    m "div", {class: "jobline"}, [
      m "span", {class: "toolname"}, ctrl.toolnameLong     # Long toolname in Job Descriptor Line
      m "span", {class: "jobinfo"},
        if ctrl.jobid
          "JobID: #{ctrl.jobid} CreatedOn: #{ctrl.createdOn}"
        else
          "Submit a new Job"
    ]
##############################################################################


# Mithril Configs for JobViewComponent
tabulated = (element, isInit) ->
  if not isInit then $(element).tabs()

# View template helper for generating parameter input fields
renderParameter = (content, moreClasses) ->
  m "div", {class: "parameter #{moreClasses}"}, content
##############################################################################


# Encompasses the individual sections of a Job, usually rendered as tabs
JobTabsComponent =
  controller: ->
    params = JobModel.tool().params

    listitems = params.map (param) -> param[0]
    views = JobModel.views()
    if views
      listitems = listitems.concat views.map (view) -> view[0]

    # Determine whether the parameters contain an alignment
    params: params
    alignmentPresent: params[0][1][0][0] is "alignment"
    isJob : JobModel.isJob()
    state: JobModel.jobstate()
    listitems: listitems
    views: views
    getParamValue : JobModel.getParamValue


  view: (ctrl) ->
    m "div", {class: "tool-tabs", id: "tool-tabs", config: tabulated}, [

# Generate the list of jobsections
      m "ul", ctrl.listitems.map (item) ->
        m "li",  {id: "tab-#{item}"},  m "a", {href: "#tabpanel-#{item}"}, item


# Generate views for all Parameter groups
      m "form", {id: "jobform"},
        ctrl.params.map (paramGroup) ->
          elements = paramGroup[1].filter((paramElem) -> paramElem[0] != "alignment")
          split = elements.length / 2
          m "div", {class: "tabs-panel", id: "tabpanel-#{paramGroup[0]}"}, [

            if ctrl.alignmentPresent and paramGroup[0] is "Alignment"

# Controller arguments of the Component to be mounted
              ctrlArgs = {options: paramGroup[1][0][1],  value: ctrl.getParamValue("alignment")}
              comp = formComponents["alignment"](ctrlArgs)
              m.component comp[0], comp[1]

# Show everything except for the alignment in the parameters div
            m "div", {class: "parameters"},
              [
                m "div", elements.slice(0,split).map (paramElem) ->
                  ctrlArgs = {options: paramElem[1],  value: ctrl.getParamValue(paramElem[0])}
                  comp = formComponents[paramElem[0]](ctrlArgs)
                  m.component comp[0], comp[1]

                m "div", elements.slice(split,elements.length).map (paramElem) ->
                  ctrlArgs = {options: paramElem[1],  value: ctrl.getParamValue(paramElem[0])}
                  comp = formComponents[paramElem[0]](ctrlArgs)
                  m.component comp[0], comp[1]
              ]
            m.component(JobSubmissionComponent)
          ]
      if ctrl.views
        ctrl.views.map (view) ->
          m "div", {class: "tabs-panel", id: "tabpanel-#{view[0]}"},
            m "div", {class: "result-panel"}, m.trust view[1]
    ]
##############################################################################
# Job Submission input elements
JobSubmissionComponent =
  controller: ->
    submit: (startJob) ->
      jobid = JobModel.jobid()    # TODO Maybe merge with jobID validation
      if not jobid
        jobid = null
      submitRoute = jsRoutes.controllers.Tool.submit(JobModel.tool().toolname, startJob, jobid)
      formData = new FormData(document.getElementById("jobform"))
      m.request {url: submitRoute.url, method: submitRoute.method, data: formData, serialize: (data) -> data}

  view: (ctrl) ->
    m "div", {class: "submitbuttons"}, [
      m "input", {type: "button", class: "success button small submitJob", value: "Submit Job", onclick: ctrl.submit.bind(ctrl, true)}  #TODO
      m "input", {type: "button", class: "success button small prepareJob", value: "Prepare Job", onclick: ctrl.submit.bind(ctrl, false)}
      m "input", {type: "reset", class: "alert button small resetJob", value: "Reset"}
      m "input", {type: "text", class: "jobid", placeholder: "Custom JobID", onchange: m.withAttr("value", JobModel.jobid), value: JobModel.jobid()}
    ]
##############################################################################


# Components for generating form input fields. Also allows to encapsulate value validation
ParameterAlignmentComponent =
  model: (args) ->
    value: m.prop args.value    # Alignment Text
    format: m.prop null         # Alignment Format

  controller: (args) ->     # TODO Introduce function for automatic alignment format detection
    this.param = new ParameterAlignmentComponent.model args
    name: "alignment"
    id: "alignment"
    placeholder: "Enter multiple sequence alignment"
    formatOptions : args.options
    param : this.param


  view: (ctrl) ->
    renderParameter [
      m "textarea",                   # Textarea field for alignment input
        name: ctrl.name
        placeholder: ctrl.placeholder
        rows: 10
        cols: 70
        id: ctrl.id
        onchange: m.withAttr("value", ctrl.param.value)
        value: ctrl.param.value()
      m "input",                     # Place example alignment
        type: "button"
        class: "button small alignmentExample"
        value: "Paste Example"
        onclick: () -> ctrl.param.value = m.prop alnviz_example()
      m "div", {class: "alignmentFormatBlock"}, [
        m "label", {for: "alignment_format"}, "Alignment Format"
        m "select", {name: "alignment_format", id: "alignment_format", label: "Select Alignment format", class: "alignmentFormat"}, ctrl.formatOptions.map (entry) ->
          m "option", {value: entry[0]}, entry[1]
      ]
    ], "alignmentParameter"  # TODO Should be a controller argument


ParameterSelectComponent =
  model: (args) ->
    value: m.prop args.value

  controller: (args) ->
    options: args.options
    name: args.name
    id: args.id
    label: args.label
    param: new ParameterSelectComponent.model args
    value: args.value

  view: (ctrl) ->
    renderParameter [
      m "label", {for: ctrl.id}, ctrl.label
      m "select", {name: ctrl.name, id: ctrl.id}, ctrl.options.map (entry) ->
        if entry[0] == ctrl.value
          m "option", {value: entry[0], selected: "selected"}, entry[1]
        else
          m "option", {value: entry[0]}, entry[1]
    ]

ParameterNumberComponent =

  model: (args) ->
    value: m.prop args.value

  controller: (args ) ->
    this.param = new ParameterNumberComponent.model args
    name: args.name
    id: args.id
    label: args.label
    param : this.param

  view: (ctrl) ->
    renderParameter [
      m "label", {for: ctrl.id}, ctrl.label
      m "input", {type: "text", id: ctrl.id, name: ctrl.name, value: ctrl.param.value(), onchange: m.withAttr("value", ctrl.param.value) }
    ]



##############################################################################
# Associates each parameter name with the respective component
formComponents =
  "alignment" : (args) -> [ ParameterAlignmentComponent, {options: args.options, value: args.value}]
  "standarddb": (args) -> [
    ParameterSelectComponent
  ,
    options: args.options
    name: "standarddb"
    id: "standarddb"
    label: "Select Standard Database"
    value: args.value
  ]

  "matrix": (args) -> [
    ParameterSelectComponent
  ,
    options: args.options
    name: "matrix"
    id: "matrix"
    label: "Scoring Matrix"
    value: args.value
  ]
  "num_iter": (args) -> [
    ParameterNumberComponent
  ,
    name: "num_iter"
    id: "num_iter"
    label: "Number of Iterations"
    value: args.value
  ]

  "evalue": (args) -> [
    ParameterNumberComponent
  ,
    name: "evalue"
    id: "evalue"
    label: "E-Value"
    value: args.value
  ]

  "inclusion_ethresh": (args) -> [
    ParameterNumberComponent
  ,
    name: "inclusion_ethresh"
    id: "inclusion_ethresh"
    label: "E-value inclusion threshold"
    value: args.value
  ]

  "gap_open": (args) -> [
    ParameterNumberComponent
  ,
    name: "gap_open"
    id: "gap_open"
    label: "Gap Open penalty"
    value: args.value
  ]
  "gap_ext":  (args) -> [
    ParameterNumberComponent
  ,
    name: "gap_ext"
    id: "gap_ext"
    label: "Gap Extension penalty"
    value: args.value
  ]
  "desc": (args) -> [
    ParameterNumberComponent
  ,
    name: "desc"
    id: "desc"
    label: "Number of alignments and descriptions"
    value: args.value
  ]






