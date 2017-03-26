/*
let FrontendTools : any;

FrontendTools = {
    "alnviz": FrontendAlnvizComponent,
    "reformat": FrontendReformatComponent
};

let a = ['0', 'p', 'q', 'r', 'e', 'd', 'i'];

let jobs = {};
*/

// Velocity animation config

let fadesIn = function(element : any, isInitialized : boolean, context : any) {

    let url = window.location.href;
    let parts = url.split("/");
    let isJob = parts[parts.length-2] == "jobs";

    if (!isInitialized && !isJob) {
        element.style.opacity = 0;
        $(element).velocity({opacity: 1, top: "50%"}, 750);
    }
};

/*

let Toolkit : any = {
    currentJobID : -1,
    getSuggestion : function (ctrl : any) {
        return function (e : Event) {}
    },
    jobManagerAdded : false,
    toggleJobManager : function (ctrl : any, t : any) {
        return function (e : Event) {
            t.jobManagerAdded = true;
            return t.jobManagerAdded
        }
    },
    initFoundation : function (ctrl : any) {
        return function (elem : Element, isInit : boolean) {
            if (!isInit) {
                $("#" + elem.id).foundation();
            }
        }
    },
    controller: function(args : any) {

        document.title = "Bioinformatics Toolkit";
        let job : any, jobID : any, toolname : string, viewComponent : any;
        if (args.isJob) {
            jobID = m.route.param("jobID");
            if (!JobListComponent.contains(jobID) || !(Toolkit.currentJobID === jobID)) {
                Toolkit.currentJobID = jobID;
                // ensure addition to the job list
                (<any>window).sendMessage({ type: "RegisterJobs", "jobIDs": [jobID] });
                // request job
                m.request({ url: "/api/job/load/" + jobID, method: "GET" }).then(function(data) {
                    JobListComponent.pushJob(JobListComponent.Job(data), true);
                });
            } else {
                Toolkit.currentJobID = jobID;
            }
        } else {
            JobListComponent.selectedJobID = -1;
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

*/