var JobTable = {
    controller: function() {

    },
    view: function(ctrl, args) {
        return m('div', [
            m('table.liveTable', [
                m('thead', [
                    m('tr', [
                        m('th', 'Running'),
                        m('th', 'Users'),
                        m('th', 'Last job'),
                        m('th', 'Jobs last 24h'),
                        m('th', 'Cluster load')
                    ]),
                m('tbody', [m('tr', [
                    m('td', '10'),
                    m('td', '200'),
                    m('td', 'HHpred'),
                    m('td', '1111'),
                    m('td', '14')
                ])
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