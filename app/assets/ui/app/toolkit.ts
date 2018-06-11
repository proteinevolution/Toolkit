/// <reference path="frontend.ts"/>
/// <reference path="helper.ts"/>
let FrontendTools : any = {
    "alnviz": FrontendAlnvizComponent,
    "reformat": FrontendReformatComponent
};

const a = ['0', 'p', 'q', 'r', 'e', 'd', 'i', 'w']; // TODO rename this to something like "JobStateMap"

let jobs = {};

interface Window { Toolkit: any; }

window.Toolkit = {
    currentJobID : -1,
    trackedJobIDs: [],
    notFoundJobID: "", // keep track of which job was not found last
    resultView: null,
    getSuggestion : function (ctrl : any) {
        return function (e : any) {}
    },
    jobManagerAdded : false,
    toggleJobManager : function (ctrl : any, t : any) {
        return function (e : any) {
            t.jobManagerAdded = true;
            return t.jobManagerAdded
        }
    },
    initFoundation : function (ctrl : any) {
        return function (elem : any, isInit : boolean) {
            if (!isInit) {
                $("#" + elem.id).foundation();
            }
        }
    },
    controller: function(args : any) {
        currentRoute = args.isJob ? "jobs" : "tools";
        let job : any, jobID : string, toolname : string, viewComponent : any;
        const loadJobRoute = "/api/job/load/";
        if (args.isJob) {
            jobID = m.route.param("jobID");
            Toolkit.currentJobID = jobID;
            // check if job was not found. Try again but display 404 first
            if(Toolkit.notFoundJobID === jobID) {
                m.request({method: "GET", url: loadJobRoute + jobID}).then(function (data) {
                    Toolkit.notFoundJobID = ""; // reset not found job
                    JobListComponent.pushJob(JobListComponent.Job(data), true);
                });
            } else if (!JobListComponent.contains(jobID) || !(Toolkit.currentJobID === jobID)) {
                // ensure addition to the job list
                //ws.send({ type: "RegisterJobs", "jobIDs": [jobID] });
                // request job
                m.request({method: "GET", url: loadJobRoute + jobID}).catch(function (e) {
                    console.log("Job Not found", e);
                    Toolkit.notFoundJobID = jobID;
                    m.route("/jobs/" + jobID); // reload and show not found
                }).then(function (data) {
                    JobListComponent.pushJob(JobListComponent.Job(data), true);
                });
            }
        } else {
            JobListComponent.selectedJobID = null;
            JobSubmissionComponent.currentJobID = null;
        }
        toolname = m.route.param("toolname");

        if (FrontendTools[toolname]) {
            viewComponent = function() { return FrontendTools[toolname]; };
        }
        else {
            // checks whether toolname is valid
            if (!args.isJob) {
                m.request({method: "GET", url: "/check-tool/" + toolname}).catch(function (e) {
                    m.route("/404");
                    console.log("Tool not found", e);
                }).then(() => {});
            }
            if(Toolkit.notFoundJobID === jobID) {
                viewComponent = function() {
                    return m(Job404Component, { jobID: jobID });
                }
            } else {
                job = window.JobModel.update(args, args.isJob ? jobID : toolname); // job variable is a toolname ?? TODO refactor this
                viewComponent = function() {
                    return m(JobViewComponent, { job: job });
                };
            }
        }
        return {
            viewComponent: viewComponent
        };
    },
    view: function(ctrl : any) {
        return [
            m("div", {config: showSidebar}),
            //m("div", { "class": "large-2 padded-column columns show-for-large", id: "sidebar" }, [
                //m(JobListComponent, { activejobID : m.route.param("jobID") })
            //]),
            m("div", { id: "content", config: fadesIn },
                ctrl.viewComponent())
        ];
    }
};