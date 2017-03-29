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
                    'background': '#c0b5bf',
                    'box-shadow': '0 1 6px #9192af'
                });
            } else if (lastJob.state === 5) {
                console.log("Traffic Bar goes to done");
                $(elem).css({
                    'background': 'green',
                    'box-shadow': '0 1 6px #C3FFC3'
                });
            } else if (lastJob.state === 4) {
                console.log("Traffic Bar goes to error");
                $(elem).css({
                    'background': '#ff0000',
                    'box-shadow': '0 1 6px #FFC5C5'
                });
            } else if (lastJob.state === 3) {
                console.log("Traffic Bar goes to running");
                $(elem).css({
                    'background': '#ffff00',
                    'box-shadow': '0 1 6px #FFF666'
                });
            }
        }
    };
};

class LiveTable {
    static lastJob     : Job = null;
    static totalJobs   : number = 0;
    static load        : number = 0.5;
    static updateJobInfo () : void {
        m.request({method: "GET", url: "indexPageInfo"})
            .then(function(pageInfo) {
                console.log(pageInfo);
                LiveTable.lastJob   = pageInfo.lastJob;
                LiveTable.totalJobs = pageInfo.totalJobs;
            }).catch(function(error){console.log(error);});
    }
    static updateLoad (load : number) : void {
        LiveTable.load = load;
    }
    static controller (args : any) : any {
        if (args) {
            LiveTable.lastJob   = args.lastJob   ? args.lastJob          : LiveTable.lastJob;
            LiveTable.totalJobs = args.totalJobs ? args.totalJobs        : LiveTable.totalJobs;
            LiveTable.load      = args.load      ? parseFloat(args.load) : LiveTable.load;

            if(args.totalJobs == null && args.lastJob == null) {
                LiveTable.updateJobInfo();
            }
        }
        //console.log("register load..");
        //(<any>window).sendMessage({type: "RegisterLoad"}); // <- TODO this wont work for some reason
        //console.log("...sent!");
        return {}
    }
    static view (ctrl : any, args : any) : any {
        let loadString  : any = (LiveTable.load * 100).toPrecision(4) + "%",
            colorString : string = LiveTable.load < 0.7 ? "green;" : LiveTable.load < 0.9 ? "yellow;" : "red;";

        return m('div', [
            //m('div', {"class" : "clusterLoad column large-4"}, ""),
            m('table', {"class" : "liveTable column large-12"}, [
               ], [
                m('tbody',
                    [m('tr', [
                        m('td',{id: 'clusterLoadString'}, "Clusterload"),
                        m('td', {id: 'currentLoad', style: "color: " + colorString}, [
                        m("ul",
                            m("li"),
                            m("li"),
                            m("li"),
                            m("li"),
                            m("li"),
                            m("li"),
                            m("li"),
                        )
                        ]),
                        m('td',{id: 'currentLoadNumber'}, "" + loadString),
                        m('td',{id: 'separator'}),
                        m('td', { id: 'lastJobName' },
                            LiveTable.lastJob != null ?
                                m('a', { href: "/#/jobs/" + LiveTable.lastJob.jobID}, "Last Job: " + LiveTable.lastJob.toolnameLong) :
                                m('b', "No Jobs")
                        ),
                        m('td', {id: "joblistIcon"},
                            m('a', {href: "/#/joblist/", style: "font-weight: bold;" }, [
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