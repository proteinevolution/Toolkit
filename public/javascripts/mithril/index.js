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
                        href: "https://elifesciences.org/content/4/e09410"
                    }, m("div", {
                        "class": "slide-caption"
                    }, "Max Planck Institute for Developmental Biology"))
                ]), m("div", [
                    m("img", {
                        src: "/assets/images/lambda0.5_crop2.png"
                    }), m("a", {
                        href: "https://elifesciences.org/content/4/e09410"
                    }, m("div", {
                        "class": "slide-caption"
                    }, "A galaxy of protein folds."))
                ]), m("div", [
                    m("img", {
                        src: "/assets/images/protfromfragments3.png"
                    }), m("a", {
                        href: "https://elifesciences.org/content/4/e09410"
                    }, m("div", {
                        "class": "slide-caption"
                    }, "Folded proteins from peptides."))
                ])
            ]), trafficBarComponent, styleComponent, tilescomponent
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
            }), m("div", {
                "class": "quick_container"
            }, [
                m("div", {
                    "class": "quick",
                    id: "search_quick"
                }, [m("p", "HHpred"), m("a", "Search")]), m("div", {
                    "class": "quick",
                    id: "search_quick"
                }, [m("p", "PSI-Blast"), m("a", "Search")]), m("div", {
                    "class": "quick",
                    id: "search_quick"
                }, [m("p", "Hmmer3"), m("a", "Search")]), m("div", {
                    "class": "quick",
                    id: "align_quick"
                }, [m("p", "T-Coffee"), m("a", "Alignment")]), m("div", {
                    "class": "quick",
                    id: "analy_quick"
                }, [m("p", "FRpred"), m("a", "Analysis")])
            ])
        ]));
    }
};

styleComponent = {
    view: function() {
        return m("style", "#jobsearchform { display: none;}");
    }
};


/*


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
 */

quickLinksComponent = {
    view: function() {
        return m("div", {
            "class": "quicklinks"
        });
    }
};

recentUpdatesComponent = {
    view: function() {
        return m("div", {
            "class": "recentUpdates"
        });
    }
};

tilescomponent = {
    model: function() {
        var getRecentArticlesRoute;
        getRecentArticlesRoute = jsRoutes.controllers.DataController.getRecentArticles(5);
        return {
            articles: m.request({
                url: getRecentArticlesRoute.url,
                method: getRecentArticlesRoute.method
            })
        };
    },
    controller: function() {
        var mod;
        mod = new tilescomponent.model;
        return {
            articles: mod.articles
        };
    },
    view: function(ctrl) {}
};
