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
                return m('a[href="/#/jobs/' + job.jobID + '"]', m("div", {
                    "class": ("job " + a[job.state]).concat(job.jobID === args.selected ? " selected" : "")
                }, [
                    m("div", {
                        "class": "jobid"
                    }, job.jobID), m("span", {
                        "class": "toolname"
                    }, job.toolname.substr(0, 4).toUpperCase()), m("a", {
                        "class": "boxclose",
                        onclick: args.clear.bind(ctrl, job.jobID)
                    })
                ]));
            }))
        ]);
    }
};