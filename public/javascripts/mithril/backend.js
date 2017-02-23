/**
 * Created by astephens on 21.02.17.
 */

window.Backend = {

    plotter : function (ctrl) {
        return function (elem, isin, context) {
            if (!isin) {
                var chartElements = ctrl.data().map(function (item) {
                    console.log(item);
                    return item
                });
                var xAxisElements = chartElements[0].datePushed.map(function(date){
                    var newDate = date.month + " " + date.year;
                    console.log(newDate);
                    return newDate;
                });
                xAxisElements.push("Today");

                context.chart = new Highcharts.chart("statchart",
                    {
                        chart: { type: 'area'},
                        title: { text: 'Monthly useage by Tool' },
                        yAxis: { title: { text: 'Jobs per month' } },
                        xAxis: { title: { text: 'Month'},
                                 categories: xAxisElements
                        },
                        plotOptions: {
                            area: {
                                stacking: 'normal',
                                lineColor: '#666666',
                                lineWidth: 1,
                                marker: {
                                    lineWidth: 1,
                                    lineColor: '#666666'
                                }
                            }
                        },
                        series: chartElements.map(function (item) {
                            var monthlyElements = [];
                            item.monthly.forEach(function(element){monthlyElements.push(element)});
                            monthlyElements.push(item.current);
                            console.log(monthlyElements);
                            return {
                                name : item.toolName,
                                data : monthlyElements
                            }
                        })
                    }
                )
            }
        }
    },

    content : function (ctrl) {
        switch(ctrl.section) {
            case "cms" :
                return m("table", [
                    m("tr", {class:"header"},[
                        m("th", "Title"),
                        m("th", "Date created")
                    ]),
                    ctrl.data().map(function (item) {
                        return m("tr", [
                            m("th", item.title),
                            m("th", item.dateCreated)
                        ])
                    })
                ]);

            case "statistics" :
                return [m("#statchart", {config : this.plotter(ctrl)}),
                m("table", [
                    m("tr", {class: "header"}, [
                        m("th", "Tool"),
                        m("th", "Usage this month"),
                        m("th", "Usage total"),
                        m("th", "Failed this month"),
                        m("th", "Failed total"),
                        m("th", "Last reset")]),
                    ctrl.data().map(function (item) {
                        var total       = item.current;
                        var totalFailed = item.currentFailed;
                        item.monthly.forEach(function(monthlyAmount){total = total + monthlyAmount});
                        item.monthlyFailed.forEach(function(monthlyAmount){totalFailed = totalFailed + monthlyAmount});
                        return m("tr", [
                            m("th", item.toolName),
                            m("th", item.current),
                            m("th", total),
                            m("th", item.currentFailed),
                            m("th", totalFailed),
                            m("th", item.datePushed[item.datePushed.length - 1].string)
                        ])
                    })
                ])];
            default:
                return "Index Page"
        }
    },

    model : function(section) {
        return { data : m.request({method      : "GET",
                                   url         : "/backend/" + section})}
    },

    controller: function() {
        var model = new Backend.model(m.route.param("section"));
        return {
            section : m.route.param("section"),
            data    : model.data
        };
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
                id: "content"}, [
                    this.content(ctrl)
                ]
            )
        ];
    }
};
