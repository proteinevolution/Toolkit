###################################################################################################################3
tooltipSearch = (elem, isInit) ->
  if not isInit
    elem.setAttribute "data-tooltip", "data-tooltip"
    elem.setAttribute "aria-haspopup", "true"
    elem.setAttribute "data-disable-hover", "false"
    elem.setAttribute "title", "Search for job"


jobNoteArea = (elem, isInit) ->
  if not isInit


    $.ajax
      url: '/api/jobs/getnotes/' + $(elem).attr('id').substring(7)
      type: 'get'
      success: (data) ->
        #console.log(data)
        $(elem).html(data)
        return


    $(elem).keyup (e) ->
      #console.log($(this).val())
      contentString = $(this).val()
      #console.log($(this).attr('id').substring(7))
      $.post jsRoutes.controllers.Jobs.annotation($(this).attr('id').substring(7), contentString), (response) ->
      # Log the response to the console
        console.log 'Response: ' + response
        return

      return



window.JobListComponent =


  controller: ->

    #user: m.request {method: "GET", url: "/getCurrentUser", success: (data) -> data.user}
    user: m.prop ""

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
        if ctrl.user() then [ m "div", {class: "notesheader"}, "Notes" ] else []


        if ctrl.user() then [ m "div", {class: "jobnotes"}, [
          m "textarea", {id: "notepad"+args.selected(), placeholder: "Type private notes here", spellcheck: false, config: jobNoteArea}
        ] ] else []
      ]
