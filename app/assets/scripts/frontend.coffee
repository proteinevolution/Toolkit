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

    submitted = false

    frontendSubmit: () ->
      if !submitted
        $.ajax
          url: '/api/frontendSubmit/Alnviz'
          type: 'POST'
          success: (result) ->
            console.log 'ok'
            submitted = true
            return
          error: (result) ->
            console.warn 'error'
            submitted = true
            return
      else
        return


    initMSA: ->
      seqs = $('#alignment').reformat('Fasta')
      width = $('#tool-tabs').width() - 180
      if !seqs
        return

      opts =
        colorscheme: {"scheme": "mae"}
        el: document.getElementById('yourDiv')
        vis:
          conserv: false
          overviewbox: false
          conserv: false
          overviewbox: false
          seqlogo: false
          labels: false
          labelName: true
          labelId: false
          labelPartition: false
          labelCheckbox: false
        menu: 'small'
      opts.seqs = fasta2json(seqs)

      opts.zoomer = {alignmentHeight: 600, alignmentWidth: width, labelNameLength: 165, labelWidth: 85,labelFontsize: "13px",labelIdLength: 75,   menuFontsize: "12px",menuMarginLeft: "2px", menuPadding: "0px 10px 0px 0px", menuItemFontsize: "14px", menuItemLineHeight: "14px", autoResize: true}



      opts.zoomer = {alignmentWidth: width}
      alignment = new (msa.msa)(opts)
      # the menu is independent to the MSA container
      menuOpts = {}
      menuOpts.el = document.getElementById('menuDiv')
      menuOpts.msa = alignment
      defMenu = new (msa.menu.defaultmenu)(menuOpts)
      alignment.addView 'menu', defMenu
      alignment.render()
      $('#tool-tabs').tabs 'option', 'active', $('#tool-tabs').tabs('option', 'active') + 1



    forwardTab: () ->
      $('#tool-tabs').tabs 'option', 'active', $('#tool-tabs').tabs('option', 'active') + 1


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


######################################################################################################################
# RetrieveSeq

window.FrontendRetrieveSeqComponent =

  model: (args) ->
    tabs: ["Input"]
    content: [
      m "div", {class: "parameter-panel"}, [
        m "textarea"
      ]
    ]
  controller: (args) ->
    this.model = new ParameterAlignmentComponent.model args


  view: (ctrl) ->
    renderTabs(ctrl.model.tabs, ctrl.model.content)


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



renderTabs = (tabs, content) ->

  m "div", {class: "tool-tabs", id: "tool-tabs", config: tabulated}, [

    m "ul", tabs.map (tab) ->
      m "li", {id: "tab-#{tab}"}, m "a", {href: "#tabpanel-#{tab}"}, tab

    tabs.map (tab, idx) ->
      m "div", {class: "tabs-panel", id: "tabpanel-#{tab}"},
        content[idx]
  ]


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
    m "div", {class: "parameter-panel"}, [
      m ParameterAlignmentComponent, {options: [["fas", "FASTA"]], value: ""}
      m "div", {class: "submitbuttons", onclick: ctrl.frontendSubmit},
        m "input", { type: "button", class: "success button small submitJob", value: "View Alignment", onclick: ctrl.initMSA, id: "viewAlignment"}
    ]

  "Visualization": (ctrl) ->
    m "div", [
      m "div", {id: "menuDiv"}
      m "div", {id: "yourDiv"}
    ]
  "Freqs": (ctrl) ->
    "Test"