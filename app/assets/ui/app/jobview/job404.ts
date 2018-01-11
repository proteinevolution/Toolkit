(<any>window).Job404Component = {

    controller: function(args: any) {
        return {};
    },
    view: function(ctrl: any, args: any) {
        return m("div", {"class": "callout"}, [
            m("h5", "Job not found."),
            m("a[href=/jobs/" + args.jobID + "]", "Try again!")
        ]);
    }
};