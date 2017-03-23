import ElementConfig = Mithril.ElementConfig;


let gaugeConfig = function(elem: Element, isInit: boolean, ctx: any) {

    if(!isInit){

        let g = new JustGage({
            id: "clusterLoad",
            value:32,
            min:0,
            max:100,
            title:"Cluster load",
            label:"",
            showMinMax:false,
            gaugeColor:"#fff",
            levelColors:["#a9d70b", "#f9c802", "#ff0000"],
            refreshAnimationType:"linear"
        });

    }

};


let JobTable = {
    controller: function(args : any) {

        let ctrl = this;
        ctrl.totalJobs = -1;
        ctrl.lastJob = {};

        m.request({method: "GET", url: "/api/jobs"})
            .then(function(jobs) {

                if(jobs.length > 0){
                    ctrl.totalJobs = jobs.length;
                    ctrl.lastJob = jobs.slice(-1)[0];
                } else {
                    ctrl.totalJobs = 0;
                    ctrl.lastJob = {
                        "jobID": -1,
                        "toolnameLong": ""
                    };
                }

            }).catch(function(e) {
            ctrl.totalJobs = 0;
            ctrl.lastJob = {
                "jobID": -1,
                "toolnameLong": ""
            };

        });

    },
    view: function(ctrl : any, args : any) {
        return m('div', {"class" : "row"}, [
            m('div#clusterLoad', {"class" : "column large-4", config: gaugeConfig}, ''),
            m('table.liveTable', {"class" : "column large-5"}, [
                m('thead', [
                    m('tr', [
                        m('th#lastJob', 'Last own job'),
                        m('th#lastJobs', 'Total own jobs')
                    ])]
                )], [
                m('tbody',
                    [m('tr', [
                        m('td#lastJobName', m('a[href="/#/jobs/' + ctrl.lastJob.jobID + '"]', ctrl.lastJob.toolnameLong)),
                        m('td', ctrl.totalJobs)
                    ])]
                )])
        ]);
    }
};

let LiveTable = {
    controller: function(args : any) {
        let ctrl = this;

    },
    view: function(ctrl : any, args : any) {
        return m('div', [
            m.component(JobTable, {})
        ]);
    }
};