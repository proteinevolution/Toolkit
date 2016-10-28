FrontendTools =
  "alnviz": FrontendAlnvizComponent
  "reformat": FrontendReformatComponent

####################################################


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

  vm.addJob = (mainID) ->
    m.request({url: "/jobs/add/" + mainID, method: "GET"})

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









window.Toolkit =

  controller: (args)  ->
    toolname = m.route.param("toolname")
    console.log "Requested Toolname was: #{toolname}"

    # Case that the requested tool is a Frontend tool
    if FrontendTools[toolname]
      console.log "Start Frontend tool"
      viewComponent = () -> FrontendTools[toolname]
      listComponent = () -> m JobListComponent, {selected: null}

    else
      console.log "Start backend tool"
      job = JobModel.update(args, if args.isJob then m.route.param("mainID") else m.route.param("toolname"))
      viewComponent = () -> m JobViewComponent, {job : job}
      listComponent = () -> m JobListComponent, {selected: job().mainID}
    viewComponent : viewComponent
    listComponent : listComponent


  view: (ctrl) -> [
    m "div", {class: "large-2 padded-column columns show-for-large", id: "sidebar"}, ctrl.listComponent()
    m "div", {id: "content", class: "large-10 small-10 columns padded-column"}, ctrl.viewComponent()
  ]
