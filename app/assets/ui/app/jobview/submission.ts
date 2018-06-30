(<any>window).JobSubmissionComponent = {
    submitting      : false,    // Job is being sent if true
    oldJobID        : null,     // jobID of the old job, if there is an old job
    currentJobID    : null,     // Currently entered jobID
    jobIDValid      : false,    // Is the current jobID valid?
    jobIDValidationTimeout : null,     // timer ID for the timeout
    jobIDRegExp     : new RegExp(/^([0-9a-zA-Z_]{3,96})(?:_([0-9]{1,3}))?$/),
    checkJobID : function (jobID : string) {
        // ensure that the user can not send the job form
        JobSubmissionComponent.jobIDValid = false;
        // clear all previous timeouts
        clearTimeout(JobSubmissionComponent.jobIDValidationTimeout);
        // ignore checking if the field is empty as the server will generate a jobID in that case.
        if (jobID === "" || jobID == null) {
            JobSubmissionComponent.currentJobID = "";
            JobSubmissionComponent.jobIDValid   = true;
        } else {
            // set the jobID to the new jobID
            JobSubmissionComponent.currentJobID = jobID;
            if (JobSubmissionComponent.jobIDRegExp.test(jobID)) {   // Check if the JobID is passing the Regex
                JobSubmissionComponent.checkJobIDWithRequest();
            }
        }
        return JobSubmissionComponent.currentJobID;
    },
    checkJobIDTimed : function (timeout : number) : Function {
        return function(jobID : string) : string {
            // ensure that the user can not send the job form
            JobSubmissionComponent.jobIDValid = false;
            // clear all previous timeouts
            clearTimeout(JobSubmissionComponent.jobIDValidationTimeout);
            if (jobID === "" || jobID == null) {
                JobSubmissionComponent.currentJobID = "";
                JobSubmissionComponent.jobIDValid   = true;
            } else {
                // set the jobID to the new jobID
                JobSubmissionComponent.currentJobID = jobID;
                // ignore checking if the field is empty as the server will generate a jobID in that case.
                if (JobSubmissionComponent.jobIDRegExp.test(jobID)) {   // Check if the JobID is passing the Regex
                    JobSubmissionComponent.jobIDValidationTimeout = setTimeout(function (a) {   // create the timeout
                        JobSubmissionComponent.checkJobIDWithRequest();
                    }, timeout);
                }
            }
            return JobSubmissionComponent.currentJobID;
        }
    },
    checkJobIDWithRequest : function () {
        m.request({
            method: "GET",
            url: "/api/jobs/check/jobid/"
                + JobSubmissionComponent.currentJobID
                + "/?resubmitJobID="
                + JobSubmissionComponent.oldJobID
        }).then(
            function (data : any) {
                JobSubmissionComponent.jobIDValid = !data.exists;
                if (data.exists && data.suggested != null) {
                    JobSubmissionComponent.currentJobID = data.suggested;
                    JobSubmissionComponent.jobIDValid = true;
                }
                console.log("Current JobID is:",    JobSubmissionComponent.currentJobID,
                    "Old jobID is:",        JobSubmissionComponent.oldJobID,
                    "Suggested version:",   data.version,
                    "Current jobID Valid?", JobSubmissionComponent.jobIDValid);
            },
            function(data : any) {
            }
        );
    },
    jobIDComponent : function (ctrl : any) {
        let style: string = "jobID";

        style += JobSubmissionComponent.currentJobID === "" ? " white" :
            (JobSubmissionComponent.jobIDValid ? " green" : " red");

        let options: any = {
            type:        "text",
            id:          "jobID",
            "class":     style,
            placeholder: "Custom JobID",
            onkeyup:     m.withAttr("value", JobSubmissionComponent.checkJobIDTimed(800)),
            value:       JobSubmissionComponent.currentJobID
        };

        return m("input", options);
    },
    controller: function(args : any) {
        if (args.isJob) {
            if (JobSubmissionComponent.oldJobID !== args.job().jobID) {
                JobSubmissionComponent.currentJobID = null;
                JobSubmissionComponent.oldJobID     = null;
            }
        } else {
            JobSubmissionComponent.oldJobID = null;
        }
        if (JobSubmissionComponent.currentJobID == null) {
            if (args.isJob) {
                JobSubmissionComponent.oldJobID = args.job().jobID;
                JobSubmissionComponent.checkJobID(JobSubmissionComponent.oldJobID); // ask server for new jobID
            } else {
                JobSubmissionComponent.checkJobID("");
            }
        }
        return {
            submit: function(startJob : boolean) {
                if (!JobSubmissionComponent.jobIDValid) {
                    console.log("[Job Submission] failed to submit - jobID is invalid: ", JobSubmissionComponent.submitting);
                    return;
                }
                if (JobSubmissionComponent.submitting) {
                    console.log("[Job Submission] failed to submit - Already submitting: ", JobSubmissionComponent.submitting);
                    return;
                }
                // ensure that the submission is not reinitiated while a submission is ongoing
                JobSubmissionComponent.submitting = true;

                let submitRoute : any, formData : FormData, jobID : string, tool : string, submitButton : JQuery, form : any;
                form = document.getElementById("jobform");
                if(!form.checkValidity()) {
                    JobSubmissionComponent.submitting = false;
                    (<any>window).AlertService.alert("Parameters are invalid", "alert-danger");
                    return;
                }

                // disable submit button
                submitButton = $(".submitJob");
                submitButton.prop("disabled", true);
                tool = args.job().tool.toolname;
                jobID = JobSubmissionComponent.currentJobID;
                // collect form data
                formData = new FormData(form);
                formData.append("tool", tool);

                if ((jobID != null) && (jobID !== "")) { formData.append("jobID", jobID); }

                // Append file to upload
                let file = ((<any>$("input[type=file]"))[0].files[0]);
                if (file) formData.append("file", file);
                m.request({
                    method: "POST",
                    url: "/api/jobs/?toolName=" + tool,
                    data: formData,
                    serialize: function(submissionReturnData) {
                        return submissionReturnData;
                    }
                }).then(function(submissionReturnData : any){
                    if (submissionReturnData.successful) {
                        jobID = submissionReturnData.jobID;
                        // listen for this job
                        Toolkit.trackedJobIDs.push(jobID);
                        let jobListComp = JobListComponent.Job(
                            { jobID: jobID, state: 0, dateCreated: Date.now().valueOf(), tool: tool }
                        );
                        if (!JobListComponent.contains(jobID)) {
                            JobListComponent.pushJob(jobListComp, true);
                        }
                    } else {
                        /* TODO handling needs to be reworked since the old controller is gone */
                        console.log("Error while submitting:", submissionReturnData.message);
                        switch(submissionReturnData.code) {
                            case 2:
                                JobSubmissionComponent.jobIDValid = false;
                                break;
                            case 4:
                                JobSubmissionComponent.jobIDValid = false;
                                break;
                            default:
                                // Add more error handling stuff here
                                break;
                        }
                    }
                    $(".submitJob").prop("disabled", false);
                    JobSubmissionComponent.submitting = false;
                },function(error : any){
                    console.log("Error while submitting:", error);
                    $(".submitJob").prop("disabled", false);
                    JobSubmissionComponent.submitting = false;
                });
                return;
            }
        };
    },
    hide : function(ctrl : any, args: any) {
        return function (elem : any, isInit : any) {
            if (!isInit) {
                $("#uploadBoxClose").hide();
                $(".uploadFileName").hide();
                // hide submitbuttons
                if (args.job().jobstate > -1)
                    $(elem).hide();
            }
        }
    },
    view: function(ctrl : any, args : any) {
        return m("div", { "class":  "submitbuttons", config: JobSubmissionComponent.hide(ctrl, args) }, [
            m("div", {
                "class":               "reveal",
                "data-reveal":         "data-reveal",
                "data-animation-in":   "fade-in",
                "data-overlay":        "true",
                "transition-duration": "fast",
                id:                    "submit_modal",
                config:                submitModal
            }, [
                m("p", "Already existing job found!"),
                m("input", { "class": 'button', id: 'reload_job', type: 'button', value: 'Reload' }),
                m("input", { "class": 'button', id: 'submit_again', type: 'button', value: 'New Submission' })
            ]),
            Auth.user == null ? null :
           m("div", {"class": "switchContainer"},
                m("label", {"class": "switch tiny"},
                    m("input", {
                        id: "emailUpdate",
                        type: "checkbox",
                        "class": "checkbox",
                        name: "emailUpdate",
                        value: true}),
                    m("div", {"class": "sliderSwitch round"})
                ),
                m("label",{"class": "firstLabel"},"E-Mail notification")
            ),
            Auth.user == null ? null :
                m("label", {style: "width: 16em; float:left; display: none;"}, [
                    m("input", { type: "checkbox", "class": "checkbox", id:"public", name: "public"}),
                    "Public Job"
                ]),
            m("input", {
                type: "button",
                "class": "success button small submitJob",
                value: (args.isJob ? "Res" : "S") + "ubmit Job",
                style: "float: right;",
                onclick: ctrl.submit.bind(ctrl, true)
            }),
            m("div", {
                "class": "float-right custom-job-id-wrapper",
                "data-tooltip": "",
                "data-position": "left",
                title: "Alphanumeric IDs are permitted (e.g. HISA, HISA1, HISA_HHPRED). Border turns green when a valid ID is entered.",
                config: tooltipConf
            }, JobSubmissionComponent.jobIDComponent(ctrl))
        ])
    }
};