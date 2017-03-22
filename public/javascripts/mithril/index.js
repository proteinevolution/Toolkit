var slickSlider, tilescomponent, trafficBarComponent, trafficbar, typeAhead, sendMessage;

slickSlider = function (elem, isInit) {
    if (!isInit) {
        return ($(elem).on("init", function () {
            return $(this).fadeIn(3000);
        })).slick({
            autoplay: true,
            autoplayspeed: 5000,
            speed: 2500,
            dots: false,
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
        engine = new Bloodhound({
            remote: {
                url: '/suggest/%QUERY%',
                wildcard: '%QUERY%'
            },
            datumTokenizer: Bloodhound.tokenizers.whitespace('q'),
            queryTokenizer: Bloodhound.tokenizers.whitespace
        });
        return $(elem).typeahead({
            hint: true,
            highlight: true,
            minLength: 4
        }, {
            source: engine.ttAdapter(),
            name: 'jobList',
            displayKey: "jobID",
            templates: {
                empty: ['<div class="list-group search-results-dropdown"><div class="list-group-item-notfound">Nothing found.</div></div>'],
                suggestion: function (data) {
                    console.log(data);
                    return '<div class="list-group-item"><a href="#/jobs/' + data.jobID + '">' + data.jobID + '</a> - ' + data.toolname + '</div>';
                }
            }
        });
    }
};

trafficbar = function (elem, isInit) {
    var status;

        elem.setAttribute("data-disable-hover", "false");
        elem.setAttribute("data-tooltip", "data-tooltip");
        elem.setAttribute("title", "Click to view last job: " + Job.lastUpdated);
        status = Job.lastUpdatedState;
        console.log("Traffic bar sees status " + status);
        if (status === -1) {
            return console.log("Hide Trafficbar");
        } else if (status === 2) {
            console.log("Traffic Bar goes to queued");
            return $(elem).css({
                'background': '#c0b5bf',
                'box-shadow': '0 1 6px #9192af'
            });
        } else if (status === 5) {
            console.log("Traffic Bar goes to done");
            return $(elem).css({
                'background': 'green',
                'box-shadow': '0 1 6px #C3FFC3'
            });
        } else if (status === 4) {
            console.log("Traffic Bar goes to error");
            return $(elem).css({
                'background': '#ff0000',
                'box-shadow': '0 1 6px #FFC5C5'
            });
        } else if (status === 3) {
            console.log("Traffic Bar goes to running");
            return $(elem).css({
                'background': '#ffff00',
                'box-shadow': '0 1 6px #FFF666'
            });
        }
};

window.Index = {
    controller: function () {
        document.title = "Bioinformatics Toolkit";
        return Job.selected = -1;
    },
    view: function () {
        return m("div", {
            "class": "small-12 large-12 columns",
            config: fadesIn
        }, [
            m("section", {
                "class": "slider",
                config: slickSlider
            }, [
                m("div", [
                    m("img", {
                        'data-interchange': '[/assets/images/institute_small.jpg , small], ' +
                        '[/assets/images/institute_small.jpg, medium], [/assets/images/institute.jpg, large]',
                        config: foundationConfig
                    }), m("a", {
                        href: "http://www.eb.tuebingen.mpg.de/"
                    }, m("div", {
                            "class": "slide-caption"
                        },
                        m("div", {"class": "slide-header"}, "Max Planck Institute for Developmental Biology"),
                        m("div", {"class": "slide-text"}, "Welcome to the Bioinformatics Toolkit hosted at the Max Planck Institute for Developmental Biology in Tübingen, Germany.")
                    ))
                ]), m("div", [
                    m("img", {
                        'data-interchange': '[/assets/images/lambda0.5_crop2_small.png , small],' +
                        ' [/assets/images/lambda0.5_crop2_small.png, medium], [/assets/images/lambda0.5_crop2.png, large]',
                        config: foundationConfig
                    }), m("a", {
                        href: "https://www.ncbi.nlm.nih.gov/pmc/articles/PMC2817847/"
                    }, m("div", {
                            "class": "slide-caption"
                        },
                        m("div", {"class": "slide-header"}, "A galaxy of protein folds"),
                        m("div", {"class": "slide-text"}, "Although the diversity of proteins in nature may seem endless, it is in fact narrowly bounded. Proteins are far less polyphyletic than hitherto assumed and may have evolved from a rather small set of ancestral forms.")
                    ))
                ]), m("div", [
                    m("img", {
                        'data-interchange': '[/assets/images/protfromfragments3_small.png , small],' +
                        ' [/assets/images/protfromfragments3_small.png, medium], [/assets/images/protfromfragments3.png, large]',
                        config: foundationConfig
                    }), m("a", {
                        href: "https://elifesciences.org/content/4/e09410"
                    }, m("div", {
                            "class": "slide-caption"
                        },
                        m("div", {"class": "slide-header"}, "Folded proteins from peptides."),
                        m("div", {"class": "slide-text"}, "The first folded proteins may have arisen by repetition, recombination, and accretion from an ancestral set of peptides active as co-factors of an RNA world.")
                    ))
                ])
            ]),
            m(trafficBarComponent), m(tilescomponent)
        ]);
    }
};


trafficBarComponent = {
    view: function () {
        return m("div", {
            "class": "grid"
        }, m("div", {
            "class": "tool-finder row centered"
        }, [
            m("div", {"class": "news"},
                m("div", {"class": "news_container"},
                m("p", {"class": "news_header"}, "Social Network"),
                m("p", {"class": "news_date"}, "February 19, 2017"),
                m("p", {"class": "news_feed"}, m('a[href="/#/news/"]', "News Feed")),
                m("p", {"class": "news_content"}, "Follow us on Facebook and Twitter.")
                ), m(LiveTable),
                m("div", {"class": "search_container"},
                    m("div", {
                        "class": "columns large-12 form-group"
                    }, m("input", { "class": "search_Input",
                        type: "text",
                        id: "searchInput",
                        name: "q",
                        placeholder: "Search Keywords",
                        config: typeAhead
                    }))
                ),
                m("div", {
                    "class": "trafficbar",
                    id: "trafficbar",
                    config: trafficbar,
                    onclick: function () {
                        return m.route("/jobs/" + Job.lastUpdated);
                    }
                })
            )
        ]));
    }
};

// TODO add different type of tile (bigger one?)
tilescomponent = {
    model: function () {
        var getRecentArticlesRoute = jsRoutes.controllers.DataController.getRecentArticles(5);
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
                            m("a", article.title),
                            m("p", article.text)
                        )
                    )
                )

            })
        )

    }
};

window.sendMessage = function (object) {};


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
