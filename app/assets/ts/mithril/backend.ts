/**
 * Created by astephens on 21.02.17.
 */

interface Window { Backend: any; }

window.Backend = {

    user : function(inputData : any) {
        let user = {
            id            : inputData.id,
            sessionID     : inputData.sessionID,
            sessionData   : inputData.sessionData,
            connected     : inputData.connected,
            accountType   : inputData.accountType,
            nameLogin     : inputData.nameLogin,
            eMail         : inputData.eMail,
            jobs          : inputData.jobs,
            dateLastLogin : inputData.dateLastLogin,
            dateCreated   : inputData.dateCreated,
            dateUpdated   : inputData.dateUpdated,
            data : function(key : string, val : string) {
                switch (key) {
                    case "nameLogin" :
                        this.nameLogin = val;
                        return this.nameLogin;
                    case "eMail" :
                        this.eMail = val;
                        return this.eMail;
                    case "accountType" :
                        this.accountType = val;
                        return this.accountType;
                    default: return { id            : this.id,
                        sessionID     : this.sessionID,
                        sessionData   : this.sessionData,
                        connected     : this.connected,
                        accountType   : this.accountType,
                        nameLogin     : this.nameLogin,
                        eMail         : this.eMail,
                        jobs          : this.jobs,
                        dateLastLogin : this.dateLastLogin,
                        dateCreated   : this.dateCreated,
                        dateUpdated   : this.dateUpdated }
                }
            },
            edit       : false,
            edited     : false,
            editField  : function(user : any) { return function(e : any) { console.log(user.data(e.target.id, e.target.value)) }},
            editToggle : function(user : any) { return function(e : any) { user.edit = !user.edit }},
            view : function(ctrl : any) {
                return m("tr", [
                    m("th", m("button", { "class" : "button small", onclick : this.editToggle(this) }, ">")),
                    m("th", this.edit ? m("input", {id:"nameLogin",   value: this.nameLogin,   onchange: this.editField(this) }) : m("p", this.nameLogin)),
                    m("th", this.edit ? m("input", {id:"accountType", value: this.accountType, onchange: this.editField(this) }) : m("p", this.accountType)),
                    m("th", this.edit ? m("input", {id:"eMail",       value: this.eMail,       onchange: this.editField(this) }) : m("p", this.eMail)),
                    m("th", m("p", this.dateCreated))
                ])
            }
        };
        //console.log(user.data());
        return user
    },

    plotter : function (ctrl : any) {
        return function (elem : any, isin : boolean, context : any) {
            if (!isin) {
                let chartElements = ctrl.data();//.map(function (item) {
                //console.log(item);
                //return item
                //});
                let xAxisElements = chartElements[0].datePushed.map(function(date : any){
                    return date.string
                });
                xAxisElements.splice(0,0,"Today");

                context.chart = Highcharts.chart("statchart",
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
                        series: chartElements.map(function (item : any) {
                            let monthlyElements = [item.current];
                            item.monthly.forEach(function(element : any){monthlyElements.push(element)});
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

    sendEdits : function (ctrl : any) {
        return function (e : any){ console.log(ctrl.user)}
    },

    content : function (ctrl : any) : any {
        switch(ctrl.section) {
            case "users" :
                //console.log(ctrl.user);
                let tableRows = ctrl.users.map(function(user : any){return user.view(ctrl)});
                tableRows.splice(0,0,
                    m("tr", {"class":"header"},[
                        m("th", "Edit"),
                        m("th", "User Name"),
                        m("th", "Account Type"),
                        m("th", "eMail Address"),
                        m("th", "Date created")
                    ]));
                //console.log(tableRows);
                return [m("table", tableRows),
                    m("button", { "class" : "button small", onclick : this.sendEdits(ctrl)}, "Save edited Users")];

            case "cms" :
                return m("table", [
                    m("tr", {"class" :"header"},[
                        m("th", "Title"),
                        m("th", "Date created")
                    ]),
                    ctrl.data().map(function (item : any) {
                        return m("tr", [
                            m("th", item.title),
                            m("th", item.dateCreated)
                        ])
                    })
                ]);

            case "statistics" :
                return [m("#statchart", {config : this.plotter(ctrl)}),
                    m("table", [
                        m("tr", {"class": "header"}, [
                            m("th", "Tool"),
                            m("th", "Usage this month"),
                            m("th", "Usage total"),
                            m("th", "Failed this month"),
                            m("th", "Failed total"),
                            m("th", "Last reset")]),
                        ctrl.data().map(function (item : any) {
                            let total       = item.current;
                            let totalFailed = item.currentFailed;
                            item.monthly.forEach(function(monthlyAmount : number){total = total + monthlyAmount});
                            item.monthlyFailed.forEach(function(monthlyAmount : number){totalFailed = totalFailed + monthlyAmount});
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

    model : function(section : any) {
        let editList : any = [];

        let data = m.request({method : "GET", url : "/backend/" + section});

        let userModel : any = [];

        data.then(function(a){
            a.map(function(b : any){
                if (section === "users") {
                    userModel.push(new window.Backend.user(b))
                }
                return editList.push(false)
            })
        });
        return { data : data , userModel : userModel}
    },

    controller: function(args : any) {
        currentRoute = "backend";
        let model = new window.Backend.model(m.route.param("section"));

        return {
            section : m.route.param("section"),
            data    : model.data,
            users   : model.userModel
        }
    },

    view: function(ctrl : any) {
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
        ]
    }
};
