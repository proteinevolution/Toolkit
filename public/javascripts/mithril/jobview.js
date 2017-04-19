var JobErrorComponent, jobNoteArea, JobValidationComponent, ParameterAlignmentComponent, JobRunningComponent, JobLineComponent, JobQueuedComponent, JobSubmissionComponent, JobTabsComponent, ParameterBoolComponent, ParameterNumberComponent, ParameterRadioComponent, ParameterSelectComponent, ParameterTextComponent, ParameterSlideComponent, SearchformComponent, alignmentUpload, closeShortcut, formComponents, foundationConfig, helpModalAccess, mapParam, renderParameter, selectBoxAccess, submitModal, tabulated ;

helpModalAccess = function(elem, isInit) {
    if (!isInit) {
        return elem.setAttribute("data-open", "help-" + (this.job().tool.toolname));
    }
};


selectBoxAccess = function(elem, isInit) {
    if (!isInit) {
        return $(elem).niceSelect();
    } else {
        return $(elem).niceSelect('update');
    }
};



window.JobViewComponent = {
    view: function(ctrl, args) {
        if (!args.job()) {
            return m("div", "Waiting for Job");
        } else {
            return m("div", {
                id: "jobview"
            }, [
                m(JobLineComponent, { job: args.job }),
                m(JobTabsComponent, { job: args.job, owner: args.owner })
            ]);
        }
    }
};

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

SearchformComponent = {
    view: function() {
        return m("div", { id: "jobsearchform" },
            m("input", { type: "text", placeholder: "Search by JobID, e.g. 6881313", id: "jobsearch" })
        );
    }
};

foundationConfig = function(elem, isInit) {
    if (!isInit) {
        return $(elem).foundation();
    }
};


jobNoteArea = function(elem, isInit) {
    if (!isInit && $(elem).attr('id').substring(7) > -1) {
        $.ajax({
            url: '/api/jobs/getnotes/' + $(elem).attr('id').substring(7),
            type: 'get',
            success: function(data) {
                if(data && data.length > 0){
                    $("#notesTab").addClass("hasNotes");
                } else {
                    $("#notesTab").removeClass("hasNotes");
                }
                $(elem).html(data);
            },
            error: function(e){
                console.warn(JSON.stringify(e));
            }
        });
        return $(elem).keyup(function(e) {
            var contentString;
            $("#notesTab").addClass("hasNotes");
            if($(elem).val().length === 0)
                $("#notesTab").removeClass("hasNotes");
            contentString = $(this).val();
            $.post(jsRoutes.controllers.Jobs.annotation($(this).attr('id').substring(7), contentString), function(response) {
                console.log('Response: ' + response);
            });
        });
    }
};


