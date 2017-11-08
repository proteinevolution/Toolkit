/// <reference path="helper.ts"/>

const foundationConfig = function(elem : any, isInit : boolean) : any {
    if (!isInit) {
        return $(elem).foundation();
    }
};

/** Slick slider in frontend not in use
const slickSlider = function (elem : any, isInit : boolean) {
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

let bloodHoundConfig = {
    engine : new Bloodhound({
        remote: {
            url: '/suggest/%QUERY%',
            wildcard: '%QUERY%'
        },
        datumTokenizer: Bloodhound.tokenizers.whitespace('q'),
        queryTokenizer: Bloodhound.tokenizers.whitespace
    }),
    tools : new Bloodhound({
        datumTokenizer: Bloodhound.tokenizers.obj.whitespace('long'),
        queryTokenizer: Bloodhound.tokenizers.whitespace,
        prefetch: {
            url: '/getToolList'
        }
    })
};
bloodHoundConfig.tools.initialize();

const typeAhead = function (elem : any, isInit : boolean) : any {
    if (!isInit) {
        console.log("Initializing Search on " + elem.id);
        return $('#'+elem.id +" .search-input").typeahead({
            highlight: true,
            minLength: 1,
            autoselect: 'first'

        },[
            {
                displayKey: 'long',
                source: bloodHoundConfig.tools.ttAdapter(),
                templates: {
                    suggestion: function (data: any) {
                        if(data !=null) {
                            //console.log(data.long);
                            return '<div class="list-group-item"><a class="search-results" href="#/tools/' + data.short + '" name="' + data.long + '">' + data.long + '</a></div>';
                        }else {
                            return '<div style="display: none"></div>';
                        }
                    },
                    header: '<h6 class="header-name">Tools</h6>',
                    empty: ['']
                }
            }, {
                source: bloodHoundConfig.engine.ttAdapter(),
                name: 'jobList',
                limit: 30,
                displayKey: "jobID",
                templates: {
                    empty: ['<div class="list-group search-results-dropdown"><div class="list-group-item-notfound">Nothing found.</div></div>'],
                    suggestion: function (data : any) {
                            if(data != null) {
                                return '<div class="list-group-item"><a class="search-results" href="#/jobs/' + data.jobID + '" name="' + data.jobID + ' - ' + data.toolnameLong + '">'
                                       + data.jobID + '<span class="search-result-tool"> - ' + data.toolnameLong + '</span></a></div>';
                            } else {
                                return ''
                            }
                    },
                    header: '<h6 class="header-name">Jobs</h6>',
                }
            }
        ]).on('keyup', function(e : any) : any {
            console.log("Working on element: "+ elem.id);
            let selectables = $('#'+elem.id).siblings(".tt-menu").find(".tt-selectable").find('.search-results');
            if (e.which == 13) {
                e.preventDefault();
                //find the selectable item under the input, if any:
                if (selectables.length > 0) {
                    selectables[0].click();
                    return false;
                }
            }
        });
    }
};

const searchBarComponent = {
    controller : function() : any {},
    view : function(ctrl : any, args : any) {
        return m("div", { id: args.area + "-search", "class": "search-container", config: typeAhead },
            m("input", {
                "class": "search-input",
                type: "text",
                name: "q",
                placeholder: "enter a job ID or a tool name"
            })
        )
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
                            'data-interchange': '[/assets/images/Toolkit100.png, small]',
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

const trafficBarComponent = {
    controller : function() : any {},
    view: function () {
        return m("div", {
            "class": "grid"
        }, m("div", {
            "class": "tool-finder row centered"
        }, [
            m("div", {"class": "liveTableContainer"},
                m.component(LiveTable, {}),
                m("div",
                m("div", { "class": "large-12 form-group"},
                    m.component(searchBarComponent, {area:"index"})
                ))
            )
        ]));
    }
};

// TODO add different type of tile (bigger one?)
const tilescomponent = {

    controller: function () {
        let getRecentArticlesRoute = jsRoutes.controllers.DataController.getRecentArticles(1);
        return {
            articles: m.request({
                url: getRecentArticlesRoute.url,
                method: getRecentArticlesRoute.method
            })
        };
    },
    view: function (ctrl : any) {
        return m("div", {
                "class": "row article_container small-up-1 medium-up-2 large-up-3",
                config: hideSidebar

            },
            ctrl.articles().map(function (article : any) { // TODO this javascript error existed before migration "Uncaught TypeError: Cannot read property 'map' of undefined"
                return m("div", {
                        "class": "column column-block tile_main_container"
                    },
                        m("div", {"class": "tile_container"},
                            m("div", {"class": "tile_img", 'style': {'background-image': 'url(/assets/images/fold_galaxy.png)'}}
                            ),
                            m("div", {"class": "text_part"},
                                m("h5", "Recent Updates"),
                                m("a", {"data-open": "recentUpdatesModal", href: "#uniclust_2017_10"},
                                    m("h6", "October 31, 2017"),
                                    m("p", "HHpred: the Uniclust30 DB is now available for query A3M generation.")
                                ),
                                m("a", {"data-open": "recentUpdatesModal", href: "#uniclust_2017_7"},
                                    m("h6", "September 29, 2017"),
                                    m("p", "HHblits: a new version of the Uniclust30 DB is online.")
                                ),
                                m("a", {"data-open": "recentUpdatesModal", href: "#fisc"},
                                    m("h6", "September 24, 2017"),
                                    m("p", "HHpred: DBs of ",
                                        m("em", "Fischerella muscicola"), ", ",
                                        m("em", "Frankia alni"), ", ",
                                        m("em", "Streptomyces scabiei"), " and ",
                                        m("em", "Thermus aquaticus"), " are online."
                                    )
                                ),
                                m("a", {"data-open": "recentUpdatesModal", href: "#aqui"},
                                    m("h6", "September 19, 2017"),
                                    m("p", "HHpred: DBs of ",
                                        m("em", "A. aeolicus"), ", ",
                                        m("em", "D. radiodurans"), ", ",
                                        m("em", "S. pombe"), " and ",
                                        m("em", "T. thermophilus"), " are online."
                                    )
                                )
                            ),
                            m("div", {"class": "quick_links"},
                                m("h5", "Quick Links"),
                                m("table",
                                    m("tr",
                                        m("td",
                                            m("a", {href: "/#/tools/hhpred"},
                                                m("a", "HHpred")
                                            )
                                        ),
                                        m("td",
                                            m("a", {href: "/#/tools/hhpred"},
                                                m("i", {"class": "fa fa-angle-right fa-2x", "id": "arrow-right"})
                                            )
                                        )
                                    ),
                                    m("tr",
                                        m("td",
                                            m("a", {href: "/#/tools/hhblits"},
                                                m("a", "HHblits")
                                            )
                                        ),
                                        m("td",
                                            m("a", {href: "/#/tools/hhblits"},
                                                m("i", {"class": "fa fa-angle-right fa-2x", "id": "arrow-right"})
                                            )
                                        )
                                    ),
                                    m("tr",
                                        m("td",
                                            m("a", {href: "/#/tools/hhrepid"},
                                                m("a", "HHrepID")
                                            )
                                        ),
                                        m("td",
                                            m("a", {href: "/#/tools/hhprepid"},
                                                m("i", {"class": "fa fa-angle-right fa-2x", "id": "arrow-right"})
                                            )
                                        )
                                    ),
                                    m("tr",
                                        m("td",
                                            m("a", {href: "/#/tools/psiblast"},
                                                m("a", "BLAST")
                                            )
                                        ),
                                        m("td",
                                            m("a", {href: "/#/tools/psiblast"},
                                                m("i", {"class": "fa fa-angle-right fa-2x", "id": "arrow-right"})
                                            )
                                        )
                                    ),
                                    m("tr",
                                        m("td",
                                            m("a", {href: "/#/tools/pcoils"},
                                                m("a", "PCOILS")
                                            )
                                        ),
                                        m("td",
                                            m("a", {href: "/#/tools/pcoils"},
                                                m("i", {"class": "fa fa-angle-right fa-2x", "id": "arrow-right"})
                                            )
                                        )
                                    ),
                                    m("tr",
                                        m("td",
                                            m("a", {href: "/#/tools/clans"},
                                                m("a", "CLANS")
                                            )
                                        ),
                                        m("td",
                                            m("a", {href: "/#/tools/clans"},
                                                m("i", {"class": "fa fa-angle-right fa-2x", "id": "arrow-right"})
                                            )
                                        )
                                    ),
                                    m("tr",
                                        m("td",
                                            m("a", {href: "/#/tools/mafft"},
                                                m("a", "MAFFT")
                                            )
                                        ),
                                        m("td",
                                            m("a", {href: "/#/tools/mafft"},
                                                m("i", {"class": "fa fa-angle-right fa-2x", "id": "arrow-right"})
                                            )
                                        )
                                    ),
                                    m("tr",
                                        m("td",
                                            m("a", {href: "/#/tools/quick2d"},
                                                m("a", "Quick2D")
                                            )
                                        ),
                                        m("td",
                                            m("a", {href: "/#/tools/quick2d"},
                                                m("i", {"class": "fa fa-angle-right fa-2x", "id": "arrow-right"})
                                            )
                                        )
                                    ),
                                    m("tr",
                                        m("td",
                                            m("a", {href: "/#/tools/mmseqs2"},
                                                m("a", "MMseqs2")
                                            )
                                        ),
                                        m("td",
                                            m("a", {href: "/#/tools/mmseqs2"},
                                                m("i", {"class": "fa fa-angle-right fa-2x", "id": "arrow-right"})
                                            )
                                        )
                                    )
                                )
                            )
                        )
                )

            })
        )

    }
};


