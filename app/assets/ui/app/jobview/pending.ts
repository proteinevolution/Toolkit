/// <reference path="helper.ts"/>
const JobPendingComponent = {

    controller : function(args : any){


        return {

            copyConfig : function(elem: any, isInit : boolean) {
                if (!isInit) {
                    let route = jsRoutes.controllers.JobController.checkHash(args.job().jobID);
                    m.request({method:route.method, url:route.url, extract: nonJsonErrors}).then(function(data : any){
                        if (data != null && data.jobID != null) {
                            $("#copyID").val(data.jobID.toString());
                            $("#copyDate").val(data.dateCreated);
                        }
                    }, function(error) {console.log(error)}).catch(function(e) {});
                }
            },

        }
    },
    view: function(ctrl : any, args : any) {
        return m("div", { "class": "pending-panel", config: foundationConfig }, [
            m('input', {id: "copyID", "type" : "hidden"}),
            m('input', {id: "copyDate", "type" : "hidden"}),
            m('h6', "We found an identical copy of your job in our database!"),
            m('h6', {config: ctrl.copyConfig}, "Job ID: " + $("#copyID").val() + ", which was created " + moment(parseInt($("#copyDate").val())).fromNow()),
            m('div',{"class":"copySpacer"}),
            m('div', {"class": "openSimilarJob"}, [
                m("button",{ "class"   : "button submitJob",
                    config : enabled,
                    onclick : function(e : any){
                        e.preventDefault();
                        const route = jsRoutes.controllers.JobController.startJob(args.job().jobID);
                        m.request({method:route.method, url:route.url}).then(function(data : any){
                            console.log("requested:",data);
                        });
                    }
                }, "Start job anyway"),
                m("button",{ "class" : "hashPrompt button submitJob",
                    config: enabled,
                    onclick : function(e : any){
                        e.preventDefault();
                        let route = jsRoutes.controllers.JobController.checkHash(args.job().jobID);
                        m.request({method:route.method, url:route.url, extract: nonJsonErrors}).then(function(data : any){
                            if (data != null && data.jobID != null) {
                                m.route("/jobs/"+data.jobID);
                                JobListComponent.reloadJob(data.jobID);
                            }
                            console.log("requested:",data);
                        }, function(error) {console.log(error)}).catch(function(e) {});
                    }
                }, "Load existing job"),
                m("button",{ "class" : "hashPrompt button submitJob",
                    config: enabled,
                    onclick : function(e : any){
                        e.preventDefault();
                        const route = jsRoutes.controllers.JobController.checkHash(args.job().jobID);
                        m.request({method:route.method, url:route.url, extract: nonJsonErrors}).then(function(data : any){
                            if (data != null && data.jobID != null) {
                                JobListComponent.removeJob(args.job().jobID, true, true);
                                m.route("/jobs/"+data.jobID);
                                JobListComponent.reloadJob(data.jobID);
                            }
                            console.log("requested:",data);
                        }, function(error) {console.log(error)}).catch(function(e) {});
                    }
                }, "Load existing job and delete this one")
            ]),
            m("div", {"class": "processJobIdContainer"},
            )
        ]);
    }
};