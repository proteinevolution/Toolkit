var slickSlider, tilescomponent, trafficBarComponent, typeAhead;

slickSlider = function (elem, isInit) {
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
};

typeAhead = function (elem, isInit) {
    var engine;
    if (!isInit) {
        $('#searchInput').on('keyup', function(e) {
            var selectables = $('#searchInput').siblings(".tt-menu").find(".tt-selectable").find('.search-results');
            if(e.which == 13) {
                e.preventDefault();
                //find the selectable item under the input, if any:
                if (selectables.length > 0){
                    selectables[0].click();
                    return false;
                }
            }

        });
        // $('#searchInput').on('typeahead:asyncreceive', function(){
        //     var selectables = $('#searchInput').siblings(".tt-menu").find(".tt-selectable").find('.search-results');
        //     if(selectables.length > 0) {
        //         $('#searchInput').val(selectables[0].name)
        //     }
        // });
        engine = new Bloodhound({
            remote: {
                url: '/suggest/%QUERY%',
                wildcard: '%QUERY%'
            },
            datumTokenizer: Bloodhound.tokenizers.whitespace('q'),
            queryTokenizer: Bloodhound.tokenizers.whitespace
        });
        return $('.search_Input').typeahead({
            highlight: true,
            minLength: 1,
            hint: true
        }, {
            source: engine.ttAdapter(),
            name: 'jobList',
            limit: 30,
            displayKey: "jobID",
            templates: {
                empty: ['<div class="list-group search-results-dropdown"><div class="list-group-item-notfound">Nothing found.</div></div>'],
                suggestion: function (data) {
                    return '<div class="list-group-item"><a class="search-results" href="#/jobs/' + data.jobID + '" name="'+data.jobID+' - ' + data.toolnameLong+ '">'
                        + data.jobID + '<span class="search-result-tool"> - ' + data.toolnameLong + '</span></a></div>' ;
                }
            }
        })
    }
};

window.Index = {
    controller: function () {
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


trafficBarComponent = {
    view: function () {
        return m("div", {
            class: "grid"
        }, m("div", {
            class: "tool-finder row centered"
        }, [
            m("div", {class: "liveTableContainer"},
                m.component(LiveTable, {}),
                m("div", {class: "search_container"},
                    m("div", {
                        class: "columns large-12 form-group"
                    }, m("input", { class: "search_Input",
                        type: "text",
                        id: "searchInput",
                        name: "q",
                        placeholder: "Search Keywords",
                        config: typeAhead
                    }))
                )
            )
        ]));
    }
};

// TODO add different type of tile (bigger one?)
tilescomponent = {
    model: function () {
        var getRecentArticlesRoute = jsRoutes.controllers.DataController.getRecentArticles(2);
        return {
            articles: m.request({
                url: getRecentArticlesRoute.url,
                method: getRecentArticlesRoute.method
            })
        };
    },
    controller: function () {
        var mod = new tilescomponent.model;
        return {
            articles: mod.articles
        };
    },
    view: function (ctrl) {
        return m("div", {
                "class": "row article_container small-up-1 medium-up-2 large-up-3"
            },
            ctrl.articles().map(function (article) {
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




// ("div", {
//         "class": "column column-block article_tile"
//     },
//         m("div", {"class": "tile_content"},
//             m("div", {"class": "tile_img"
//                 },
//                 m("img", {"src": article.imagePath})
//             ),
//             m("div", {"class": "tile_title"
//                 },
//                 m("p", article.title)
//             ),
//             m("div", {"class": "tile_text"
//                 }, article.text
//             ),
//             m("div", {"class": "read_tile"},
//                 m("i", {"class": "icon-chevron_right"})
//             )
//         )
// )
//
