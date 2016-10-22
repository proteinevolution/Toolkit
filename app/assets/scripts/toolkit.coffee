window.Toolkit =

  controller:  ->
    component = null
    section = m.route.param("section")
    if section == "jobs"
      component = m.component JobViewComponent, {isJob: true, mainID: m.route.param("argument")}
    else if section == "tools"
      component = m.component JobViewComponent, {isJob: false, toolname: m.route.param("argument")}
    component: component


  view: (ctrl) -> [
    m "div", {class: "large-2 padded-column columns show-for-large", id: "sidebar"},
      m.component JobListComponent

    m "div", {id: "content", class: "large-9 small-10 columns"}, ctrl.component
  ]



###

                            <div id="content" class="large-9 small-10 columns" style="float: left;">
                            </div>
                            -->
###