
# Config for displaying the help modals:
helpModalAccess = (elem, isInit) ->
  if not isInit
    elem.setAttribute "data-open", "help-#{this.job().tool.toolname}"


window.JobViewComponent =

  view: (ctrl, args) ->
    if not args.job()
      m "div", "Waiting for Job"
    else
      m "div", {id: "jobview"}, [
        m JobLineComponent, {job: args.job}
        m JobTabsComponent, {job: args.job, add: args.add}
      ]

#############################################################################
# Component for the Jobline
JobLineComponent =

  view: (ctrl, args) ->
    m "div", {class: "jobline"}, [
      m HelpModalComponent, {toolname: args.job().tool.toolname, toolnameLong: args.job().tool.toolnameLong}
      m "span", {class: "toolname"}, [
        args.job().tool.toolnameLong
        m "a", {config: helpModalAccess.bind(args)},
          m "i", {class: "icon-white_question helpicon"}
      ]
      m "span", {class: "jobdate"}, if args.job().isJob then "Created: #{args.job().createdOn()}" else ""
      m "span", {class: "jobinfo"}, if args.job().isJob then "JobID: #{args.job().jobID()}" else "Submit a new Job"
      m "span", {class: "ownername"}, if args.job().ownerName then args.job().ownerName() else ""
    ]
##############################################################################


###
m HelpModalComponent, {toolname: args.job().tool.toolname, toolnameLong: args.job().tool.toolnameLong}
###
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
    params = args.job().tool.params
    listitems = (params.filter (param) -> param[1].length != 0).map (param) -> param[0]
    views = args.job().views
    if views
      listitems = listitems.concat views.map (view) -> view[0]

    # Determine whether the parameters contain an alignment
    params: params
    alignmentPresent: params[0][1][0][0] is "alignment"
    isJob : args.job().isJob
    state: args.job().jobstate
    listitems: listitems
    views: views
    getParamValue : JobModel.getParamValue
    job : args.job


  view: (ctrl, args) ->
    m "div", {class: "tool-tabs", id: "tool-tabs", config: tabulated}, [

# Generate the list of jobsections
      m "ul", ctrl.listitems.map (item) ->
        m "li",  {id: "tab-#{item}"},  m "a", {href: "#tabpanel-#{item}"}, item


# Generate views for all Parameter groups
      m "form", {id: "jobform"},
        ctrl.params.map (paramGroup) ->
          if paramGroup[1].length != 0
            elements = paramGroup[1].filter((paramElem) -> paramElem[0] != "alignment")
            split = (elements.length / 2) + 1
            m "div", {class: "tabs-panel", id: "tabpanel-#{paramGroup[0]}"}, [

              if ctrl.alignmentPresent and paramGroup[0] is "Input"

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
              m JobSubmissionComponent, {job: ctrl.job, isJob: ctrl.isJob, add: args.add}
            ]
      if ctrl.views
        ctrl.views.map (view) ->
          m "div", {class: "tabs-panel", id: "tabpanel-#{view[0]}"},
            m "div", {class: "result-panel"}, m.trust view[1]
    ]
##############################################################################
# Job Submission input elements
###
if (json.existingJobs)
          if confirm("There is an identical job, would You like to see it?")  # This blocks mithril
            m.route("/jobs/#{json.existingJob.mainID}")
          else
            sendMessage("type":"StartJob", "mainID":json.mainID)
        else
          m.route("/jobs/#{mainID}")
