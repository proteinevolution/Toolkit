exampleSequence = """
>NP_877456#7 putative ATP-dependent DNA ligase [Bacteriophage phiKMV]
PEITVDGRIVGYVMGKTG-KNVGRVVGYRVELEDGSTVAATGLSEE
>CAK25951#9 putative ATP-dependent DNA ligase [Bacteriophage LKD16]
PSLAVEGIVVGFVMGKTG-ANVGKVVGYRVDLEDGTIVSATGLTRD
>CAK24995#5 putative DNA ligase [Bacteriophage LKA1]   E=4e-40 s/c=1.7
PGFEADGTVIDYVWGDPDKANANKIVGFRVRLEDGAEVNATGLTQD
>NP_813751#8 putative DNA ligase [Pseudomonas phage gh-1]   gi|29243565
PDDNEDGFIQDVIWGTKGLANEGKVIGFKVLLESGHVVNACKISRA
>YP_249578#6 DNA ligase [Vibriophage VP4]   gi|66473268|gb|AAY46277.1|
PEGEIDGTVVGVNWGTVGLANEGKVIGFQVLLENGVVVDANGITQE
>YP_338096#3 ligase [Enterobacteria phage K1F]   gi|72527918|gb|AAZ7297
PSEEADGHVVRPVWGTEGLANEGMVIGFDVMLENGMEVSATNISRA
>NP_523305#4 DNA ligase [Bacteriophage T3]   gi|118769|sp|P07717|DNLI_B
PECEADGIIQGVNWGTEGLANEGKVIGFSVLLETGRLVDANNISRA
>YP_91898#2 DNA ligase [Yersinia phage Berlin]   gi|119391784|emb|CAJ
PECEADGIIQSVNWGTPGLSNEGLVIGFNVLLETGRHVAANNISQT
"""

# Config for displaying the help modals:
helpModalAccess = (elem, isInit) ->
  if not isInit
    elem.setAttribute "data-open", "help-#{this.job().tool.toolname}"

selectBoxAccess = (elem, isInit) ->
  if not isInit
    $(elem).niceSelect()

# The Job View consists of the JobTabs Component and the JobLineComponent
window.JobViewComponent =
  view: (ctrl, args) ->
    if not args.job()
      m "div", "Waiting for Job" # TODO Show at least JobLine here
    else
      m "div", {id: "jobview"}, [
        m JobLineComponent, {job: args.job}
        m JobTabsComponent, {job: args.job, add: args.add}
      ]

#############################################################################
# Component for the Jobline
JobLineComponent =
  view: (ctrl, args) ->
    isJob = args.job().isJob
    m "div", {class: "jobline"}, [
      m HelpModalComponent, {toolname: args.job().tool.toolname, toolnameLong: args.job().tool.toolnameLong}
      m "span", {class: "toolname"}, [
        args.job().tool.toolnameLong
        m "a", {config: helpModalAccess.bind(args)},
          m "i", {class: "icon-information_white helpicon"}
      ]
      m "span", {class: "jobdate"}, if isJob then "Created: #{args.job().createdOn}" else ""
      m "span", {class: "jobinfo"}, if isJob then "JobID: #{args.job().jobID}" else "Submit a new Job"
      m "span", {class: "ownername"}, if args.job().ownerName then args.job().ownerName else ""
    ]
##############################################################################

SearchformComponent =
  view: () ->
    m "div", {id: "jobsearchform"},
      m "input", {type: "text", placeholder: "Search by JobID, e.g. 6881313", id: "jobsearch"}


## Component that is displayed once the Job is running

foundationTable = (elem, isInit) ->
  if not isInit
    $(elem).foundation()

JobErrorComponent =
  view: ->
    m "div", {class: "error-panel"},
      m "p", "Job has reached Error state"

JobRunningComponent =
  view: (ctrl, args) ->
    m "div", {class: "running-panel"}, [
      m "table", {config: foundationTable},
        m "tbody", [
          m "tr", [
            m "td", "MainID"
            m "td", args.job().mainID
          ]
          m "tr", [
            m "td", "JobID"
            m "td", args.job().jobID
          ]
          m "tr", [
              m "td", "Created On"
              m "td", args.job().createdOn
          ]
        ]
        ]

