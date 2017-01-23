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
          m "span", {class: "bar"}
        ]

      m "div", {class: "job-button"}, [
        m "div", {class: "idsort textcenter", onclick: Job.sortToolname}, "ID"
        m "div", {class: "toolsort textcenter", onclick: Job.sortJobID}, "Tool"
      ]

      m "div",{id: "joblistbottom"}, args.jobs().map (job, idx) ->
        m "div", {class: "job #{a[job.state()]}".concat(if job.jobID() == args.selected() then " selected" else "")}, [
          m "div", {class: "jobid"},  m 'a[href="/#/jobs/' + job.jobID() + '"]', job.jobID()
          m "span", {class: "toolname"}, job.toolname.substr(0,4).toUpperCase()
          m "a", {class: "boxclose", onclick: args.clear.bind(ctrl, idx)}
        ]
      m "div", {class: "notesheader"}, [
        "Notes"
      ]
      m "div", {class: "jobnotes", id: "currentJobID"}, [
        m "textarea", {placeholder: "Type private notes here", spellcheck: false}
      ]

    ]
