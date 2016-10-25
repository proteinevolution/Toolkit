FrontendTools =
  "alnviz": FrontendAlnvizComponent



window.Toolkit =

  controller: (args)  ->
    toolname = m.route.param("toolname")
    console.log "Requested Toolname was: #{toolname}"

    # Case that the requested tool is a Frontend tool
    if FrontendTools[toolname]
      console.log "Start Frontend tool"
      viewComponent = () -> FrontendTools[toolname]
      listComponent = () -> m JobListComponent, {selected: null}

    else
      console.log "Start backend tool"
      job = JobModel.update(args, if args.isJob then m.route.param("mainID") else m.route.param("toolname"))
      viewComponent = () -> m JobViewComponent, {job : job}
      listComponent = () -> m JobListComponent, {selected: job().mainID}
    viewComponent : viewComponent
    listComponent : listComponent


  view: (ctrl) -> [
    m "div", {class: "large-2 padded-column columns show-for-large", id: "sidebar"}, ctrl.listComponent()
    m "div", {id: "content", class: "large-9 small-10 columns"}, ctrl.viewComponent()
  ]
