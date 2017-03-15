var JobTable = {
    controller: function() {

    },
    view: function(ctrl, args) {
        return m('div', [
            m('table.liveTable', [
                m('thead', [
                    m('tr', [
                        m('th#run', 'Running jobs'),
                        m('th', 'Users'),
                        m('th', 'Last job'),
                        m('th#lastJobs', 'Jobs last 24h')
                    ])
                ])],[
                m('tbody', [m('tr', [
                    m('td', '16'),
                    m('td', '46'),
                    m('td', 'HHpred'),
                    m('td', '1111')
                ])])
            ])
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