###
JobSubmissionComponent =
  controller: (args) ->
    this.submitting = false
    submit: (startJob) ->
      submitting:true
      mainID = JobModel.mainID()

      jobid = args.job().jobID()    # TODO Maybe merge with jobID validation
      if not jobid                # TODO Prevent submission if validation fails
        jobid = window.Job.generateJobID()
      args.add(new Job({mainID: mainID, jobID: jobid, state: 6, createdOn: "now", toolname: args.job().tool.toolname}))

      submitRoute = jsRoutes.controllers.Tool.submit(args.job().tool.toolname, mainID, jobid)
      formData = new FormData(document.getElementById("jobform"))
      Job.requestTool(true)

      # Send submission request and see whether server accepts or job already exists
      m.request({url: submitRoute.url, method: submitRoute.method, data: formData, serialize: (data) -> data}).then (json) ->
          m.route("/jobs/#{mainID}")

    addJob: ->
      jobs.vm.addJob(args.job.mainID)

    startJob: ->
      sendMessage("type":"StartJob", "mainID":args.job.mainID)

  revealJobAlert: (mainID) ->
    m.route("/jobs/#{mainID}")

  view: (ctrl, args) ->
    m "div", {class: "submitbuttons"}, [
      if !this.submitting then m "input", {type: "button", class: "success button small submitJob", value: "#{if args.isJob then "Res" else "S"}ubmit Job", onclick: ctrl.submit.bind(ctrl, true)} else null #TODO
      if !args.isJob
        m "label", [
          m "input", {type: "checkbox", name:"private", value: "true", checked: "checked"}  #TODO style me
          "Private"
        ]
      else null #TODO
      #if !args.isJob then m "input", {type: "button", class: "success button small submitJob", value: "Prepare Job", onclick: ctrl.submit.bind(ctrl, false)} else null #TODO
      if  args.isJob && args.job().jobstate == 1 then m "input", {type: "button", class: "button small addJob", value: "Start Job", onclick: ctrl.startJob} else null  #TODO
      if  args.isJob then m "input", {type: "button", class: "button small addJob", value: "Add Job", onclick: ctrl.addJob} else null  #TODO
      m "input", {type: "text", class: "jobid", placeholder: "Custom JobID", onchange: m.withAttr("value", args.job().jobID), value: args.job().jobID()}
    ]
##############################################################################
m.capture = (eventName, handler) ->

  bindCapturingHandler = (element) ->
    element.addEventListener eventName, handler, true
    return

  (element, init) ->
    if !init
      bindCapturingHandler element
    return


dropzone_psi  = (element, isInit) ->

  handleFileSelect = (evt) ->
    evt.stopPropagation()
    evt.preventDefault()
    files = evt.dataTransfer.files
    # FileList object.
    # files is a FileList of File objects. List some properties.
    output = []
    i = 0
    f = undefined
    while f = files[i]
      output.push '<li><strong>', escape(f.name), '</strong> (', f.type or 'n/a', ') - ', f.size, ' bytes, last modified: ', f.lastModifiedDate.toLocaleDateString(), '</li>'
      i++
    document.getElementById('list').innerHTML = '<ul>' + output.join('') + '</ul>'
    return

  handleDragOver = (evt) ->
    evt.stopPropagation()
    evt.preventDefault()
    evt.dataTransfer.dropEffect = 'copy'
      # Explicitly show this is a copy.
    return

  if not isInit

    $(element).addEventListener 'dragover', handleDragOver, false
    $(element).addEventListener 'drop', handleFileSelect, false



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

    ], "alignmentParameter"  # TODO Should be a controller argument

###
  <div class="large-6 large-centered columns" id="dropzonewrapper" style="display: none;">
                    <div id="progress_bar"><div class="percent">0%</div></div>
                    <input type="file" id="files" name="file" />
                        <!--<div id="drop_zone" type="file" class="dropzone" name="file" style="margin-top: 10px; text-align: center; line-height: 150px; vertical-align: middle;">Drop files here</div>-->
                    <output id="list"></output>


                </div>###



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
  "consistency": (args) -> [
    ParameterNumberComponent
  ,
    name: "consistency"
    id: "consistency"
    label: "Passes of consistency transformation"
    value: args.value
  ]
  "itrefine": (args) -> [
    ParameterNumberComponent
  ,
    name: "itrefine"
    id: "itrefine"
    label: "Passes of iterative refinements"
    value: args.value
  ]
  "pretrain": (args) -> [
    ParameterNumberComponent
  ,
    name: "pretrain"
    id: "pretrain"
    label: "Rounds of pretraining"
    value: args.value
  ]
  "maxrounds": (args) -> [
    ParameterNumberComponent
  ,
    name: "maxrounds"
    id: "maxrounds"
    label: "Maximum number of iterations"
    value: args.value
  ]
  "offset": (args) -> [
    ParameterNumberComponent
  ,
    name: "offset"
    id: "offset"
    label: "Offset"
    value: args.value
  ]
  "outorder": (args) -> [
    ParameterNumberComponent
  ,
    name: "outorder"
    id: "outorder"
    label: "Outorder"
    value: args.value
  ]
  "gap_term": (args) -> [
    ParameterNumberComponent
  ,
    name: "gap_term"
    id: "gap_term"
    label: "Gap Termination penalty"
    value: args.value
  ]
  "bonusscore": (args) -> [
    ParameterNumberComponent
  ,
    name: "bonusscore"
    id: "bonusscore"
    label: "Bonus Score"
    value: args.value
  ]