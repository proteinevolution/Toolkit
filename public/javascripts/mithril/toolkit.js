var FrontendTools;

FrontendTools = {
    "alnviz": FrontendAlnvizComponent,
    "reformat": FrontendReformatComponent
};

a = ['0', 'p', 'q', 'r', 'e', 'd', 'i'];

jobs = {};

Users = -1;

/*
window.Job = (function() {
    function Job(args) {
        this.jobID = args.jobID;
        this.state = args.state;
        this.createdOn = args.createdOn;
        this.toolname = args.toolname;
    }

    Job.selected = -1;

    Job.owner = "";

    Job.lastUpdated = -1;

    Job.lastUpdatedMainID = -1;

    Job.lastUpdatedState = -1;

    Job.contains = function(jobID) {
        return Job.list.then(function(jobs) {
            var i, job, len;
            for (i = 0, len = jobs.length; i < len; i++) {
                job = jobs[i];
                if (job.jobID === jobID) {
                    return true;
                }
            }
            return false;
        });
    };

    Job.list = (function() {
        var x;
        x = m.request({
            url: "/api/jobs",
            method: "GET",
            type: Job
        });
        x.then(function(jobs) {
            return Job.register(jobs.map(function(job) {
                return job.jobID;
            }));
        });
        return x;
    })();

    Job.register = function(jobIDs) {
        return sendMessage({
            type: "RegisterJobs",
            "jobIDs": jobIDs
        });
    };

    Job.reloadList = function() {
        Job.list = m.request({
            url: "/api/jobs",
            method: "GET",
            type: Job
        });
        Job.list.then(Job.list);
        return Job.list.then(function(data) {
            return Job.register(data.map(function(job) {
                return job.jobID;
            }));
        });
    };

    Job.getJobByID = function(jobID) {
        return Job.list.then(function(jobs) {
            var i, job, len;
            for (i = 0, len = jobs.length; i < len; i++) {
                job = jobs[i];
                if (job.jobID === jobID) {
                    return job;
                }
            }
            return null;
        });
    };


    Job.clear = function(jobID) {
        return Job.list.then(function(jobs) {
            var job;
            //console.log(JSON.stringify(jobs));
            var index = jobs.findIndex(function(item, i){
                return item.jobID === jobID
            });
            //console.log("Index: " + index);
            job = jobs[index];
            jobs[index] = null;
            jobs.splice(index, 1);
            //m.redraw(true);

            sendMessage({
                "type": "ClearJob",
                "jobID": job.jobID
            });
            if (job.jobID === Job.selected) {
                return m.route("/tools/" + job.toolname);
            }
        });
    };

    Job.removeJob = function(jobID, messageServer, deleteJob) {
        if (messageServer == null) {
            messageServer = false;
        }
        if (deleteJob == null) {
            deleteJob = false;
        }
        return Job.list.then(function(jobs) {
            return jobs.map(function(job, idx) {
                if (job.jobID === jobID) {
                    jobs[idx] = null;
                    jobs.splice(idx, 1);
                    if (messageServer) {
                        if (deleteJob) {
                            sendMessage({
                                "type": "DeleteJob",
                                "jobID": job.jobID
                            });
                        } else {
                            sendMessage({
                                "type": "ClearJob",
                                "jobID": job.jobID
                            });
                        }
                    }
                    if (job.jobID === Job.selected) {
                        return m.route("/tools/" + job.toolname);
                    }
                }
            });
        });
    };

    Job["delete"] = function(jobID) {
        return Job.list.then(function(jobs) {
            return jobs.map(function(job) {
                var deletionRoute;
                if (job.jobID === jobID) {
                    deletionRoute = jsRoutes.controllers.JobController["delete"](jobID);
                    m.request({
                        url: deletionRoute.url,
                        method: deletionRoute.method
                    });
                    return Job.clear(jobID);
                }
            });
        });
    };

    Job.sortToolname = function() {
        return (Job.list.then(function(list) {
            return list.sort(function(job1, job2) {
                return job2.toolname.localeCompare(job1.toolname);
            });
        })).then(Job.list);
    };

    Job.sortJobID = function() {
        return (Job.list.then(function(list) {
            return list.sort(function(job1, job2) {
                return job2.jobID.localeCompare(job1.jobID);
            });
        })).then(Job.list);
    };

    Job.updateState = function(jobID, state) {
        var i, job, len, ref;
        Job.lastUpdated = jobID;
        Job.lastUpdatedState = state;
        if (jobID === Job.selected) {
            m.route("/jobs/" + jobID);
        }
        ref = Job.list();
        for (i = 0, len = ref.length; i < len; i++) {
            job = ref[i];
            if (job.jobID === jobID) {
                job.state = state;
                return true;
            }
        }
        return false;
    };

    Job.pushJob = function(job) {
        var foundJob;
        foundJob = Job.updateState(job.jobID, job.state);
        if (!foundJob) {
            return Job.add(new Job(job));
        }
    };

    Job.add = function(job) {
        return Job.list.then(function(list) {
            return list.push(job);
        });
    };

    return Job;

})();
*/


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

        document.title = "Bioinformatics Toolkit";
        var job, jobID, toolname, viewComponent;
        if (args.isJob) {
            jobID = m.route.param("jobID");
            if (!JobListComponent.contains(jobID) || !(Toolkit.currentJobID === jobID)) {
                Toolkit.currentJobID = jobID;
                // ensure addition to the job list
                sendMessage({ type: "RegisterJobs", "jobIDs": [jobID] });
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
