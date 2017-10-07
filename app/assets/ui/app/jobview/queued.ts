let JobQueuedComponent = {
    updateLog: function(){
        m.redraw(true);
    },
    controller : function() : any {
      return null;
    },
    view: function(ctrl : any, args : any) {
        return m("div", { "class": "queued-panel", config: foundationConfig }, [
            m('h5', "Your submission is queued!"),
            m("div", {"class": "processCiteContainer"},
                m("span", "If you use the Toolkit for your research, please cite: "),
                m("a", {href: "https://academic.oup.com/nar/article-lookup/doi/10.1093/nar/gkw348", target: "_blank"},
                    m("a", "Alva et al. NAR (2016).")),
                m("p"," ")
            ),
            m("div", {"class": "processJobIdContainer"},
                m('p', "Job ID:"),
                m('p', ' ' + args.job().jobID)),
        ]);
    }
};
