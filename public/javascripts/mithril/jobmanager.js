/**
 * Created by astephens on 07.03.17.
 */

window.JobManager = {
    model: function (ctrl) {
        return {data: m.request({"url": "jobs", "method": "GET", background: true})};
    },

    tableObjects: {
        names: [{id: "jobID", label: "Job ID"},
            {id: "tool", label: "Tool"},
            {id: "status", label: "Job State"},
            {id: "owner", label: "Owner"},
            {id: "dateCreated", label: "Created On", source: {_: "dateCreated.string", sort: "dateCreated.timestamp"}},
            {
                id: "dateUpdated",
                label: "Last Updated",
                source: {_: "dateUpdated.string", sort: "dateUpdated.timestamp"}
            },
            {id: "dateViewed", label: "Last Viewed", source: {_: "dateViewed.string", sort: "dateViewed.timestamp"}}],
        toColumnItems: function () {
            var tableHeaderItems = this.names.map(function (item) {
                return m("th", {id: item.id}, item.label)
            });
            tableHeaderItems.splice(0, 0, m("th", {id: "add"}, ""));
            return tableHeaderItems
        },
        toColumnNames: function () {
            var tableRowDataSelection = this.names.map(function (item) {
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
            return tableRowDataSelection
        }
    },

    dataTableLoader: function (ctrl) {
        return function (elem, isInit) {
            if (!isInit) {
                ctrl.data.then(function (jobData) {
                    var table = $("#" + elem.id).DataTable({
                        data: jobData,
                        columns: JobManager.tableObjects.toColumnNames(),
                        order: [[1, 'asc']]
                    });
                    table.on('click', 'td.addButton', function () {
                        var tr = $(this).closest('tr');
                        var row = table.row(tr);
                        m.route("/jobs/" + row.data().jobID);
                    })
                })
            }
        }
    },

    controller: function () {
        currentRoute = "jobmanager";
        var model = new JobManager.model();
        return {data: model.data}
    },

    view: function (ctrl) {
        return [
            m("div", { "class": "large-2 padded-column columns show-for-large", id: "sidebar" },
                m(JobListComponent, { activejobID : m.route.param("jobID") })
            ),
            m("div", { id: "content", "class": "large-10 small-12 columns padded-column", config: fadesIn },
                m("table", {id: "jobManagerTable", class: "dataTable", config: this.dataTableLoader(ctrl)}, [
                        m("thead", m("tr", JobManager.tableObjects.toColumnItems())),
                        m("tbody", [])
                    ]
                )
            )
        ];
    }
};