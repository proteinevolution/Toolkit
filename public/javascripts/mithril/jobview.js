var JobErrorComponent, JobLineComponent, JobQueuedComponent, JobSubmissionComponent, JobTabsComponent, ParameterBoolComponent, ParameterNumberComponent, ParameterRadioComponent, ParameterRangeSliderComponent, ParameterSelectComponent, SearchformComponent, alignmentUpload, closeShortcut, dropzone_psi, exampleSequence, formComponents, foundationTable, helpModalAccess, mapParam, renderParameter, selectBoxAccess, submitModal, tabulated ;

exampleSequence = ">NP_877456#7 putative ATP-dependent DNA ligase [Bacteriophage phiKMV]\nPEITVDGRIVGYVMGKTG-KNVGRVVGYRVELEDGSTVAATGLSEE\n>CAK25951#9 putative ATP-dependent DNA ligase [Bacteriophage LKD16]\nPSLAVEGIVVGFVMGKTG-ANVGKVVGYRVDLEDGTIVSATGLTRD\n>CAK24995#5 putative DNA ligase [Bacteriophage LKA1]   E=4e-40 s/c=1.7\nPGFEADGTVIDYVWGDPDKANANKIVGFRVRLEDGAEVNATGLTQD\n>NP_813751#8 putative DNA ligase [Pseudomonas phage gh-1]   gi|29243565\nPDDNEDGFIQDVIWGTKGLANEGKVIGFKVLLESGHVVNACKISRA\n>YP_249578#6 DNA ligase [Vibriophage VP4]   gi|66473268|gb|AAY46277.1|\nPEGEIDGTVVGVNWGTVGLANEGKVIGFQVLLENGVVVDANGITQE\n>YP_338096#3 ligase [Enterobacteria phage K1F]   gi|72527918|gb|AAZ7297\nPSEEADGHVVRPVWGTEGLANEGMVIGFDVMLENGMEVSATNISRA\n>NP_523305#4 DNA ligase [Bacteriophage T3]   gi|118769|sp|P07717|DNLI_B\nPECEADGIIQGVNWGTEGLANEGKVIGFSVLLETGRLVDANNISRA\n>YP_91898#2 DNA ligase [Yersinia phage Berlin]   gi|119391784|emb|CAJ\nPECEADGIIQSVNWGTPGLSNEGLVIGFNVLLETGRHVAANNISQT";

;

helpModalAccess = function(elem, isInit) {
    if (!isInit) {
        return elem.setAttribute("data-open", "help-" + (this.job().tool.toolname));
    }
};

selectBoxAccess = function(elem, isInit) {
    if (!isInit) {
        return $(elem).niceSelect();
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
                m(JobLineComponent, {
                    job: args.job
                }), m(JobTabsComponent, {
                    job: args.job,
                    add: args.add
                })
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
            m(HelpModalComponent, {
                toolname: args.job().tool.toolname,
                toolnameLong: args.job().tool.toolnameLong
            }), m("span", {
                "class": "toolname"
            }, [
                args.job().tool.toolnameLong, m("a", {
                    config: helpModalAccess.bind(args)
                }, m("i", {
                    "class": "icon-information_white helpicon"
                }))
            ]), m("span", {
                "class": "jobdate"
            }, isJob ? "Created: " + (args.job().createdOn) : ""), m("span", {
                "class": "jobinfo"
            }, isJob ? "JobID: " + (args.job().jobID) : "Submit a new Job"), m("span", {
                "class": "ownername"
            }, args.job().ownerName ? args.job().ownerName : "")
        ]);
    }
};

SearchformComponent = {
    view: function() {
        return m("div", {
            id: "jobsearchform"
        }, m("input", {
            type: "text",
            placeholder: "Search by JobID, e.g. 6881313",
            id: "jobsearch"
        }));
    }
};

foundationTable = function(elem, isInit) {
    if (!isInit) {
        return $(elem).foundation();
    }
};

JobErrorComponent = {
    view: function() {
        return m("div", {
            "class": "error-panel"
        }, m("p", "Job has reached Error state"));
    }
};

