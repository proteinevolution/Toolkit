const JobQueuedComponent = {
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
                m("a", {href: "https://www.sciencedirect.com/science/article/pii/S0022283617305879", target: "_blank"},
                    m("p", m("b", "A Completely Reimplemented MPI Bioinformatics Toolkit with a New HHpred Server at its Core. J Mol Biol. 2017 Dec 16."))
                )
            ),
            m("div", {"class": "processJobIdContainer"},
                m('p', "Job ID:"),
                m('p', ' ' + args.job().jobID)),
        ]);
    }
};
