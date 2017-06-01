declare var onCollapse : any, onFullscreenToggle : any, onExpand : any, setViewport : any, onCollapsescreen : any;

let JobTabsComponent = {
    model: function() {
        return {
            isFullscreen: false,
            label: "Expand"
        };
    },
    controller: function(args : any) {
        let active, listitems, mo, params, state, views;
        mo = new (<any>window).JobTabsComponent.model();
        params = args.job().tool.params;
        listitems = (params.filter(function(param : any) {
            return param[1].length !== 0;
        })).map(function(param : any) {
            return param[0];
        });
        active = null;
        if (args.job().isJob) {
            state = args.job().jobstate;
            switch (state) {
                case 2:
                    active = listitems.length;
                    listitems = listitems.concat("Queued");
                    break;
                case 3:
                    active = listitems.length;
                    listitems = listitems.concat("Running");
                    break;
                case 4:
                    active = listitems.length;
                    listitems = listitems.concat("Error");
                    break;
                case 5:
                    active = listitems.length;
                    break;
                case 7:
                    active = listitems.length;
                    listitems = listitems.concat("Pending");
                    break;
                default:
                    break;
            }
        } else {
            active = 0;
        }
        views = args.job().views;
        if (views) {
            listitems = listitems.concat(views.map(function(view : any) {
                return view;
            }));
        }
        return {
            owner: args.job().ownerName,
            params: params,
            isJob: args.job().isJob,
            state: args.job().jobstate,
            listitems: listitems,
            views: views,
            getParamValue: window.JobModel.getParamValue,
            job: args.job,
            active: active,
            getLabel: (function() {
                return this.label;
            }).bind(mo),
            fullscreen: (function() {
                let job_tab_component;
                job_tab_component = $("#tool-tabs");
                if (this.isFullscreen) {
                    job_tab_component.removeClass("fullscreen");
                    this.isFullscreen = false;
                    if (typeof onCollapse === "function") {
                        onCollapse();
                    }
                    $("#collapseMe").addClass("fa-expand").removeClass("fa-compress");
                    followScroll(document);
                    if (typeof setViewport === "function") {
                        setViewport();
                    }

                } else {
                    job_tab_component.addClass("fullscreen");
                    this.isFullscreen = true;
                    if (typeof onExpand === "function") {
                        onExpand();
                    }
                    $("#collapseMe").addClass("fa-compress").removeClass("fa-expand");
                    followScroll(job_tab_component);
                    if (typeof setViewport === "function") {
                        setViewport();
                    }

                }
                if (typeof onFullscreenToggle === "function" && this.isFullscreen === true) {
                    return onFullscreenToggle();
                } else if(typeof onCollapsescreen === "function" && this.isFullscreen === false) {
                    return onCollapsescreen();
                }
            }).bind(mo),
            "delete": function() {
                let jobID;
                jobID = this.job().jobID;
                if (confirm("Do you really want to delete this Job (ID: " + jobID + ")")) {
                    console.log("Delete for job " + jobID + " clicked");
                    LiveTable.updateJobInfo();
                    return JobListComponent.removeJob(jobID, true, true);
                }
            }
        };
    },
    view: function(ctrl : any, args : any) {
        return m("div", { "class": "tool-tabs", id: "tool-tabs", config: tabulated.bind(ctrl) }, [
            m("ul", [ // Tab List
                ctrl.listitems.map(function(item : any) {
                    if(item == "Input" || item == "Parameters" || item == "Running" || item == "Queued" || item == "Error" || item == "Pending"){
                        return m("li", { id: "tab-" + item},
                            m("a", { href: "#tabpanel-" + item, config: hideSubmitButtons }, item)
                        );
                    }else{
                        return m("li", { id: "tab-" + item },

                            m("a", { href: "/api/job/result/"+args.job().jobID+"/"+args.job().tool.toolname+"/"+item, config: hideSubmitButtons }, item)
                        );
                    }
                }),
                document.cookie.split("&username=")[1] === ctrl.owner ? [ m("li", {
                    "class" : "notesheader"
                }, m("a", {
                    href: "#tabpanel-notes",
                    id: "notesTab"
                }, "Notes")) ] : [] ,
                document.cookie.split("&username=")[1] != 'invalid' ? [m("li", { style: "float: right;" }
                )] : [],
                m("li", { style: "float: right;" },
                    m("i", {
                        type:    "button",
                        id:      "collapseMe",
                        "class":   "button_fullscreen fa fa-expand",
                        onclick: ctrl.fullscreen,
                        config:  closeShortcut
                    })
                ),
                ctrl.isJob ? m("li", { style: "float: right; margin-right: 24px; margin-top: 7px" },
                    m("i", {
                        type: "button",
                        "class": "delete fa fa-trash-o",
                        title :"Delete job",
                        onclick: ctrl["delete"].bind(ctrl)
                    })
                ) : void 0
            ]), // Actual Tab Divs start here
            document.cookie.split("&username=")[1] === ctrl.owner ? [
                m("div", {
                    "class": "tab-panel parameter-panel",
                    id:    "tabpanel-notes"
                }, [
                    m("textarea", {
                        "class" : "noteArea",
                        placeholder: "Type private notes here",
                        rows: 18,
                        cols: 70,
                        id: "notepad" + ctrl.job().jobID,
                        spellcheck: true,
                        config: jobNoteArea
                    })
                ])
            ] : null,
            m("form", { id: "jobform" },
                ctrl.params.map(function(paramGroup : any) : any {
                    let elements;
                    if (paramGroup[1].length !== 0) {
                        elements = paramGroup[1];
                        return m("div", {
                            "class": "tabs-panel parameter-panel",
                            id:    "tabpanel-" + paramGroup[0]
                        }, [
                            m("div", { "class": "parameters" },
                                paramGroup[0] === "Input" ?
                                    elements[0].name === "alignment" ? [
                                        m("div", { "class": "row" },
                                            m("div", { "class": "" },
                                                mapParam(elements[0], ctrl)
                                            )
                                        ),
                                        elements.length > 1 ? m("div", { "class": "row", style: "margin-top: 35px;" },
                                            elements.slice(1).map(function(param : any) {
                                                //console.log(JSON.stringify(mapParam(param,ctrl)));
                                                return m("div", {"class" : "large-6 medium-3 small-1 columns", style: "padding-right: 20px"},
                                                    mapParam(param, ctrl));
                                            })
                                        ) : void 0
                                    ] :
                                        m("div", { "class": "row small-up-1 medium-up-2 large-up-3" },
                                            elements.map(function(param : any) {
                                                return m("div", { "class": "column column-block" }, mapParam(param, ctrl));
                                            })
                                        ) :
                                    m("div", { "class": "row small-up-1 medium-up-2 large-up-3" },
                                        elements.map(function(param : any) {
                                            return m("div", { "class": "column column-block" }, mapParam(param, ctrl));
                                        })
                                    )
                            )
                        ])
                    }}),
                ctrl.isJob && ctrl.state === 2 ? m("div", { "class": "tabs-panel", id: "tabpanel-Queued"  },
                    m(JobQueuedComponent, { job: ctrl.job })) : void 0,
                ctrl.isJob && ctrl.state === 3 ? m("div", { "class": "tabs-panel", id: "tabpanel-Running" },
                    m(JobRunningComponent, { job: ctrl.job })) : void 0,
                ctrl.isJob && ctrl.state === 4 ? m("div", { "class": "tabs-panel", id: "tabpanel-Error"   },
                    m(JobErrorComponent, {job: ctrl.job})) : void 0,
                ctrl.isJob && ctrl.state === 7 ? m("div", { "class": "tabs-panel", id: "tabpanel-Pending" },
                    m(JobPendingComponent, {job: ctrl.job})) : void 0,
                m(JobSubmissionComponent, { job: ctrl.job, isJob: ctrl.isJob })
            ),
            ctrl.views ? ctrl.views.map(function(view : any) {
                return m("div", { "class": "tabs-panel", id: "tabpanel-" + view}
                );
            }) : void 0
        ]);
    }
};