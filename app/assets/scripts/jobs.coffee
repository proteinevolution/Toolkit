a = ['ss', 'pp', 'qq', 'rr', 'ee', 'dd']
@jobs = {}

jobs.Job = (data) ->
  @job_id = m.prop(data.job_id)
  @state = m.prop(data.state)
  @code = m.prop(data.code)
  return # This return statement is important!

jobs.JobList = Array

jobs.vm = do ->
  vm = {}

  vm.init = ->

    vm.list = new (jobs.JobList)

    vm.onclick = (event) ->
      jobID = event()
      route = jsRoutes.controllers.Tool.result(jobID)
      $.ajax(
        url: route.url
        type: 'POST').done (data) ->
          $('#content').empty().append data


    vm.update = (desc, state, code) ->
      i = 0
      while i < vm.list.length
        job = vm.list[i]
        if job.job_id() == desc
          vm.list[i] = new (jobs.Job)(
            job_id: desc
            state: state
            code: code)
          return
        i++
      vm.list.push new (jobs.Job)( job_id: desc, state: state, code: code)

    # Send Ajax call to retrieve all Jobs from the Server

    $.post("/jobs", (data) ->
        m.startComputation()
        for job in data.jobs
          vm.update(job.i, job.s, "")
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
    m 'tr[class=job]', {onclick: jobs.vm.onclick.bind(task, task.job_id)} , [
      m('td[class=' + a[task.state()] + ']')
      m('td', task.job_id())
      m('td', task.code())
    ]
  ) ] ]
m.mount(document.getElementById('jobtable-rows'),  { controller: jobs.controller, view: jobs.view})