JobErrorComponent = {
    log : "",
    controller: function (args) {
        m.request({ method: "GET", url: "files/"+args.job().jobID+"/process.log", contentType: "charset=utf-8",
            deserialize: function (data) {JobRunningComponent.log = data.toString().split('#')}});

        return {}
    },
    view: function(ctrl, args) {
        return m("div", { class: "running-panel" }, [
            m("table", { config: foundationConfig },
                m("tbody", [
                    m("tr", [m("td", "JobID"), m("td", args.job().jobID)]),
                    m("tr", [m("td", "Created On"), m("td", args.job().createdOn)])
                ])
            ),
            m("h6", "Job has reached Error state"),
            m("br"),
            m("br"),
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
                            m("div", {class: "logElemText"}, "error in runscript"))
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
    view: function(ctrl, args) {
        return m("div", { class: "queued-panel" }, [
            m("table", { config: foundationConfig },
                m("tbody", [
                    m("tr", [ m("td", "JobID"), m("td", args.job().jobID)]),
                    m("tr", [ m("td", "Created On"), m("td", args.job().createdOn)])
                ])
            )
        ]);
    }
};

JobRunningComponent = {
    log : "",
    controller: function (args) {
            m.request({ method: "GET", url: "files/"+args.job().jobID+"/process.log", contentType: "charset=utf-8",
                deserialize: function (data) {JobRunningComponent.log = data.toString().split('#')}});

      return {}
    },
    view: function(ctrl, args) {
        return m("div", { class: "running-panel" }, [
            m("table", { config: foundationConfig },
                m("tbody", [
                    m("tr", [m("td", "JobID"), m("td", args.job().jobID)]),
                    m("tr", [m("td", "Created On"), m("td", args.job().createdOn)])
                ])
            ),
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

tabulated = function(element, isInit) {
    if (!isInit) { return $(element).tabs({ active: this.active }); }
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

closeShortcut = function() {
    return $(document).keydown(function(e) {
        if (e.keyCode === 27 && $("#tool-tabs").hasClass("fullscreen")) {
            $("#collapseMe").click();
        }
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
            }
        } else {
            active = 0;
        }
        views = args.job().views;
        if (views) {
            listitems = listitems.concat(views.map(function(view) {
                return view[0];
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
                    $("#collapseMe").addClass("fa-expand");
                    $("#collapseMe").removeClass("fa-compress");
                } else {
                    job_tab_component.addClass("fullscreen");
                    this.isFullscreen = true;
                    if (typeof onExpand === "function") {
                        onExpand();
                    }
                    $("#collapseMe").removeClass("fa-expand");
                    $("#collapseMe").addClass("fa-compress");
                }
                if (typeof onFullscreenToggle === "function") {
                    return onFullscreenToggle();
                }
            }).bind(mo),
            "delete": function() {
                var jobID;
                jobID = this.job().jobID;
                if (confirm("Do you really want to delete this Job (ID: " + jobID + ")")) {
                    console.log("Delete for job " + jobID + " clicked");
                    return JobListComponent.removeJob(jobID, true, true);
                }
            }
        };
    },
    view: function(ctrl, args) {
        return m("div", { class: "tool-tabs", id: "tool-tabs", config: tabulated.bind(ctrl) }, [
            m("ul", [
                ctrl.listitems.map(function(item) {
                    return m("li", { id: "tab-" + item },
                        m("a", { href: "#tabpanel-" + item }, item)
                    );
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
            ]),
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
                ctrl.params.map(function(paramGroup, index) {
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
                                        m("div", { class: "small-12 large-12 medium-12 columns" },
                                            mapParam(elements[0], ctrl)
                                        )
                                    ),
                                    m("div", { class: "row small-up-1 medium-up-2 large-up-3", style: "margin-top: 35px;" },
                                        elements.slice(1).map(function(param) {
                                            return m("div", { class: "column column-block multiSelectParameter" },
                                                mapParam(param, ctrl));
                                        })
                                    )
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
                        ),
                        index == 0 ? m(JobSubmissionComponent, { job: ctrl.job, isJob: ctrl.isJob }) : null
                    ])
                }}),
                ctrl.isJob && ctrl.state === 2 ? m("div", { class: "tabs-panel", id: "tabpanel-Queued"  },
                    m(JobQueuedComponent, { job: ctrl.job })) : void 0,
                ctrl.isJob && ctrl.state === 3 ? m("div", { class: "tabs-panel", id: "tabpanel-Running" },
                    m(JobRunningComponent, { job: ctrl.job })) : void 0,
                ctrl.isJob && ctrl.state === 4 ? m("div", { class: "tabs-panel", id: "tabpanel-Error"   },
                    m(JobErrorComponent, {job: ctrl.job})) : void 0
            ),
            ctrl.views ? ctrl.views.map(function(view) {
                return m("div", { class: "tabs-panel", id: "tabpanel-" + view[0] },
                    m("div", { class: "result-panel" }, m.trust(view[1]))
                );
            }) : void 0
        ]);
    }
};

submitModal = function(elem, isInit) {
    if (!isInit) {
        $(elem).foundation();
        return $(elem).bind('closed.zf.reveal	', (function() {
            return $(".submitJob").prop("disabled", false);
        }));
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

JobSubmissionComponent = {
    submitting      : false,    // Job is being sent if true
    currentJobID    : null,     // Currently entered jobID
    jobIDValid      : false,    // Is the current jobID valid?
    jobIDValidationTimeout : null,     //
    checkJobID : function (jobID) {
        clearTimeout(JobSubmissionComponent.jobIDValidationTimeout);    // clear all previous timeouts
        JobSubmissionComponent.jobIDValid = false;      // ensure that the user can not send the job form
        JobSubmissionComponent.currentJobID = jobID;    // set the jobID to the new jobID
        if (jobID !== "") { // ignore checking if the field is empty as the server will generate a jobID in that case.
            JobSubmissionComponent.jobIDValidationTimeout = setTimeout(function (a) {   // create the timeout
                m.request({ method: "GET", url: "/search/checkJobID/"+jobID }).then(
                    function (data) { JobSubmissionComponent.jobIDValid = !data.exists; }
                );
            }, 500);
        } else {
            JobSubmissionComponent.jobIDValid = true;
        }
        return jobID;
    },
    jobIDComponent : function (ctrl) {
        var style = "jobid";
        style += JobSubmissionComponent.currentJobID === "" ? " white" :
                (JobSubmissionComponent.jobIDValid          ? " green" : " red");
        return m("input", { type:        "text",
                            id:          "jobID",
                            class:       style,
                            placeholder: "Custom JobID",
                            onkeyup:     m.withAttr("value", JobSubmissionComponent.checkJobID),
                            value:       JobSubmissionComponent.currentJobID
        })
    },
    controller: function(args) {
        if (JobSubmissionComponent.jobID == null) {
            var oldJobID, version, newJobID;
            if (args.isJob) {
                oldJobID = args.job().jobID.split(".");
                version = parseInt(oldJobID[1]);
                newJobID = oldJobID[0] + "." + (Number.isNaN(version) ? 1 : version + 1);
                JobSubmissionComponent.jobIDValid = false;
            } else {
                newJobID = "";
                JobSubmissionComponent.jobIDValid = true;
            }
            JobSubmissionComponent.checkJobID(newJobID);
        }
        return {
            submit: function(startJob) {
                if (this.submitting) return;
                this.submitting = true; // ensure that the submission is not reinitiated while a submission is ongoing
                var form = document.getElementById("jobform");

                if(!form.checkValidity()) {
                    alert("Parameters are invalid");
                    return
                }

                var checkRoute, formData, jobID, toolname, doCheck;
                toolname = args.job().tool.toolname;
                doCheck = true;
                jobID = JobSubmissionComponent.jobIDValid ? JobSubmissionComponent.currentJobID : null;
                if (!jobID) {
                    jobID = null;
                }
                // Use check route and specify that the hashing function should be used
                checkRoute = jsRoutes.controllers.JobController.check(toolname, jobID, doCheck);
                formData = new FormData(form);

                // appendParentID if in storage
                var parentid = localStorage.getItem("parentid");
                if(!parentid) {
                    parentid = '';
                }
                formData.append('parentid', parentid);
                $(".submitJob").prop("disabled", true);
                var file = ($("input[type=file]")[0].files[0]);
                formData.append("file", file);
                return m.request({
                    method: checkRoute.method,
                    url: checkRoute.url,
                    data: formData,
                    serialize: function(data) {
                        return data;
                    }
                }).then(function(data) {
                    var submitRoute;
                    jobID = data.jobID;
                    if (data.existingJobs) {
                        $('#reload_job').unbind('click');
                        $('#submit_again').unbind('click');
                        $('#reload_job').on('click', function() {
                            $('#submit_modal').foundation('close');
                            return m.route("/jobs/" + data.existingJob.jobID);
                        });
                        $('#submit_again').on('click', function() {
                            var submitRoute;
                            $('#submit_modal').foundation('close');
                            var jobListComp = JobListComponent.Job(
                                    { jobID: jobID, state: 0, createdOn: Date.now().valueOf(), toolname: toolname }
                            );
                            console.log(jobListComp);
                            JobListComponent.pushJob(jobListComp, true); // setActive = true
                            submitRoute = jsRoutes.controllers.JobController.create(toolname, jobID);
                            m.request({
                                url: submitRoute.url,
                                method: submitRoute.method,
                                data: formData,
                                serialize: function(data) { m.route("/jobs/" + jobID); return data; }
                            });
                            return null;
                        });
                        return $('#submit_modal').foundation('open');
                    } else {
                        var jobListComp = JobListComponent.Job(
                            { jobID: jobID, state: 0, createdOn: Date.now().valueOf(), toolname: toolname }
                        );
                        console.log(jobListComp);
                        JobListComponent.pushJob(jobListComp, true); // setActive = true

                        submitRoute = jsRoutes.controllers.JobController.create(toolname, jobID);
                        m.request({
                            method: submitRoute.method,
                            url: submitRoute.url,
                            data: formData,
                            extract: extractStatus,
                            serialize: function(data) { m.route("/jobs/" + jobID); return data; }
                        });
                        return null;
                    }
                }, function(error) {
                    console.warn("Bad Request");
                    return $(".submitJob").prop("disabled", false);
                });
            }
        };
    },
    view: function(ctrl, args) {
        var hide = {
            oninit: function (elem, isInit) {
                if (!isInit) {
                    $("#uploadBoxClose").hide();
                    $(".uploadFileName").hide();
                }
            }
        };
        return m("div", { class:  "submitbuttons", config: hide.oninit }, [
            m("div", {
                class:                 "reveal",
                "data-reveal":         "data-reveal",
                "data-animation-in":   "fade-in",
                "data-overlay":        "true",
                "transition-duration": "fast",
                id:                    "submit_modal",
                config:                submitModal
            }, [
                m("p", "Already existing job found!"),
                m("input", { class: 'button', id: 'reload_job', type: 'button', value: 'Reload' }),
                m("input", { class: 'button', id: 'submit_again', type: 'button', value: 'New Submission' })
            ]),
            Auth.user == null ? null :
                m("label", {style: "width: 16em; float:left;"}, [
                    m("input", { type: "checkbox", id:"emailUpdate", name: "emailUpdate", value:true}),
                    "E-Mail notification"
                ]),
            Auth.user == null ? null :
                m("label", {style: "width: 16em; float:left;"}, [
                    m("input", { type: "checkbox", id:"private", name: "private", checked:"checked", value:true}),
                    "Private"
                ]),
            !this.submitting ? m("input", {
                type: "button",
                class: "success button small submitJob",
                value: (args.isJob ? "Res" : "S") + "ubmit Job",
                style: "float: right;",
                onclick: ctrl.submit.bind(ctrl, true)
            }) : null,
            //!args.isJob ? m("label", m("input", { type: "checkbox", name: "private", value: "true", checked: "checked" }), "Private" ) : null, // TODO reimplement private checkbox
            //args.isJob && args.job().jobstate === 1 ?
            //    m("input", { type: "button", class: "button small addJob", value: "Start Job", onclick: ctrl.startJob })
            //    : null,
            //args.isJob ? m("input", { type: "button", class: "button small addJob", value: "Add Job", onclick: ctrl.addJob })
            //    : null,
            this.jobIDComponent(ctrl), m(ProjectComponent, {})
        ])
    }
};

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
};

alignmentUpload = function(elem, isInit) {
    if (!isInit) {
        elem.setAttribute("data-reveal", "data-reveal");
        return $(elem).foundation();
    }
};



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
                textArea2),
            m("div", {
                id: "upload_alignment_modal",
                "class": "tiny reveal",
                config: alignmentUpload
            }, m("input", {
                type: "file",
                id: "upload_alignment_input",
                name: "upload_alignment_input",
                onchange: function() {
                    if (this.value) {
                        $("#upload_alignment_modal").foundation("close");
                        $(".uploadFileName").show();
                        $("#uploadBoxClose").show();
                        $("#" + ctrl.id).prop("disabled", true);
                        $("#" + ctrl.id + "_two").prop("disabled", true);
                    }
                }
            })), m("div", {
                "class": "alignment_buttons"
            }, [
                m("div", {"class": "leftAlignmentButtons"},
                m("input", {
                    type: "button",
                    "class": "button small alignmentExample",
                    value: "Paste Example",
                    config: sampleSeqConfig,
                    onclick: function() {
                        $('.submitJob').prop('disabled', false);
                        $("#validOrNot").removeClass("alert warning primary secondary");
                        originIsFasta = true; // resets changed validation filter
                    }
                }), m("input", {
                    type: "button",
                    "class": "button small alignmentExample",
                    value: "Upload File",
                    onclick: function() {
                        $('#upload_alignment_modal').foundation('open');
                    }
                }), m("div",
                        {"class": "uploadFileName"},
                        $("input[type=file]").val(),
                    m("a", {
                        "class": "boxclose",
                        "id": "uploadBoxClose",
                        onclick: function(){
                            $(".uploadFileName").hide();
                            $("input[type=file]").val(null);
                            return $("#" + ctrl.id).prop("disabled", false);
                            }
                    })),
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



var alignment_format = function(elem, isInit) {

    if (!isInit) {
        $(elem).niceSelect();
    } else {
        $(elem).niceSelect('update');
    }
    if(this.length == 0) {
        $(".alignment_format").hide();
    }
};



ParameterRadioComponent = {
    view: function(ctrl, args) {
        return renderParameter([
            m("label", {
                "for": args.param.name
            }, args.param.label), args.param.paramType.options.map(function(entry) {
                return m("span", [
                    m("input", {
                        type: "radio",
                        name: args.param.name,
                        value: entry[0]
                    }), entry[1]
                ]);
            })
        ]);
    }
};

ParameterSelectComponent = {
    //not needed so far but is working
    controller: function(args) {
        return {
            preventMultiSelection: (function () {
                var last_valid_selection = null;

                $('.inputDBs').change(function (event) {

                    if ($('#hhsuitedb').val().length > 3) {

                        $('#hhsuitedb').val(last_valid_selection);
                    } else {
                        last_valid_selection = $('#hhsuitedb').val();
                    }
                });
            }).bind(this.mo),

            solveDBSelection: (function () {
                if((($('#hhsuitedb').val() != "") || ($('#proteomes').val() != ""))) {
                    $('.inputDBs').removeAttr("required");
                }

                if((($('#hhsuitedb').val() == "") && ($('#proteomes').val() == ""))) {
                    $('.inputDBs').prop("required", true);
                }
            }).bind(this.mo)
        }
    },

    view: function(ctrl, args) {
        var paramAttrs = {
            name: args.param.name,
            "class": "wide",
            id: args.param.name,
            //if max count of chosen databases is needed
            //onclick: ctrl.preventMultiSelection
            onclick: ctrl.solveDBSelection
        };
        if(args.param.name == "hhsuitedb" || args.param.name == "proteomes") {
            paramAttrs["multiple"] = "multiple";
            paramAttrs["class"] = "inputDBs";
        }else{
            paramAttrs["config"] = selectBoxAccess;
        }
        return renderParameter([
            m("label", {
                "for": args.param.name
            }, args.param.label),
            m("select", paramAttrs,
                args.param.paramType.options.map(function(entry) {
                    return m("option", (args.value.indexOf(entry[0]) > -1 ? {
                            value: entry[0],
                            selected: "selected"
                        } : {
                            value: entry[0]
                        }), entry[1])
                }))
        ]);
    }
};


ParameterNumberComponent = {
    view: function(ctrl, args) {
        var paramAttrs = {
            type: "number",
            id: args.param.name,
            name: args.param.name,
            value: args.value
        };
        // Add minimum and maximum if present
        if(args.param.paramType["min"] != null) {
            paramAttrs["min"] = args.param.paramType["min"];
        }
        if(args.param.paramType["max"]) {
            paramAttrs["max"] = args.param.paramType["max"];
        }
        if(args.param.paramType["step"]) {
            paramAttrs["step"] = args.param.paramType["step"];
        }
        return renderParameter([
            m("label", {
                "for": args.param.name
            }, args.param.label), m("input", paramAttrs)
        ]);
    }
};


ParameterTextComponent = {
    view: function(ctrl, args) {
        var paramAttrs = {
            type: "text",
            id: args.param.name,
            name: args.param.name,
            value: args.value
        };
        return renderParameter([
            m("label", {
                "for": args.param.name
            }, args.param.label), m("input", paramAttrs)
        ]);
    }
};

ParameterBoolComponent = {
    view: function(ctrl, args) {
        return renderParameter([
            m("label", {
                "for": args.param.name
            }, args.label), m("input", {
                type: "checkbox",
                id: args.param.name,
                name: args.param.name,
                value: args.value
            })
        ]);
    }
};

ParameterSlideComponent = {
    model: function(args) {

    },
    controller: function(args){

        this.value = args.value;
        this.config = function (el, isInit, ctx) {
            if (!isInit) {
                $(el).ionRangeSlider({
                    grid: true,
                    values: [0.000000000000000000000000000000000000000000000000001,0.00000000000000000000000000000000000000001,0.000000000000000000000000000001,0.00000000000000000001,0.000000000000001,0.0000000001,0.00000001,0.000001, 0.0001, 0.001, 0.01, 0.02, 0.05, 0.1],
                    grid_snap: true,
                    keyboard: true
                })
            }
        }.bind(this)

    },
    view: function (ctrl, args) {
        var paramAttrs = {
            type: "range",
            id: args.param.name,
            name: args.param.name,
            value: ctrl.value,
            config: ctrl.config
        };
        // Add minimum and maximum if present
        if(args.param.paramType["max"]) {
            paramAttrs["max"] = args.param.paramType["max"];
        }
        if(args.param.paramType["min"]) {
            paramAttrs["min"] = args.param.paramType["min"];
        }
        return renderParameter([
            m("label", args.value),
            m("input", paramAttrs)
        ])

    }

};

formComponents = {
    1: ParameterAlignmentComponent,
    2: ParameterNumberComponent,
    3: ParameterSelectComponent,
    4: ParameterBoolComponent,
    5: ParameterRadioComponent,
    6: ParameterSlideComponent,
    7: ParameterTextComponent
};