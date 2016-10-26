helpModalReveal = (elem, isInit) ->
  if not isInit
    elem.setAttribute "data-reveal", "data-reveal"
    $(elem).foundation()


helpModalTabs = (elem, isInit) ->
  if not isInit
    $(elem).tabs()


exampleContent =
  "psiblast" : ["This is the General HelpPage", "These are parameter specific information", "These are result descriptions"]




window.HelpModalComponent =

  view: (ctrl, args) ->
    overview = exampleContent[args.toolname][0]
    params = exampleContent[args.toolname][1]
    results = exampleContent[args.toolname][2]

    m "div", {id: "help-#{args.toolname}", class: "reveal", config: helpModalReveal},
      m "div", {id: "help-tabs", config: helpModalTabs}, [
        m "ul", [

          m "li",
            m "a", {href: "#help-tabs1"}, "Overview"

          m "li",
            m "a", {href: "#help-tabs2"}, "Parameters"

          m "li",
            m "a", {href: "#help-tabs3"}, "Results"
        ]

        m "div", {id: "help-tabs1"}, overview
        m "div", {id: "help-tabs2"}, params
        m "div", {id: "help-tabs3"}, results
      ]

