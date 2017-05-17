// Velocity animation config
/// <reference path="helper.ts"/>

declare var moment : any;

(<any>window).JobManager = {
    data: [],
    dataTableLoader: function () {
        return function (elem: any, isInit: boolean) {
            if (!isInit) {

                m.request({"url": "jobs", "method": "GET", background: true})
                    .then(function(response){
                        JobManager.data = response;
                        JobManager.dataTables();
                    });

            }
        }
    },


    /** When receiving a message from the websocket*/
    pushToTable : function(job_: Job) {
        let job = JobListComponent.Job(job_);
        JobManager.data =  JobManager.data.filter(function(jobData: Job){return jobData.jobID != job.jobID});
        JobManager.data.push(job);
        JobManager.reload();
    },
    removeFromTable : function(jobID: String) {
        JobManager.data = JobManager.data.filter(function(job: Job){return job.jobID != jobID});
        JobManager.reload();
    },

    reload : function() {
        let table = $('#jobManagerTable');
        table.DataTable()
            .clear().rows.add(JobManager.data).draw(false);
        m.redraw(true);
    },

    /** called by clicking on delete Job*/
    deleteJob: function(jobID : string): any{
        m.request({ url: "/api/job/" + jobID, method: "DELETE" }).then(function(){
            JobManager.removeFromTable(jobID);
        });
    },

    /** send message to websocket */
    removeFromList: function(jobID: string): any{
        sendMessage({ "type" : "ClearJob",  "jobIDs" : [jobID] });
    },

    addToList: function(jobID: string): any{
        sendMessage({ "type" : "RegisterJobs",  "jobIDs" : [jobID]});
    },

    /**
     * to get a job that is stored
     * inside JobManager.list by jobID
     * @param jobID
     */
    getJob: function(jobID:string): any{
      return JobManager.data.filter(function(job: any){return job.jobID == jobID});
    },
    dataTables: function() : any {
        $('#jobManagerTable').dataTable({
            "bInfo": false,
            "bFilter": true,
            "data": JobManager.data,
            "order": [[ 3, "desc" ]],
            "columns": [
                {"mDataProp": "jobID"},
                {"mDataProp": "jobID"},
                {"mDataProp": "tool"},
                {"mDataProp": "dateCreated"},
                {"mDataProp": "jobID"}
            ],
            'columnDefs': [
                {
                    'targets': 4,
                    'searchable': false,
                    'orderable': false,
                    'render': function (jobID: any) {
                        return '<i class="delete fa fa-trash-o" onclick="JobManager.deleteJob(\'' + jobID + '\')"></i>';
                    }
                },
                {
                    'targets': 1,
                    'render': function(jobID: any){
                        return '<a href="/jobs/'+jobID+'">'+jobID+'</a>';
                    },
                    "createdCell": function (td: any, cellData: any, rowData: any, row: any, col: any) {
                        let job = JobManager.getJob(cellData);
                        if(job.length < 1){
                            return;
                        }
                        let status = a[job[0].status];
                        $(td).addClass(status);


                    },
                },
                {
                    'targets': 0,
                    'searchable': false,
                    'orderable': false,
                    'render': function (jobID: any) {
                        if(JobListComponent.contains(jobID)){
                            return '<i class="icon-minus remove" onclick="JobManager.removeFromList(\''+jobID+'\')"></i>';
                        }else{
                            return '<i class="icon-plus add" onclick="JobManager.addToList(\''+jobID+'\')"></i>';
                        }
                    }
                },
                {
                    'targets': 3,
                    render: function ( timestamp: any, type: any) {
                        // If display or filter data is requested, format the date
                        if ( type === 'display') {
                            return moment(timestamp).fromNow();
                        }

                        // Otherwise the data type requested (`type`) is type detection or
                        // sorting data, for which we want to use the integer, so just return
                        // that, unaltered
                        return timestamp;
                    }

                }],
        });
    },

    controller: function () {
        return {}
    },

    view: function (ctrl : any) {
        return [
            m("div", { "class": "large-2 padded-column columns show-for-large", id: "sidebar" }, [
                m(JobListComponent, { activejobID : m.route.param("jobID") })
            ]),
            m("div", {"class": "jobManagerContainer large-10"},
                m("div", {"class": "jobline"}, [
                    m("span", {"class": "toolname"}, [
                        m("a", "Job Manager")

                    ])
                ]),
                m("div", {id: "content", "class": "row columns padded-column", config: fadesIn},
                    m("table", {id: "jobManagerTable", "class": "dataTable hover row-border compact job-manager", config: this.dataTableLoader()}, [
                            m("thead", m("tr", [
                                m("th", "Job List"),
                                m("th", "Job ID"),
                                m("th", "Tool"),
                                m("th", "Created On"),
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