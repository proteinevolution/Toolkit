let JobQueuedComponent = {
    updateLog: function(){
        m.redraw(true);
    },
    controller : function() : any {
      return null;
    },
    view: function(ctrl : any, args : any) {
        return m("div", { "class": "queued-panel", config: foundationConfig }, [
            m('h5', "Your submission is queueing!"),
            m("div", {"class": "processJobIdContainer"},
                m('p', "Job ID:"),
                m('p', ' ' + args.job().jobID)),
        ]);
    }
};
