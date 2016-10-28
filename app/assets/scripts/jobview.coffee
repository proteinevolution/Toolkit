# Config for displaying the help modals:
helpModalAccess = (elem, isInit) ->
  if not isInit
    elem.setAttribute "data-open", "help-#{this.toolname}"
    $(elem).foundation()


window.JobViewComponent =

  view: (ctrl, args) ->
    job = args.job()
    m "div", {id: "jobview"}, [
      m JobLineComponent, {toolnameLong: job.tool.toolnameLong, isJob: job.isJob, jobID: job.jobid, toolname: job.tool.toolname, createdOn : job.createdOn}
      m JobTabsComponent, {job: job}
    ]

##############################################################################
# Component for the Jobline
JobLineComponent =
  controller: (args) ->
    jobinfo: if args.isJob then "JobID: #{args.jobID()}" else "Submit a new Job"
    jobdate: if args.isJob then "Created: #{args.createdOn()}" else ""


  view: (ctrl, args) ->
    m "div", {class: "jobline"}, [
      m HelpModalComponent, {toolname: args.toolname, toolnameLong: args.toolnameLong}
      m "span", {class: "toolname"}, [
        args.toolnameLong
        m "a", {config: helpModalAccess.bind(args)},
          m "i", {class: "icon-white_question helpicon"}
      ]
      m "span", {class: "jobdate"}, ctrl.jobdate
      m "span", {class: "jobinfo"}, ctrl.jobinfo

    ]
##############################################################################



###
<div class="reveal" id="exampleModal1" data-reveal>
  <h1>Awesome. I Have It.</h1>
  <p class="lead">Your couch. It is mine.</p>
  <p>I'm a cool paragraph that lives inside of an even cooler modal. Wins!</p>
  <button class="close-button" data-close aria-label="Close modal" type="button">
    <span aria-hidden="true">&times;</span>
  </button>
</div>
###




# Mithril Configs for JobViewComponent
tabulated = (element, isInit) ->
  if not isInit then $(element).tabs()

# View template helper for generating parameter input fields
renderParameter = (content, moreClasses) ->
  m "div", {class: "parameter #{moreClasses}"}, content

##############################################################################


# Encompasses the individual sections of a Job, usually rendered as tabs
JobTabsComponent =
  controller: (args) ->
    params = args.job.tool.params
    listitems = params.map (param) -> param[0]
    views = args.job.views
    if views
      listitems = listitems.concat views.map (view) -> view[0]

    # Determine whether the parameters contain an alignment
    params: params
    alignmentPresent: params[0][1][0][0] is "alignment"
    isJob : args.job.isJob
    state: args.job.jobstate
    listitems: listitems
    views: views
    getParamValue : JobModel.getParamValue
    job : args.job


  view: (ctrl) ->
    m "div", {class: "tool-tabs", id: "tool-tabs", config: tabulated}, [

# Generate the list of jobsections
      m "ul", ctrl.listitems.map (item) ->
        m "li",  {id: "tab-#{item}"},  m "a", {href: "#tabpanel-#{item}"}, item


# Generate views for all Parameter groups
      m "form", {id: "jobform"},
        ctrl.params.map (paramGroup) ->
          elements = paramGroup[1].filter((paramElem) -> paramElem[0] != "alignment")
          split = (elements.length / 2)
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
            m JobSubmissionComponent, {job: ctrl.job, isJob: ctrl.isJob}
          ]
      if ctrl.views
        ctrl.views.map (view) ->
          m "div", {class: "tabs-panel", id: "tabpanel-#{view[0]}"},
            m "div", {class: "result-panel"}, m.trust view[1]
    ]
##############################################################################
# Job Submission input elements
JobSubmissionComponent =
  controller: (args) ->
    submit: (startJob) ->
      jobid = args.job.jobid()    # TODO Maybe merge with jobID validation
      if not jobid
        jobid = null
      submitRoute = jsRoutes.controllers.Tool.submit(args.job.tool.toolname, startJob, jobid)
      formData = new FormData(document.getElementById("jobform"))
      m.request({url: submitRoute.url, method: submitRoute.method, data: formData, serialize: (data) -> data})
    addJob: ->
      jobs.vm.addJob(args.job.mainID)

  view: (ctrl, args) ->
    m "div", {class: "submitbuttons"}, [
      if !args.isJob then m "input", {type: "button", class: "success button small submitJob", value: "Submit Job", onclick: ctrl.submit.bind(ctrl, true)} else null #TODO
      if  args.isJob then m "input", {type: "button", class: "success button small submitJob", value: "Resubmit Job", onclick: ctrl.submit.bind(ctrl, true)} else null   #TODO
      if  args.isJob then m "input", {type: "button", class: "button small addJob", value: "Add Job", onclick: ctrl.addJob} else null  #TODO
      m "input", {type: "text", class: "jobid", placeholder: "Custom JobID", onchange: m.withAttr("value", args.job.jobid), value: args.job.jobid()}
    ]
##############################################################################


# Components for generating form input fields. Also allows to encapsulate value validation
window.ParameterAlignmentComponent =
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
        m "div", [
          m "textarea",                   # Textarea field for alignment input
            name: ctrl.name
            placeholder: ctrl.placeholder
            rows: 10
            cols: 70
            id: ctrl.id
            onchange: m.withAttr("value", ctrl.param.value)
            value: ctrl.param.value()
        ]
        m "div", [
          m "input",                         # Place example alignment
            type: "button"
            class: "button small alignmentExample"
            value: "Paste Example"
            onclick: () -> ctrl.param.value = m.prop alnviz_example()
        ]
      m "div", {class: "fileUpload"}, [
        m "label",
        for: "FileUpload"
        class: "button",
        "Browse..."
        m "form",
          enctype: "multipart/form-data"
          m "input",
            class: "show-for-sr"
            id: "FileUpload"
            name: "file"
            type: "file"
          m "input",
            class: "button uploadButton"
            type: "button"
            value: "Upload File"
            onclick: () ->
              console.log "Click"
              file = $('input[type=file]')[0].files[0]
              alert(file.name)
              data = new FormData
              data.append 'file', file
              m.request
                method: 'POST'
                url: '/upload'
                data: data
                serialize: (data) ->
                  data
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






