helpModalReveal = (elem, isInit) ->
  if not isInit
    elem.setAttribute "data-reveal", "data-reveal"
    $(elem).foundation()


helpModalTabs = (elem, isInit) ->
  if not isInit
    $(elem).tabs()


exampleContent =
  "psiblast" : [
    """
    Search with an amino acid sequence against protein databases for locally similar sequences.
    Similar to Protein BLAST+ but more sensitive. PSI-BLAST+ first performs a BLAST+ search and builds an alignment
    from the best local hits. This alignment is then used as a query for the next round of search.
    After each successive round the search alignment is updated.
    """
    "These are parameter specific information"
    "These are result descriptions"
    ]


helpContent = (tool, category)  ->
  if exampleContent[tool]
    exampleContent[tool][category]
  else
    ""

window.HelpModalComponent =

  view: (ctrl, args) ->
    overview = helpContent args.toolname, 0
    params = helpContent args.toolname, 1
    results = helpContent args.toolname, 2

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