JobQueuedComponent = {
    view: function(ctrl, args) {
        return m("div", {
            "class": "queued-panel"
        }, [
            m("table", {
                config: foundationTable
            }, m("tbody", [m("tr", [m("td", "MainID"), m("td", args.job().mainID)]), m("tr", [m("td", "JobID"), m("td", args.job().jobID)]), m("tr", [m("td", "Created On"), m("td", args.job().createdOn)])]))
        ]);
    }
};

JobRunningComponent = {
    view: function(ctrl, args) {
        return m("div", {
            "class": "running-panel"
        }, [
            m("table", {
                config: foundationTable
            }, m("tbody", [m("tr", [m("td", "MainID"), m("td", args.job().mainID)]), m("tr", [m("td", "JobID"), m("td", args.job().jobID)]), m("tr", [m("td", "Created On"), m("td", args.job().createdOn)])]))
        ]);
    }
};

tabulated = function(element, isInit) {
    if (!isInit) {
        return $(element).tabs({
            active: this.active
        });
    }
};

renderParameter = function(content, moreClasses) {
    return m("div", {
        "class": moreClasses ? "parameter " + moreClasses : "parameter"
    }, content);
};

mapParam = function(paramElem, ctrl) {
    var comp, ctrlArgs;
    ctrlArgs = {
        paramName: paramElem[0],
        options: paramElem[1],
        paramType: paramElem[2],
        label: paramElem[3],
        value: ctrl.getParamValue(paramElem[0]),
        toolname: ctrl.job().tool.toolname
    };
    comp = formComponents[paramElem[2].type](ctrlArgs);
    return m(comp[0], comp[1]);
};

