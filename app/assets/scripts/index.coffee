slickSlider = (elem, isInit) ->
  if not isInit
    ($(elem).on "init", () -> $(this).fadeIn(3000)).slick
      autoplay: true
      autoplayspeed : 5000
      speed: 2500
      dots: false
      fade: true
      lazyLoad: "ondemand"
      cssEase: "ease-out"
      zIndex: "1"
      responsive: [{
        breakpoint: 500,
        settings: {
          dots: false,
          arrows: false,
          infinite: false,
          slidesToShow: 2,
          slidesToScroll: 2
        }
      }]


typeAhead = (elem, isInit) ->
  if not isInit

    engine = new Bloodhound(
      remote:
        url: '/suggest/%QUERY%'
        wildcard: '%QUERY%'
      datumTokenizer: Bloodhound.tokenizers.whitespace('q')
      queryTokenizer: Bloodhound.tokenizers.whitespace)

    $(elem).typeahead {
        hint: true
        highlight: true
        minLength: 4
      },
      source: engine.ttAdapter()
      name: 'jobList'
      displayKey: "jobID",
      templates:
        empty: [ '<div class="list-group search-results-dropdown"><div class="list-group-item-notfound">Nothing found.</div></div>' ]
        #header: [ '<div class="list-group search-results-dropdown">Hello World</div>' ]
        suggestion: (data) ->
          console.log(data)
          '<div class="list-group-item"><a href="#/jobs/' + data.jobID + '">' + data.jobID + '</a> - ' + data.toolname + '</div>'



trafficbar = (elem, isInit) ->
  if not isInit
    elem.setAttribute "data-disable-hover", "false"
    elem.setAttribute "data-tooltip", "data-tooltip"
    elem.setAttribute "title", "Click to view last job: " + Job.lastUpdated()
    status = Job.lastUpdatedState()
    console.log "Traffic bar sees status " + status
    if status == -1
      console.log "Hide Trafficbar"
      #$(elem).hide()
    else if status == 5
      console.log "Traffic Bar goes to done"
      $(elem).css
        'background': 'green',
        'box-shadow': '0 0 10px darkgreen'

    else if status == 4
      console.log "Traffic Bar goes to error"
      $(elem).css
        'background': '#ff0000',
        'box-shadow': '0 0 10px #d2071d'

    else if status == 3
      console.log "Traffic Bar goes to running"
      $(elem).css
        'background': '#ffff00',
        'box-shadow': '0 0 10px #ffce27'



window.Index =

  controller: ->
    Job.selected(-1)

  view: ->
    m "div", {class: "small-12 large-12 columns"}, [

      m "section", {class: "slider show-for-medium", config: slickSlider}, [

          m "div", [
            m "img", {src: "/assets/images/institute.jpg"}
            m "a", {href: "https://elifesciences.org/content/4/e09410"},
              m "div", {class: "slide-caption"}, "Max Planck Institute for Developmental Biology"
          ]

          m "div", [
            m "img", {src: "/assets/images/lambda0.5_crop2.png"}
            m "a", {href: "https://elifesciences.org/content/4/e09410"},
              m "div", {class: "slide-caption"}, "A galaxy of protein folds."
            ]

          m "div", [
            m "img", {src: "/assets/images/protfromfragments3.png"}
            m "a", {href: "https://elifesciences.org/content/4/e09410"},
              m "div", {class: "slide-caption"}, "Folded proteins from peptides."
            ]
        ]

      trafficBarComponent
      styleComponent
      tilescomponent
  ]

