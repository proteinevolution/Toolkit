var tooltipSearch;

tooltipSearch = function(elem, isInit) {
    if (!isInit) {
        elem.setAttribute("data-tooltip", "data-tooltip");
        elem.setAttribute("aria-haspopup", "true");
        elem.setAttribute("data-disable-hover", "false");
        return elem.setAttribute("title", "Search for job");
    }
};


window.JobListComponent = {

    Job : function (data) {
        return {
            jobID     : data.jobID,
            state     : data.state,
            createdOn : data.createdOn,
            toolname  : data.toolname
        }
    },
    list            : [],   // List containing the jobs
    index           : 0,    // Index of the first shown item in the job list
    shown           : 15,   // Number of shown jobs
    selected        : -1,   // Index of the selected job
    lastUpdatedJob  : null, // Job which has been updated last
    sort            : "createdOn",
    getJob          : function (jobID) {    // Returns a job with the given jobID
        var foundJob = null;
        this.list.map(function (job){ if(job.jobID === jobID) foundJob = job });
        return foundJob
    },
    getJobIndex     : function (jobID) {    // Returns the index of a job with the given jobID
        var index = -1, foundIndex = null;
        this.list.map(function (job){
            index++;
            if (job.jobID === jobID) foundIndex = index
        });
        return foundIndex
    },
    contains        : function (jobID) {    // Checks if the job with the given jobID is in the list
        return this.getJob(jobID) == null
    },
    jobIDs          : function () {         // Returns the jobIDs from the list
        return this.list.map(function(job){ return job.jobID })
    },
    register        : function (jobIDs) {   // Notices the server to send update messages about the jobs
        if (jobIDs == null) { jobIDs = this.jobIDs }
        sendMessage({ type: "RegisterJobs", "jobIDs": jobIDs })
    },
    emptyList       : function () { this.list = [] },   // empties the job list
    reloadList      : function () {         // reloads the job list from the server
        var request = m.request({
            url: "/api/jobs",
            method: "GET",
            type: this.Job
        });
        request.then(function(data) {
            this.list = data;
            register()
        })
    },
    removeJob       : function(jobID, messageServer, deleteJob) { // removes a job from the list
        if (messageServer == null) { messageServer = false }
        if (deleteJob     == null) { deleteJob     = false }
        this.list.map( function(job, idx) {
            if (job.jobID === jobID) {
                jobs[idx] = null;
                jobs.splice(idx, 1);
                if (messageServer) {
                    if (deleteJob) { sendMessage({ "type" : "DeleteJob", "jobID" : job.jobID }) }
                    else           { sendMessage({ "type" : "ClearJob",  "jobID" : job.jobID }) }
                }
                if (job.jobID === this.selected) { m.route("/tools/" + job.toolname) }
            }
        });
    },
    sortToolname    : function(sort) {      // Sorting comperator for the tool name
        if (sort != null) { this.sort = sort }
        this.list.sort(function(job1, job2) {
            switch (type) {
                case "toolName"  : return job2.toolname.localeCompare(job1.toolname);
                case "jobID"     : return job2.jobID.localeCompare(job1.jobID);
                case "createdOn" :
                default          : return job2.createdOn.localeCompare(job1.createdOn)
            }
        })
    },
    sortJobID       : function() {          // Sorting comperator for the jobID
        this.list.sort(function(job1, job2) { return job2.jobID.localeCompare(job1.jobID) })
    },
    sortCreatedOn   : function() {          // Sorting comperator for the creation date (default)
        this.list.sort(function(job1, job2) {  })
    },
    pushJob         : function(newJob) {
        this.lastUpdatedJob = newJob;
        var index = null, oldJob;
        this.list.map(function(job, idx){
            if(job.jobID === newJob.jobID) { index = idx; oldJob = job }
        });
        if (index != null) {
            this.list[index] = newJob
        } else {
            this.list.push(newJob)
        }
        this.sortToolname();
        this.index = 0;
    },
    model: function() {},
    controller: function() {},
    view: function(ctrl, args) {
        return m("div", {
            id: "job-list"
        }, [m("div", {
                "class": "job-button"
            }, [
                m("div", {
                    "class": "idsort textcenter",
                    onclick: Job.sortToolname
                }, "ID"), m("div", {
                    "class": "toolsort textcenter",
                    onclick: Job.sortJobID
                }, "Tool")
            ]), m("div", {
                id: "job-list-bottom"
            }, args.jobs().slice(0).slice(-5).map(function(job) {
                return m("div", {
                    class   : ("job " + a[job.state]).concat(job.jobID === args.selected ? " selected" : ""),
                    onclick : function(e) { if (e.target.id != "boxclose") m.route("/jobs/" + job.jobID) }
                }, [
                    m("div",  { class: "jobid"    }, job.jobID),
                    m("span", { class: "toolname" }, job.toolname.substr(0, 4).toUpperCase()),
                    m("a", {
                        class   : "boxclose",
                        id      : "boxclose",
                        onclick : args.clear.bind(ctrl, job.jobID)
                    })
                ]);
            }))
        ]);
    }
};