closeShortcut = function(element, isInit) {
    if (!isInit) {

    }
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
                    this.label = "Expand";
                    if (typeof onCollapse === "function") {
                        onCollapse();
                    }
                } else {
                    job_tab_component.addClass("fullscreen");
                    this.isFullscreen = true;
                    this.label = "Collapse";
                    if (typeof onExpand === "function") {
                        onExpand();
                    }
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
                    return Job["delete"](jobID);
                }
            }
        };
    },
    view: function(ctrl, args) {
        return m("div", {
            "class": "tool-tabs",
            id: "tool-tabs",
            config: tabulated.bind(ctrl)
        }, [
            m("ul", [
                ctrl.listitems.map(function(item) {
                    return m("li", {
                        id: "tab-" + item
                    }, m("a", {
                        href: "#tabpanel-" + item
                    }, item));
                }), m("li", {
                    style: "float: right;"
                }, m("input", {
                    type: "button",
                    id: "collapseMe",
                    "class": "button small button_fullscreen",
                    value: ctrl.getLabel(),
                    onclick: ctrl.fullscreen,
                    config: closeShortcut
                })), ctrl.isJob ? m("li", {
                        style: "float: right;"
                    }, m("input", {
                        type: "button",
                        "class": "button small delete",
                        value: "Delete Job",
                        onclick: ctrl["delete"].bind(ctrl)
                    })) : void 0
            ]), m("form", {
                id: "jobform"
            }, ctrl.params.map(function(paramGroup) {
                var elements;
                if (paramGroup[1].length !== 0) {
                    elements = paramGroup[1];
                    return m("div", {
                        "class": "tabs-panel parameter-panel",
                        id: "tabpanel-" + paramGroup[0]
                    }, [
                        m("div", {
                            "class": "parameters"
                        }, paramGroup[0] === "Input" ? elements[0][0] === "alignment" ? [
                                    m("div", {
                                        "class": "row"
                                    }, m("div", {
                                        "class": "small-12 large-12 medium-12 columns"
                                    }, mapParam(elements[0], ctrl))), m("div", {
                                        "class": "row small-up-1 medium-up-2 large-up-3"
                                    }, elements.slice(1).map(function(paramElem) {
                                        return m("div", {
                                            "class": "column column-block"
                                        }, mapParam(paramElem, ctrl));
                                    }))
                                ] : m("div", {
                                    "class": "row small-up-1 medium-up-2 large-up-3"
                                }, elements.map(function(paramElem) {
                                    return m("div", {
                                        "class": "column column-block"
                                    }, mapParam(paramElem, ctrl));
                                })) : m("div", {
                                "class": "row small-up-1 medium-up-2 large-up-3"
                            }, elements.map(function(paramElem) {
                                return m("div", {
                                    "class": "column column-block"
                                }, mapParam(paramElem, ctrl));
                            }))), m(JobSubmissionComponent, {
                            job: ctrl.job,
                            isJob: ctrl.isJob,
                            add: args.add
                        })
                    ]);
                }
            }), ctrl.isJob && ctrl.state === 2 ? m("div", {
                    "class": "tabs-panel",
                    id: "tabpanel-Queued"
                }, m(JobQueuedComponent, {
                    job: ctrl.job
                })) : void 0, ctrl.isJob && ctrl.state === 3 ? m("div", {
                    "class": "tabs-panel",
                    id: "tabpanel-Running"
                }, m(JobRunningComponent, {
                    job: ctrl.job
                })) : void 0, ctrl.isJob && ctrl.state === 4 ? m("div", {
                    "class": "tabs-panel",
                    id: "tabpanel-Error"
                }, JobErrorComponent) : void 0), ctrl.views ? ctrl.views.map(function(view) {
                    return m("div", {
                        "class": "tabs-panel",
                        id: "tabpanel-" + view[0]
                    }, m("div", {
                        "class": "result-panel"
                    }, m.trust(view[1])));
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

JobSubmissionComponent = {
    controller: function(args) {
        this.submitting = false;
        return {
            setJobID: (function(jobID) {
                return this.jobID = jobID;
            }).bind(args.job()),
            submit: function(startJob) {

                var form = document.getElementById("jobform");
                if(!form.checkValidity()) {
                    alert("Parameters are invalid");
                    return
                }

                var checkRoute, formData, jobID, toolname;
                toolname = args.job().tool.toolname;
                jobID = args.job().jobID;
                if (!jobID) {
                    jobID = null;
                }
                // Use check route and specify that the hashing function should be used
                checkRoute = jsRoutes.controllers.JobController.check(toolname, jobID, true);
                formData = new FormData(form);
                $(".submitJob").prop("disabled", true);
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
                            sendMessage({
                                type: "RegisterJobs",
                                "jobIDs": [jobID]
                            });
                            Job.add(new Job({
                                mainID: jobID,
                                jobID: jobID,
                                state: 0,
                                createdOn: 'now',
                                toolname: toolname
                            }));
                            submitRoute = jsRoutes.controllers.JobController.create(toolname, jobID);
                            m.request({
                                url: submitRoute.url,
                                method: submitRoute.method,
                                data: formData,
                                serialize: function(data) {
                                    return data;
                                }
                            });
                            return m.route("/jobs/" + jobID);
                        });
                        return $('#submit_modal').foundation('open');
                    } else {
                        sendMessage({
                            type: "RegisterJobs",
                            "jobIDs": [jobID]
                        });
                        Job.add(new Job({
                            mainID: jobID,
                            jobID: jobID,
                            state: 0,
                            createdOn: 'now',
                            toolname: toolname
                        }));
                        submitRoute = jsRoutes.controllers.JobController.create(toolname, jobID);
                        m.request({
                            method: submitRoute.method,
                            url: submitRoute.url,
                            data: formData,
                            serialize: function(data) {
                                return data;
                            }
                        });
                        return m.route("/jobs/" + jobID);
                    }
                }, function(error) {
                    alert("Bad Request");
                    return $(".submitJob").prop("disabled", false);
                });
            }
        };
    },
    view: function(ctrl, args) {
        return m("div", {
            "class": "submitbuttons"
        }, [
            m("div", {
                "class": "reveal",
                'data-reveal': 'data-reveal',
                'data-animation-in': 'fade-in',
                'transition-duration': 'fast',
                id: 'submit_modal',
                config: submitModal
            }, m("p", "Already existing job found!"), m("input", {
                "class": 'button',
                id: 'reload_job',
                type: 'button',
                value: 'Reload'
            }), m("input", {
                "class": 'button',
                id: 'submit_again',
                type: 'button',
                value: 'New Submission'
            })), !this.submitting ? m("input", {
                    type: "button",
                    "class": "success button small submitJob",
                    value: (args.isJob ? "Res" : "S") + "ubmit Job",
                    onclick: ctrl.submit.bind(ctrl, true)
                }) : null, !args.isJob ? m("label", {
                    hidden: "hidden"
                }, [
                    m("input", {
                        type: "checkbox",
                        name: "private",
                        value: "true",
                        checked: "checked",
                        hidden: "hidden"
                    }), "Private"
                ]) : null, args.isJob && args.job().jobstate === 1 ? m("input", {
                    type: "button",
                    "class": "button small addJob",
                    value: "Start Job",
                    onclick: ctrl.startJob
                }) : null, args.isJob ? m("input", {
                    type: "button",
                    "class": "button small addJob",
                    value: "Add Job",
                    onclick: ctrl.addJob
                }) : null, m("input", {
                type: "text",
                id: "jobID",
                "class": "jobid",
                placeholder: "Custom JobID",
                onchange: m.withAttr("value", ctrl.setJobID),
                value: args.job().jobID
            }), m("input", {
                type: "text",
                "class": "jobid",
                placeholder: "E-Mail Notification",
                style: "width: 16em; float: right;"
            })
        ]);
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

dropzone_psi = function(element, isInit) {
    var handleDragOver, handleFileSelect;
    handleFileSelect = function(evt) {
        var f, files, i, output;
        evt.stopPropagation();
        evt.preventDefault();
        files = evt.dataTransfer.files;
        output = [];
        i = 0;
        f = void 0;
        while (f = files[i]) {
            output.push('<li><strong>', escape(f.name), '</strong> (', f.type || 'n/a', ') - ', f.size, ' bytes, last modified: ', f.lastModifiedDate.toLocaleDateString(), '</li>');
            i++;
        }
        document.getElementById('list').innerHTML = '<ul>' + output.join('') + '</ul>';
    };
    handleDragOver = function(evt) {
        evt.stopPropagation();
        evt.preventDefault();
        evt.dataTransfer.dropEffect = 'copy';
    };
    if (!isInit) {
        $(element).addEventListener('dragover', handleDragOver, false);
        return $(element).addEventListener('drop', handleFileSelect, false);
    }
};

window.ParameterAlignmentComponent = {
    model: function(args) {
        return {
            value: args.value,
            format: null
        };
    },
    controller: function(args) {
        this.param = new ParameterAlignmentComponent.model(args);
        return {
            name: "alignment",
            id: "alignment",
            placeholder: "Enter multiple sequence alignment",
            formatOptions: args.options,
            param: this.param
        };
    },
    view: function(ctrl) {
        return renderParameter([
            m("div", {
                "class": "alignment_textarea"
            }, m("textarea", {
                name: ctrl.name,
                placeholder: ctrl.placeholder,
                rows: 15,
                cols: 70,
                id: ctrl.id,
                onchange: m.withAttr("value", ctrl.param.value),
                value: ctrl.param.value,
                required: "required",
                spellcheck: false
            })), m("div", {
                id: "upload_alignment_modal",
                "class": "tiny reveal",
                config: alignmentUpload
            }, m("input", {
                type: "file",
                id: "upload_alignment_input",
                name: "upload_alignment_input",
                onchange: function() {
                    if (this.value) {
                        return $("#" + ctrl.id).prop("disabled", true);
                    }
                }
            })), m("div", {
                "class": "alignment_buttons"
            }, [
                m("input", {
                    type: "button",
                    "class": "button small alignmentExample",
                    value: "Paste Example",
                    onclick: function() {
                        return ctrl.param.value = exampleSequence;
                    }
                }), m("input", {
                    type: "button",
                    "class": "button small alignmentExample",
                    value: "Upload File",
                    onclick: function() {
                        return $('#upload_alignment_modal').foundation('open');
                    }
                })
            ])
        ], "alignmentParameter");
    }
};

ParameterRadioComponent = {
    view: function(ctrl, args) {
        return renderParameter([
            m("label", {
                "for": args.id
            }, args.label), args.options.map(function(entry) {
                return m("span", [
                    m("input", {
                        type: "radio",
                        name: args.name,
                        value: entry[0]
                    }), entry[1]
                ]);
            })
        ]);
    }
};

ParameterSelectComponent = {
    view: function(ctrl, args) {
        return renderParameter([
            m("label", {
                "for": args.id
            }, args.label), m("select", {
                name: args.name,
                "class": "wide",
                id: args.id,
                config: selectBoxAccess
            }, args.options.map(function(entry) {
                return m("option", (entry[0] === args.value ? {
                        value: entry[0],
                        selected: "selected"
                    } : {
                        value: entry[0]
                    }), entry[1]);
            }))
        ]);
    }
};

ParameterNumberComponent = {
    controller: function(args) {
        this.value = args.value;
        return {
            getValue: (function() {
                return this.value;
            }).bind(this),
            validate: (function(val) {
                return this.value = val;
            }).bind(this)
        };
    },
    view: function(ctrl, args) {
        var paramAttrs = {
            type: "number",
            id: args.id,
            name: args.name,
            value: ctrl.getValue(),
            onchange: m.withAttr("value", ctrl.validate)
        };
        // Add minimum and maximum if present
        if(args.paramType["max"]) {
            paramAttrs["max"] = args.paramType["max"];
        }
        if(args.paramType["min"]) {
            paramAttrs["min"] = args.paramType["min"];
        }
        return renderParameter([
            m("label", {
                "for": args.id
            }, args.label), m("input", paramAttrs)
        ]);
    }
};

ParameterBoolComponent = {
    view: function(ctrl, args) {
        return renderParameter([
            m("label", {
                "for": args.id
            }, args.label), m("input", {
                type: "checkbox",
                id: args.id,
                name: args.name,
                value: args.value
            })
        ]);
    }
};

ParameterRangeSliderComponent = {
    view: function(ctrl, args) {
        return renderParameter([
            m("div", {
                "class": "small-10 columns"
            }, [
                m("div", {
                    "class": "slider"
                }, [
                    m("span", {
                        "class": "slider-handle",
                        tabindex: "1"
                    }), m("span", {
                        "class": "slider-fill"
                    })
                ])
            ])
        ]);

        /*
         <div class="small-10 columns">
         <div class="slider" data-slider data-initial-start="50" data-step="5">
         <span class="slider-handle"  data-slider-handle role="slider" tabindex="1" aria-controls="sliderOutput2"></span>
         <span class="slider-fill" data-slider-fill></span>
         </div>
         </div>
         <div class="small-2 columns">
         <input type="number" id="sliderOutput2">
         </div>
         */
    }
};

formComponents = {
    1: function(args) {
        return [
            ParameterAlignmentComponent, {
                options: args.options,
                value: args.value,
                paramType: args.paramType
            }
        ];
    },
    2: function(args) {
        return [
            ParameterNumberComponent, {
                options: args.options,
                name: args.paramName,
                id: args.paramName,
                label: args.label,
                value: args.value,
                paramType: args.paramType
            }
        ];
    },
    3: function(args) {
        return [
            ParameterSelectComponent, {
                options: args.options,
                name: args.paramName,
                id: args.paramName,
                label: args.label,
                value: args.value,
                paramType: args.paramType
            }
        ];
    },
    4: function(args) {
        return [
            ParameterBoolComponent, {
                options: args.options,
                name: args.paramName,
                id: args.paramName,
                label: args.label,
                value: args.value,
                paramType: args.paramType
            }
        ];
    },
    5: function(args) {
        return [
            ParameterRadioComponent, {
                name: args.paramName,
                id: args.paramName,
                label: args.label,
                value: args.value,
                paramType: args.paramType
            }
        ];
    }
};