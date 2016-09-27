sortObjectsArray = (objectsArray, sortKey) ->
  # Quick Sort:
  retVal = undefined
  if 1 < objectsArray.length
    pivotIndex = Math.floor((objectsArray.length - 1) / 2)
    # middle index
    pivotItem = objectsArray[pivotIndex]
    # value in the middle index
    less = []
    more = []
    objectsArray.splice pivotIndex, 1
    # remove the item in the pivot position
    objectsArray.forEach (value, index, array) ->
      if value[sortKey] <= pivotItem[sortKey] then less.push(value) else more.push(value)
      return
    retVal = sortObjectsArray(less, sortKey).concat([ pivotItem ], sortObjectsArray(more, sortKey))
  else
    retVal = objectsArray
  retVal



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
      for job in jobList
        vm.update(job)
      m.endComputation()


    # Sort by toolname // TODO the sorting algorithm works (tested it with the same JSON data in a plain html file), the sorting here does not stabilize, though
    vm.alphaSort = () ->
      vm.list = sortObjectsArray(vm.list, 'toolname')

    # Sort by job id
    vm.numericSort = () ->
      vm.list = sortObjectsArray(vm.list, 'job_id')

      
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

      m('div', id: 'job_id', {style: {width: '33.5%', align: 'center'}}, m('a[href="/#/jobs/' + task.mainID() + '"]', task.job_id())),
      m('div', id: 'tool_name', {class: task.toolname()}, {style: {width: '33.5%', align: "center",}}, m('span[class=toolname]', task.toolname().substr(0,4))),

    ]
  ) ]]





m.mount(document.getElementById('jobtable'),  { controller: jobs.controller, view: jobs.view})









