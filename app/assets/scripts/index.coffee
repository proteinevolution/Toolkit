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


window.Index =
  controller: ->

  view: ->
    m "div", {class: "small-12 large-12 columns"}, [

      m "section", {class: "slider show-for-medium", config: slickSlider}, [

          m "div", [
            m "img", {src: "/assets/images/lambda0.5_crop2.png"}
            m "a", {href: "https://elifesciences.org/content/4/e09410"},
              m "div", {class: "slide-caption"}, "This is a galaxy of folds"
            ]

          m "div", [
            m "img", {src: "/assets/images/protfromfragments3.png"}
            m "a", {href: "https://elifesciences.org/content/4/e09410"},
              m "div", {class: "slide-caption"}, "The seemingly limitless diversity of proteins in nature arose from only a few thousand domain prototypes, but the origin of these themselves has remained unclear. We are pursuing the hypothesis that they arose by fusion and accretion."
            ]
        ]

      m "div", {class: "grid", style: "margin-top: 355px;"},
        m "div", {class: "tool-finder show-for-medium row centered"},
          m "div", {class: "search-query large-12 medium-6"},
              m "input", {type: "text", id: "searchInput", name: "searchInput", value: "", placeholder: "Search Keywords"}
  ]


###
      <div data-disable-hover="false" class="trafficbar" id="trafficbar"></div>

    $('#trafficbar').attr('data-tooltip');
        $('#trafficbar').attr('title', 'Click to view last job: '+json.job_id+', status: '+status);
        $('#trafficbar' ).css({'cursor': 'pointer'});

        $('.trafficbar').on('click', function () {
            //console.log(JSON.stringify(jobs.vm.getLastJob()));
            window.location.href = "/#/jobs/"+json.mainID;

        });


        if (jobs.vm.getJobState(jobs.vm.getLastJob()) == 'done')
            $('#trafficbar').css({
            'background': 'green',
            'box-shadow': '0 0 10px darkgreen'
            });
        else if (jobs.vm.getJobState(jobs.vm.getLastJob()) == 'error')
            $('#trafficbar').css({
            'background': '#ff0000',
            'box-shadow': '0 0 10px #d2071d'
            });
        else if (jobs.vm.getJobState(jobs.vm.getLastJob()) == 'running')
            $('#trafficbar').css({
            'background': '#ffff00',
            'box-shadow': '0 0 10px #ffce27'
            });
###