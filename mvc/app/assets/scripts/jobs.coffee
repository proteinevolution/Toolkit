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
    m.redraw.strategy("all")

    vm.list = new (jobs.JobList)
    vm.stateList = {"0": "Partially Prepared", "p": "Prepared", "q": "Queued", "r": "Running", "e": "Error", "d": "Done", "i": "Submitted"}

    # Remove a Job from the View
    vm.clear = (job_id) ->
      $.ajax(
        async : true
        url : '/jobs/clear/' + job_id
        type : 'POST'
      )
      toClear = undefined
      i = 0
      while i < vm.list.length
        job = vm.list[i]
        if job.job_id() == job_id
          toClear = i
          break
        i++
      vm.list.splice(toClear, 1)


    # Delete a Job
    vm.delete = (job_id) ->

      $.ajax(
        async : true
        url : '/jobs/del/' + job_id
        type : 'POST'
      )
      toDelete = undefined
      i = 0
      while i < vm.list.length

        job = vm.list[i]
        if job.job_id() == job_id
          toDelete = i
          break
        i++
      vm.list.splice(toDelete, 1)

    # Update a Job Object
    vm.update = (job_id, state, toolname) ->
      i = 0
      while i < vm.list.length
        job = vm.list[i]
        if job.job_id() == job_id
          vm.list[i] = new (jobs.Job)(
            job_id: job_id
            state: state
            toolname: toolname)
          return
      # limit job list to 5 jobs at the moment
        if i >= 4
          vm.clear(vm.list[0])
        m.redraw()
        i++
      vm.list.push new (jobs.Job)( job_id: job_id, state: state, toolname: toolname)

    # Update the joblist
    vm.updateList = (joblist) ->
      m.startComputation()
      vm.list = new (jobs.JobList)
      for job in joblist
        vm.update(job.i, job.s, job.t)
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
    m 'tr[class=job]',   [
      m('td[class=' + a[task.state()] + ']',

        m('span'), )
      m("div", {style: {cssFloat: "left", border: "0px solid black", paddingRight: "0.7em", paddingLeft: "0.7em"}},
        m('br'), m('input',{type: "checkbox", id: "jobid[]", value: task.job_id(), name: "jobid[]"})),
      m('td',  m('a[href="/#/jobs/' + task.job_id() + '"]', task.job_id())),
      m('td', {class: task.toolname()}, {style: {textAlign: "center", border: "0px solid black"}},

        m("div", {style: {cssFloat: "center", border: "0px solid black", marginLeft: "0.75em"}},
          task.toolname().substr(0,4)
        ))
      m('td', {style: {cssFloat: "center", marginLeft: "0.7em", fontSize: "0.5em"}},
      m('span', {class: "masterTooltip", title: "Hide in your job list", ariaHidden: true}
        m('a',{ onclick: jobs.vm.clear.bind(task, task.job_id())},
          m('img[src="/assets/images/icons/fi-x.svg"][width=10em]', {class: 'clear'})))

      )
    ]
  ) ]]


m.mount(document.getElementById('jobtable-rows'),  { controller: jobs.controller, view: jobs.view})