searchField = (elem, isInit) ->
  if not isInit
    $("#searchInput").keyup (event) ->
      if event.keyCode == 13
        m.route("/tools/#{ $("#searchInput").val()}")


trafficBarComponent =

  view: ->
    m "div", {class: "grid", style: "margin-top: 355px;"},
      m "div", {class: "tool-finder show-for-medium row centered"},[
        m "div", {class: "search-query large-12 medium-6"},
          m "div", {class: "columns large-12 form-group"},
            m "input", {type: "text", id: "searchInput", name: "q", placeholder: "Search Keywords", config: typeAhead}
        m "div", {class: "trafficbar", id: "trafficbar", config: trafficbar, onclick: () -> m.route "/jobs/#{Job.lastUpdated()}"}
        m "div", {class: "quick_container"}, [
          m "div", {class: "quick", id: "search_quick"}, [
            m "p", "HHpred"
            m "a", "Search"
          ]
          m "div", {class: "quick", id: "search_quick"}, [
            m "p", "PSI-Blast"
            m "a", "Search"
          ]
          m "div", {class: "quick", id: "search_quick"}, [
            m "p", "Hmmer3"
            m "a", "Search"
          ]
          m "div", {class: "quick", id: "align_quick"}, [
            m "p", "T-Coffee"
            m "a", "Alignment"
          ]
          m "div", {class: "quick", id: "analy_quick"}, [
            m "p", "FRpred"
            m "a", "Analysis"
          ]
        ]
      ]



styleComponent =

  view: ->
    m "style", "#jobsearchform { display: none;}"
###


jobTickerComponent =
  view: ->
    m "div", {class: "jobTicker"},[
      m "table",[
        m "thead",[
          m "tr",[
            m "th","id"
            m "th","timestamp"
            ]
        ]
        m "tbody",[

        ]
      ]
    ]

###
quickLinksComponent =
  view: ->
    m "div", {class: "quicklinks"}


recentUpdatesComponent =
  view: ->
    m "div", {class: "recentUpdates"}


tilescomponent =

  model: ->
    getRecentArticlesRoute = jsRoutes.controllers.DataController.getRecentArticles(5);
    this.articles = m.request
      url: getRecentArticlesRoute.url,
      method: getRecentArticlesRoute.method
    this.articles.then (data) ->
      console.log(data)


  controller: ->
    mod = new tilescomponent.model
    articles: mod.articles



  ###view: (ctrl, args) ->
    ctrl.articles()###

  view: ->

    m "div", {class: "lazy-container"},[

      m "div", {class: "tile-row"}, [
          m "div", {class: "content-wrapper-half large-3 large-offset-2 columns"}, [
            m "div", {class: "image-wrapper"},
              m "img", { src: '/assets/images/EBGebaeude-04.tif'}
            m "div", {class: "text-wrapper-half"}, [
              m "hr", {class: "hr-index"}
              m "div", {class: "header-font"}, "Department of Protein Evolution, MPI for Developmental Biology"
              m "p", "The seemingly limitless diversity of proteins arose from only a few thousand domain prototypes."
              m "a", {href: "http://www.eb.tuebingen.mpg.de/research/departments/protein-evolution.html"},
                m "i", {class: "icon-index icon-chevron_right"}

            ]
          ]
          m "div", {class: "content-wrapper-half margin-tile large-5 columns"}, [
            m "div", {class: "image-wrapper"},
              m "img", { src: '/assets/images/bioj.jpg'}
            m "div", {class: "text-wrapper-half"}, [
              m "hr", {class: "hr-index"}
              m "div", {class: "header-font"}, "The MPI bioinformatics Toolkit"
              m "p", "is an open, interactive web service for protein bioinformatic analysis. It offers a wide array of interconnected, state-of-the-art bioinformatics tools to experts and non-experts alike, developed both externally (e.g. BLAST+, HMMER3, MUSCLE) and internally (e.g. HHpred, HHblits, PCOILS). "
              m "a", {href: "https://www.ncbi.nlm.nih.gov/pubmed/27131380"},
                m "i", {class: "icon-index icon-chevron_right"}
         ]
        ]
      ]
    ]





###m "div", {class: "row quicklinks"}, [
  m "div", {class: "columns search_quick"}, [
    m "p", "HHpred"
    m "a", "Search"
  ]
  m "div", {class: "columns search_quick"}, [
    m "p", "PSI-Blast"
    m "a", "Search"
  ]
  m "div", {class: "columns search_quick"}, [
    m "p", "Hmmer3"
    m "a", "Search"
  ]
  m "div", {class: "columns align_quick"}, [
    m "p", "T-Coffee"
    m "a", "Alignment"
  ]
  m "div", {class: "columns analy_quick"}, [
    m "p", "FRpred"
    m "a", "Analysis"
  ]
]###
