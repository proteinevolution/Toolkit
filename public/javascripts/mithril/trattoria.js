var JobErrorComponent, JobValidationComponent, JobRunningComponent, JobLineComponent, JobQueuedComponent, JobSubmissionComponent, JobTabsComponent;

JobLineComponent = {
    view: function(ctrl, args) {
        var isJob;
        isJob = args.job().isJob;
        return m("div", {
            "class": "jobline"
        }, [
            m(HelpModalComponent, { toolname: args.job().tool.toolname, toolnameLong: args.job().tool.toolnameLong }),
            m("span", { class: "toolname" }, [
                m("input", { id: "toolnameAccess", "style": "display: none;", type: "text", value: args.job().tool.toolname}),
                m("a", { href: "/#/tools/" + args.job().tool.toolname }, args.job().tool.toolnameLong),
                m("a", { config: helpModalAccess.bind(args) },
                    m("i", { class: "icon-information_white helpicon" })
                )
            ]),
            m("span", { class: "jobdate" }, isJob ? "Created: " + (args.job().createdOn) : "")
        ]);
    }
};

JobErrorComponent = {
    updateLog: function(){
        m.redraw(true);
    },
    log : "",
    controller: function (args) {
        m.request({ method: "GET", url: "files/"+args.job().jobID+"/process.log", contentType: "charset=utf-8",
            deserialize: function (data) {JobRunningComponent.log = data.toString().split('#')}});

        return {}
    },
    view: function(ctrl, args) {
        return m("div", { class: "running-panel", config: foundationConfig }, [
            m('h6', "Your Job has reached error state!"),
            m("div", {"class": "processJobIdContainer"},
                m('b', "Job ID:"),
                m('p', '' + args.job().jobID)),
            //m("h6", "Job has reached Error state"),
            //m("br"),
            //m("br"),
            JobRunningComponent.log.map(function(logElem){
                if(logElem == "")
                    return;
                logElem = logElem.split("\n");
                // delete empty entries from array
                logElem = logElem.filter(Boolean);
                var len = logElem.length-1;
                console.log(len);
                if(len > 0){
                    if(len > 1){
                        return [m("div", {class: "logElem"},
                            m("i", {class: "icon-check_circle logElemDone"}),
                            m("div", {class: "logElemText"}, logElem[0])),
                            m("div", {class: "logElem"},
                            m("i", {class: "icon-cancel_circle logElemError"}),
                            m("div", {class: "logElemText"}, "Error."))
                            ]
                    }
                    else if(logElem[1] == "done"){
                        return m("div", {class: "logElem"},
                            m("i", {class: "icon-check_circle logElemDone"}),
                            m("div", {class: "logElemText"}, logElem[0]))

                    }
                    else if(logElem[1] == "error"){
                        return m("div", {class: "logElem"},
                            m("i", {class: "icon-cancel_circle logElemError"}),
                            m("div", {class: "logElemText"}, logElem[0]))
                    }
                }
                return m("div", {class: "logElem"},
                    m("div", {class: "logElemRunning"}),
                    m("div", {class: "logElemText"}, logElem[0]))
            })

        ]);
    }
};

JobQueuedComponent = {
    updateLog: function(){
        m.redraw(true);
    },
    view: function(ctrl, args) {
        return m("div", { class: "queued-panel", config: foundationConfig }, [
            m('h6', "Your submission is queued!"),
            m("div", {"class": "processJobIdContainer"},
                m('b', "Job ID:"),
                m('p', ' ' + args.job().jobID)),
        ]);
    }
};

