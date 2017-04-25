var FrontendTools;

FrontendTools = {
    "alnviz": FrontendAlnvizComponent,
    "reformat": FrontendReformatComponent
};

a = ['0', 'p', 'q', 'r', 'e', 'd', 'i'];

jobs = {};

Users = -1;



window.Toolkit = {
    currentJobID : -1,
    getSuggestion : function (ctrl) {
        return function (e) {}
    },
    jobManagerAdded : false,
    toggleJobManager : function (ctrl, t) {
        return function (e) {
            t.jobManagerAdded = true;
            return t.jobManagerAdded
        }
    },
    initFoundation : function (ctrl) {
        return function (elem, isInit) {
            if (!isInit) {
                $("#" + elem.id).foundation();
            }
        }
    },
    controller: function(args) {
        currentRoute = args.isJob ? "jobs" : "tools";
        document.title = "Bioinformatics Toolkit";
        var job, jobID, toolname, viewComponent;
        if (args.isJob) {
            jobID = m.route.param("jobID");
            if (!JobListComponent.contains(jobID) || !(Toolkit.currentJobID === jobID)) {
                Toolkit.currentJobID = jobID;
                // ensure addition to the job list
                //sendMessage({ type: "RegisterJobs", "jobIDs": [jobID] });
                // request job
                m.request({ url: "/api/job/load/" + jobID, method: "GET" }).catch(function(e) {
                    console.log("Job Not found", e);
                }).then(function(data) {
                    JobListComponent.pushJob(JobListComponent.Job(data), true);
                });
            } else {
                Toolkit.currentJobID = jobID;
            }
        } else {
            JobListComponent.selectedJobID = null;
        }
        toolname = m.route.param("toolname");
        if (FrontendTools[toolname]) {
            viewComponent = function() { return FrontendTools[toolname]; };
        } else {
            job = JobModel.update(args, args.isJob ? jobID : toolname);
            viewComponent = function() {
                return m(JobViewComponent, { job: job });
            };
        }
        return {
            viewComponent: viewComponent
        };
    },
    view: function(ctrl) {
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
