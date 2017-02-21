/**
 * Created by astephens on 21.02.17.
 */

window.Backend = {
    controller: function() {
        var v = m.request({method      : "GET",
                           url         : "/backend/" + m.route.param("section"),
                           deserialize : function(data) {return data;}});
        return {view: v};
    },
    view: function(ctrl) {
        return [
            m("div", {
                "class": "large-2 padded-column columns show-for-large",
                id: "sidebar"
                }, [
                m("ul",[
                    m("li", m("a", {href:"#/backend/index"}, "Index")),
                    m("li", m("a", {href:"#/backend/statistics"}, "Statistics")),
                    m("li", m("a", {href:"#/backend/cms"}, "CMS")),
                    m("li", m("a", {href:"#/backend/test"}, "Test"))
                    ])
                ]
            ),
            m("div", {
                "class": "large-10 padded-column columns show-for-large",
                id: "content"},
                m.trust(ctrl.view())
            )
        ];
    }
};