JobRunningComponent = {
    updateLog: function(){
      m.redraw(true);
    },
    log : "",
    controller: function (args) {
            m.request({ method: "GET", url: "files/"+args.job().jobID+"/process.log", contentType: "charset=utf-8",
                deserialize: function (data) {JobRunningComponent.log = data.toString().split('#')}});

      return {}
    },
    view: function(ctrl, args) {
        return m("div", { class: "running-panel" , config: foundationConfig}, [
            m('h6', "Your submission is processing!"),
            m("div", {"class": "processJobIdContainer"},
                m('b', "Job ID:"),
                m('p',  {style: "margin-left: 5px"}, ' ' + args.job().jobID)),
            JobRunningComponent.log.map(function(logElem){
                if(logElem == "")
                    return;
                logElem = logElem.split("\n");
                if(logElem.length > 1){
                    if(logElem[1] == "done"){
                        return m("div", {class: "logElem"},
                            m("i", {class: "icon-check_circle logElemDone"}),
                            m("div", {class: "logElemText"}, logElem[0]))
                    }
                    else if(logElem[1] == "error"){
                        return m("div", {class: "logElem"},
                            m("i", {class: "icon-cancel_circle logElemError"}),
                            m("div", {class: "logElemText"}, logElem[0]))
                    }
                }
                    return m("div", {class: "logElem"},
                        m("div", {class: "logElemRunning"}),
                        m("div", {class: "logElemText"}, logElem[0]))
            })
        ]);
    }
};

JobPendingComponent = {
    view: function(ctrl, args) {
        return m("div", { class: "pending-panel", config: foundationConfig }, [
            m('h6', "Your submission is pending, as there is a different job with similar parameters!"),
            m('div', {"class": "openSimilarJob"}, [
                m("button",{ class   : "button small",
                    onclick : function(e){
                        e.preventDefault();
                        var route = jsRoutes.controllers.JobController.startJob(args.job().jobID);
                        m.request({method:route.method, url:route.url}).then(function(data){
                            console.log("requested:",data);
                        });
                    }
                }, "Start Job anyways"),
                m("button",{ class   : "button small",
                    onclick : function(e){
                        e.preventDefault();
                        var route = jsRoutes.controllers.JobController.checkHash(args.job().jobID);
                        m.request({method:route.method, url:route.url}).then(function(data){
                            if (data != null && data.jobID != null) {
                               m.route(data.jobID);
                            }
                            console.log("requested:",data);
                        });
                    }
                }, "Start the found job")
            ]),
            m("div", {"class": "processJobIdContainer"},
                m('b', "Job ID:"),
                m('p', ' ' + args.job().jobID)
            )
        ]);
    }
};

renderParameter = function(content, moreClasses) {
    return m("div", { class: moreClasses ? "parameter " + moreClasses : "parameter" }, content);
};

mapParam = function(param, ctrl) {
    var comp = formComponents[param.paramType.type];
    return m(comp, {
        param: param,
        value: ctrl.getParamValue(param.name)
    });
};



