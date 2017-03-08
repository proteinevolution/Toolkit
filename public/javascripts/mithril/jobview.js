var JobErrorComponent, jobNoteArea, JobLineComponent, JobQueuedComponent, JobSubmissionComponent, JobTabsComponent, ParameterBoolComponent, ParameterNumberComponent, ParameterRadioComponent, ParameterSelectComponent, ParameterSlideComponent, SearchformComponent, alignmentUpload, closeShortcut, exampleSequence, formComponents, foundationConfig, helpModalAccess, mapParam, renderParameter, selectBoxAccess, submitModal, tabulated ;

exampleSequence = ">NP_877456#7 putative ATP-dependent DNA ligase [Bacteriophage phiKMV]\nPEITVDGRIVGYVMGKTG-KNVGRVVGYRVELEDGSTVAATGLSEE\n>CAK25951#9 putative ATP-dependent DNA ligase [Bacteriophage LKD16]\nPSLAVEGIVVGFVMGKTG-ANVGKVVGYRVDLEDGTIVSATGLTRD\n>CAK24995#5 putative DNA ligase [Bacteriophage LKA1]   E=4e-40 s/c=1.7\nPGFEADGTVIDYVWGDPDKANANKIVGFRVRLEDGAEVNATGLTQD\n>NP_813751#8 putative DNA ligase [Pseudomonas phage gh-1]   gi|29243565\nPDDNEDGFIQDVIWGTKGLANEGKVIGFKVLLESGHVVNACKISRA\n>YP_249578#6 DNA ligase [Vibriophage VP4]   gi|66473268|gb|AAY46277.1|\nPEGEIDGTVVGVNWGTVGLANEGKVIGFQVLLENGVVVDANGITQE\n>YP_338096#3 ligase [Enterobacteria phage K1F]   gi|72527918|gb|AAZ7297\nPSEEADGHVVRPVWGTEGLANEGMVIGFDVMLENGMEVSATNISRA\n>NP_523305#4 DNA ligase [Bacteriophage T3]   gi|118769|sp|P07717|DNLI_B\nPECEADGIIQGVNWGTEGLANEGKVIGFSVLLETGRLVDANNISRA\n>YP_91898#2 DNA ligase [Yersinia phage Berlin]   gi|119391784|emb|CAJ\nPECEADGIIQSVNWGTPGLSNEGLVIGFNVLLETGRHVAANNISQT";


helpModalAccess = function(elem, isInit) {
    if (!isInit) {
        return elem.setAttribute("data-open", "help-" + (this.job().tool.toolname));
    }
};

sliderAccess = function(elem, isInit) {
    if (!isInit) {
        return $(elem).ionRangeSlider({
            grid: true,
            values: [0.000000000000000000000000000000000000000000000000001,0.00000000000000000000000000000000000000001,0.000000000000000000000000000001,0.00000000000000000001,0.000000000000001,0.0000000001,0.00000001,0.000001, 0.0001, 0.001, 0.01, 0.02, 0.05, 0.1],
            grid_snap: true,
            keyboard: true
        })
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
                m(JobLineComponent, {
                    job: args.job
                }), m(JobTabsComponent, {
                    owner: args.owner,
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
            }, isJob ? "JobID: " + (args.job().jobID) : "Submit a new Job")
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

foundationConfig = function(elem, isInit) {
    if (!isInit) {
        return $(elem).foundation();
    }
};


jobNoteArea = function(elem, isInit) {
    if (!isInit) {
        $.ajax({
            url: '/api/jobs/getnotes/' + $(elem).attr('id').substring(7),
            type: 'get',
            success: function(data) {
                $(elem).html(data);
            }
        });
        return $(elem).keyup(function(e) {
            var contentString;
            contentString = $(this).val();
            $.post(jsRoutes.controllers.Jobs.annotation($(this).attr('id').substring(7), contentString), function(response) {
                console.log('Response: ' + response);
            });
        });
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
                config: foundationConfig
            }, m("tbody", [m("tr", [m("td", "JobID"), m("td", args.job().jobID)]), m("tr", [m("td", "Created On"), m("td", args.job().createdOn)])]))
        ]);
    }
};

