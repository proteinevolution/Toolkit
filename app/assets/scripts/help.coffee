helpModalReveal = (elem, isInit) ->
  if not isInit
    elem.setAttribute "data-reveal", "data-reveal"
    $(elem).foundation()


helpModalTabs = (elem, isInit) ->
  if not isInit
    $(elem).tabs()


parameterContent =
  "Alignment": "Enter single protein sequence or protein sequence alignment and choose its format from the dropdown menu."
  "Database": "The selected databases are used by BLAST to perform the search. <br />
      - <b>nr:</b> the non-redundant sequence database at the NCBI, updated weekly. (See NCBI's BLAST tutorial). <br />
      - <b>env:</b> NCBI's env database with environnmental sequences, usually from unknown organisms. <br />
      - <b>nre:</b> nr+env database. Updated weekly. <br />
      - <b>nr90, nr70, env90 etc.:</b> representative sequences, filtered for a maximum pairwise sequence identity of ~90% or 70% with kClust. Updated bimonthly. <br />
      - <b>nr_euk, nr_bac, nr_arc, nr_pro, nr_vir:</b> eukaryotic, bacterial, archaeal, prokaryotic, and viral sequences from the non-redundant database nr. Updated weekly. <br />
      - <b>uniprot_sprot:</b> http://www.expasy.org/sprot/ Updated weekly. <br />
      - <b>uniprot_trembl:</b> http://www.ebi.ac.uk/swissprot/ Updated weekly. <br />
      - <b>pdb_nr: Sequences</b> from proteins whose structures have been deposited in the Protein Data Bank PDB. This database uses the more complete SEQRES records of the PDB files (not the ATOM records that contain the atomic coordinates). This is the database used to construct the HHpred PDB alignment database. Updated weekly from the PDB. <br /> "
  "Matrix": "Specify an alternate scoring matrix for PSIBLAST."
  "Number of Iterations": "Specify the maximum number of rounds of search. After each successive round the search alignment is updated. "
  "E-Value": "The statistical significance threshold for reporting matches against database sequences; the default value is 10, meaning that 10 matches are expected to be found merely by chance, according to the stochastic model of Karlin and Altschul (1990). If the statistical significance ascribed to a match is greater than the e-value threshold, the match will not be reported. Lower e-value thresholds are more stringent, leading to fewer chance matches being reported. Increasing the threshold shows less stringent matches. Fractional values are acceptable."
  "E-value inclusion threshold": "The statistical significance threshold for including a sequence in the model used by PSI-BLAST to create the PSSM on the next iteration."
  "Filter (low complexity)": "Mask off segments of the query sequence that have low compositional complexity, as determined by the SEG program of Wootton & Federhen (Computers and Chemistry, 1993) or, for BLASTN, by the DUST program of Tatusov and Lipman (in preparation). Filtering can eliminate statistically significant but biologically uninteresting reports from the blast output (e.g., hits against common acidic-, basic- or proline-rich regions), leaving the more biologically interesting regions of the query sequence available for specific matching against database sequences.
Filtering is only applied to the query sequence (or its translation products), not to database sequences. Default filtering is DUST for BLASTN, SEG for other programs.
It is not unusual for nothing at all to be masked by SEG, when applied to sequences in SWISS-PROT, so filtering should not be expected to always yield an effect. Furthermore, in some cases, sequences are masked in their entirety, indicating that the statistical significance of any matches reported against the unfiltered query sequence should be suspect."
  "Compute Smith-Waterman alignment": "Compute locally optimal Smith-Waterman alignments."
  "Use nr70 for all but last iteration": " If this option is selected the programm will use the nr70-database for all but last round. In the last round, the database you selected in the database section will be used."
  "Alignments and descriptions": "Restricts the number of short descriptions of matching sequences reported to the number specified; default limit is 100 descriptions. Restricts also database sequences to the number specified for which high-scoring segment pairs (HSPs) are reported. If more database sequences than this happen to satisfy the statistical significance threshold for reporting, only the matches ascribed the greatest statistical significance are reported."

exampleContent =
  "psiblast" : [
    """
    Search with an amino acid sequence against protein databases for locally similar sequences.
    Similar to Protein BLAST+ but more sensitive. PSI-BLAST+ first performs a BLAST+ search and builds an alignment
    from the best local hits. This alignment is then used as a query for the next round of search.
    After each successive round the search alignment is updated.
    """
    ["Alignment", "Database", "Matrix", "Number of Iterations", "E-Value", "E-value inclusion threshold", "Filter (low complexity)",
      "Compute Smith-Waterman alignment", "Use nr70 for all but last iteration", "Alignments and descriptions"]
    """
    At the top of the results page is a "View alignment"-"button, that shows the multiple alignment of all hits.
    Then there is a list with brief information about all hits that were found.
    They are listed together with their scores and e-values.
    There you can select the ones, you want to work with. Below this you can find more information.
    There are all pairwise alignments and there is also additional information about the method,
    the identities and the positives. There you also have the possibility to select the hits, you want to work with.
    At the bottom you can find more information about the database and the matrix.
    The "View alignment"-button shows the multiple alignment of all sequences that was found by PSI-BLAST. The different colours illustrate the identities between the different amino acids. This helps you to decide, whether the found sequences may be really homologue.
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

           m "li", {class: "toolname" },
             m "a", {href: "#", class: "not-active"}, args.toolnameLong
        ]

        m "div", {id: "help-tabs1"},
          m "div", overview
        m "div", {id: "help-tabs2"},
            m "ul", {class: "accordion", config: accordion}, params.map (param) ->
              m "li", {class: "accordion-item", config: accordionItem}, [
                m "a", {href: "#", class: "accordion-title"}, param
                m "div", {class: "accordion-content",  config: accordionContent}, m.trust(parameterContent[param])
              ]

        m "div", {id: "help-tabs3"},
              m "div", results
              m("img", { src: '/assets/images/psiblast.png'
              , style: { marginTop: '2em' } })

      ]

