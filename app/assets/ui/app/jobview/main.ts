(<any>window).JobViewComponent = {

    controller: function(args : any) {
       return {}
    },
    view: function(ctrl : any, args : any) {
        if (!args.job()) {
            return m("div", {"class" : "still_waiting", config: initLoader}, "");
        } else {
            return m("div", {
                id: "jobview"
            }, [
                m(JobLineComponent, { job: args.job }),
                m(JobTabsComponent, { job: args.job })
            ]);
        }
    }
};


const JobLineComponent = {
    controller : function() {},
    view: function(ctrl : any, args : any) {
        let isJob: boolean;

        const dateCreated = moment(args.job().dateCreated).utcOffset(2, true).local();

        isJob = args.job().isJob;
        return m("div", {
            "class": "jobline"
        }, [
            m((<any>window).HelpModalComponent, { toolname: args.job().tool.toolname, toolnameLong: args.job().tool.toolnameLong }),
            m("span", { "class": "toolname" }, [
                m("input", { id: "toolnameAccess", "style": "display: none;", type: "text", value: args.job().tool.toolname}),
                m("a", { onclick: function(){m.route("/tools/" + args.job().tool.toolname)}}, args.job().tool.toolnameLong),
                m("a", { onclick: function(){
                        const route = jsRoutes.controllers.DataController.getHelp(args.job().tool.toolname);
                        $.ajax({
                            url: route.url,
                            method: route.method
                        }).done(function (help) {
                            const helpModal = $("#helpModal");
                            helpModal.find(".modal-content").html(help);
                            helpModal.find(".accordion").foundation();
                            helpModal.foundation("open");
                            helpModal.find("#tabs").tabs();
                        });
                    } },
                        m("i", { "class": "icon-white_question helpicon", "title": "Help page", "data-tooltip": "", "config": tooltipConf})
                )
            ]),
            m("span", { "class": "jobdate" }, isJob ? "Created: " + dateCreated.format("lll") : ""),
            m("span", { "class": "jobinfo" }, isJob ? "JobID: " + args.job().jobID: "")
        ]);
    }
};

const initLoader = function(elem: any, isInit: boolean) : any {
    if(!isInit) {
        return setTimeout(function(){ $(elem).show(); }, 1000); // css loading animation to be shown only when the transition to job state tabs takes too long
    }
};
