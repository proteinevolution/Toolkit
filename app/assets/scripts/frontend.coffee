exampleSequence = """
>gi|33300828|ref|NP_877456#7 putative ATP-dependent DNA ligase [Bacteriophage phiKMV]
PEITVDGRIVGYVMGKTG-KNVGRVVGYRVELEDGSTVAATGLSEEHIQLLTCAYLNAHI
D---EAMPNYGRIVEVSAMERSAN-TLRHPSFSRFR
>gi|114796395|emb|CAK25951#9 putative ATP-dependent DNA ligase [Bacteriophage LKD16]
PSLAVEGIVVGFVMGKTG-ANVGKVVGYRVDLEDGTIVSATGLTRDRIEMLTTEAELLGG
A-DHPGMADLGRVVEVTAMERSAN-TLRHPKFSRFR
>gi|114796457|emb|CAK24995#5 putative DNA ligase [Bacteriophage LKA1]   E=4e-40 s/c=1.7
PGFEADGTVIDYVWGDPDKANANKIVGFRVRLEDGAEVNATGLTQDQMACYTQSYHATAY
EVGITQTIYIGRACRVSGMERTKDGSIRHPHFDGFR
>gi|29366706|ref|NP_813751#8 putative DNA ligase [Pseudomonas phage gh-1]   gi|29243565
PDDNEDGFIQDVIWGTKGLANEGKVIGFKVLLESGHVVNACKISRALMDEFTDTETRLPG
-------YYKGHTAKVTFMERYPDGSLRHPSFDSFR
>gi|68299729|ref|YP_249578#6 DNA ligase [Vibriophage VP4]   gi|66473268|gb|AAY46277.1|
PEGEIDGTVVGVNWGTVGLANEGKVIGFQVLLENGVVVDANGITQEQMEEYTNLVYKTGH
D-----DCFNGRPVQVKYMEKTPKGSLRHPSFQRWR
>gi|77118174|ref|YP_338096#3 ligase [Enterobacteria phage K1F]   gi|72527918|gb|AAZ7297
PSEEADGHVVRPVWGTEGLANEGMVIGFDVMLENGMEVSATNISRALMSEFTENVKSDP-
------DYYKGWACQITYMEETPDGSLRHPSFDQWR
>gi|17570796|ref|NP_523305#4 DNA ligase [Bacteriophage T3]   gi|118769|sp|P07717|DNLI_B
PECEADGIIQGVNWGTEGLANEGKVIGFSVLLETGRLVDANNISRALMDEFTSNVKAHGE
D------FYNGWACQVNYMEATPDGSLRHPSFEKFR
>gi|119637753|ref|YP_91898#2 DNA ligase [Yersinia phage Berlin]   gi|119391784|emb|CAJ
PECEADGIIQSVNWGTPGLSNEGLVIGFNVLLETGRHVAANNISQTLMEELTANAKEHGE
D------YYNGWACQVAYMEETSDGSLRHPSFVMFR
"""
window.FrontendAlnvizComponent =
  controller: ->
    pasteExample: () ->
      $('#alignment').val(exampleSequence)

    initMSA: ->
      seqs = $('#alignment').reformat('Fasta')
      if !seqs
        return
      opts =
        el: document.getElementById('yourDiv')
        vis:
          conserv: false
          overviewbox: false
          conserv: false
          overviewbox: true
          seqlogo: true
          labels: true
          labelName: true
          labelId: false
          labelPartition: false
          labelCheckbox: false
        menu: 'small'
      opts.seqs = fasta2json(seqs)
      alignment = new (msa.msa)(opts)
      # the menu is independent to the MSA container
      menuOpts = {}
      menuOpts.el = document.getElementById('menuDiv')
      menuOpts.msa = alignment
      defMenu = new (msa.menu.defaultmenu)(menuOpts)
      alignment.addView 'menu', defMenu
      alignment.render()

  view: (ctrl) ->
    m "div", {id: "jobview"}, [

      m "div", {class: "jobline"},
        m "span", {class: "toolname"}, "Alignment Viewer"

      m GeneralTabComponent, {tabs: ["Alignment", "Visualization"], ctrl: ctrl}
    ]

######################################################################################################################
# Reformat

# Mithril Configs for JobViewComponent
tabulated = (element, isInit) ->
  if not isInit then $(element).tabs()


fndt = (elem, isInit) ->
  if not isInit
    $(elem).foundation()

window.FrontendReformatComponent =
    controller: ->
      html: m.request {method: "GET", url: "/static/get/reformat", deserialize: (data) -> data}

    view: (ctrl) ->
      m "div", {id: "jobview", config: fndt},
        m.trust ctrl.html()



###

      $(document).foundation()
      $("html, body").animate({ scrollTop: 0 }, "fast")

window.FrontendReformatComponent =
  controller: ->

  view: (ctrl) ->
    m "div", {id: "jobview"}, [

      m "div", {class: "jobline"},
        m "span", {class: "toolname"}, "Reformat"

      m GeneralTabComponent, {tabs: ["Alignment", "Freqs"], ctrl: ctrl}
    ]
###

# Used to abstract over the tabulated view as it is used for all views
GeneralTabComponent =

  controller: ->

  view: (ctrl, args) ->
    m "div", {class: "tool-tabs", id: "tool-tabs", config: tabulated}, [

      # List of categories
      m "ul", args.tabs.map (tab) ->
        m "li", {id: "tab-#{tab}"}, m "a", {href: "#tabpanel-#{tab}"}, tab


      # The corresponding panels
      args.tabs.map (tab) ->
        m "div", {class: "tabs-panel", id: "tabpanel-#{tab}"}, tabsContents[tab](args.ctrl)
    ]





###
 m "div", {class: "tabs-panel", id: "tabpanel-#{paramGroup[0]}"}, [

###


tabsContents =
  "Alignment": (ctrl) ->
    m "div", {class: "parameter"}, [
      m ParameterAlignmentComponent, {options: [["fas", "FASTA"]], value: ""}
      m "button", {class: "secondary button", onclick: ctrl.initMSA, id: "viewAlignment"}, "View Alignment"
    ]

  "Visualization": (ctrl) ->
    m "div", [
      m "div", {id: "menuDiv"}
      m "div", {id: "yourDiv"}
    ]
  "Freqs": (ctrl) ->
    "Test"