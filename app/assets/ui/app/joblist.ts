const tooltipSearch = function(elem : any, isInit : boolean) {
    if (!isInit) {
        $(elem).attr({ "data-tooltip": "","aria-has-popup": true }).foundation();
        return elem.setAttribute("title", "Search for job");
    }
};

interface Window { JobListComponent: any; }

declare var jobList : any;
declare var jobListOffCanvas : any;

window.JobListComponent = {
    // Generates a job object
    Job : function (data : any) {
        return {
            jobID       : ((data && data.jobID        != null) ? data.jobID       : null),
            status      : ((data && data.status       != null) ? data.status      : null),
            dateCreated : ((data && data.dateCreated  != null) ? data.dateCreated : null),
            tool        : ((data && data.tool         != null) ? data.tool        : null),
            toolnameLong: ((data && data.toolnameLong != null) ? data.toolnameLong        : null),
            // Functions
            select    : function(job : any) {     // marks a job as selected and changes the route
                return function(e : any) {        // ensure that the event bubble is not triggering when the clear button is hit
                    if (e.target.id != "boxclose") {
                        JobListComponent.selectedJobID = job.jobID;
                        m.route("/jobs/" + job.jobID);
                    }
                }
            },
            // View component
            controller : function (args : any) {
                return {} },
            view : function (ctrl : any) {
                return m("div", {
                    "class"   : ("job " +   a[this.status]).concat(this.jobID === JobListComponent.selectedJobID ? " selected" : ""),
                    id      : this.jobID,
                    onclick : this.select(this)
                }, [
                    m("div", { "class": "jobid"    }, this.jobID),
                    m("div", { "class": "toolname" }, this.tool.substr(0, 4).toUpperCase()),
                    m("div", {
                        id        : "boxclose",
                        "class"   : "boxclose",
                        onclick : JobListComponent.removeJob.bind(ctrl, this.jobID)
                    })
                ]);
            }
        }
    },
    list            : [],   // List containing the jobs
    index           : 0,    // Index of the first shown item in the job list
    numVisibleItems : 12,   // Number of shown jobs
    selectedJobID   : null, // JobID of the selected job
    lastUpdatedJob  : null, // Job which has been updated last
    // object holding the current sorting mode
    sort            : { mode : "dateCreated", asc : false },
    // Regex for jobIDs
    jobIDRegExp     : new RegExp(/^[a-zA-Z0-9\_]{6,96}(\_\d{1,3})?$/),
    // Returns a job with the given jobID
    getJob          : function (jobID : string) : any {
        let foundJob = null;
        JobListComponent.list.map(function (job : any){ if(job.jobID === jobID) foundJob = job });
        return foundJob
    },
    // Returns the index of a job with the given jobID
    getJobIndex     : function (jobID : string) : any {
        let foundIndex = null;
        JobListComponent.list.map(function (job : any, index : number){ if (job.jobID === jobID) foundIndex = index });
        return foundIndex
    },
    // Checks if the job with the given jobID is in the list
    contains        : function (jobID : string) : boolean {
        return JobListComponent.getJob(jobID) != null
    },
    // Returns all the jobIDs from the list
    jobIDs          : function () : Array<string> {
        return JobListComponent.list.map(function(job : any){ return job.jobID })
    },
    // Returns all the jobIDs from the list which can still be updated
    jobIDsFiltered  : function () : Array<string> {
        return JobListComponent.list.filter(function(job : any){
            return job.status != 4 && job.status != 5
        }).map(function(job : any){ return job.jobID })
    },
    // Notices the server to send update messages about the jobs
    register        : function (jobIDs? : Array<string>) : void {
        if (jobIDs != null) {
            ws.send({ type: "RegisterJobs", "jobIDs": jobIDs });
        } else {
            ws.send({ type: "RegisterJobs", "jobIDs": JobListComponent.jobIDsFiltered() });
        }
    },
    emptyList       : function () { JobListComponent.list = [] },   // empties the job list
    reloadList      : function () {         // reloads the job list from the server
        let request = m.request({
            url: "/api/jobs",
            method: "GET",
            type: JobListComponent.Job
        });
        request.then(function(data : any) {
            JobListComponent.list = data;   // put the jobs in the list
            JobListComponent.sortList();    // sort the list with the current sorting mode
            JobListComponent.register();    // send the server a message that these items are being watched
            // TODO Something will hinder the execution of any code after JobListComponent.register()
        });
        return JobListComponent.list
    },
    removeJob       : function(jobID : string, messageServer : boolean, deleteJob : boolean) { // removes a job from the list
        if (messageServer == null) { messageServer = false }
        if (deleteJob     == null) { deleteJob     = false }
        JobListComponent.list.map( function(job : any, idx : number) {
            if (job.jobID === jobID) {
                if (jobID === JobListComponent.selectedJobID) {
                    JobListComponent.selectedJobID = null;
                    m.route("/jobmanager");
                }
                JobListComponent.list[idx] = null;
                JobListComponent.list.splice(idx, 1);
                if (messageServer) {

                    if (deleteJob) {
                        //ws.send({ "type" : "DeleteJob", "jobID" : job.jobID }) TODO reimplement the deletion over WS
                        m.request({ url: "/api/job/" + job.jobID, method: "DELETE" })
                    }
                    else           { ws.send({ "type" : "ClearJob",  "jobIDs" : [job.jobID] }) }
                }
            }
        });
        jobList.redraw();
    },
    sortList        : function(sortMode? : string, reverse? : boolean) : boolean {      // Sorting the list elements
        let oldSort, sameMode : boolean, inv : number, selectedJobID : string, selectedInView : boolean = false;
        oldSort = JobListComponent.sort;    // grab the old sorting method
        if (sortMode != null && reverse != null) {
            sameMode = (oldSort.mode === sortMode); // see if the mode has changed
            // If the mode has changed adjust the order (ascending - true / descending - false)
            JobListComponent.sort = { mode : sortMode, asc : ((sameMode && reverse) ? !oldSort.asc : true) }
        }
        // inv gets multiplied to invert the sorting order
        inv = JobListComponent.sort.asc ? 1 : -1;
        // Check if the selected jobID is in the view to get the new index to scroll to
        selectedJobID = JobListComponent.selectedJobID;
        JobListComponent.visibleJobs().map(function(job : any) { if (job.jobID === selectedJobID) selectedInView = true; });
        // Sort the list
        JobListComponent.list.sort(function(job1 : any, job2 : any) {
            switch (JobListComponent.sort.mode) {
                case "tool":
                    return inv * job1.tool.localeCompare(job2.tool);
                case "jobID":
                    return inv * job1.jobID.localeCompare(job2.jobID);
                case "dateCreated":
                    if(isNaN(parseInt(job1.dateCreated)) || isNaN(parseInt(job2.dateCreated))) {
                        console.log("Illegal object with dateCreated NaN:", job1, job2);
                    }
                    return inv * (job1.dateCreated - job2.dateCreated);
                default:
                    return inv * (job1.dateCreated - job2.dateCreated);
            }
        });
        // Scroll to the selected item if it was in the view before
        if (selectedJobID != null && selectedInView) {
            JobListComponent.scrollToJobListItem(JobListComponent.getJobIndex(selectedJobID));
        }
        return true;
    },
    pushJob         : function(newJob : any, setActive? : boolean) {
        if (newJob == null || newJob.jobID == null) { console.log(newJob); return }  // ensure that there are no empty jobs pushed
        JobListComponent.lastUpdatedJob = newJob;                        // change the "last updated" job to this one
        if (setActive) { JobListComponent.selectedJobID = newJob.jobID } // change the selectedJobID to this job when setActive is on
        let index = JobListComponent.getJobIndex(newJob.jobID);          // check if the job is in the list already
        if (index != null) {
            JobListComponent.list[index] = newJob;              // Job is not new, update it
        } else {
            JobListComponent.list.push(newJob);                 // Job is new, push it to the list
            JobListComponent.register([newJob.jobID]);
        }
        JobListComponent.sortList();                            // Sort the list with the current sorting mode
        if (newJob.jobID === JobListComponent.selectedJobID) {  // Since the job is selected
            index = this.getJobIndex(newJob.jobID);             // find the new index of the job,
            m.route("/jobs/" + newJob.jobID);                   // actualize the content view and
            this.scrollToJobListItem(index);                    // scroll to the new position in the joblist
        }

        // TODO may need to make the job list into a non static object at some point...
        jobList.redraw.strategy("diff");
        jobList.redraw(true);
        jobListOffCanvas.redraw.strategy("diff");
        jobListOffCanvas.redraw(true);
    },
    selectJob : function() {
        let activeJob = JobListComponent.getJob(m.route.param("jobID"));
        JobListComponent.pushJob(activeJob, true);
        //JobListComponent.reloadList();
        jobList.redraw();
    },
    visibleJobs : function () {     // function cuts the list down to the visible elements
        return JobListComponent.list.slice(JobListComponent.index, JobListComponent.index + JobListComponent.numVisibleItems);
    },
    scrollJobList : function (number : number, doScroll : boolean) {   // function scrolls the index number of items up or down
        return function(e : any) {
            if (doScroll) {
                let numScrollItems = JobListComponent.index + number;
                JobListComponent.index = numScrollItems < 0 ? 0 : numScrollItems;   // ensure that the index is not overscrolled
            }
        }
    },
    reloadJob : function (jobID : string) : any {
        JobListComponent.scrollToJobListItem(JobListComponent.getJobIndex(jobID));
    },
    scrollToJobListItem : function (index: number) {    // function scrolls the job list to the index while scrolling exact numVisibleItems
        let scrollIndex = Math.floor(index / JobListComponent.numVisibleItems);
        scrollIndex *= JobListComponent.numVisibleItems;
        JobListComponent.index = scrollIndex < 0 ? 0 : scrollIndex;
       },
    model: function() {},
    controller: function(args : any) {
        //console.log(args.activejobID + " : " + m.route.param("jobID"));
        let activeJob = JobListComponent.getJob(m.route.param("jobID"));
        JobListComponent.pushJob(activeJob, true);

        JobListComponent.reloadList();
        // TODO this is a hack to make the controller use the reload list command only once
        JobListComponent.controller = function(){return {}};
        return {}
    },
    view: function(ctrl : any, args : any) {
        let shownList:any, listLength:any, listTooLong:any, onTopOfList:any, onBottomOfList:any, numScrollItems:any, page:any, pagesTotal:any;
        shownList  = JobListComponent.visibleJobs();
        listLength = JobListComponent.list.length;                   // lenght of the original list
        page       = Math.floor(JobListComponent.index / JobListComponent.numVisibleItems) + 1;  // Calculate the current page
        pagesTotal = Math.ceil(JobListComponent.list.length / JobListComponent.numVisibleItems); // Calculate the total pages
        listTooLong = listLength > JobListComponent.numVisibleItems; // is the list longer than numVisibleItems - if so don't display any page buttons
        onTopOfList = (JobListComponent.index <= 0);                 // is the list at the top?
        onBottomOfList = ((JobListComponent.index + JobListComponent.numVisibleItems) >= listLength);   // is the list at the bottom?
        if (onBottomOfList && (JobListComponent.index >= listLength)) JobListComponent.scrollToJobListItem(-JobListComponent.numVisibleItems); // ensures view when elements are cleared

        numScrollItems = JobListComponent.numVisibleItems; // How many items to scroll per click
        return m("div", { "class": "job-list", config: tooltipConf }, [
            m("div", { "class": "job-button" }, [
                m("div", { "class": "sort id textcenter" + (JobListComponent.sort.mode == "jobID" ? " selected" : ""),
                           title: "Sort by job ID", onclick: JobListComponent.sortList.bind(ctrl, "jobID", true) }, "ID"),
                m("div", { "class": "sort date textcenter" + (JobListComponent.sort.mode == "dateCreated" ? " selected" : ""),
                           title: "Sort by date created", onclick: JobListComponent.sortList.bind(ctrl, "dateCreated", true) }, "Date"),
                m("div", { "class": "sort tool textcenter" + (JobListComponent.sort.mode == "tool" ? " selected" : ""),
                           title: "Sort by tool name", onclick: JobListComponent.sortList.bind(ctrl, "tool", true) }, "Tool"),
                m("div", { "class": "openmanager textcenter", title: "Open job manager"}, m('a', { href : "/#/jobmanager"}, m("i", {"class": "icon-list"})))
            ]),
            m("div", { "class": "elements noselect" }, [
                listTooLong ?
                    m("div", {
                        "class": "arrow top" + (onTopOfList ? " inactive" : ""), // Add class to gray out when onTopOfList == true
                        onclick: JobListComponent.scrollJobList(-numScrollItems, !onTopOfList) }, "\u25b2"
                    ) : null,
                shownList.map(function(job : any) { return job.view(ctrl) }),
                listTooLong ? m("div", {"class": "pages"},"Page "+page+" of "+pagesTotal) : null,
                listTooLong ?
                    m("div", {
                        "class": "arrow bottom" + (onBottomOfList ? " inactive" : ""), // Add class to gray out when onTopOfList == true
                        onclick: JobListComponent.scrollJobList(+numScrollItems, !onBottomOfList) }, "\u25bc"
                    ) : null
            ])
        ]);

    }
};
