/**
 * Created by astephens on 21.02.17.
 */
var editUserArray = [];
window.Backend = {

    plotter : function (ctrl) {
        return function (elem, isin, context) {
            if (!isin) {
                var chartElements = ctrl.data();//.map(function (item) {
                    //console.log(item);
                    //return item
                //});
                var xAxisElements = chartElements[0].datePushed.map(function(date){
                    var newDate = date.string;
                    return newDate;
                });
                xAxisElements.splice(0,0,"Today");

                context.chart = new Highcharts.chart("statchart",
                    {
                        chart: { type: 'area'},
                        title: { text: 'Monthly useage by Tool' },
                        yAxis: { title: { text: 'Jobs per month' } },
                        xAxis: { title: { text: 'Month'},
                                 categories: xAxisElements,
                                 reversed: true
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
                            var monthlyElements = [item.current];
                            item.monthly.forEach(function(element){monthlyElements.push(element)});
                            //console.log(monthlyElements);
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
            case "users" :
                editUserArray = [];
                return m("table", [
                    m("tr", {class:"header"},[
                        m("th", "Edit"),
                        m("th", "User Name"),
                        m("th", "Account Type"),
                        m("th", "eMail Address"),
                        m("th", "Date created")
                    ]),
                    ctrl.data().map(function (user) {
                        editUserArray.push(false);
                        var arrayIndex = editUserArray.length -1;
                        console.log(arrayIndex + " " + editUserArray[arrayIndex]);
                        return m("tr", [
                            m("th", m("button", {class:"button small"}, {onClick : console.log("button Pressed: " + arrayIndex)}, ">")),
                            //editUserArray[arrayIndex] = !editUserArray[arrayIndex] TODO onclick does not seem to work - it triggers onload.
                            m("th", editUserArray[arrayIndex] ? m("input", {value: user.nameLogin})   : m("p",user.nameLogin)),
                            m("th", editUserArray[arrayIndex] ? m("input", {value: user.accountType}) : m("p",user.accountType)),
                            m("th", editUserArray[arrayIndex] ? m("input", {value: user.eMail})       : m("p",user.eMail)),
                            m("th", m("p",user.dateCreated))
                        ])
                    })
                ]);

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
                    m("li", m("a", {href:"#/backend/users"}, "Users"))
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
