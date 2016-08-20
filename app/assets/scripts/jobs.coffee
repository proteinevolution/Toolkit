

@a = ['0', 'p', 'q', 'r', 'e', 'd','i']
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

        m.redraw()
        i++
      vm.list.push new (jobs.Job)( job_id: job_id, state: state, toolname: toolname)

    # Update the joblist
    vm.updateList = (joblist) ->
      m.startComputation()
      vm.list = new (jobs.JobList)
      for job in joblist
        vm.update(job.job_id, job.state, job.toolname)
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
        m('div[class=checkbox' + ' ' +  a[task.state()] + ']', {style: {height: '2em'}}
          m('input[type=checkbox]', id: task.job_id(), name: task.job_id()),
          m('label', for: task.job_id()),
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







