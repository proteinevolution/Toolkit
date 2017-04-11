import ElementConfig = Mithril.ElementConfig;


let trafficBarConfig = function(lastJob : any) {
    return function (elem : any, isInit : Boolean) : void {
        if (lastJob != null && !isInit) {
            elem.setAttribute("data-disable-hover", "false");
            //elem.setAttribute("data-tooltip", "data-tooltip");
            elem.setAttribute("title", "Click to view last job: " + lastJob.jobID);
            console.log("Traffic bar sees status " + lastJob.state);
            if (lastJob.state === -1) {
                return console.log("Hide Trafficbar");
            } else if (lastJob.state === 2) {
                console.log("Traffic Bar goes to queued");
                $(elem).css({
                    'background': 'rgba(192, 181, 191, 0.5)',
                    'box-shadow': '0 1 6px #9192af'
                });
            } else if (lastJob.state === 5) {
                console.log("Traffic Bar goes to done");
                $(elem).css({
                    'background': 'rgba(0, 180, 40, 0.2)',
                    'box-shadow': '0 1 6px #C3FFC3'
                });
            } else if (lastJob.state === 4) {
                console.log("Traffic Bar goes to error");
                $(elem).css({
                    'background': 'rgba(180, 0, 40, 0.2)',
                    'box-shadow': '0 1 6px #FFC5C5'
                });
            } else if (lastJob.state === 3) {
                console.log("Traffic Bar goes to running");
                $(elem).css({
                    'background': 'rgba(255, 255, 0, 0.4)',
                    'box-shadow': '0 1 6px #FFF666'
                });
            }
        }
    };
};

class LoadBar {
    static load : number = 0.5;
    static updateLoad (load : number) : any {
        LoadBar.load = load;
        m.redraw.strategy("diff");
        m.redraw();
    }
    static controller (args : any) : any {
        if (args) {
            LoadBar.load      = args.load      ? parseFloat(args.load) : LoadBar.load;
        }
        return {}
    }
    static view (ctrl : any, args : any) : any {
        console.log("redrawing");
        let currentLoad : number = LoadBar.load,
            loadString : string = Math.ceil(currentLoad * 100) + "%",
            colorClass : string = "loadBar " + (currentLoad < 0.90 ? "green" : currentLoad < 1.3 ? "yellow" : "red");

        return m('div', {id:"indexLoadBar"},  [
            m('div', {class: 'loadBarLabel'}, "Cluster workload"),
            m('div', {class: 'loadBarGraph'}, m('div', {class: 'loadBarSize'}, m('table',
                m('tr', [
                    m("th", {class: colorClass + (currentLoad < 0.4 ? " pulsating" : "")}),
                    m("th", {class: (currentLoad < 0.4 ? "loadBar gray" : colorClass) +
                                    (0.4  < currentLoad && currentLoad < 0.6 ? " pulsating" : "")}),
                    m("th", {class: (currentLoad < 0.6 ? "loadBar gray" : colorClass) +
                                    (0.6 <= currentLoad && currentLoad < 0.8 ? " pulsating" : "")}),
                    m("th", {class: (currentLoad < 0.8 ? "loadBar gray" : colorClass) +
                                    (0.8 <= currentLoad && currentLoad < 1.0 ? " pulsating" : "")}),
                    m("th", {class: (currentLoad < 1.0 ? "loadBar gray" : colorClass) +
                                    (1.0 <= currentLoad && currentLoad < 2.5 ? " pulsating" : "")}),
                    m("th", {class: (currentLoad < 2.5 ? "loadBar gray" : colorClass) +
                                    (2.5 <= currentLoad && currentLoad < 5.0 ? " pulsating" : "")}),
                    m("th", {class: (currentLoad < 5.0 ? "loadBar gray" : colorClass + " pulsating")}),
                ])
            )),m('div',{class: 'loadBarString'}, "" + loadString)),

        ])
    }
}

class LiveTable {
    static lastJob     : Job = null;
    static totalJobs   : number = 0;
    static updateJobInfo () : void {
        m.request({method: "GET", url: "indexPageInfo"})
            .then(function(pageInfo) {
                console.log(pageInfo);
                LiveTable.lastJob   = pageInfo.lastJob;
                LiveTable.totalJobs = pageInfo.totalJobs;
                m.redraw.strategy("diff");
            }).catch(function(error){console.log(error);});
    }
    static controller (args : any) : any {
        currentRoute = "index"; // Need to use this method to find the current route
        if (args) {
            LiveTable.lastJob   = args.lastJob   ? args.lastJob          : LiveTable.lastJob;
            LiveTable.totalJobs = args.totalJobs ? args.totalJobs        : LiveTable.totalJobs;

            if(args.totalJobs == null && args.lastJob == null) {
                LiveTable.updateJobInfo();
            }
        }
        return {}
    }
    static view (ctrl : any, args : any) : any {
        return m('div', [
            //m('div', {"class" : "clusterLoad column large-4"}, ""),
            m('table', {"class" : "liveTable"}, [
                m('tbody',
                    [m('tr', [
                        m('td', m.component(LoadBar, {})),
                        m('td',{id: 'separator'}),
                        m('td', { id: 'lastJob' },
                            LiveTable.lastJob != null ?
                                m('a', { href: "/#/jobs/" + LiveTable.lastJob.jobID },
                                    "Last Job: " + LiveTable.lastJob.toolnameLong) :
                                m('b', "No Jobs")
                        ),
                        m('td', {id: "joblistIcon"},
                            m('a', {href: "/#/jobmanager", id: "jobmanagerIcon", style: "font-weight: bold;" }, [
                                m("i", {class: "icon-list"})
                            ])
                        )
                    ])]
                )
            ]),
            m("div", { id: "trafficbar",
                       "class": "trafficbar",
                       config: trafficBarConfig(LiveTable.lastJob),
                       onclick: function () { m.route("/jobs/" + LiveTable.lastJob.jobID); }
            })
        ]);
    }
}