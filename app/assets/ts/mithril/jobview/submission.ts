(<any>window).JobSubmissionComponent = {
    submitting      : false,    // Job is being sent if true
    currentJobID    : null,     // Currently entered jobID
    jobIDValid      : false,    // Is the current jobID valid?
    jobIDValidationTimeout : null,     // timer ID for the timeout
    jobIDRegExp     : new RegExp(/^([0-9a-zA-Z_]+){6,96}(_[0-9]{1,3})?$/),
    jobResubmit     : false,
    checkJobID : function (jobID : string, addResubmitVersion : boolean) {
        clearTimeout(JobSubmissionComponent.jobIDValidationTimeout);    // clear all previous timeouts
        JobSubmissionComponent.jobIDValid   = false;    // ensure that the user can not send the job form
        JobSubmissionComponent.currentJobID = jobID;    // set the jobID to the new jobID
        if (jobID !== "") { // ignore checking if the field is empty as the server will generate a jobID in that case.
            if (JobSubmissionComponent.jobIDRegExp.test(jobID)) {   // Check if the JobID is passing the Regex
                JobSubmissionComponent.jobIDValidationTimeout = setTimeout(function (a) {   // create the timeout
                    m.request({ method: "GET", url: "/search/checkJobID/"+(addResubmitVersion?"resubmit/"+jobID:jobID)}).then(
                        function (data) {
                            console.log(data);
                            JobSubmissionComponent.jobIDValid = !data.exists;
                            if (data.exists && data.suggested != null) {
                                JobSubmissionComponent.currentJobID = data.suggested;
                                JobSubmissionComponent.jobIDValid = true;
                            }
                            console.log("Current JobID is: ", jobID, "suggested version", data.version, "valid:", JobSubmissionComponent.jobIDValid);
                        }
                    );
                }, 800);
            }
        } else {
            JobSubmissionComponent.jobIDValid = true;
        }
        return jobID;
    },
    jobIDComponent : function (ctrl : any) {
        let style : string = "jobid";
        style += JobSubmissionComponent.currentJobID === "" ? " white" :
            (JobSubmissionComponent.jobIDValid          ? " green" : " red");
        return m("input", { type:        "text",
                            id:          "jobID",
                            class:       style,
                            placeholder: "Custom JobID",
                            onkeyup:     m.withAttr("value", JobSubmissionComponent.checkJobID),
                            onchange:    m.withAttr("value", JobSubmissionComponent.checkJobID),
                            value:       JobSubmissionComponent.currentJobID
        });
    },
    controller: function(args : any) {
        if (JobSubmissionComponent.currentJobID == null) {
            let newJobID;
            if (args.isJob) {
                JobSubmissionComponent.jobResubmit = true;
                JobSubmissionComponent.jobIDValid  = false;
                newJobID = args.job().jobID;
                JobSubmissionComponent.checkJobID(newJobID, true); // ask server for new jobID
            } else {
                JobSubmissionComponent.jobIDValid  = true;
            }
        }
        return {
            submit2: function(startJob : boolean) {
                if (JobSubmissionComponent.submitting || !JobSubmissionComponent.jobIDValid) {
                    console.log("Job Submission is blocked: Already submitting: ", JobSubmissionComponent.submitting,
                                "jobID valid: ",                                   JobSubmissionComponent.jobIDValid);
                    return;
                }
                JobSubmissionComponent.submitting = true; // ensure that the submission is not reinitiated while a submission is ongoing

                let submitRoute : any, formData : FormData, jobID : string, toolName : string, submitButton : JQuery, form : any;
                form = document.getElementById("jobform");
                if(!form.checkValidity()) {
                    JobSubmissionComponent.submitting = false;
                    alert("Parameters are invalid");
                    return;
                }

                // disable submit button
                submitButton = $(".submitJob");
                submitButton.prop("disabled", true);
                toolName = args.job().tool.toolname;
                jobID = JobSubmissionComponent.currentJobID;
                console.log("Current JobID is: ", jobID, JobSubmissionComponent.currentJobID, "valid:", JobSubmissionComponent.jobIDValid);

                // collect form data
                formData = new FormData(form);
                formData.append("toolName", toolName);

                if ((jobID != null) && (jobID !== "")) { formData.append("jobID", jobID); }

                // Append file to upload
                let file = ((<any>$("input[type=file]"))[0].files[0]);
                formData.append("file", file);

                // appendParentID if in storage // TODO use a better method for this (save it for the current job in the job view)
                let parentid = localStorage.getItem("parentid");
                if(parentid) {
                    formData.append('parentid', parentid);
                }

                //submitRoute = jsRoutes.controllers.JobController.submitJob(toolname);
                m.request({
                    method: "POST",
                    url: "/api/job/create/" + toolName,
                    data: formData,
                    serialize: function(submissionReturnData) {
                        return submissionReturnData;
                    }
                }).then(function(submissionReturnData : any){
                    console.log("Data(then):", submissionReturnData);
                    if (submissionReturnData.successful === true) {
                        console.log("Job Submission was successful.");
                        jobID = submissionReturnData.jobID;
                        var jobListComp = JobListComponent.Job(
                            { jobID: jobID, state: 0, createdOn: Date.now().valueOf(), toolname: toolName }
                        );
                        JobListComponent.pushJob(jobListComp, true);
                    } else {
                        console.log("Error while submitting:", submissionReturnData.message)
                    }
                    $(".submitJob").prop("disabled", false);
                    JobSubmissionComponent.submitting = false;
                }).catch(function(error){
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
                //console.log(args.job().jobstate);
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
                "class":                 "reveal",
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
                m("label", {style: "width: 16em; float:left;"}, [
                    m("input", { type: "checkbox", id:"emailUpdate", name: "emailUpdate", value:true}),
                    "E-Mail notification"
                ]),
            Auth.user == null ? null :
                m("label", {style: "width: 16em; float:left;"}, [
                    m("input", { type: "checkbox", id:"public", name: "public", value:true}),
                    "Public Job"
                ]),
            m("input", {
                type: "button",
                "class": "success button small submitJob",
                value: (args.isJob ? "Res" : "S") + "ubmit Job",
                style: "float: right;",
                onclick: ctrl.submit2.bind(ctrl, true)
            }),
            //!args.isJob ? m("label", m("input", { type: "checkbox", name: "private", value: "true", checked: "checked" }), "Private" ) : null, // TODO reimplement private checkbox
            //args.isJob && args.job().jobstate === 1 ?
            //    m("input", { type: "button", class: "button small addJob", value: "Start Job", onclick: ctrl.startJob })
            //    : null,
            //args.isJob ? m("input", { type: "button", class: "button small addJob", value: "Add Job", onclick: ctrl.addJob })
            //    : null,
            JobSubmissionComponent.jobIDComponent(ctrl), m(ProjectComponent, {})
        ])
    }
};