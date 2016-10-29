FrontendTools =
  "alnviz": FrontendAlnvizComponent
  "reformat": FrontendReformatComponent

####################################################


@a = ['0', 'p', 'q', 'r', 'e', 'd','i']
@jobs = {}

class window.Job
  constructor: (args) ->
    @mainID =  args.mainID
    @jobID = m.prop args.jobID
    @state = m.prop args.state
    @createdOn = args.createdOn
    @toolname = args.toolname


  # MainID of the currently selected job
  this.selected = m.prop -1
  this.lastUpdated = m.prop -1
  this.lastUpdatedState = m.prop -1

  this.list = do ->
    console.log "Reloading Job List"
    m.request({url: "/api/jobs", method: "GET", type: Job})

  # Clears a job from the joblist by index
  this.clear = (idx) ->
    Job.list.then (jobs) ->
      m.request({url: "/jobs?mainIDs=#{jobs[idx].mainID}", method: "DELETE"})
      Job.list().splice(idx, 1)



  this.updateState = (mainID, state) ->

    # If the job is selected, do something
    if mainID == Job.selected()
      m.route("/jobs/#{mainID}")  # This is not my final solution. I stil have some other ideas1


    Job.lastUpdated(mainID)
    Job.lastUpdatedState(state)
    for job in Job.list()
      if job.mainID == mainID
        job.state(state)
        break
  # Adds a new Job to the JobList.
  this.add = (job) -> Job.list().push(job)


  this.generateJobID = () -> [
    Math.floor((Math.random()) * 9)
    Math.floor((Math.random()) * 9)
    Math.floor((Math.random()) * 9)
    Math.floor((Math.random()) * 9)
    Math.floor((Math.random()) * 9)
    Math.floor((Math.random()) * 9)
    Math.floor((Math.random()) * 9)
  ].join('')


jobs.JobList = Array

jobs.vm = do ->
  vm = {}
  vm.list = []

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
  vm


window.Toolkit =

  controller: (args)  ->


    console.log "Controller of Toolkit"
    if args.isJob
      Job.selected(m.route.param("mainID"))
    else
      Job.selected(-1)

    toolname = m.route.param("toolname")
    # Case that the requested tool is a Frontend tool
    if FrontendTools[toolname]
      viewComponent = () -> FrontendTools[toolname]
    else
      job = JobModel.update(args, if args.isJob then m.route.param("mainID") else m.route.param("toolname"))
      # Which job the user has selected
      ###
      job = Job.list.then (jobs) ->
        for job in jobs
          if job.mainID == Job.selected()
            return job
      ###
      viewComponent = () -> m JobViewComponent, {job : job, add: Job.add}
    jobs : Job.list
    viewComponent : viewComponent
    selected: Job.selected
    clear: Job.clear



  view: (ctrl) -> [
    m "div", {class: "large-2 padded-column columns show-for-large", id: "sidebar"},
      m.component JobListComponent, {jobs: ctrl.jobs, selected: ctrl.selected, clear: ctrl.clear}
    m "div", {id: "content", class: "large-10 small-10 columns padded-column"},
      ctrl.viewComponent()
  ]