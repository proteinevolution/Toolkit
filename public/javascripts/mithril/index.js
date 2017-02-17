var quickLinksComponent, recentUpdatesComponent, searchField, slickSlider, styleComponent, tilescomponent, trafficBarComponent, trafficbar, typeAhead;

slickSlider = function(elem, isInit) {
    if (!isInit) {
        return ($(elem).on("init", function() {
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

typeAhead = function(elem, isInit) {
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
                suggestion: function(data) {
                    console.log(data);
                    return '<div class="list-group-item"><a href="#/jobs/' + data.jobID + '">' + data.jobID + '</a> - ' + data.toolname + '</div>';
                }
            }
        });
    }
};

trafficbar = function(elem, isInit) {
    var status;
    if (!isInit) {
        elem.setAttribute("data-disable-hover", "false");
        elem.setAttribute("data-tooltip", "data-tooltip");
        elem.setAttribute("title", "Click to view last job: " + Job.lastUpdated);
        status = Job.lastUpdatedState;
        console.log("Traffic bar sees status " + status);
        if (status === -1) {
            return console.log("Hide Trafficbar");
        } else if (status === 5) {
            console.log("Traffic Bar goes to done");
            return $(elem).css({
                'background': 'green',
                'box-shadow': '0 0 10px darkgreen'
            });
        } else if (status === 4) {
            console.log("Traffic Bar goes to error");
            return $(elem).css({
                'background': '#ff0000',
                'box-shadow': '0 0 10px #d2071d'
            });
        } else if (status === 3) {
            console.log("Traffic Bar goes to running");
            return $(elem).css({
                'background': '#ffff00',
                'box-shadow': '0 0 10px #ffce27'
            });
        }
    }
};

window.Index = {
    controller: function() {
        return Job.selected = -1;
    },
    view: function() {
        return m("div", {
            "class": "small-12 large-12 columns"
        }, [
            m("section", {
                "class": "slider show-for-medium",
                config: slickSlider
            }, [
                m("div", [
                    m("img", {
                        src: "/assets/images/institute.jpg"
                    }), m("a", {
                        href: "http://www.eb.tuebingen.mpg.de/"
                    }, m("div", {
                        "class": "slide-caption"},
                       m("div", {"class": "slide-header"}, "Max Planck Institute for Developmental Biology"),
                       m("div", {"class": "slide-text"}, "Welcome to the Bioinformatics Toolkit hosted at the Max Planck Institute for Developmental Biology in Tuebingen, Germany.")
                    ))
                ]), m("div", [
                    m("img", {
                        src: "/assets/images/lambda0.5_crop2.png"
                    }), m("a", {
                        href: "https://www.ncbi.nlm.nih.gov/pmc/articles/PMC2817847/"
                    }, m("div", {
                            "class": "slide-caption"},
                        m("div", {"class": "slide-header"}, "A galaxy of protein folds"),
                        m("div", {"class": "slide-text"}, "Although the diversity of proteins in nature may seem endless, it is in fact narrowly bounded. Proteins are far less polyphyletic than hitherto assumed and may have evolved from a rather small set of ancestral forms.")
                    ))
                ]), m("div", [
                    m("img", {
                        src: "/assets/images/protfromfragments3.png"
                    }), m("a", {
                        href: "https://elifesciences.org/content/4/e09410"
                    }, m("div", {
                            "class": "slide-caption"},
                        m("div", {"class": "slide-header"}, "Folded proteins from peptides."),
                        m("div", {"class": "slide-text"}, "The first folded proteins may have arisen by repetition, recombination, and accretion from an ancestral set of peptides active as co-factors of an RNA world.")
                    ))
                ])
            ]),
            m(trafficBarComponent), m(styleComponent), m(tilescomponent)
        ]);
    }
};

searchField = function(elem, isInit) {
    if (!isInit) {
        return $("#searchInput").keyup(function(event) {
            if (event.keyCode === 13) {
                return m.route("/tools/" + ($("#searchInput").val()));
            }
        });
    }
};

trafficBarComponent = {
    view: function() {
        return m("div", {
            "class": "grid",
            style: "margin-top: 355px;"
        }, m("div", {
            "class": "tool-finder show-for-medium row centered"
        }, [
            m("div", {
                "class": "search-query large-12 medium-6"
            }, m("div", {
                "class": "columns large-12 form-group"
            }, m("input", {
                type: "text",
                id: "searchInput",
                name: "q",
                placeholder: "Search Keywords",
                config: typeAhead
            }))), m("div", {
                "class": "trafficbar",
                id: "trafficbar",
                config: trafficbar,
                onclick: function() {
                    return m.route("/jobs/" + Job.lastUpdated);
                }
            })
        ]));
    }
};

styleComponent = {
    view: function() {
        return m("style", "#jobsearchform { display: none;}");
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
                        "class": "column column-block article_tile"
                    },
                    m("div", {"class": "tile_content"},
                        m("div", {"class": "tile_img"
                        },
                            m("img", {"src": article.imagePath})
                        ),
                        m("div", {"class": "tile_title"
                            },
                            m("p", article.title)
                        ),
                        m("div", {"class": "tile_text"
                        }, article.text
                        ),
                        m("div", {"class": "read_tile"},
                            m("i", {"class": "icon-chevron_right"})
                        )
                    )
                )
            })
        )
    }
};

