
let FrontendTools : any = {
    "alnviz": FrontendAlnvizComponent,
    "reformat": FrontendReformatComponent
};

let a = ['0', 'p', 'q', 'r', 'e', 'd', 'i'];

let jobs = {};

let Users = -1;

interface Window { Toolkit: any; }

window.Toolkit = {
    currentJobID : -1,
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
        document.title = "Bioinformatics Toolkit";
        let job : any, jobID : string, toolname : string, viewComponent : any;
        if (args.isJob) {
            jobID = m.route.param("jobID");
            Toolkit.currentJobID = jobID;
            if (!JobListComponent.contains(jobID) || !(Toolkit.currentJobID === jobID)) {
                // ensure addition to the job list
                //sendMessage({ type: "RegisterJobs", "jobIDs": [jobID] });
                // request job
                m.request({url: "/api/job/load/" + jobID, method: "GET"}).catch(function (e) {
                    console.log("Job Not found", e);
                }).then(function (data) {
                    JobListComponent.pushJob(JobListComponent.Job(data), true);
                });
            }
        } else {
            JobListComponent.selectedJobID = null;
        }
        toolname = m.route.param("toolname");
        if (FrontendTools[toolname]) {
            viewComponent = function() { return FrontendTools[toolname]; };
        } else {
            job = window.JobModel.update(args, args.isJob ? jobID : toolname);
            viewComponent = function() {
                return m(JobViewComponent, { job: job });
            };
        }
        return {
            viewComponent: viewComponent
        };
    },
    view: function(ctrl : any) {
        return [
            m("div", { "class": "large-2 padded-column columns show-for-large", id: "sidebar" }, [
                m("div", { id : "job-search-div" }, [
                    m("input", {
                        type:        "text",
                        placeholder: "Search by JobID",
                        id:          "job-search",
                        name:        "job-search"
                    }),
                    m("span", { "class": "bar" })
                ]),
                m(JobListComponent, { activejobID : m.route.param("jobID") })
            ]),
            m("div", { id: "content", "class": "large-10 small-12 columns padded-column", config: fadesIn },
                ctrl.viewComponent())
        ];
    }
};