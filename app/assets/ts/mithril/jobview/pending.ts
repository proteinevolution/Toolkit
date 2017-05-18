(<any>window).JobPendingComponent = {

    controller : function(args : any){


        return {

            copyConfig : function(elem: any, isInit : boolean) {
                if (!isInit) {
                    let route = jsRoutes.controllers.JobController.checkHash(args.job().jobID);
                    m.request({method:route.method, url:route.url}).then(function(data : any){
                        if (data != null && data.jobID != null) {
                            $("#copyID").val(data.jobID.toString());
                        }
                    });
                }
            },

        }
    },
    view: function(ctrl : any, args : any) {
        return m("div", { "class": "pending-panel", config: foundationConfig }, [
            m('input', {id: "copyID", "type" : "hidden"}),
            m('h6', "A copy of your job was found in our database!"),
            m('h6', {config: ctrl.copyConfig}, "Job ID: " + $("#copyID").val()),
            m('div',{"class":"copySpacer"}),
            m('div', {"class": "openSimilarJob"}, [
                m("button",{ "class"   : "button submitJob",
                    onclick : function(e : any){
                        e.preventDefault();
                        let route = jsRoutes.controllers.JobController.startJob(args.job().jobID);
                        m.request({method:route.method, url:route.url}).then(function(data : any){
                            console.log("requested:",data);
                        });
                    }
                }, "Start Job anyways"),
                m("button",{ "class" : "hashPrompt button submitJob",
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
            )
        ]);
    }
};