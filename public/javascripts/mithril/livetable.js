var JobTable = {
    controller: function(args) {

        var ctrl = this;
        ctrl.totalJobs = -1;
        ctrl.lastJob = null;

        var lastJob = m.request({method: "GET", url: "/api/jobs"})
            .then(function(jobs) {
                console.log("JOBS" + jobs);
                ctrl.totalJobs = jobs.length;
                ctrl.lastJob = jobs.slice(-1)[0];
                console.log(JSON.stringify(ctrl.lastJob));

            });

    },
    view: function(ctrl, args) {
        return m('div', [
            m('table.liveTable', [
                m('thead', [
                    m('tr', [
                        m('th#run', 'Running jobs'),
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

var LiveTable = {
    controller: function(args) {
        var ctrl = this;

    },
    view: function(ctrl, args) {
        return m('div', [
            m.component(JobTable, {})
        ]);
    }
};