JobTabsComponent = {
    model: function() {
        return {
            isFullscreen: false,
            label: "Expand"
        };
    },
    controller: function(args) {
        var active, listitems, mo, params, state, views;
        mo = new JobTabsComponent.model();
        params = args.job().tool.params;
        listitems = (params.filter(function(param) {
            return param[1].length !== 0;
        })).map(function(param) {
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
            listitems = listitems.concat(views.map(function(view) {
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
            getParamValue: JobModel.getParamValue,
            job: args.job,
            active: active,
            getLabel: (function() {
                return this.label;
            }).bind(mo),
            fullscreen: (function() {
                var job_tab_component;
                job_tab_component = $("#tool-tabs");
                if (this.isFullscreen) {
                    job_tab_component.removeClass("fullscreen");
                    this.isFullscreen = false;
                    if (typeof onCollapse === "function") {
                        onCollapse();
                    }
                    $("#collapseMe").addClass("fa-expand").removeClass("fa-compress");
                    followScroll(document);
                    setViewport();

                } else {
                    job_tab_component.addClass("fullscreen");
                    this.isFullscreen = true;
                    if (typeof onExpand === "function") {
                        onExpand();
                    }
                    $("#collapseMe").addClass("fa-compress").removeClass("fa-expand");
                    followScroll(job_tab_component);
                    setViewport();
                }
                if (typeof onFullscreenToggle === "function" && this.isFullscreen === true) {
                    return onFullscreenToggle();
                } else if(typeof onCollapsescreen === "function" && this.isFullscreen === false) {
                    return onCollapsescreen();
                }
            }).bind(mo),
            "delete": function() {
                var jobID;
                jobID = this.job().jobID;
                if (confirm("Do you really want to delete this Job (ID: " + jobID + ")")) {
                    console.log("Delete for job " + jobID + " clicked");
                    LiveTable.updateJobInfo();
                    return JobListComponent.removeJob(jobID, true, true);
                }
            }
        };
    },
    view: function(ctrl, args) {
        return m("div", { class: "tool-tabs", id: "tool-tabs", config: tabulated.bind(ctrl) }, [
            m("ul", [ // Tab List
                ctrl.listitems.map(function(item) {
                    if(item == "Input" || item == "Parameters" || item == "Running" || item == "Queued" || item == "Error" || item == "Pending"){
                        return m("li", { id: "tab-" + item},
                            m("a", { href: "#tabpanel-" + item, config: hideSubmitButtons }, item)
                        );
                    }else{
                        return m("li", { id: "tab-" + item},
                            m("a", { href: "/api/job/result/"+args.job().jobID+"/"+args.job().tool.toolname+"/"+item, config: hideSubmitButtons }, item)
                        );
                    }
                }),
                document.cookie.split("&username=")[1] === ctrl.owner ? [ m("li", {
                        "class" : "notesheader"
                    }, m("a", {
                        href: "#tabpanel-notes",
                        id: "notesTab"
                        //"class": "hasNotes"
                    }, "Notes")) ] : [] ,
                document.cookie.split("&username=")[1] != 'invalid' ? [m("li", { style: "float: right;" }
                        // m("input", {
                        //     type: "button",
                        //     class: "button small button_fullscreen",
                        //     value: "Add to project",
                        //     onclick: function() {
                        //         $('#projectReveal').foundation('open');
                        //     },
                        //     config: foundationConfig
                        // })
                    )] : [],
                m("li", { style: "float: right;" },
                    m("i", {
                        type:    "button",
                        id:      "collapseMe",
                        class:   "button_fullscreen fa fa-expand",
                        onclick: ctrl.fullscreen,
                        config:  closeShortcut
                    })
                ),
                ctrl.isJob ? m("li", { style: "float: right; margin-right: 24px; margin-top: 7px" },
                    m("i", {
                        type: "button",
                        class: "delete fa fa-trash-o",
                        title :"Delete job",
                        onclick: ctrl["delete"].bind(ctrl)
                    })
                ) : void 0
            ]), // Actual Tab Divs start here
            document.cookie.split("&username=")[1] === ctrl.owner ? [
                m("div", {
                    class: "tab-panel parameter-panel",
                    id:    "tabpanel-notes"
                }, [
                    m("textarea", {
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
                ctrl.params.map(function(paramGroup) {
                var elements;
                if (paramGroup[1].length !== 0) {
                    elements = paramGroup[1];
                    return m("div", {
                        class: "tabs-panel parameter-panel",
                        id:    "tabpanel-" + paramGroup[0]
                    }, [
                        m("div", { class: "parameters" },
                            paramGroup[0] === "Input" ?
                                elements[0].name === "alignment" ? [
                                    m("div", { class: "row" },
                                        m("div", { class: "" },
                                            mapParam(elements[0], ctrl)
                                        )
                                    ),
                                    elements.length > 1 ? m("div", { class: "row", style: "margin-top: 35px;" },
                                        elements.slice(1).map(function(param) {
                                            //console.log(JSON.stringify(mapParam(param,ctrl)));
                                            return m("div", {class : "large-6 medium-3 small-1 columns", style: "padding-right: 20px"},
                                                mapParam(param, ctrl));
                                        })
                                    ) : void 0
                                ] :
                                m("div", { class: "row small-up-1 medium-up-2 large-up-3" },
                                    elements.map(function(param) {
                                        return m("div", { class: "column column-block" }, mapParam(param, ctrl));
                                    })
                                ) :
                            m("div", { class: "row small-up-1 medium-up-2 large-up-3" },
                                elements.map(function(param) {
                                    return m("div", { class: "column column-block" }, mapParam(param, ctrl));
                                })
                            )
                        )
                    ])
                }}),
                ctrl.isJob && ctrl.state === 2 ? m("div", { class: "tabs-panel", id: "tabpanel-Queued"  },
                    m(JobQueuedComponent, { job: ctrl.job })) : void 0,
                ctrl.isJob && ctrl.state === 3 ? m("div", { class: "tabs-panel", id: "tabpanel-Running" },
                    m(JobRunningComponent, { job: ctrl.job })) : void 0,
                ctrl.isJob && ctrl.state === 4 ? m("div", { class: "tabs-panel", id: "tabpanel-Error"   },
                    m(JobErrorComponent, {job: ctrl.job})) : void 0,
                ctrl.isJob && ctrl.state === 7 ? m("div", { class: "tabs-panel", id: "tabpanel-Pending" },
                    m(JobPendingComponent, {job: ctrl.job})) : void 0,
                m(JobSubmissionComponent, { job: ctrl.job, isJob: ctrl.isJob })
            ),
            ctrl.views ? ctrl.views.map(function(view) {
                return m("div", { class: "tabs-panel", id: "tabpanel-" + view}
                );
            }) : void 0
        ]);
    }
};



JobValidationComponent = {
    view: function(){
        return m("div#validOrNot", {class: "callout", style: "display: none"}, "")
    }
};

//return status code if error
var extractStatus = function(xhr, xhrOptions) {
    if(xhr.status == 413){
        alert("File too big!");
        return false;
    }
};





/*
m.capture = function(eventName, handler) {
    var bindCapturingHandler;
    bindCapturingHandler = function(element) {
        element.addEventListener(eventName, handler, true);
    };
    return function(element, init) {
        if (!init) {
            bindCapturingHandler(element);
        }
    };
}; */ // TODO: most likely not in use anymore



window.ParameterAlignmentComponent = {
    model: function(args) {
        this.modes = args.param.paramType.modes;
        this.label = "";
        this.formats = [];
        if(this.modes.length > 0) {
            this.label = this.modes[0].label;
            if(this.modes[0].mode == 1) {
                this.formats = this.modes[0].formats
            }
        }
        this.allowsTwoTextAreas = args.param.paramType.allowsTwoTextAreas;
        this.twoTextAreas = (window.JobModel.getParamValue("hhpred_align") == 'true');
    },
    controller: function(args) {
        this.mo = new window.ParameterAlignmentComponent.model(args);
        return {
            name: "alignment",
            id: "alignment",
            // Function to List all supported modes of the component
            getModes: (function() {
                return this.modes;
            }).bind(this.mo),
            getLabel: (function() {
                return this.label;
            }).bind(this.mo),
            getAllowsTwoTextAreas: (function() {
                return this.allowsTwoTextAreas;
            }).bind(this.mo),
            setMode: (function(mode) {
                for(var i = 0; i < this.modes.length; i++) {
                    var current_mode = this.modes[i];
                    if(current_mode.mode == mode) {
                        this.label = current_mode.label;
                        if(mode == 1) {
                            this.formats = current_mode.formats;
                        } else {
                            this.formats = [];
                        }
                    }
                }
            }).bind(this.mo),
            getFormats: (function() {
                return this.formats;
            }).bind(this.mo),
            toggleTwoTextAreas: (function() {
                this.twoTextAreas = !this.twoTextAreas;
                if (this.twoTextAreas) {
                    $(".inputDBs").prop('disabled', true);
                    $(".inputDBs option:selected").prop("selected", false);
                    $("#alignment").attr("rows", "8");
                    $('#alignment_two').show();
                    $("#alignment_two").prop("required", true);
                    $("#hhpred_align").prop('checked', true);
                } else {
                    $(".inputDBs").prop('disabled', false);
                    $("#alignment").attr("rows", "18");
                    $("#alignment_two").hide();
                    $("#alignment_two").removeAttr("required", false);
                    $("#hhpred_align").prop('checked', false);

                }
            }).bind(this.mo),
            setTwoTextAreas: (function(bool) {
                this.twoTextAreas = bool;
            }).bind(this.mo),
            getTwoTextAreas: (function(){
                return this.twoTextAreas;
            }).bind(this.mo)

        };
    },
    view: function(ctrl, args) {
        var params = {
            oninit: function (elem, isInit) {
                if (!isInit) {
                    if (ctrl.getTwoTextAreas()) {
                        $(".inputDBs").prop('disabled', true);
                        $(".inputDBs option:selected").prop("selected", false);
                        $("#hhpred_align").prop('checked', true);
                        $("#alignment").attr("rows", "8");
                        $('#alignment_two').show();
                        $("#alignment_two").prop("required", true);
                    } else {
                        $(".inputDBs").prop('disabled', false);
                        $("#hhpred_align").prop('checked', false);
                        $("#alignment").attr("rows", "19");
                        $("#alignment_two").hide();
                        $("#alignment_two").removeAttr("required", false);

                    }
                }
            }
        };
        if(ctrl.getAllowsTwoTextAreas()) {

            var alignmentSwitch = m("div", {"class": "switchContainer"},
                m("label", {"class": "switch tiny"},
                m("input", {
                    id: "hhpred_align",
                    type: "checkbox",
                    name: "hhpred_align",
                    value: "true",
                    config: params.oninit,
                onclick: function () {
                    ctrl.toggleTwoTextAreas();
                }}),
                m("div", {"class": "sliderSwitch round"})
            ),
            m("label",{"class": "firstLabel"},"Align two sequences or MSAs")
        );
            var textArea2 =
            m("textarea", {
                name: ctrl.name+"_two",
                placeholder: ctrl.getLabel(),
                rows: 8,
                cols: 70,
                class: "alignment",
                id: ctrl.id + "_two",
                value: window.JobModel.getParamValue("alignment_two"),
                style: "display: none; margin-top: 1em;",
                spellcheck: false,
                config: validation
            });
        }

        return renderParameter([
            m("div", {
                    "class": "alignment_textarea"
                },
                m("textarea", {
                    name: ctrl.name,
                    placeholder: ctrl.getLabel(),
                    rows: 18,
                    cols: 70,
                    id: ctrl.id,
                    class: "alignment",
                    value: args.value,
                    required: "required",
                    spellcheck: false,
                    config: validation
                }),
                textArea2)
            , m("div", {
                "class": "alignment_buttons"
            }, [
                m("div", {"class": "leftAlignmentButtons"},
                m("input", {
                    type: "button",
                    id: "pasteButton",
                    "class": "button small alignmentExample",
                    value: "Paste Example",
                    config: sampleSeqConfig,
                    onclick: function() {
                        //$('.submitJob').prop('disabled', false);
                        setTimeout(function(){
                            validationProcess($('#alignment'),$("#toolnameAccess").val());
                        }, 100);
                        $("#validOrNot").removeClass("alert warning primary secondary");
                        originIsFasta = true; // resets changed validation filter
                    }
                }),
                m("div", {"class": "uploadContainer"},
                m("label",{
                "for": "fileUpload",
                "class" : "button small fileUpload"
                },"Upload File"),
                m("input", {
                type: "file",
                id: "fileUpload",
                "class": "show-for-sr",
                onchange: function() {
                    if (this.value) {
                        $(".submitJob").prop("disabled", false);
                        $(".uploadFileName").show();
                        $("#uploadBoxClose").show();
                        $("#" + ctrl.id).prop("disabled", true);
                        $("#" + ctrl.id + "_two").prop("disabled", true);
                        }
                    }
                }), m("div",
                        {"class": "uploadFileName"},
                        $("#fileUpload").val() ? $("#fileUpload")[0].files[0].name : "",
                    m("a", {
                        "class": "boxclose",
                        "id": "uploadBoxClose",
                        onclick: function(){
                            $(".uploadFileName").hide();
                            $("input[type=file]").val(null);
                            return $("#" + ctrl.id).prop("disabled", false);
                            }
                    }, m("i", {"class": "fa fa-times"})))),
                    m(JobValidationComponent, {})
                    , m("select", {"id": "alignment_format", "class": "alignment_format", config: alignment_format.bind(ctrl.getFormats())}, ctrl.getFormats().map(function(format){
                    return m("option", {value: format[0]}, format[1])}
                )
                ),
                m("div", {"class": "switchDiv"},
                alignmentSwitch
                ))


            ])
        ], "alignmentParameter");
    }
};





