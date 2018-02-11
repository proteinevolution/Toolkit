/// <reference path="helper.ts"/>

const foundationConfig = function(elem: any, isInit: boolean): any {
    if (!isInit) {
        return $(elem).foundation();
    }
};

let bloodHoundConfig = {
    engine: new Bloodhound({
        remote: {
            url: "/suggest/%QUERY%",
            wildcard: "%QUERY%"
        },
        datumTokenizer: Bloodhound.tokenizers.whitespace("q"),
        queryTokenizer: Bloodhound.tokenizers.whitespace
    }),
    tools: new Bloodhound({
        datumTokenizer: Bloodhound.tokenizers.obj.whitespace("long"),
        queryTokenizer: Bloodhound.tokenizers.whitespace,
        prefetch: {
            url: "/getToolList"
        }
    })
};
bloodHoundConfig.tools.initialize();

const typeAhead = function(elem: any, isInit: boolean): any {
    if (!isInit) {
        return $("#" + elem.id + " .search-input").typeahead({
            highlight: true,
            minLength: 1,
            hint: true
        }, [
            {
                displayKey: "long",
                source: bloodHoundConfig.tools.ttAdapter(),
                templates: {
                    suggestion: function(data: any) {
                        if (data != null) {
                            return "<div class=\"list-group-item\"><a class=\"search-results\" data-link=\"/tools/" + data.short + "\" name=\"" + data.long + "\">" + data.long + "</a></div>";
                        } else {
                            return "<div style=\"display: none\"></div>";
                        }
                    },
                    header: "<h6 class=\"header-name\">Tools</h6>",
                    empty: [""]
                }
            }, {
                source: bloodHoundConfig.engine.ttAdapter(),
                name: "jobList",
                limit: 30,
                displayKey: "jobID",
                templates: {
                    //empty: '<div class="list-group search-results-dropdown"><div class="list-group-item-notfound">Nothing found.</div></div>',
                    suggestion: function(data: any) {
                        if (data != null) {
                            return "<div class=\"list-group-item\"><a class=\"search-results\" data-link=\"/jobs/" + data.jobID + "\" name=\"" + data.jobID + " - " + data.toolnameLong + "\">" +
                                "<span class=\"search-result-jobid\">" + data.jobID + "</span> <span class=\"search-result-tool\"> " +
                                "(" + data.toolnameLong + ")</span> <span class=\"search-result-tool-short\"> (" + data.tool.substr(0, 4).toUpperCase() + ")</span></a></div>";
                        } else {
                            return "";
                        }
                    },
                    header: "<h6 class=\"header-name\">Jobs</h6>",
                }
            }
        ]).on("typeahead:select", function(e: any, option: any): any {
            e.preventDefault();
            let selectables = $(this).siblings(".tt-menu").find(".tt-selectable").find(".search-results");
            selectables.each(function() {
                if ($(this).attr("name") === option.long) {
                    console.log(this);
                    m.route($(this).data("link"));
                }
            })
        }).on('focus', function(e : any) : any {
            $(this).siblings(".search-input.tt-hint").addClass("white");
        }).on("blur", function(): any {
            $(this).val("").siblings(".search-input.tt-hint").removeClass("white");
        });
    }
};

interface Window {
    SearchBarComponent: any;
}

window.SearchBarComponent = {
    controller: function(): any {
    },
    view: function(ctrl: any, args: any) {
        return m("div", {id: args.id, "class": "search-container", config: typeAhead},
            m("input", {
                "class": "search-input",
                type: "text",
                name: "q",
                placeholder: args.placeholder ? args.placeholder : "enter a job ID or a tool name"
            })
        );
    }
};

interface Window {
    Index: any;
}

window.Index = {
    controller: function(): any {
        return JobListComponent.selectedJobID = null;
    },
    view: function() {
        return m("div", {
            "class": "small-12 large-12 columns",
            config: fadesIn
        }, [
            m("section", {}, [
                m("div", {"class": "caption-container"}, [
                    m("div", {
                            "class": "slide-caption"
                        },
                        m("div", {"class": "slide-header"}, "Welcome to the Bioinformatics Toolkit"),
                        m("div", {"class": "slide-text"}, "of the Max Planck Institute for Developmental Biology, Tübingen, Germany.")
                    ),
                    m("div",
                        m("img", {
                            style: "width: 100%",
                            "data-interchange": "[/assets/images/Toolkit100.png, small]",
                            config: foundationConfig
                        })
                    ), m("a", {
                        href: "http://www.eb.tuebingen.mpg.de/"
                    })
                ])
            ]),
            m(trafficBarComponent), m(tilescomponent)
        ]);
    }
};

const trafficBarComponent = {
    controller: function(): any {
    },
    view: function() {
        return m("div", {
            "class": "grid"
        }, m("div", {
            "class": "tool-finder row centered"
        }, [
            m("div", {"class": "liveTableContainer"},
                m.component(LiveTable, {}),
                m("div",
                    m("div", {"class": "large-12 form-group"},
                        m.component(window.SearchBarComponent, {id: "index-search"})
                    ))
            )
        ]));
    }
};

// TODO add different type of tile (bigger one?)
const tilescomponent = {

    controller: function () {

    },
    view: function (ctrl : any) {
        return m("div", {
                "class": "row article_container small-up-1 medium-up-2 large-up-3",
                config: hideSidebar

            },
            m("div", {
                    "class": "column column-block tile-main-container"
                },
                m("div", {"class": "tile_container"},
                    m("div", {"class": "tile-img", 'style': {"background-image": "url(/assets/images/fold_galaxy.png)"}}
                    ),
                    m("div", {"class": "text_part"},
                        m("h5", "Recent Updates"),
                        m("a", {href: "http://scop.berkeley.edu/statistics/ver=2.07", target: "_blank"},
                            m("h6", "March 7, 2018"),
                            m("p", "HHpred: a new version of the SCOPe database,",
                                m("span", {style: "color:#2E8C81;"},
                                    m("em", " version 2.07")),
                                m("span", ", is now online."))
                        ),
                        m("a", {"data-open": "recentUpdatesModal", href: "#ecod70_2018_03"},
                            m("h6", "March 4, 2018"),
                            m("p", "HHpred: a new version of the ECOD database, version 20180219 (develop204), is now online.")
                        ),
                        m("a", {href: "https://www.sciencedirect.com/science/article/pii/S0022283617305879", target: "_blank"},
                            m("h6", "December 24, 2017"),
                            m("p", "Our paper on the new Toolkit is out: ",
                                m("em", "A Completely Reimplemented MPI Bioinformatics Toolkit with a New HHpred Server at its Core. "),
                                m("span", {style: "color:#2E8C81;"},
                                    m("em", "J Mol Biol. 2017 Dec 16.")))
                        )
                    ),
                    m("div", {"class": "quick-links"},
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
        )
    }
};
