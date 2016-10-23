tooltipSearch = (elem, isInit) ->
  if not isInit
    elem.setAttribute "data-tooltip", "data-tooltip"
    elem.setAttribute "aria-haspopup", "true"
    elem.setAttribute "data-disable-hover", "false"
    elem.setAttribute "title", "Search for job"

window.Toolkit =

  controller:  ->
    component = null
    section = m.route.param("section")
    if section == "jobs"
      component = m.component JobViewComponent, {isJob: true, mainID: m.route.param("argument")}
    else if section == "tools"
      component = m.component JobViewComponent, {isJob: false, toolname: m.route.param("argument")}
    component: component



  view: (ctrl) ->
    m "div", {class: "large-2 padded-column columns show-for-large", id: "sidebar"}, JobListComponent


###

                            <div id="content" class="large-9 small-10 columns" style="float: left;">
                            </div>
                            -->
      m "div", {id: "content", class: "large-9 small-10 columns"}, ctrl.component

TestComponent =
  controller: () ->

    select: (all) ->
      $('input:checkbox.sidebarCheck').each ->
        $(this).prop 'checked', if all then "checked" else ""

    delete: (fromServer) ->

      message = if fromServer then "Do you really want to delete the selected jobs permanently?"
      else "Do you really want to clear the selected jobs from the joblist?"
      ids = ($("input:checkbox.sidebarCheck:checked").map () -> $(this).val())
      if ids.length > 0 and confirm(message)
        for id in ids
          jobs.vm.delete(id, fromServer)

    sortToolname : -> jobs.vm.sortToolname()
    sortJobID: -> jobs.vm.sortJobID()

  view: (ctrl)  ->
    m "div", [
      m "div", {id: "joblist"}, [
        m "form", {id: "jobsearchform"},
          m "div", [
            m "input", {type: "text", placeholder: "Search by JobID", id: "jobsearch"}
            m "div", {id: "magnifier", class: "button", config: tooltipSearch},
              m "i", {class: "icon-magnifying", id: "iconsidebar" }
          ]

        m "div", {class: "button job-handle"}, [

          m "div", {class: "delete", onclick: ctrl.delete.bind(ctrl, true)}, "Delete"
          m "div", {onclick: ctrl.delete.bind(ctrl, false)} ,"Clear"
        ]

        m "div", {class: "button job-button"}, [

          m "div", [
            m "span",  {onclick: ctrl.select.bind(ctrl, true)}, "All"
            m "span", "/"
            m "span",  {onclick: ctrl.select.bind(ctrl, false)}, "None"
          ]
          m "div", {class: "idsort", onclick: ctrl.sortJobID}, "ID"
          m "div", {class: "toolsort", onclick: ctrl.sortToolname}, "Tool"
        ]
    ]
  ]
###