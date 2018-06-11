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
            url: "/tool/list"
        }
    })
};
bloodHoundConfig.tools.initialize();

const typeAhead = function(elem: any, isInit: boolean): any {
    if (!isInit) {
        return $("#" + elem.id + " .search-input").typeahead({
            highlight: true,
            minLength: 1,
            autoselect: "first"
        }, [
            {
                displayKey: "long",
                source: bloodHoundConfig.tools.ttAdapter(),
                templates: {
                    suggestion: function(data: any) {
                        if (data != null) {
                            return "<div class=\"list-group-item\"><a class=\"search-results\" data-typeahead-id='"+ data.long +"' data-link=\"/tools/" + data.short + "\" name=\"" + data.long + "\">" + data.long + "</a></div>";
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
                            return "<div class=\"list-group-item\"><a class=\"search-results\" data-typeahead-id='"+ data.jobID +"' data-link=\"/jobs/" + data.jobID + "\" name=\"" + data.jobID + " - " + data.toolnameLong + "\">" +
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
                let typeAheadId = $(this).data("typeahead-id");
                if (typeAheadId == option.long || typeAheadId == option.jobID) {
                    m.route($(this).data("link"));
                }
            });
        }).on('typeahead:render', function(e) {
            $(this).parent().find('.tt-selectable:first').addClass('tt-cursor');
        }).on("focus", function(e: any): any {
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
    controller: function(args: any): any {
        return {
            placeholder: args.placeholder? args.placeholder: 'enter a job ID or a tool name'
        }
    },
    view: function(ctrl: any, args: any) {
        return m("div", {id: args.id, "class": "search-container", config: typeAhead}, [
            m("input", {
                "class": "search-input",
                type: "text",
                name: "q",
                placeholder: ctrl.placeholder
            })
        ]);
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
            "class": "small-12 large-12 columns index-container",
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

const tilescomponent = {
    controller: () => {},
    view: (ctrl: any) => {
        return m("div", {
                "class": "row article_container small-up-1 medium-up-2 large-up-3",
                config: (elem, isInit) => {
                    if(!isInit) {
                        hideSidebar(elem, isInit);
                        m.request({
                            url: "/recent/updates",
                            method: "GET",
                            background: true,
                            deserialize: value => {return value;}
                        }).then(html => {
                            m.render(elem, m.trust(html))
                        });
                    }
                }
            },
        );
    }
};