# Mithril Configs for JobViewComponent
tabulated = (element, isInit) ->
  if not isInit then $(element).tabs({active: this.active})


# View template helper for generating parameter input fields
renderParameter = (content, moreClasses) ->
  m "div", {class: if moreClasses then "parameter #{moreClasses}" else "parameter"}, content


# Maps parameter received from the server to the correct component
mapParam = (paramElem, ctrl) ->
  ctrlArgs = {options: paramElem[1],  value: ctrl.getParamValue(paramElem[0])}
  comp = formComponents[paramElem[0]](ctrlArgs)
  m(comp[0], comp[1])


closeShortcut = (element, isInit) ->
  if not isInit then
  $(document).keydown (e) ->
    if e.keyCode == 27 && $("#tool-tabs").hasClass("fullscreen")
      $("#collapseMe").click()
    return



# Encompasses the individual sections of a Job, usually rendered as tabs
JobTabsComponent =
  model:  ->
    isFullscreen: false
    label:  "Expand"

  controller: (args) ->
    mo = new JobTabsComponent.model()
    # Show parameter tabs in all case
    params = args.job().tool.params
    listitems = (params.filter (param) -> param[1].length != 0).map (param) -> param[0]

    # Modify the displayed view depending on the JobState
    active = null
    if args.job().isJob
      state = args.job().jobstate
      # Activate first tab after parameters
      switch state
        when 3
          active = listitems.length
          listitems = listitems.concat "Running"   # Add the Running Tab if the JobState is Running
        when 4
          active = listitems.length
          listitems = listitems.concat "Error"   # Add the Running Tab if the JobState is Running
        when 5
          active = listitems.length
    else
      active = 0

    views = args.job().views
    if views
      listitems = listitems.concat views.map (view) -> view[0]
    params: params
    alignmentPresent: params[0][1][0][0] is "alignment"
    isJob : args.job().isJob
    state: args.job().jobstate
    listitems: listitems
    views: views
    getParamValue : JobModel.getParamValue
    job : args.job
    active : active
    getLabel: (-> this.label).bind(mo)
    fullscreen: (->
      job_tab_component = $("#tool-tabs")
      if(this.isFullscreen)
        job_tab_component.removeClass("fullscreen")
        this.isFullscreen = false
        this.label = "Expand"
        if typeof onCollapse == "function" then onCollapse()

      else
        job_tab_component.addClass("fullscreen")
        this.isFullscreen = true
        this.label = "Collapse"
        if typeof onExpand == "function" then onExpand()
      if typeof onFullscreenToggle == "function" then onFullscreenToggle()).bind(mo)

    delete: ->
      jobID = this.job().jobID
      if confirm "Do you really want to delete this Job (ID: #{jobID})"
        console.log "Delete for job #{jobID} clicked"
        Job.delete(jobID)



  view: (ctrl, args) ->
    m "div", {class: "tool-tabs", id: "tool-tabs", config: tabulated.bind(ctrl)}, [

      m "ul", [
        ctrl.listitems.map (item) ->
          m "li",  {id: "tab-#{item}"},  m "a", {href: "#tabpanel-#{item}"}, item

        m "li", {style: "float: right;"},
          m "input", {type: "button", id:"collapseMe", class: "button small button_fullscreen", value: ctrl.getLabel(), onclick: ctrl.fullscreen, config: closeShortcut},
        if ctrl.isJob
          m "li", {style: "float: right;" },
            m "input", {type: "button", class: "button small delete", value: "Delete Job", onclick: ctrl.delete.bind(ctrl)}
      ]
      m "form", {id: "jobform"},
        ctrl.params.map (paramGroup) ->
          if paramGroup[1].length != 0
            elements = paramGroup[1]
            m "div", {class: "tabs-panel parameter-panel", id: "tabpanel-#{paramGroup[0]}"}, [
              m "div", {class: "parameters"},
                # One column Layout for the input tab
                if paramGroup[0] is "Input"
                    # If the alignment parameter exists, render it first
                    if elements[0][0] == "alignment"
                      [
                        m "div", {class: "row"}, m "div", {class: "small-12 large-12 medium-12 columns"},
                          mapParam(elements[0], ctrl)
                        m "div", {class: "row small-up-1 medium-up-2 large-up-3"}, elements.slice(1).map (paramElem) ->
                          m "div", {class: "column column-block"}, mapParam(paramElem, ctrl)
                      ]
                    else
                      m "div", {class: "row small-up-1 medium-up-2 large-up-3"}, elements.map (paramElem) ->
                        m "div", {class: "column column-block"}, mapParam(paramElem, ctrl)
                else
                  m "div", {class: "row small-up-1 medium-up-2 large-up-3"}, elements.map (paramElem) ->
                    m "div", {class: "column column-block"}, mapParam(paramElem, ctrl)
              m JobSubmissionComponent, {job: ctrl.job, isJob: ctrl.isJob, add: args.add}
          ]
        if ctrl.isJob and ctrl.state == 3
          m "div", {class: "tabs-panel", id: "tabpanel-Running"},
            m JobRunningComponent, {job: ctrl.job}

        if ctrl.isJob and ctrl.state == 4
          m "div", {class: "tabs-panel", id: "tabpanel-Error"},
            JobErrorComponent

      if ctrl.views
        ctrl.views.map (view) ->
          m "div", {class: "tabs-panel", id: "tabpanel-#{view[0]}"},
            m "div", {class: "result-panel"}, m.trust view[1]
    ]
