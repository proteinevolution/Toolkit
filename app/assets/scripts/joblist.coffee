###################################################################################################################3
tooltipSearch = (elem, isInit) ->
  if not isInit
    elem.setAttribute "data-tooltip", "data-tooltip"
    elem.setAttribute "aria-haspopup", "true"
    elem.setAttribute "data-disable-hover", "false"
    elem.setAttribute "title", "Search for job"

window.JobListComponent =
  controller: (args) ->
    # Select Job
    for job in jobs.vm.list
      job.selected(args.selected == job.mainID)
      if args.selected == job.mainID
        jobs.vm.lastStatus = job.state()
        jobs.vm.lastmainID = job.mainID
        jobs.vm.lastjobID = job.job_id()


    select: (all) ->
      $('input:checkbox.sidebarCheck').each ->
        $(this).prop 'checked', if all then "checked" else ""

    delete: (deleteCompletely, mainID) ->
      jobs.vm.remove(mainID)
      if(deleteCompletely) then mainID += "&deleteCompletely=true"
      m.request({url: "/jobs?mainIDs="+mainID.toString(), method: "DELETE"})

    sortToolname : -> jobs.vm.sortToolname()
    sortJobID: -> jobs.vm.sortJobID()

  view: (ctrl) ->
    m "div", {id: "joblist"}, [
      m "form", {id: "jobsearchform"},
        m "div", [
          m "input", {type: "text", placeholder: "Search by JobID", id: "jobsearch"}
          m "div", {id: "magnifier", class: "button", config: tooltipSearch},
            m "i", {class: "icon-magnifying", id: "iconsidebar" }
        ]

      m "div", {class: "button job-button"}, [
        m "div", {class: "idsort", onclick: ctrl.sortJobID}, "ID"
        m "div", {class: "toolsort", onclick: ctrl.sortToolname}, "Tool"
      ]

      m "div", jobs.vm.list.map (job) ->
        m "div", {class: "job #{a[job.state()]}".concat(if job.selected() then " selected" else "")},  [

          m "div", {class: "jobid"},  m 'a[href="/#/jobs/' + job.mainID + '"]', job.job_id()
          m "span", {class: "toolname"}, job.toolname.substr(0,4)
          m "a", {class: "boxclose", onclick: ctrl.delete.bind(ctrl, false, job.mainID)}
        ]
    ]




###
  <div class="button job-handle" style="display: flex;">
            <div style=" border-right: 1px solid lightgray;" onclick="deleteIDs();" data-tooltip title="Delete selected jobs from database">Delete</div>
            <div onclick="clearIDs();" data-tooltip title="Clear selected jobs from joblist">Clear</div>

        </div>

  a[href="/#/jobs/' + job.mainID + '"]
###



