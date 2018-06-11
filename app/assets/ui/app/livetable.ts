const trafficBarConfig = function(lastJob: any) {
    return function(elem: any, isInit: Boolean): void {
        if (lastJob != null && !isInit) {
            elem.setAttribute("data-disable-hover", "false");
            elem.setAttribute("title", "View last job: " + lastJob.jobID);
            $(elem).attr({"data-tooltip": ""}).foundation();
        }
    };
};

class LoadBar {
    static load: number = 0;

    static updateLoad(load: number): any {
        LoadBar.load = load;
        m.redraw.strategy("diff");
        m.redraw();
    }

    static controller(args: any): any {
        if (args) {
            LoadBar.load = args.load ? parseFloat(args.load) : LoadBar.load;
        }
        return {};
    }

    static view(ctrl: any, args: any): any {
        let currentLoad: number = LoadBar.load,
            loadval: number = Math.ceil(currentLoad * 100),
            loadString: string = loadval + "%",
            colorClass: string = "loadBar " + (currentLoad < 0.50 ? "green" : currentLoad < 0.7 ? "yellow" : "red");

        return m("div", {id: "indexLoadBar"}, [
            m("div", {"class": "loadBarLabel"}, "Cluster workload: " + loadString),
            m("div", {"class": "loadBarGraph"}, m("div", {"class": "loadBarSize"}, m("table",
                m("tr", [
                    m("th", {"class": colorClass + (loadval < 40 ? " pulsating" : "")}),
                    m("th", {
                        "class": (loadval < 40 ? "loadBar gray" : colorClass) +
                        (40 < loadval && loadval < 60 ? " pulsating" : "")
                    }),
                    m("th", {
                        "class": (loadval < 60 ? "loadBar gray" : colorClass) +
                        (60 <= loadval && loadval < 80 ? " pulsating" : "")
                    }),
                    m("th", {
                        "class": (loadval < 80 ? "loadBar gray" : colorClass) +
                        (80 <= loadval && loadval < 100 ? " pulsating" : "")
                    })
                ])
            )))
        ]);
    }
}

class LiveTable {
    static lastJob: Job = null;
    static totalJobs: number = 0;

    static updateJobInfo(): void {
        m.request({method: "GET", url: "/index/page/info", background: true})
            .then(function(pageInfo) {
                LiveTable.lastJob = pageInfo.lastJob;
            }).catch(function(error) {
            console.warn(error);
        });
    }

    static pushJob(job: Job): void {
        LiveTable.lastJob = job;
        // only redraw when on index page since a m.redraw would affect the inputs
        if (m.route() == "/") {
            m.redraw.strategy("diff");
            m.redraw();
        }
    }

    static controller(args: any): any {
        currentRoute = "index"; // Need to use this method to find the current route
        if (args) {
            LiveTable.lastJob = args.lastJob ? args.lastJob : LiveTable.lastJob;
            LiveTable.totalJobs = args.totalJobs ? args.totalJobs : LiveTable.totalJobs;
            if (args.totalJobs == null && args.lastJob == null) {
                LiveTable.updateJobInfo();
            }
        }
        return {};
    }

    static view(ctrl: any, args: any): any {
        let trafficBarStatus: any;
        if (LiveTable.lastJob != null) {
            switch (LiveTable.lastJob.status) {
                case 2:
                    trafficBarStatus = "queue";
                    break;
                case 3:
                    trafficBarStatus = "running";
                    break;
                case 4:
                    trafficBarStatus = "error";
                    break;
                case 5:
                    trafficBarStatus = "done";
                    break;
                default:
                    trafficBarStatus = "not_init";
                    break;
            }
        } else {
            trafficBarStatus = "not_init";
        }
        return m("div", [
            //m('div', {"class" : "clusterLoad column large-4"}, ""),
            m("table", {"class": "liveTable"}, [
                m("tbody",
                    [m("tr", [
                        m("td", m.component(LoadBar, {})),
                        m("td", {id: "joblistIcon"},
                            m("a", {
                                href: "/#/jobmanager",
                                id: "jobmanagerIcon",
                                style: "font-weight: bold;"
                            }, "Job Manager", [
                                m("i", {"class": "icon-list"})
                            ])
                        )
                    ])]
                )
            ]),
            m("a", {
                id: "trafficbar",
                "class": ("trafficbar " + trafficBarStatus),
                config: trafficBarConfig(LiveTable.lastJob),
                href: LiveTable.lastJob !== null ? "#/jobs/" + LiveTable.lastJob.jobID : "#",
                onclick: function() {
                    // hide old tooltip
                    $('#' + $("#trafficbar").data('toggle')).remove();
                }
            })
        ]);
    }
}
