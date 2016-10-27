
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

    delete: (deleteCompletely, mainID) ->
      jobs.vm.remove(mainID)
      if(deleteCompletely) then mainID += "&deleteCompletely=true"
      m.request({url: "/jobs?mainIDs="+mainID.toString(), method: "DELETE"})

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

      m "div", {class: "button job-button"}, [
        m "div", {class: "idsort", onclick: ctrl.sortJobID}, "ID"
        m "div", {class: "toolsort", onclick: ctrl.sortToolname}, "Tool"
      ]

      m "div", jobs.vm.list.map (job) ->
        m "div", {class: "job #{a[job.state()]}".concat(if job.selected() then " selected" else "")},  [

          m "div", {class: "jobid"},  m 'a[href="/#/jobs/' + job.mainID + '"]', job.job_id()
          m "span", {class: "toolname"}, job.toolname.substr(0,4)
          m "a", {class: "boxclose", onclick: ctrl.delete.bind(ctrl, false, job.mainID)}
        ]
    ]




###
  <div class="button job-handle" style="display: flex;">
            <div style=" border-right: 1px solid lightgray;" onclick="deleteIDs();" data-tooltip title="Delete selected jobs from database">Delete</div>
            <div onclick="clearIDs();" data-tooltip title="Clear selected jobs from joblist">Clear</div>

        </div>

  a[href="/#/jobs/' + job.mainID + '"]
###



