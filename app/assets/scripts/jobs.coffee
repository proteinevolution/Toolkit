a = ['ss', 'pp', 'qq', 'rr', 'ee', 'dd']
@jobs = {}


jobs.Job = (data) ->
  @job_id = m.prop(data.job_id)
  @state = m.prop(data.state)
  @code = m.prop(data.code)
  @name = m.prop(data.toolname)
  return # This return statement is important!

jobs.JobList = Array

jobs.vm = do ->
  vm = {}

  vm.init = ->

    vm.list = new (jobs.JobList)

    vm.onclick = (event) ->
      jobID = event()
      $.ajax(
        async : false
        url: '/jobs/' + jobID
        type: 'POST')

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


    vm.delete = (desc) ->

      $.ajax(
        async : true
        url : '/jobs/del/' + desc()
        type : 'POST'
      )
      toDelete = undefined
      i = 0
      while i < vm.list.length

        job = vm.list[i]
        if job.job_id() == desc()
          toDelete = i
          break
        i++
      vm.list.splice(toDelete, 1)


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
    m 'tr[class=job]',   [

      m('td[class=' + a[task.state()] + ']'),
      m("div", {style: {cssFloat: "left", border: "0px solid black", paddingRight: "0.7em", paddingLeft: "0.7em"}},
        m('br'), m('input',{type: "checkbox", id: task.job_id(), value: task.job_id(), name: task.job_id()})),
      m('td',  m('a[href="/#/jobs/' + task.job_id() + '"]', task.job_id())),
      m('td', {style: {textAlign: "center", border: "1px solid black"}}
        m("div", {style: {cssFloat: "center", border: "0px solid black", paddingRight: "0.7em", paddingLeft: "0.7em"}},
          "TOOL",
        ))
      m('td', {style: {cssFloat: "center", marginLeft: "0.7em"}},
        m('input',{type: "button", value: "x",onclick: jobs.vm.clear.bind(task, task.job_id)})   )


    ]
  ) ] ]


m.mount(document.getElementById('jobtable-rows'),  { controller: jobs.controller, view: jobs.view})




###


  {onclick: jobs.vm.onclick.bind(task, task.job_id)}
###


