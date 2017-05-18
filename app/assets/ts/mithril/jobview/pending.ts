let JobPendingComponent = {
    view: function(ctrl : any, args : any) {
        return m("div", { "class": "pending-panel", config: foundationConfig }, [
            m('h6', "Your submission is pending, as there is a different job with similar parameters!"),
            m('div', {"class": "openSimilarJob"}, [
                m("button",{ "class"   : "button small",
                    onclick : function(e : any){
                        e.preventDefault();
                        let route = jsRoutes.controllers.JobController.startJob(args.job().jobID);
                        m.request({method:route.method, url:route.url}).then(function(data : any){
                            console.log("requested:",data);
                        });
                    }
                }, "Start Job anyways"),
                m("button",{ "class"   : "button small",
                    onclick : function(e : any){
                        e.preventDefault();
                        let route = jsRoutes.controllers.JobController.checkHash(args.job().jobID);
                        m.request({method:route.method, url:route.url}).then(function(data : any){
                            if (data != null && data.jobID != null) {
                                m.route("/jobs/"+data.jobID);
                                JobListComponent.reloadJob(data.jobID);
                            }
                            console.log("requested:",data);
                        });
                    }
                }, "Reload existing job")
            ]),
            m("div", {"class": "processJobIdContainer"},
                m('table',
                    m('tr',
                        m('td',
                            m('b', "Job ID:"),
                            m('p', ' ' + args.job().jobID))))
            )
        ]);
    }
};