JobRunningComponent = {
    view: function(ctrl, args) {
        return m("div", {
            "class": "running-panel"
        }, [
            m("table", {
                config: foundationConfig
            }, m("tbody", [m("tr", [m("td", "JobID"), m("td", args.job().jobID)]), m("tr", [m("td", "Created On"), m("td", args.job().createdOn)])]))
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
            owner: args.owner,
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
                }), document.cookie.split("&username=")[1] === ctrl.owner ? [ m("li", {
                        "class" : "notesheader"
                        //"style" : "display: none;"
                    }, m("a", {
                        href: "#tabpanel-notes"
                    }, "Notes")) ] : [] ,
                m("li", {
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
            ]), document.cookie.split("&username=")[1] === ctrl.owner ? [ m("div", {"class" : "tab-panel parameter-panel",
                    id: "tabpanel-notes"}, [
                    m("textarea", {
                        placeholder: "Type private notes here",
                        rows: 18,
                        cols: 70,
                        id: "notepad" + ctrl.job().jobID,
                        spellcheck: true,
                        config: jobNoteArea
                    })
                ]) ] : [],
            m("form", {
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
                        }, paramGroup[0] === "Input" ? elements[0].name === "alignment" ? [
                                    m("div", {
                                        "class": "row"
                                    }, m("div", {
                                        "class": "small-12 large-12 medium-12 columns"
                                    }, mapParam(elements[0], ctrl))), m("div", {
                                        "class": "row small-up-1 medium-up-2 large-up-3"
                                    }, elements.slice(1).map(function(param) {
                                        return m("div", {
                                            "class": "column column-block"
                                        }, mapParam(param, ctrl));
                                    }))
                                ] : m("div", {
                                    "class": "row small-up-1 medium-up-2 large-up-3"
                                }, elements.map(function(param) {
                                    return m("div", {
                                        "class": "column column-block"
                                    }, mapParam(param, ctrl));
                                })) : m("div", {
                                "class": "row small-up-1 medium-up-2 large-up-3"
                            }, elements.map(function(param) {
                                return m("div", {
                                    "class": "column column-block"
                                }, mapParam(param, ctrl));
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

                // appendParentID if in storage
                var parentid = localStorage.getItem("parentid");
                if(!parentid) {
                    parentid = '';
                }
                formData.append('parentid', parentid);
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
                    console.warn("Bad Request");
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
                'data-overlay': 'true',
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

/*
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
 }; */

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
                    $("#hhpredAlign").prop('checked', true);
                    $("#alignment").attr("rows", "8");
                    $('#alignment2').show();
                    $("#alignment2").prop("required");
                } else {
                    $(".inputDBs").prop('disabled', false);
                    $("#hhpredAlign").prop('checked', false);
                    $("#alignment").attr("rows", "19");
                    $("#alignment2").hide();
                    $("#alignment2").removeAttr("required");
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
                        $("#hhpredAlign").prop('checked', true);
                        $("#alignment").attr("rows", "8");
                        $('#alignment2').show();
                        $("#alignment2").prop("required");
                    } else {
                        $(".inputDBs").prop('disabled', false);
                        $("#hhpredAlign").prop('checked', false);
                        $("#alignment").attr("rows", "19");
                        $("#alignment2").hide();
                        $("#alignment2").removeAttr("required");
                    }
                }
            }
        };
        if(ctrl.getAllowsTwoTextAreas()) {
            var mbreak = m("br");
            var checkbox = m("label", "Align two or more sequences",
                m("input", {
                    type: "checkbox",
                    id: "hhpredAlign",
                    name: "hhpred_align",
                    value: "true",
                    config: params.oninit,
                    onclick: function() {
                        ctrl.toggleTwoTextAreas();
                    },
                }));

            var textArea2 =
            m("textarea", {
                name: ctrl.name+"_two",
                placeholder: ctrl.getLabel(),
                rows: 8,
                cols: 70,
                id: ctrl.id + "2",
                value: args.value,
                style: "display: none",
                spellcheck: false,
                config: validation
            });
            var mbreak = m("br");
        };

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
                }), mbreak,
                textArea2),
            m("div", {
                id: "upload_alignment_modal",
                "class": "tiny reveal",
                config: alignmentUpload
            }, m("input", {
                type: "file",
                id: "upload_alignment_input",
                name: "upload_alignment_input"
            })), m("div", {
                "class": "alignment_buttons"
            }, [
                m("input", {
                    type: "button",
                    "class": "button small alignmentExample",
                    value: "Paste Example",
                    onclick: function() {
                        $("#" + ctrl.id).val(exampleSequence);
                    }
                }), m("input", {
                    type: "button",
                    "class": "button small alignmentExample",
                    value: "Upload File",
                    onclick: function() {
                        return $('#upload_alignment_modal').foundation('open');
                    }
                }), m("select", {"class": "alignment_mode", onchange: m.withAttr("value", ctrl.setMode), config: selectBoxAccess}, ctrl.getModes().map(function(mode){
                    return m("option", {value: mode.mode}, mode.name)}

                )), m("select", {"id": "alignment_format", "class": "alignment_format", config: alignment_format.bind(ctrl.getFormats())}, ctrl.getFormats().map(function(format){
                    return m("option", {value: format[0]}, format[1])}
                )),
                checkbox
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
    view: function(ctrl, args) {
        var paramAttrs = {
            name: args.param.name,
            "class": "wide",
            id: args.param.name
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
    view: function(ctrl, args) {
        var paramAttrs = {
            type: "number",
            id: args.param.name,
            name: args.param.name,
            value: args.value
        };
        // Add minimum and maximum if present
        if(args.param.paramType["max"]) {
            paramAttrs["max"] = args.param.paramType["max"];
        }
        if(args.param.paramType["min"]) {
            paramAttrs["min"] = args.param.paramType["min"];
        }
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
    view: function (ctrl, args) {
        var paramAttrs = {
            type: "range",
            id: args.param.name,
            name: args.param.name,
            value: args.value,
            config: sliderAccess
        };
        // Add minimum and maximum if present
        if(args.param.paramType["max"]) {
            paramAttrs["max"] = args.param.paramType["max"];
        }
        if(args.param.paramType["min"]) {
            paramAttrs["min"] = args.param.paramType["min"];
        }
        return renderParameter([
            m("label", args.param.label),
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
    6: ParameterSlideComponent
};