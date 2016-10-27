
@a = ['0', 'p', 'q', 'r', 'e', 'd','i']
@jobs = {}


class Job
  constructor: (params) ->
    @mainID =  params.mainID
    @toolname = params.toolname
    @job_id = m.prop params.job_id
    @state = m.prop params.state
    @selected = m.prop false


jobs.JobList = Array

jobs.vm = do ->
  vm = {}
  vm.list = []

  vm.lastStatus = -1
  vm.lastmainID = "undefined"

  vm.loadList = () ->
    m.request({url: "/api/jobs", method: "GET"}).then (jobs) ->
      console.log JSON.stringify jobs
      vm.list = jobs.map (job) -> new Job(job)

  vm.loadList()

  # Remove on Job with a certain mainID from the Job List
  vm.remove = (mainID) ->
    vm.list = vm.list.filter (job) -> job.mainID != mainID

  # Update a Job Object
  vm.update = (receivedJob) ->
    updatedJob = new Job(receivedJob)
    i = 0
    while i < vm.list.length
      job = vm.list[i]
      if job.mainID == updatedJob.mainID
        vm.list[i] = updatedJob
        return
      i++
    vm.list.push(updatedJob)


  vm.sortToolname =  ->
    vm.list = vm.list.sort (job1, job2) -> job2.toolname.localeCompare(job1.job_id)

  vm.sortJobID =  ->
    vm.list = vm.list.sort (job1, job2) -> job1.job_id().localeCompare(job2.job_id())

  vm.getJobState = (receivedJob) ->

    jsonString = JSON.stringify(receivedJob)
    if jsonString.indexOf('\"state\":3') > -1
      return 'running'
    else if jsonString.indexOf('\"state\":4') > -1
      return 'error'
    else if jsonString.indexOf('\"state\":5') > -1
      return 'done'
    else
      return 'other'

  vm.getLastJob = () ->
    return vm.list[vm.list.length-1]

  vm

###################################################################################################################3

tooltipSearch = (elem, isInit) ->
  if not isInit
    elem.setAttribute "data-tooltip", "data-tooltip"
    elem.setAttribute "aria-haspopup", "true"
    elem.setAttribute "data-disable-hover", "false"
    elem.setAttribute "title", "Search for job"

window.JobListComponent =
  controller: (args) ->
    # Select Job
    for job in jobs.vm.list
      job.selected(args.selected == job.mainID)
      if args.selected == job.mainID
        jobs.vm.lastStatus = job.state()
        jobs.vm.lastmainID = job.mainID

    select: (all) ->
      $('input:checkbox.sidebarCheck').each ->
        $(this).prop 'checked', if all then "checked" else ""

    delete: (deleteCompletely) ->
      message = if deleteCompletely then "Do you really want to delete the selected jobs permanently?"
      else "Do you really want to clear the selected jobs from the joblist?"
      mainIDs = ($("input:checkbox.sidebarCheck:checked").map () -> $(this).val())
      if mainIDs.length > 0 and confirm(message)
        #TODO this is a hackjob of a String builder, may want to change this (needed: <mainID>,<mainID2>,...)
        mainIDString = ""
        #remove all items from the List
        for mainID in mainIDs
          mainIDString += mainID + ","
          jobs.vm.remove(mainID)
        if(deleteCompletely) then mainIDString += "&deleteCompletely=true"
        m.request({url: "/jobs?mainIDs="+mainIDString, method: "DELETE"})

    sortToolname : -> jobs.vm.sortToolname()
    sortJobID: -> jobs.vm.sortJobID()

  view: (ctrl) ->
    m "div", {id: "joblist"}, [
      m "form", {id: "jobsearchform"},
        m "div", [
          m "input", {type: "text", placeholder: "Search by JobID", id: "jobsearch"}
          m "div", {id: "magnifier", class: "button", config: tooltipSearch},
            m "i", {class: "icon-magnifying", id: "iconsidebar" }
        ]

      m "div", {class: "button job-handle"}, [

        m "div", {class: "delete", onclick: ctrl.delete.bind(ctrl, true)}, "Delete"
        m "div", {onclick: ctrl.delete.bind(ctrl, false)} ,"Clear"
      ]

      m "div", {class: "button job-button"}, [

        m "div", [
          m "span",  {onclick: ctrl.select.bind(ctrl, true)}, "All"
          m "span", "/"
          m "span",  {onclick: ctrl.select.bind(ctrl, false)}, "None"
        ]
        m "div", {class: "idsort", onclick: ctrl.sortJobID}, "ID"
        m "div", {class: "toolsort", onclick: ctrl.sortToolname}, "Tool"
      ]

      jobs.vm.list.map (job) ->
        m "div", {class: "job #{a[job.state()]}".concat(if job.selected() then " selected" else "")},  [

          m "div", {class: "checkbox"}, [
            m "input", {type: "checkbox", class: 'sidebarCheck', name: job.mainID, value: job.mainID, id: job.mainID}
            m "label", {for: job.mainID}
          ]
          m "div", {class: "jobid"},  m 'a[href="/#/jobs/' + job.mainID + '"]', job.job_id()

          m "div", {class: "toolname"}, job.toolname.substr(0,4)
        ]
    ]




###
  <div class="button job-handle" style="display: flex;">
            <div style=" border-right: 1px solid lightgray;" onclick="deleteIDs();" data-tooltip title="Delete selected jobs from database">Delete</div>
            <div onclick="clearIDs();" data-tooltip title="Clear selected jobs from joblist">Clear</div>

        </div>

  a[href="/#/jobs/' + job.mainID + '"]
###



