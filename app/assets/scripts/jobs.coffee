

@a = ['0', 'p', 'q', 'r', 'e', 'd','i']
@jobs = {}


jobs.Job = (data) ->
  @mainID = m.prop(data.mainID)
  @toolname = m.prop(data.toolname)
  @job_id = m.prop(data.job_id)
  @state = m.prop(data.state)
  return # This return statement is important!

jobs.JobList = Array

jobs.vm = do ->
  vm = {}

  vm.init = ->
    m.redraw.strategy("all")

    vm.list = new (jobs.JobList)
    vm.stateList = {"0": "Partially Prepared", "p": "Prepared", "q": "Queued", "r": "Running", "e": "Error", "d": "Done", "i": "Submitted"}

    # Remove a Job from the View
    vm.clear = (mainID) ->
      i = 0
      toClear = -1
      while i < vm.list.length
        job = vm.list[i]
        if job.mainID() == mainID
          toClear = i
          break
        i++
      if(toClear > -1)
        sendMessage("type":"ClearJob","mainID":mainID)
        vm.list.splice(toClear, 1)
        return true
      return false


    # Delete a Job
    vm.delete = (mainID) ->
      isCleared = vm.clear(mainID)
      if (isCleared)
        sendMessage("type":"DeleteJob","mainID":mainID)

    # Update a Job Object
    vm.update = (receivedJob) ->
      updatedJob = new jobs.Job(mainID:receivedJob.mainID, toolname:receivedJob.toolname, job_id:receivedJob.job_id, state:receivedJob.state)
      i = 0
      while i < vm.list.length
        job = vm.list[i]
        if job.mainID() == updatedJob.mainID()
          vm.list[i] = updatedJob
          m.redraw()
          return
        i++
      vm.list.push(updatedJob)
      m.redraw()

    # Update the joblist
    vm.updateList = (jobList) ->
      m.startComputation()
      vm.list = new (jobs.JobList)
      for job in jobList
        vm.update(job)
        m.redraw.strategy("all")
      m.endComputation()

  vm
#the controller defines what part of the model is relevant for the current page
#in our case, there's only one view-model that handles everything

jobs.controller = ->
  jobs.vm.init()

#here's the view
jobs.view = ->
  [ [ jobs.vm.list.map((task) ->
    m 'div[class=job' + ' ' + a[task.state()] + ']', {style: {display: 'flex', borderTop: 'none', borderBottom: '1px solid lightgray', borderLeft: '1px solid lightgray', borderRight: '1px solid lightgray', fontSize: '0.7em', paddingBottom: '0.1em', paddingTop: '0.75em'}},   [
      m('div[class=', {style: {width: '33.5%'}}
        m('div[class=checkbox]', {style: {height: '2em'}}
          m('input[type=checkbox]', id: task.mainID(), name: task.mainID(), value: task.mainID()),
          m('label', for: task.mainID()),
        )
      )

      m('div', id: 'job_id', {style: {width: '33.5%', align: 'center'}}, m('a[href="/#/jobs/' + task.job_id() + '"]', task.job_id())),
      m('div', {class: task.toolname()}, {style: {textAlign: "center", border: "0px solid black"}},
        m("div", {style: {marginLeft: '1.25em', cssFloat: "center", border: "0px solid black",}},
          task.toolname().substr(0,4)
        ))
    ]
  ) ]]


m.mount(document.getElementById('jobtable'),  { controller: jobs.controller, view: jobs.view})







