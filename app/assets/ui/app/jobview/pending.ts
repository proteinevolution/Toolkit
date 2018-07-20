/// <reference path="helper.ts"/>
const JobPendingComponent = {
    controller : (args : any) => {
        return {
            checkHashRoute: "/api/jobs/check/hash/" + args.job().jobID,
            copyConfig : (elem: any, isInit : boolean) => {
                if (!isInit) {
                    m.request({method:"GET", url: "/api/jobs/check/hash/" + args.job().jobID, extract: nonJsonErrors}).then((data : any) => {
                        if (data.jobID) {
                            $("#copyID").val(data.jobID.toString());
                            $("#copyDate").val(data.dateCreated);
                        }
                    }, error => {console.log(error)}).catch(() => {});
                }
            },
        }
    },
    view: (ctrl : any, args : any) => {
        return m("div", { "class": "pending-panel", config: foundationConfig }, [
            m('input', {id: "copyID", "type" : "hidden"}),
            m('input', {id: "copyDate", "type" : "hidden"}),
            m('h6', "We found an identical copy of your job in our database!"),
            m('h6', {config: ctrl.copyConfig}, "Job ID: " + $("#copyID").val() + ", which was created " + moment(parseInt($("#copyDate").val())).fromNow()),
            m('div',{"class":"copySpacer"}),
            m('div', {"class": "openSimilarJob"}, [
                m("button",{ "class"   : "button submitJob",
                    config : enabled,
                    onclick : (e : any) => {
                        e.preventDefault();
                        m.request({method: "POST", url: "/api/jobs/start/" + args.job().jobID })
                    }
                }, "Start job anyway"),
                m("button",{ "class" : "hashPrompt button submitJob",
                    config: enabled,
                    onclick : (e : any) => {
                        e.preventDefault();
                        m.request({method:"GET", url:ctrl.checkHashRoute, extract: nonJsonErrors}).then((data : any) => {
                            if (data.jobID) {
                                m.route("/jobs/"+data.jobID);
                                JobListComponent.reloadJob(data.jobID);
                            }
                            console.log("requested:",data);
                        }, error => {console.log(error)}).catch(e => {});
                    }
                }, "Load existing job"),
                m("button",{ "class" : "hashPrompt button submitJob",
                    config: enabled,
                    onclick : (e : any) => {
                        e.preventDefault();
                        m.request({method:"GET", url:ctrl.checkHashRoute, extract: nonJsonErrors}).then((data : any) => {
                            if (data.jobID) {
                                JobListComponent.removeJob(args.job().jobID, true, true);
                                m.route("/jobs/"+data.jobID);
                                JobListComponent.reloadJob(data.jobID);
                            }
                            console.log("requested:",data);
                        }, error => {console.log(error)}).catch(e => {});
                    }
                }, "Load existing job and delete this one")
            ]),
            m("div", {"class": "processJobIdContainer"},
            )
        ]);
    }
};