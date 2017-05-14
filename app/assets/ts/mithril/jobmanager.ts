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

    tableObjects: {
        names: [
            {id: "jobID", label: "Job ID"},
            {id: "tool", label: "Tool"},
            {id: "status", label: "Job State"},
            {id: "dateCreated", label: "Created On", source: {_: "dateCreated.string", sort: "dateCreated.timestamp"}},
            {id: "removeJob", label: "Delete Job"}
        ],
        toColumnItems: function () {
            let tableHeaderItems = this.names.map(function (item : any) {
                return m("th", {id: item.id}, item.label)
            });
            tableHeaderItems.splice(0, 0, m("th", {id: "add"}, ""));
            return tableHeaderItems;
        },
        toColumnNames: function () {
            let tableRowDataSelection = this.names.map(function (item : any) {
                if (item.source) {
                    return {data: item.source}
                } else {
                    return {data: item.id}
                }
            });
            tableRowDataSelection.splice(0, 0, {
                "className": "addButton",
                "orderable": false,
                "data": null,
                "defaultContent": "<i class='icon-reply'></i>"
            });

            tableRowDataSelection.splice(5, 5, {
                "className": "deleteJob",
                "orderable": false,
                "data": null,
                "defaultContent": "<i class='delete fa fa-trash-o' type='button'></i>"

            });
            return tableRowDataSelection;
        }
    },

    dataTableLoader: function () {
        return function (elem : any, isInit : boolean) {
            if (!isInit) {

                    //console.log(JSON.stringify(jobData));

                JobManager.data.map(function(x : any) : any {
                        switch(x.status) {
                            case 2:
                                return x.status = "queued";
                            case 3:
                                return x.status = "running";
                            case 4:
                                return x.status = "error";
                            case 5:
                                return x.status = "done";
                            case 7:
                                return x.status = "pending";
                            default:
                                return x.status = "undefined";
                        }
                    });
                    let $table = $("#" + elem.id);
                    let table = $table.DataTable({
                        data: JobManager.data,
                        columns: JobManager.tableObjects.toColumnNames(),
                        order: [[4, 'desc']]
                    });
                    $table.on('click', 'td.addButton', function () {
                        let tr = $(this).closest('tr');
                        let row = table.row(tr);
                        let rowData : JobManagerObject = <JobManagerObject>row.data();
                        m.route("/jobs/" + rowData.jobID);
                    });
                    $table.on('click', 'td.deleteJob', function () {
                        console.log(JSON.stringify(JobManager.data));
                        let tr = $(this).closest('tr');
                        let row = table.row(tr);
                        let rowData : JobManagerObject = <JobManagerObject>row.data();
                        JobListComponent.removeJob(rowData.jobID, true, true);
                        JobManager.reload();
                    })
            }
        }
    },

    data : null,

    reload : function() {
        console.log("TEST1");
        m.redraw();
            m.request({"url": "jobs", "method": "GET", background: true})
                .then(function(response){
                    JobManager.data = response;
                    m.redraw(true);
                })
                .catch(function(e){console.warn(e);})

    },

    controller: function () {
        JobManager.reload();
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
                    m("table", {id: "jobManagerTable", "class": "dataTable hover row-border compact", config: this.dataTableLoader()}, [
                            m("thead", m("tr", JobManager.tableObjects.toColumnItems())),
                            m("tbody", [])
                        ]
                    )
                )
            )
        ];
    }
};