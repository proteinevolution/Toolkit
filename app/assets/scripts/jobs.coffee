a = ['0', 'p', 'q', 'r', 'e', 'd','i']
@jobs = {}


jobs.Job = (data) ->
  @toolname = m.prop(data.toolname)
  @job_id = m.prop(data.job_id)
  @state = m.prop(data.state)
  return # This return statement is important!

jobs.JobList = Array

jobs.vm = do ->
  vm = {}

  vm.init = ->

    vm.list = new (jobs.JobList)
    vm.stateList = {"0": "Partially Prepared", "p": "Prepared", "q": "Queued", "r": "Running", "e": "Error", "d": "Done", "i": "Submitted"}

    vm.onclick = (event) ->
      jobID = event()
      $.ajax(
        async : false
        url: '/jobs/' + jobID
        type: 'POST')

    # Remove a Job from the View
    vm.clear = (desc) ->

      $.ajax(
        async : true
        url : '/jobs/clear/' + desc()
        type : 'POST'
      )
      toClear = undefined
      i = 0
      while i < vm.list.length

        job = vm.list[i]
        if job.job_id() == desc()
          toClear = i
          break
        i++
      vm.list.splice(toClear, 1)

    # Delete a Job
    vm.delete = (desc) ->

      $.ajax(
        async : true
        url : '/jobs/del/' + desc
        type : 'POST'
      )
      toDelete = undefined
      i = 0
      while i < vm.list.length

        job = vm.list[i]
        if job.job_id() == desc
          toDelete = i
          break
        i++
      vm.list.splice(toDelete, 1)

    # Update a Job Object
    vm.update = (desc, state, toolname) ->
      i = 0
      while i < vm.list.length
        job = vm.list[i]
        if job.job_id() == desc
          vm.list[i] = new (jobs.Job)(
            job_id: desc
            state: state
            toolname: toolname)
          return
          m.redraw.strategy("all")
        i++
      vm.list.push new (jobs.Job)( job_id: desc, state: state, toolname: toolname)

    # Send Ajax call to retrieve all Jobs from the Server
    vm.retrieveJobs = () ->
      $.post("/jobs/list", (data) ->
        m.startComputation()
        # Clear old jobs from the list
        vm.list = new (jobs.JobList)
        # Update the jobs
        for job in data.jobs
          vm.update(job.i, job.s, job.t)
        m.endComputation()
      )

  vm
#the controller defines what part of the model is relevant for the current page
#in our case, there's only one view-model that handles everything

jobs.controller = ->
  jobs.vm.init()



#here's the view
jobs.view = ->
  [ [ jobs.vm.list.map((task) ->
    m 'tr[class=job]',   [
      m('td[class=' + a[task.state()] + ']',

        m('span'), )
      m("div", {style: {cssFloat: "left", border: "0px solid black", paddingRight: "0.7em", paddingLeft: "0.7em"}},
        m('br'), m('input',{type: "checkbox", id: "jobid[]", value: task.job_id(), name: "jobid[]"})),
      m('td',  m('a[href="/#/jobs/' + task.job_id() + '"]', task.job_id())),
      m('td', {class: task.toolname()}, {style: {textAlign: "center", border: "1px solid black"}},

        m("div", {style: {cssFloat: "center", border: "0px solid black", paddingRight: "0.7em", paddingLeft: "0.7em"}},
          task.toolname().substr(0,4)
        ))
      m('td', {style: {cssFloat: "center", marginLeft: "0.7em", fontSize: "0.5em"}},
      m('span', {class: "masterTooltip", title: "Hide in your job list", ariaHidden: true}
        m('a',{ onclick: jobs.vm.clear.bind(task, task.job_id)},
          m('img[src="/assets/images/icons/fi-x.svg"][width=10em]', {class: 'clear'})))

      )
    ]
  ) ]]


m.mount(document.getElementById('jobtable-rows'),  { controller: jobs.controller, view: jobs.view})