##############################################################################
# Job Submission input elements
submitModal = (elem, isInit) ->
  if not isInit
    $(elem).foundation()
    $(elem).bind('closed.zf.reveal	', (-> $(".submitJob").prop("disabled", false)))


JobSubmissionComponent =

  controller: (args) ->
    this.submitting = false
    setJobID: ((jobID) ->
      this.jobID = jobID).bind(args.job())
    # Bind function to submit Job
    submit: (startJob) ->
      toolname = args.job().tool.toolname
      # Either use custom jobID or declare not using an own
      jobID = args.job().jobID
      if not jobID
        jobID = null
      checkRoute = jsRoutes.controllers.JobController.check(toolname, jobID)
      formData = new FormData(document.getElementById("jobform"))
      $(".submitJob").prop("disabled", true)
      m.request
        method: checkRoute.method
        url: checkRoute.url
        data: formData
        serialize: (data) -> data
      .then(
        (data) ->
          jobID = data.jobID
          if data.existingJobs
            # Remove previous click handlers
            $('#reload_job').unbind 'click'
            $('#submit_again').unbind 'click'

            # Bind new Click handlers
            $('#reload_job').on 'click', ->
              $('#submit_modal').foundation('close')
              m.route("/jobs/#{data.existingJob.jobID}")
            $('#submit_again').on 'click', ->
              $('#submit_modal').foundation('close')
              sendMessage({type: "RegisterJobs", "jobIDs": [jobID]})
              # Add a new Job to the Model
              Job.add(new Job({mainID: jobID, jobID: jobID, state: 0, createdOn: 'now', toolname: toolname}))
              submitRoute = jsRoutes.controllers.JobController.create(toolname, jobID)

              m.request({url: submitRoute.url, method: submitRoute.method, data: formData, serialize: (data) -> data})
              m.route("/jobs/#{jobID}")
            # Show the modal
            $('#submit_modal').foundation('open')
          else
            sendMessage({type: "RegisterJobs", "jobIDs": [jobID]})
            # Add a new Job to the Model
            Job.add(new Job({mainID: jobID, jobID: jobID, state: 0, createdOn: 'now', toolname: toolname}))
            submitRoute = jsRoutes.controllers.JobController.create(toolname, jobID)
            m.request
              method: submitRoute.method
              url: submitRoute.url
              data: formData
              serialize: (data) -> data
            m.route("/jobs/#{jobID}")

        (error) ->
          alert "Bad Request"
          $(".submitJob").prop("disabled", false)
      )


  view: (ctrl, args) ->
    m "div", {class: "submitbuttons"}, [
      m "div" , {class: "reveal", 'data-reveal': 'data-reveal', 'data-animation-in': 'fade-in', 'transition-duration': 'fast', id: 'submit_modal', config: submitModal},
        m "p", "Already existing job found!"
        m "input", {class: 'button', id: 'reload_job', type: 'button', value: 'Reload'} # data-close
        m "input", {class: 'button', id: 'submit_again', type: 'button', value: 'New Submission'} # data-close
      if !this.submitting then m "input", {type: "button", class: "success button small submitJob", value: "#{if args.isJob then "Res" else "S"}ubmit Job", onclick: ctrl.submit.bind(ctrl, true)} else null #TODO
      if !args.isJob
        m "label",{hidden: "hidden"}, [
          m "input", {type: "checkbox", name:"private", value: "true", checked:"checked", hidden: "hidden"}  #TODO style me
          "Private"
        ]
      else null #TODO
      #if !args.isJob then m "input", {type: "button", class: "success button small submitJob", value: "Prepare Job", onclick: ctrl.submit.bind(ctrl, false)} else null #TODO
      if  args.isJob && args.job().jobstate == 1 then m "input", {type: "button", class: "button small addJob", value: "Start Job", onclick: ctrl.startJob} else null  #TODO
      if  args.isJob then m "input", {type: "button", class: "button small addJob", value: "Add Job", onclick: ctrl.addJob} else null  #TODO
      m "input", {type: "text", id: "jobID", class: "jobid", placeholder: "Custom JobID", onchange: m.withAttr("value", ctrl.setJobID), value: args.job().jobID}
      #m "input", {type: "button", class: "button small checkJob", value: "Check JobID", onclick: ctrl.checkJobID.bind(this) } # TODO somehow get this together with the jobID onChange
      #m "input", {type: "button", class: "button hollow small upload", value: "Upload File", style: "margin-left: 15px;"}
      m "input", {type: "text", class: "jobid", placeholder: "E-Mail Notification", style: "width: 16em; float: right;"}
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

alignmentUpload = (elem, isInit) ->
  if not isInit
    elem.setAttribute("data-reveal", "data-reveal")
    $(elem).foundation()


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
# TODO Has to be generalized for one or multiple sequences
window.ParameterAlignmentComponent =
  model: (args) ->
    value:  args.value    # Alignment Text
    format: null         # Alignment Format

  controller: (args) ->     # TODO Introduce function for automatic alignment format detection
    this.param = new ParameterAlignmentComponent.model args
    name: "alignment"
    id: "alignment"
    placeholder: "Enter multiple sequence alignment"
    formatOptions : args.options
    param : this.param
  view: (ctrl) ->
    renderParameter [
        m "div", {class: "alignment_textarea"},
          m "textarea",                   # Textarea field for alignment input
            name: ctrl.name
            placeholder: ctrl.placeholder
            rows: 15
            cols: 70
            id: ctrl.id
            onchange: m.withAttr("value", ctrl.param.value)
            value: ctrl.param.value
            spellcheck: false
        m "div", {id: "upload_alignment_modal", class: "tiny reveal", config: alignmentUpload},
          m "input",
            type: "file"
            id: "upload_alignment_input"
            name: "upload_alignment_input"
            onchange: ->
              if this.value
                $("##{ctrl.id}").prop("disabled", true)
        m "div", {class: "alignment_buttons"}, [
          m "input",                         # Place example alignment
            type: "button"
            class: "button small alignmentExample"
            value: "Paste Example"
            onclick: () -> ctrl.param.value = exampleSequence
          m "input",                         # Place example alignment
            type: "button"
            class: "button small alignmentExample"
            value: "Upload File"
            onclick: () -> $('#upload_alignment_modal').foundation('open')
        ]

    ], "alignmentParameter"  # TODO Should be a controller argument


ParameterRadioComponent =
  view: (ctrl, args) ->
    renderParameter [
      m "label", {for: args.id}, args.label
      args.options.map (entry) ->
        m "span", [
          m "input", {type: "radio", name: args.name, value: entry[0]}
          entry[1]
        ]
    ]


ParameterSelectComponent =
  view: (ctrl, args) ->
    renderParameter [
      m "label", {for: args.id}, args.label
      m "select", {name: args.name, class: "wide", id: args.id, config: selectBoxAccess}, args.options.map (entry) ->
        m "option", (if entry[0] == args.value then {value: entry[0], selected: "selected"} else {value: entry[0]}),
          entry[1]
    ]

ParameterNumberComponent =

  controller: (args ) ->
    name: args.name
    id: args.id
    label: args.label

  view: (ctrl, args) ->
    renderParameter [
      m "label", {for: ctrl.id}, ctrl.label
      m "input", {type: "text", id: ctrl.id, name: ctrl.name, value: args.value}

    ]

ParameterBoolComponent =

  view: (ctrl, args) ->
    renderParameter [
      m "label", {for: args.id}, args.label
      m "input", {type: "checkbox", id: args.id, name: args.name, value: args.value}
    ]


ParameterRangeSliderComponent =
  view: (ctrl, args) ->
    renderParameter [
      m "div", {class: "small-10 columns"}, [
        m "div", {class: "slider"}, [
          m "span", {class: "slider-handle", tabindex: "1"}
          m "span", {class: "slider-fill"}
        ]
      ]

    ]

    ###
  <div class="small-10 columns">
  <div class="slider" data-slider data-initial-start="50" data-step="5">
    <span class="slider-handle"  data-slider-handle role="slider" tabindex="1" aria-controls="sliderOutput2"></span>
    <span class="slider-fill" data-slider-fill></span>
  </div>
</div>
<div class="small-2 columns">
  <input type="number" id="sliderOutput2">
</div>
  ###
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
  "hhblitsdb": (args) -> [
    ParameterSelectComponent
  ,
    options: args.options
    name: "hhblitsdb"
    id: "hhblitsdb"
    label: "Select Target Database"
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
  "max_lines": (args) -> [
    ParameterNumberComponent
  ,
    name: "max_lines"
    id: "max_lines"
    label: "Max. number of hits in hitlist"
    value: args.value
  ]
  "pmin": (args) -> [
    ParameterNumberComponent
  ,
    name: "pmin"
    id: "pmin"
    label: "Min. probability in hitlist"
    value: args.value
  ]
  "aliwidth": (args) -> [
    ParameterNumberComponent
  ,
    name: "aliwidth"
    id: "aliwidth"
    label: "With of alignments (columns)"
    value: args.value
  ]
  "max_seqs": (args) -> [
    ParameterNumberComponent
  ,
    name: "max_seqs"
    id: "max_seqs"
    label: "Max. number of sequences"
    value: 100
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
    value: 0
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
    value: 0.45
  ]
  "bonusscore": (args) -> [
    ParameterNumberComponent
  ,
    name: "bonusscore"
    id: "bonusscore"
    label: "Bonus Score"
    value: 0
  ]
  "msageneration": (args) -> [
    ParameterSelectComponent
  ,
    name: "msageneration"
    id: "msageneration"
    label: "Select MSA generation method"
    options: args.options
    value: args.value
  ]
  "inclusion_ethresh": (args) -> [
    ParameterSelectComponent
  ,
    name: "inclusion_ethresh"
    id: "inclusion_ethresh"
    label: "E-value inclusion threshold"
    value: args.value
    options: args.options
  ]
  "min_cov": (args) -> [
    ParameterSelectComponent
  ,
    name: "min_cov"
    id: "min_cov"
    label: "Min. coverage of hits"
    value: args.value
    options: args.options
  ]
  "hhsuitedb": (args) -> [
    ParameterSelectComponent
  ,
    name: "hhsuitedb"
    id: "hhsuitedb"
    label: "Select HHsuite Database"
    value: args.value
    options: args.options
  ]
  "msa_gen_max_iter": (args) -> [
    ParameterSelectComponent
  ,
    name: "msa_gen_max_iter"
    id: "msa_gen_max_iter"
    label: "Max. number of MSA generation iterations"
    value: args.value
    options: args.options
  ]
  "genetic_code": (args) -> [
    ParameterSelectComponent
  ,
    name: "genetic_code"
    id: "genetic_code"
    label: "Choose a genetic code"
    options: args.options
    value: args.value
  ]
  "long_seq_name": (args) -> [
    ParameterBoolComponent
  ,
    name: "long_seq_name"
    id: "long_seq_name"
    label: "Use long names?"
    value: "long_seq_name"
  ]
  "min_query_cov": (args) -> [
    ParameterNumberComponent
  ,
    name: "min_query_cov"
    id: "min_query_cov"
    label: "Minimal coverage"
    value: 0.5
  ]
  "max_eval": (args) -> [
    ParameterNumberComponent
  ,
    name: "max_eval"
    id: "max_eval"
    label: "Maximal E-Value"
    value: 1e-5
  ]
  "min_anchor_width": (args) -> [
    ParameterNumberComponent
  ,
    name: "min_anchor_width"
    id: "min_anchor_width"
    label: "Minimal Anchor width"
    value: 5
  ]
  "min_colscore": (args) -> [
    ParameterNumberComponent
  ,
    name: "min_colscore"
    id: "min_colscore"
    label: "Minimal Column Score"
    value: 0
  ]
  "max_seqid": (args) -> [
    ParameterNumberComponent
  ,
    name: "max_seqid"
    id: "max_seqid"
    label: "Maximal Sequence Identity"
    value: 0.99
  ]
  "matrix_pcoils": (args) -> [
    ParameterSelectComponent
  ,
    options: args.options
    name: "matrix_pcoils"
    id: "matrix_pcoils"
    label: "Matrix"
    value: args.value
  ]
  "matrix_phylip": (args) -> [
    ParameterSelectComponent
  ,
    options: args.options
    name: "matrix_phylip"
    id: "matrix_phylip"
    label: "Model of amino acid replacement"
    value: args.value
  ]
  "weighting": (args) -> [
    ParameterBoolComponent
  ,
    name: "weighting"
    id: "weighting"
    label: "Weighting"
    value: "weighting"
  ]
  "run_psipred": (args) -> [
    ParameterBoolComponent
  ,
    name: "run_psipred"
    id: "run_psipred"
    label: "Run PSIPRED"
    value: "run_psipred"
  ]
  "protblastprogram": (args) -> [
    ParameterSelectComponent
  ,
    options: args.options
    name: "protblastprogram"
    id: "protblastprogram"
    label: "Program for performing Protein BLAST search"
    value: args.value
  ]
  "filter_low_complexity": (args) -> [
    ParameterBoolComponent
  ,
    name: "filter_low_complexity"
    id: "filter_low_complexity"
    label: "Filter for low complexity regions"
    value: "filter_low_complexity"
  ]
  "matrix_marcoil": (args) -> [
    ParameterSelectComponent
  ,
    options: args.options
    name: "matrix_marcoil"
    id: "matrix_marcoil"
    label: "Matrix"
    value: "matrix_marcoil"
  ]
  "transition_probability": (args) -> [
    ParameterSelectComponent
  ,
    options: args.options
    name: "transition_probability"
    id: "transition_probability"
    label: "Transition Probability"
  ]
  "min_seqid_query": (args) -> [
    ParameterNumberComponent
  ,
    name: "min_seqid_query"
    id: "min_seqid_query"
    label: "Minimum sequence ID with Query (%)"
    value: 0
  ]
  "num_seqs_extract": (args) -> [
    ParameterNumberComponent
  ,
    value: 100
    name: "num_seqs_extract"
    id: "num_seqs_extract"
    label: "Minimum sequence ID with Query"
  ]
