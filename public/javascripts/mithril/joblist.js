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

    Job : function (data) { // Generates a job Object
        return {
            jobID     : data.jobID,
            state     : data.state,
            createdOn : data.createdOn,
            toolname  : data.toolname,
            // Functions
            select    : function(job) {     // marks a job as selected and changes the route
                return function(e) {        // ensure that the event bubble is not triggering when the clear button is hit
                    if (e.target.id != "boxclose") {
                        JobListComponent.selectedJobID = job.jobID;
                        m.route("/jobs/" + job.jobID);
                    }
                }
            },
            // View component
            controller : function (args) {},
            view : function (ctrl) {
                return m("div", {
                    class   : ("job " + a[this.state]).concat(this.jobID === JobListComponent.selectedJobID ? " selected" : ""),
                    id      : this.jobID,
                    onclick : this.select(this)
                }, [
                    m("div",  { class: "jobid"    }, this.jobID),
                    m("span", { class: "toolname" }, this.toolname.substr(0, 4).toUpperCase()),
                    m("a", {
                        class   : "boxclose",
                        id      : "boxclose",
                        onclick : JobListComponent.removeJob.bind(ctrl, this.jobID)
                    })
                ]);
            }
        }
    },
    list            : [],   // List containing the jobs
    index           : 0,    // Index of the first shown item in the job list
    numVisibleItems : 10,   // Number of shown jobs
    selectedJobID   : null, // JobID of the selected job
    lastUpdatedJob  : null, // Job which has been updated last
    sort            : "createdOn",
    getJob          : function (jobID) {    // Returns a job with the given jobID
        var foundJob = null;
        JobListComponent.list.map(function (job){ if(job.jobID === jobID) foundJob = job });
        return foundJob
    },
    getJobIndex     : function (jobID) {    // Returns the index of a job with the given jobID
        var index = -1, foundIndex = null;
        JobListComponent.list.map(function (job){
            index++;
            if (job.jobID === jobID) foundIndex = index
        });
        return foundIndex
    },
    contains        : function (jobID) {    // Checks if the job with the given jobID is in the list
        return this.getJob(jobID) != null
    },
    jobIDs          : function () {         // Returns the jobIDs from the list
        return JobListComponent.list.map(function(job){ return job.jobID })
    },
    register        : function (jobIDs) {   // Notices the server to send update messages about the jobs
        if (jobIDs == null) { jobIDs = this.jobIDs }
        sendMessage({ type: "RegisterJobs", "jobIDs": jobIDs })
    },
    emptyList       : function () { JobListComponent.list = [] },   // empties the job list
    reloadList      : function () {         // reloads the job list from the server
        var request = m.request({
            url: "/api/jobs",
            method: "GET",
            type: JobListComponent.Job
        });
        request.then(function(data) {
            JobListComponent.list = data;
            JobListComponent.register()
        });
        return JobListComponent.list
    },
    removeJob       : function(jobID, messageServer, deleteJob) { // removes a job from the list
        if (messageServer == null) { messageServer = false }
        if (deleteJob     == null) { deleteJob     = false }
        JobListComponent.list.map( function(job, idx) {
            if (job.jobID === jobID) {
                if (jobID === JobListComponent.selectedJobID) {
                    JobListComponent.selectedJobID = null;
                    m.route("/tools/" + job.toolname);
                }
                JobListComponent.list[idx] = null;
                JobListComponent.list.splice(idx, 1);
                if (messageServer) {
                    if (deleteJob) {
                        //sendMessage({ "type" : "DeleteJob", "jobID" : job.jobID }) TODO reimplement the deletion over WS
                        m.request({ url: "/api/job/" + job.jobID, method: "DELETE" });
                    }
                    else           { sendMessage({ "type" : "ClearJob",  "jobID" : job.jobID }) }
                }
            }
        });
    },
    sortList        : function(sort) {      // Sorting the list elements
        console.log("sort is: " + sort);
        if (sort != null) { JobListComponent.sort = sort }
        JobListComponent.list.sort(function(job1, job2) {
            switch (JobListComponent.sort) {
                case "toolName"  : return job2.toolname.localeCompare(job1.toolname);
                case "jobID"     : return job2.jobID.localeCompare(job1.jobID);
                case "createdOn" :
                default          : return job2.createdOn.toString().localeCompare(job1.createdOn)
            }
        });
    },
    pushJob         : function(newJob, setActive) {
        this.lastUpdatedJob = newJob;
        var index = null, oldJob;
        if (setActive) { JobListComponent.selectedJobID = newJob.jobID }
        JobListComponent.list.map(function(job, idx){
            if(job.jobID === newJob.jobID) { index = idx; oldJob = job }
        });
        if (index != null) {
            JobListComponent.list[index] = newJob;
        } else {
            index = JobListComponent.list.length;
            JobListComponent.list.push(newJob);
        }
        if (newJob.jobID === JobListComponent.selectedJobID) {
            m.route("/jobs/" + newJob.jobID);
            this.scrollToJobListItem(index);
        }
        //this.sortList(); TODO sort after pushing
    },
    scrollJobList : function (number, doScroll) {
        return function(e) {
            if (doScroll) {
                var numScrollItems = JobListComponent.index + number;
                JobListComponent.index = numScrollItems < 0 ? 0 : numScrollItems;
            }
        }
    },
    scrollToJobListItem : function (index) {
        var scrollIndex = Math.floor(index / JobListComponent.numVisibleItems);
        scrollIndex *= JobListComponent.numVisibleItems;
        JobListComponent.index = scrollIndex < 0 ? 0 : scrollIndex;
        //console.log("scrolling to " + index + " scroll index is " + scrollIndex + ", current scroll index is " + JobListComponent.index);
    },
    model: function() {},
    controller: function(args) {
        JobListComponent.reloadList();
        // TODO this is a hack to make the controller use the reload list command only once
        JobListComponent.controller = function(){return {}};
        return {}
    },
    view: function(ctrl, args) {
        var shownList, listLength, listTooLong, onTopOfList, onBottomOfList, numScrollItems;
        shownList  = this.list.slice(this.index, this.numVisibleItems + this.index);
        listLength = this.list.length;                                      // lenght of the original list
        listTooLong = listLength > this.numVisibleItems;                    // is the list longer than numVisibleItems?
        onTopOfList = (this.index <= 0);                                        // is the list at the top?
        onBottomOfList = ((this.index + this.numVisibleItems) >= listLength);   // is the list at the bottom?
        if (onBottomOfList && (this.index >= listLength)) this.scrollToJobListItem(-this.numVisibleItems); // ensures view when elements are cleared
        // show the status of the job list in the log
        //console.log({"Scroll Index"            : this.index,
        //             "Page Index"              : Math.floor(this.index / this.numVisibleItems),
        //             "Number of visible Items" : this.numVisibleItems,
        //             "Length of the List"      : listLength,
        //             "on Top"                  : onTopOfList,
        //             "on Bottom"               : onBottomOfList});
        numScrollItems = this.numVisibleItems; // How many items to scroll per click
        return m("div", { id: "job-list" }, [
            m("div", { class: "job-button" }, [
                m("div", { class: "idsort textcenter", onclick: this.sortList.bind(ctrl, "jobID") }, "ID"),
                m("div", { class: "toolsort textcenter", onclick: this.sortList.bind(ctrl, "toolName") }, "Tool")
            ]),
            m("div", { id: "job-list-bottom" }, [
                listTooLong ?   // Show only when list is longer than numVisibleItems
                    m("div", {
                        class: "textcenter", // TODO Add class to gray out when onTopOfList == true
                        onclick: this.scrollJobList(-numScrollItems, !onTopOfList) }, "\u25b2"
                    ) : null,
                shownList.map(function(job) { return job.view(ctrl) }),
                listTooLong ?   // Show only when list is longer than numVisibleItems
                    m("div", {
                        class: "textcenter", // TODO Add class to gray out when onBottomOfList == true
                        onclick: this.scrollJobList(+numScrollItems, !onBottomOfList) }, "\u25bc"
                    ) : null
            ])
        ]);
    }
};