(<any>window).JobViewComponent = {

    view: function(ctrl : any, args : any) {
        if (!args.job()) {
            return m("div", {"class" : "still_waiting", config: initLoader}, "");
        } else {
            return m("div", {
                id: "jobview"
            }, [
                m(JobLineComponent, { job: args.job }),
                m(JobTabsComponent, { job: args.job, owner: args.owner })
            ]);
        }
    }
};

let initLoader = function(elem: any, isInit: boolean) : any {
    if(!isInit) {
        return setTimeout(function(){ $(elem).show(); }, 200); // css loading animation to be shown only when the transition to job state tabs takes too long
    }
};