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
    model: function (ctrl : any) {
        return {data: m.request({"url": "jobs", "method": "GET", background: true})};
    },

    tableObjects: {
        names: [{id: "jobID", label: "Job ID"},
            {id: "tool", label: "Tool"},
            {id: "status", label: "Job State"},
            {id: "dateCreated", label: "Created On", source: {_: "dateCreated.string", sort: "dateCreated.timestamp"}},
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
                "defaultContent": "\u25c0"
            });
            return tableRowDataSelection;
        }
    },

    dataTableLoader: function (ctrl : any) {
        return function (elem : any, isInit : boolean) {
            if (!isInit) {
                ctrl.data.then(function (jobData : any) {
                    let $table = $("#" + elem.id);
                    let table = $table.DataTable({
                        data: jobData,
                        columns: JobManager.tableObjects.toColumnNames(),
                        order: [[1, 'asc']]
                    });
                    $table.on('click', 'td.addButton', function () {
                        let tr = $(this).closest('tr');
                        let row = table.row(tr);
                        let rowData : JobManagerObject = <JobManagerObject>row.data();
                        m.route("/jobs/" + rowData.jobID);
                    })
                })
            }
        }
    },

    controller: function () {
        currentRoute = "jobmanager";
        let model = new JobManager.model();
        return {data: model.data}
    },

    view: function (ctrl : any) {
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
            m("div", {"class": "jobManagerContainer large-10"},
                m("div", {"class": "jobline"}, [
                    m("span", {"class": "toolname"}, [
                        m("a", "Job Manager")

                    ])
                ]),
                m("div", {id: "content", "class": "row columns padded-column", config: fadesIn},
                    m("table", {id: "jobManagerTable", "class": "dataTable hover row-border compact", config: this.dataTableLoader(ctrl)}, [
                            m("thead", m("tr", JobManager.tableObjects.toColumnItems())),
                            m("tbody", [])
                        ]
                    )
                )
            )
        ];
    }
};