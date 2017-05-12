let foundationConfig = function(elem : any, isInit : boolean) : any {
    if (!isInit) {
        return $(elem).foundation();
    }
};

/** Slick slider in frontend not in use
let slickSlider = function (elem : any, isInit : boolean) {
    if (!isInit) {
        return ($(elem).on("init", function () {
            return $(this).fadeIn(3000);
        })).slick({
            autoplay: false,
            autoplayspeed: 5000,
            speed: 2500,
            dots: false,
            arrows: false,
            fade: true,
            lazyLoad: "ondemand",
            cssEase: "ease-out",
            zIndex: "1",
            responsive: [
                {
                    breakpoint: 500,
                    settings: {
                        dots: false,
                        arrows: false,
                        infinite: false,
                        slidesToShow: 2,
                        slidesToScroll: 2
                    }
                }
            ]
        });
    }
}; */

let typeAhead = function (elem : any, isInit : boolean) : any {
    let engine;
    let tools;
    if (!isInit) {
        $('#searchInput').on('keyup', function(e : any) : any {
            let selectables = $('#searchInput').siblings(".tt-menu").find(".tt-selectable").find('.search-results');
            if (e.which == 13) {
                e.preventDefault();
                //find the selectable item under the input, if any:
                if (selectables.length > 0) {
                    selectables[0].click();
                    return false;
                }
            }
        });

        engine = new Bloodhound({
            remote: {
                url: '/suggest/%QUERY%',
                wildcard: '%QUERY%'
            },
            datumTokenizer: Bloodhound.tokenizers.whitespace('q'),
            queryTokenizer: Bloodhound.tokenizers.whitespace
        });
        tools = new Bloodhound({
            datumTokenizer: Bloodhound.tokenizers.obj.whitespace('long'),
            queryTokenizer: Bloodhound.tokenizers.whitespace,
            prefetch: {
                url: '/getToolList'
            }
        });
        tools.initialize();
        return $('.search_Input').typeahead({
            highlight: true,
            minLength: 1,
            autoselect: 'first'

        },[

            {
                displayKey: 'long',
                source: tools.ttAdapter(),
                templates: {
                    suggestion: function (data: any) {
                            return '<div class="list-group-item"><a class="search-results" href="#/tools/' + data.short + '" name="'+data.long+'">' + data.long + '</a></div>';
                    },
                    header: '<h6 class="header-name">Tools</h6>',
                }
            }
            ,
            {
            source: engine.ttAdapter(),
            name: 'jobList',
            limit: 30,
            displayKey: "jobID",
            templates: {
                empty: ['<div class="list-group search-results-dropdown"><div class="list-group-item-notfound">Nothing found.</div></div>'],
                suggestion: function (data : any) {
                    return '<div class="list-group-item"><a class="search-results" href="#/jobs/' + data.jobID + '" name="'+data.jobID+' - ' + data.toolnameLong+ '">'
                        + data.jobID + '<span class="search-result-tool"> - ' + data.toolnameLong + '</span></a></div>' ;
                },
                header: '<h6 class="header-name">Jobs</h6>',
            }
        }]);
    }
};

interface Window { Index: any; }

window.Index = {
    controller: function () : any {
        document.title = "Bioinformatics Toolkit";
        return JobListComponent.selectedJobID = null;
    },
    view: function () {
        return m("div", {
            "class": "small-12 large-12 columns",
            config: fadesIn
        }, [
            m("section", {

            }, [
                m("div", {"class": "captionContainer"}, [
                    m("div", {
                            "class": "slide-caption"
                        },
                        m("div", {"class": "slide-header"}, "Welcome to the Bioinformatics Toolkit"),
                        m("div", {"class": "slide-text"}, "of the Max Planck Institute for Developmental Biology, Tübingen, Germany.")
                    ),
                    m("div",
                        m("img", {style: "width: 100%",
                            'data-interchange': '[/assets/images/Toolkit100.png, large]',
                            config: foundationConfig
                        })
                    ), m("a", {
                        href: "http://www.eb.tuebingen.mpg.de/"
                    })
                ])
                // ]), m("div", [
                //     m("img", {
                //         'data-interchange': '[/assets/images/lambda0.5_crop2TwoSixth.png , small],' +
                //         ' [/assets/images/lambda0.5_crop2FourSixth.png, medium], [/assets/images/lambda0.5_crop2.png, large]',
                //         config: foundationConfig
                //     }), m("a", {
                //         href: "https://www.ncbi.nlm.nih.gov/pmc/articles/PMC2817847/"
                //     }, m("div", {
                //             "class": "slide-caption"
                //         },
                //         m("div", {"class": "slide-header"}, "A galaxy of protein folds"),
                //         m("div", {"class": "slide-text"}, "Although the diversity of proteins in nature may seem endless, it is in fact narrowly bounded. Proteins are far less polyphyletic than hitherto assumed and may have evolved from a rather small set of ancestral forms.")
                //     ))
                // ]), m("div", [
                //     m("img", {
                //         'data-interchange': '[/assets/images/protfromfragments3BackgroundTwoSixth.png , small],' +
                //         ' [/assets/images/protfromfragments3BackgroundFourSixth.png, medium], [/assets/images/protfromfragments3Background.png, large]',
                //         config: foundationConfig
                //     }), m("a", {
                //         href: "https://elifesciences.org/content/4/e09410"
                //     }, m("div", {
                //             "class": "slide-caption"
                //         },
                //         m("div", {"class": "slide-header"}, "Folded proteins from peptides."),
                //         m("div", {"class": "slide-text"}, "The first folded proteins may have arisen by repetition, recombination, and accretion from an ancestral set of peptides active as co-factors of an RNA world.")
                //     ))
                // ])
            ]),
            m(trafficBarComponent), m(tilescomponent)
        ]);
    }
};


let trafficBarComponent = {
    controller : function() : any {},
    view: function () {
        return m("div", {
            "class": "grid"
        }, m("div", {
            "class": "tool-finder row centered"
        }, [
            m("div", {"class": "liveTableContainer"},
                m.component(LiveTable, {}),
                m("div", {"class": "search_container"},
                    m("div", {
                        "class": "columns large-12 form-group"
                    }, m("input", { "class": "search_Input",
                        type: "text",
                        id: "searchInput",
                        name: "q",
                        placeholder: "enter a job ID or a tool name",
                        config: typeAhead
                    }))
                )
            )
        ]));
    }
};

// TODO add different type of tile (bigger one?)
let tilescomponent = {

    controller: function () {
        let getRecentArticlesRoute = jsRoutes.controllers.DataController.getRecentArticles(2);
        return {
            articles: m.request({
                url: getRecentArticlesRoute.url,
                method: getRecentArticlesRoute.method
            })
        };
    },
    view: function (ctrl : any) {
        return m("div", {
                "class": "row article_container small-up-1 medium-up-2 large-up-3"
            },
            ctrl.articles().map(function (article : any) { // TODO this javascript error existed before migration "Uncaught TypeError: Cannot read property 'map' of undefined"
                return m("div", {
                        "class": "column column-block tile_main_container"
                    },
                    m("div", {"class": "tile_container"},
                        m("div", {"class": "tile_left", 'style': {'background-image': 'url(' + article.imagePath + ')'}}
                        ),
                        m("div", {"class": "tile_right"},
                            m("div", {"class": "rightContainer"},
                                m("a", {href: article.link},article.title),
                                m("hr"),
                                m("p", article.text),
                                m("a", {href: article.link}, m("i", {"class": "fa  fa-angle-right fa-2x"}))
                            )
                        )
                    )
                )

            })
        )

    }
};


