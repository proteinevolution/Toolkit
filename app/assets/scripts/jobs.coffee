a = ['0', 'p', 'q', 'r', 'e', 'd','i']
@jobs = {}


jobs.Job = (data) ->
  @toolname = m.prop(data.toolname)
  @job_id = m.prop(data.job_id)
  @state = m.prop(data.state)
  #@code = m.prop(data.code)
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
        i++
      vm.list.push new (jobs.Job)( job_id: desc, state: state, toolname: toolname)

    # Send Ajax call to retrieve all Jobs from the Server
    vm.retrieveJobs = () ->
      $.post("/jobs/list", (data) ->
        m.startComputation()
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
        #ADD tooltip not done yet
        m('span'), )
      m("div", {style: {cssFloat: "left", border: "0px solid black", paddingRight: "0.7em", paddingLeft: "0.7em"}},
        m('br'), m('input',{type: "checkbox", id: task.job_id(), value: task.job_id(), name: task.job_id()})),
      m('td',  m('a[href="/#/jobs/' + task.job_id() + '"]', task.job_id())),
      m('td', {class: task.toolname()}, {style: {textAlign: "center", border: "1px solid black"}},

        m("div", {style: {cssFloat: "center", border: "0px solid black", paddingRight: "0.7em", paddingLeft: "0.7em"}},
          task.toolname().substr(0,4)
        ))
      m('td', {style: {cssFloat: "center", marginLeft: "0.7em", fontSize: "0.5em"}},



        m('input',{type: "button", class: "button tiny alert hollow masterTooltip", style: {cssFloat: "center", padding: "0.35em 0.55em", margin: "0 0"}, title: "Clear from job table", value: "x",onclick: jobs.vm.clear.bind(task, task.job_id)})   )


    ]
  ) ]]


m.mount(document.getElementById('jobtable-rows'),  { controller: jobs.controller, view: jobs.view})




###
<span data-tooltip aria-haspopup="true" class="has-tip" title="Tooltips are awesome, you should totally use them!">extended information</span>
class="tooltip" data-tooltip="Ist das nicht ein toller Tooltip! So informativ!"
  {onclick: jobs.vm.onclick.bind(task, task.job_id)}
###


