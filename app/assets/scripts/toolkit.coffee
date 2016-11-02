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
  this.lastUpdated = m.prop -1
  this.lastUpdatedMainID  = m.prop -1
  this.lastUpdatedState = m.prop -1

  # This is currently just a hack for the presentation
  this.requestTool = m.prop false

  # Determines whether Job with the provided mainID is in the JobList
  this.contains = (mainID) ->
    Job.list.then (jobs) ->
      for job in  jobs
        if job.mainID == mainID
          return true
      return false

  this.list = m.request({url: "/api/jobs", method: "GET", type: Job})

  this.reloadList =  ->
    Job.list = m.request({url: "/api/jobs", method: "GET", type: Job})

  this.getJobByMainID = (mainID) ->
    Job.list.then (jobs) ->
      for job in jobs
        if job.mainID == mainID
          return job
      return null

  # Clears a job from the joblist by index  # TODO Abstract over clear and delete
  this.clear = (idx) ->
    Job.list.then (jobs) ->
      job = Job.list()[idx]
      # TODO We have to guarantee that the Job has vanished from the users watch list once the Request has finished.
      m.request({url: "/jobs?mainIDs=#{job.mainID}&deleteCompletely=false", method: "DELETE"}).then () ->
        Job.list()[idx] = null
        Job.list().splice(idx, 1)
        if job.mainID == Job.selected()
          m.route("/tools/#{job.toolname}")


  this.delete = (mainID) ->
    Job.list.then (jobs) ->
      jobs.map (job, idx) ->
        if job.mainID == mainID
          # TODO We have to guarantee that the Job has vanished from the users watch list once the Request has finished
          m.request({url: "/jobs?mainIDs=#{job.mainID}&deleteCompletely=true", method: "DELETE"}).then () ->
            Job.list()[idx] = null
            Job.list().splice(idx, 1)
            if job.mainID == Job.selected()
              m.route("/tools/#{job.toolname}")



  this.sortToolname =  ->
    (Job.list.then (list) -> list.sort (job1, job2) -> job2.toolname.localeCompare(job1.toolname)).then(Job.list)

  this.sortJobID =  ->
    (Job.list.then (list) -> list.sort (job1, job2) -> job2.jobID().localeCompare(job1.jobID())).then(Job.list)

  this.updateState = (mainID, jobID, state) ->
    Job.lastUpdated(jobID)
    Job.lastUpdatedMainID(mainID)
    Job.lastUpdatedState(state)
    # If the job is selected, do something
    if mainID == Job.selected()
      m.route("/jobs/#{mainID}")  # This is not my final solution. I stil have some other ideas
    for job in Job.list()
      if job.mainID == mainID
        job.state(state)
        break

  # Adds a new Job to the JobList.
  this.add = (job) -> Job.list.then (list) -> list.push(job)

  this.generateJobID = () ->
    (""+Math.random()).substring(2,9)

jobs.JobList = Array

jobs.vm = do ->
  vm = {}
  vm.list = []

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


###
  GET                                 @controllers.Service.loadJob(mainID: String)
###
window.Toolkit =

  controller: (args)  ->
    if args.isJob
      mainID = m.route.param("mainID")
      Job.selected(mainID)
      # Load the Job into the Joblist if it is not there
      Job.contains(mainID).then (jobIsPresent) ->
        if not jobIsPresent
          m.request({url: "/jobs/load/#{mainID}", method: "GET"}).then (data) -> Job.add(new Job(data))
    else
      Job.selected(-1)


    toolname = m.route.param("toolname")
    # Case that the requested tool is a Frontend tool
    if FrontendTools[toolname]
      viewComponent = () -> FrontendTools[toolname]
    else
      if Job.requestTool()
        job = JobModel.update({isJob: false}, m.route.param("toolname"))
        Job.requestTool(false)
      else
        job = JobModel.update(args, if args.isJob then m.route.param("mainID") else m.route.param("toolname"))
      viewComponent = () -> m JobViewComponent, {job : job, add: Job.add, messages: JobModel.messages, joblistItem: Job.getJobByMainID(mainID)}
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