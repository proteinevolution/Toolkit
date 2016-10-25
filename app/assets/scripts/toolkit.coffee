window.Toolkit =

  controller: (args)  ->
    job : JobModel.update(args, if args.isJob then m.route.param("mainID") else m.route.param("toolname"))

  view: (ctrl) -> [
    m "div", {class: "large-2 padded-column columns show-for-large", id: "sidebar"},
      m JobListComponent, {selected: ctrl.job().mainID}


    m "div", {id: "content", class: "large-9 small-10 columns"},
      m JobViewComponent, {job : ctrl.job}
  ]
