###################################################################################################################3
tooltipSearch = (elem, isInit) ->
  if not isInit
    elem.setAttribute "data-tooltip", "data-tooltip"
    elem.setAttribute "aria-haspopup", "true"
    elem.setAttribute "data-disable-hover", "false"
    elem.setAttribute "title", "Search for job"

window.JobListComponent =

  view: (ctrl, args) ->

    m "div", {id: "joblist"}, [
      m "form", {id: "jobsearchform"},
        m "div", [
          m "input", {type: "text", placeholder: "Search by JobID", id: "jobsearch"}
          m "div", {id: "magnifier", class: "button", config: tooltipSearch},
            m "i", {class: "icon-magnifying", id: "iconsidebar" }
        ]

      m "div", {class: "button job-button"}, [
        m "div", {class: "idsort", onclick: Job.sortToolname}, "ID"
        m "div", {class: "toolsort", onclick: Job.sortJobID}, "Tool"
      ]

      m "div", args.jobs().map (job, idx) ->
        m "div", {class: "job #{a[job.state()]}".concat(if job.mainID == args.selected() then " selected" else "")}, [
          m "div", {class: "jobid"},  m 'a[href="/#/jobs/' + job.mainID + '"]', job.jobID()
          m "span", {class: "toolname"}, job.toolname.substr(0,4).toUpperCase()
          m "a", {class: "boxclose", onclick: args.clear.bind(ctrl, idx)}
        ]
    ]




###

      delete: (deleteCompletely, mainID) ->
      jobs.vm.remove(mainID)
      if(deleteCompletely) then mainID += "&deleteCompletely=true"
      m.request({url: "/jobs?mainIDs="+mainID.toString(), method: "DELETE"})

  <div class="button job-handle" style="display: flex;">
            <div style=" border-right: 1px solid lightgray;" onclick="deleteIDs();" data-tooltip title="Delete selected jobs from database">Delete</div>
            <div onclick="clearIDs();" data-tooltip title="Clear selected jobs from joblist">Clear</div>

        </div>

  a[href="/#/jobs/' + job.mainID + '"]
###



