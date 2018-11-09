(<any>window).JobViewComponent = {
    controller: (args : any) => {
       return {}
    },
    view: (ctrl : any, args : any) => {
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
    controller : () => {},
    view: (ctrl : any, args : any) => {
        let isJob: boolean;
        const dateCreated = moment(args.job().dateCreated).utcOffset(1, true).local();
        isJob = args.job().isJob;
        return m("div", {
            "class": "jobline"
        }, [
            m((<any>window).HelpModalComponent, { toolname: args.job().tool.toolname, toolnameLong: args.job().tool.toolnameLong }),
            m("span", { "class": "toolname" }, [
                m("input", { id: "toolnameAccess", "style": "display: none;", type: "text", value: args.job().tool.toolname}),
                m("a", { onclick: () => {m.route("/tools/" + args.job().tool.toolname)}}, args.job().tool.toolnameLong),
                m("a", {
                    config: (el: any, isInit: boolean) => {
                        if (!isInit) {
                            if (args.job().tool.toolname === 'hhpred_manual') $(el).hide();
                            } else return;
                        },
                        onclick: () => {
                        m.request({
                            url: "/help/" + args.job().tool.toolname,
                            method: "GET",
                            background: true,
                            deserialize: string => { return string; }
                        }).then( help => {
                            const helpModal = $("#helpModal");
                            helpModal.find(".modal-content").html(help);
                            helpModal.find(".accordion").foundation();
                            helpModal.foundation("open");
                            helpModal.find("#tabs").tabs();
                        });
                    }},
                        m("i", { "class": "icon-white_question helpicon", "title": "Help page", "data-tooltip": "", "config": tooltipConf})
                )
            ]),
            m("span", { "class": "jobdate" }, isJob ? "Created: " + dateCreated.format("lll") : ""),
            m("span", { "class": "jobinfo" }, isJob ? "JobID: " + args.job().jobID: "")
        ]);
    }
};

const initLoader = (elem: any, isInit: boolean) : any => {
    if(!isInit) {
        return setTimeout(() => { $(elem).show(); }, 1000); // css loading animation to be shown only when the transition to job state tabs takes too long
    }
};
