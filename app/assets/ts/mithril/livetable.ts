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
        return m('div', [
            m('table.liveTable', [
                m('thead', [
                    m('tr', [
                        m('th#run', 'Cluster load'),
                        m('th', 'Users'),
                        m('th#lastJob', 'Last own job'),
                        m('th#lastJobs', 'Total own jobs')
                    ])]
                )], [
                m('tbody',
                    [m('tr', [
                        m('td', '16'),
                        m('td', '46'),
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