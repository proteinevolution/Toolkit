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


interface Window { JobManager: any; }
window.JobManager = {
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

    reload : function() {
        $('#jobManagerTable').DataTable().clear();
            m.request({"url": "jobs", "method": "GET", background: true})
                .then(function(response) {
                    if (response) {
                        JobManager.data = response;
                        $('#jobManagerTable').DataTable().rows.add(JobManager.data);
                        $('#jobManagerTable').DataTable().draw();
                        m.redraw(true);
                    }
                })
                .catch(function(e){console.warn(e);})

    },
    deleteJob: function(jobID : string): any{
        m.request({ url: "/api/job/" + jobID, method: "DELETE" }).then(function(){
            JobManager.reload();
        });
    },
    removeFromList: function(jobID: string): any{
        sendMessage({ "type" : "ClearJob",  "jobID" : jobID });
        JobManager.reload()
    },

    addToList: function(jobID: string): any{
        let job = JobListComponent.Job(JobManager.getJob(jobID)[0]);
        if(job) {
           JobListComponent.pushJob(job);
            JobManager.reload();
        }
    },

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
                {"mDataProp": "dateCreated.string"},
                {"mDataProp": "jobID"}
            ],
            'columnDefs': [
                {
                    'targets': 4,
                    'searchable': false,
                    'orderable': false,
                    'render': function (jobID: any) {
                        return '<i class="delete fa fa-trash-o" type="button" onclick="JobManager.deleteJob(\'' + jobID + '\')"></i>';
                    }
                },
                {
                    'targets': 1,
                    'render': function(jobID: any){
                        return '<a href="/jobs/'+jobID+'" type="button">'+jobID+'</a>';
                    }
                },
                {
                    'targets': 0,
                    'searchable': false,
                    'orderable': false,
                    'render': function (jobID: any) {
                        if(JobListComponent.contains(jobID)){
                            return '<i class="icon-minus remove" type="button" onclick="JobManager.removeFromList(\''+jobID+'\')"></i>';
                        }else{
                            return '<i class="icon-plus add" type="button" onclick="JobManager.addToList(\''+jobID+'\')"></i>';
                        }
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