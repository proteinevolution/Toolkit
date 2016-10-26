helpModalReveal = (elem, isInit) ->
  if not isInit
    elem.setAttribute "data-reveal", "data-reveal"
    $(elem).foundation()


helpModalTabs = (elem, isInit) ->
  if not isInit
    $(elem).tabs()


parameterContent =
  "alignment": "Enter single protein sequence or protein sequence alignment and choose its format from the dropdown menu."
  "standarddb": "The selected databases are used by BLAST to perform the search"
  "matrix": "Select the Scoring Matrix used by the tool."
  "num_iter": "Select the number of iterations"

exampleContent =
  "psiblast" : [
    """
    Search with an amino acid sequence against protein databases for locally similar sequences.
    Similar to Protein BLAST+ but more sensitive. PSI-BLAST+ first performs a BLAST+ search and builds an alignment
    from the best local hits. This alignment is then used as a query for the next round of search.
    After each successive round the search alignment is updated.
    """
    ["alignment", "standarddb", "matrix", "num_iter"]
    """
    At the top of the results page is a "View alignment"-"button, that shows the multiple alignment of all hits.
    Then there is a list with brief information about all hits that were found.
    They are listed together with their scores and e-values.
    There you can select the ones, you want to work with. Below this you can find more information.
    There are all pairwise alignments and there is also additional information about the method,
    the identities and the positives. There you also have the possibility to select the hits, you want to work with.
    At the bottom you can find more information about the database and the matrix.
    """
  ]


helpContent = (tool)  ->
  if exampleContent[tool]
    exampleContent[tool]
  else
    ["",[],""]


accordion = (elem, isInit) ->
  if not isInit
    elem.setAttribute "data-accordion", "data-accordion"

accordionItem = (elem, isInit) ->
  if not isInit
    elem.setAttribute "data-accordion-item",  "data-accordion-item"

accordionContent = (elem, isInit) ->
  if not isInit
    elem.setAttribute "data-tab-content",  "data-tab-content"

window.HelpModalComponent =

  view: (ctrl, args) ->
    overview = helpContent(args.toolname)[0]
    params = helpContent(args.toolname)[1]
    results = helpContent(args.toolname)[2]

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
        m "div", {id: "help-tabs2"},
          m "ul", {class: "accordion", config: accordion}, params.map (param) ->
            m "li", {class: "accordion-item", config: accordionItem}, [
              m "a", {href: "#", class: "accordion-title"}, param
              m "div", {class: "accordion-content",  config: accordionContent}, parameterContent[param]
            ]

        m "div", {id: "help-tabs3"}, results
      ]

