declare var moment : any;
interface Window { JobManager: any; }
window.JobManager = {
    data: [],
    table: null,
    dataTableLoader: function () {
        return function (elem: any, isInit: boolean) {
            if (!isInit) {
                m.request({"url": "jobs", "method": "GET", background: true})
                    .then(function(response){
                        JobManager.data = response;
                        JobManager.table = $('#jobManagerTable').dataTable({
                            "bInfo": false,
                            "bFilter": true,
                            "data": JobManager.data,
                            "order": [[3, "desc"]],
                            "columns": [
                                {"mDataProp": "jobID"},
                                {"mDataProp": "jobID"},
                                {"mDataProp": "toolnameLong"},
                                {"mDataProp": "dateCreated"},
                                {"mDataProp": "jobID"}
                            ],
                            'columnDefs': [
                                {
                                    "defaultContent": "-",
                                    "targets": "_all"
                                },
                                {
                                    'targets': 4,
                                    "defaultContent": "-",
                                    'searchable': false,
                                    'orderable': false,
                                    'render': function (jobID: any) {
                                        return '<i class="delete fa fa-trash-o" onclick="JobManager.deleteJob(\'' + jobID + '\')"></i>';
                                    }
                                },
                                {
                                    'targets': 1,
                                    "defaultContent": "-",
                                    'render': function (jobID: any) {
                                        return '<a href="#/jobs/' + jobID + '">' + jobID + '</a>';
                                    },
                                    "createdCell": function (td: any, cellData: any, rowData: any, row: any, col: any) {
                                        let job = JobManager.getJob(cellData);
                                        if (job.length < 1) {
                                            return;
                                        }
                                        let status = a[job[0].status];
                                        $(td).addClass(status);


                                    },
                                },
                                {
                                    'targets': 0,
                                    "defaultContent": "-",
                                    'searchable': false,
                                    'orderable': false,
                                    'render': function (jobID: any) {
                                        if (JobListComponent.contains(jobID)) {
                                            return '<i class="fa fa-minus-circle remove" aria-hidden="true" onclick="JobManager.removeFromList(\'' + jobID + '\')"></i>';
                                        } else {
                                            return '<i class="fa fa-plus-circle add" aria-hidden="true" onclick="JobManager.addToList(\'' + jobID + '\')"></i>';
                                        }
                                    }
                                },
                                {
                                    'targets': 3,
                                    "defaultContent": "-",
                                    render: function (timestamp: any, type: any) {
                                        // If display or filter data is requested, format the date
                                        if (type === 'display') {
                                            return moment(timestamp).local().fromNow();
                                        }
                                        return timestamp;
                                    }

                                }],
                        });
                    });
            }
        }
    },


    /** When receiving a message from the websocket*/
    pushToTable : function(job_: Job) {
        if(JobManager.table) {
            let job = JobListComponent.Job(job_);
            JobManager.data = JobManager.data.filter(function (jobData: Job) {
                return jobData.jobID != job.jobID
            });
            JobManager.data.push(job);
            JobManager.reload();
        }
    },
    removeFromTable : function(jobID: String) {
        if(JobManager.table) {
            JobManager.data = JobManager.data.filter(function (job: Job) {
                return job.jobID != jobID
            });
            JobManager.reload();
        }
    },

    reload : function() {
        JobManager.table.DataTable().clear().rows.add(JobManager.data).draw(false);
    },

    /** called by clicking on delete Job*/
    deleteJob: function(jobID : string): any{
        m.request({ url: "/api/job/" + jobID, method: "DELETE" }).then(function(){
            JobManager.removeFromTable(jobID);
        });
        jobList.redraw();
    },

    /** send message to websocket */
    removeFromList: function(jobID: string): any{
        ws.send({ "type" : "ClearJob",  "jobIDs" : [jobID] });
    },

    addToList: function(jobID: string): any{
        ws.send({ "type" : "RegisterJobs",  "jobIDs" : [jobID]});
    },

    /**
     * to get a job that is stored
     * inside JobManager.list by jobID
     * @param jobID
     */
    getJob: function(jobID:string): any{
      return JobManager.data.filter(function(job: any){return job.jobID == jobID});
    },
    controller: function () {
        return {

        }
    },

    view: function (ctrl : any) {
        return [
            //m("div", { "class": "large-2 padded-column columns show-for-large", id: "sidebar" }, [
            //    m(JobListComponent, { activejobID : m.route.param("jobID") })
            //]),
            m("div", {"class": "jobManagerContainer", config: showSidebar},
                m("div", {"class": "jobline"}, [
                    m("span", {"class": "toolname"}, [
                        m("a", "Job Manager")
                    ])
                ]),
                m("div", {id: "content", "class": "row columns", config: fadesIn},
                    m("table", {id: "jobManagerTable", "class": "dataTable hover row-border compact job-manager", config: JobManager.dataTableLoader()}, [
                            m("thead", m("tr", [
                                m("th", "Job List"),
                                m("th", "Job ID"),
                                m("th", "Tool"),
                                m("th", "Created"),
                                m("th", "Delete Job")
                                ])),
                            m("tbody", [])
                        ]
                    )
                )
            )
        ];
    }
};