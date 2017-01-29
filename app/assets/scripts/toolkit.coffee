FrontendTools =
  "alnviz": FrontendAlnvizComponent
  "reformat": FrontendReformatComponent
  "retseq": FrontendRetrieveSeqComponent

####################################################


@a = ['0', 'p', 'q', 'r', 'e', 'd','i']
@jobs = {}

class window.Job
  constructor: (args) ->
    @jobID = m.prop args.jobID
    @state = m.prop args.state
    @createdOn = args.createdOn
    @toolname = args.toolname


  # MainID of the currently selected job
  this.selected = m.prop -1
  this.owner = m.prop ""
  this.lastUpdated = m.prop -1
  this.lastUpdatedMainID  = m.prop -1
  this.lastUpdatedState = m.prop -1

  # Determines whether Job with the provided mainID is in the JobList
  this.contains = (jobID) ->
    Job.list.then (jobs) ->
      for job in jobs
        if job.jobID() == jobID
          return true
      return false

  this.list = m.request({url: "/api/jobs", method: "GET", type: Job})

  # loads the complete job list
  this.reloadList =  ->
    Job.list = m.request({url: "/api/jobs", method: "GET", type: Job})
    Job.list.then(Job.list)


  this.getJobByID = (jobID) ->
    Job.list.then (jobs) ->
      for job in jobs
        if job.jobID == jobID
          return job
      return null

  # Clears a job from the joblist by index  # TODO Abstract over clear and delete
  this.clear = (idx) ->
    console.log("Job Cleared invoked")
    Job.list.then (jobs) ->
      job = jobs[idx]
      jobs[idx] = null
      jobs.splice(idx, 1)
      sendMessage({ "type": "ClearJob",  "jobID": job.jobID()})
      if job.jobID() == Job.selected()
        m.route("/tools/#{job.toolname}")


  this.delete = (jobID) ->
    console.log("Deletion in JobModel for JOb " + jobID)
    Job.list.then (jobs) ->
      jobs.map (job, idx) ->
        if job.jobID() == jobID
          deletionRoute = jsRoutes.controllers.JobController.delete(jobID)
          m.request {url: deletionRoute.url, method: deletionRoute.method}
          Job.clear(idx)


  this.sortToolname =  ->
    (Job.list.then (list) -> list.sort (job1, job2) -> job2.toolname.localeCompare(job1.toolname)).then(Job.list)

  this.sortJobID =  ->
    (Job.list.then (list) -> list.sort (job1, job2) -> job2.jobID().localeCompare(job1.jobID())).then(Job.list)

  this.updateState = (jobID, state) ->
    Job.lastUpdated(jobID)
    Job.lastUpdatedState(state)

    # If the updated job is currently selected, make a new request to get latest job view
    if jobID == Job.selected()
      m.route("/jobs/#{jobID}")

    for job in Job.list()
      if job.jobID() == jobID
        job.state(state)
        break

  # Adds a new Job to the JobList.
  this.add = (job) ->
    Job.list.then (list) -> list.push(job)



window.Toolkit =

  controller: (args)  ->
    if args.isJob
      jobID  = m.route.param("jobID")
      Job.selected(jobID)
      # Load the Job into the Joblist if it is not there
      Job.contains(jobID).then (jobIsPresent) ->
        if not jobIsPresent
          console.log("Loading Requested Job with JobID #{jobID}")
          m.request({url: "/api/job/load/#{jobID}", method: "GET"}).then (data) -> Job.add(new Job(data))
    else
      Job.selected(-1)



    toolname = m.route.param("toolname")
    # Case that the requested tool is a Frontend tool
    if FrontendTools[toolname]
      viewComponent = () -> FrontendTools[toolname]
    else
      job = JobModel.update(args, if args.isJob then m.route.param("jobID") else m.route.param("toolname"))
      viewComponent = () -> m JobViewComponent, {job : job, add: Job.add, messages: JobModel.messages, joblistItem: Job.getJobByID(jobID)}
    jobs : Job.list
    viewComponent : viewComponent
    selected: Job.selected
    clear: Job.clear
    ownerName: Job.owner

  view: (ctrl) -> [
    m "div", {class: "large-2 padded-column columns show-for-large", id: "sidebar"},
      m.component JobListComponent, {owner: ctrl.ownerName, jobs: ctrl.jobs, selected: ctrl.selected, clear: ctrl.clear}
    m "div", {id: "content", class: "large-10 small-12 columns padded-column"},
      ctrl.viewComponent()
  ]