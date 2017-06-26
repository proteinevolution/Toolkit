let RunningLog : Array<string> = [];

let JobRunningComponent = {
    updateLog: function(){
        m.redraw(true);
    },

    controller: function (args : any) {
        m.request({ method: "GET", url: "files/"+args.job().jobID+"/process.log", contentType: "charset=utf-8",
            deserialize: function (data) {RunningLog = data.split('#')}});

        return {}
    },
    view: function(ctrl : any, args : any) : any {
        return m("div", { "class": "running-panel" , config: foundationConfig}, [
            m('h5', "Your submission is being processed!"),
            m("div", {"class": "processCiteContainer"},
                m("span", "If you use our Toolkit for your research, please cite: "),
                m("a", {href: "https://academic.oup.com/nar/article-lookup/doi/10.1093/nar/gkw348", target: "_blank"},
                m("a", "Alva et al. NAR (2016).")),
                m("p"," ")
            ),
            m("div", {"class": "processJobIdContainer"},
                m('p', "Job ID:"),
                m('p',  {style: "margin-left: 5px"}, ' ' + args.job().jobID)),
           RunningLog.map(function(logElem : any) : any{
                if(logElem == "")
                    return;
                logElem = logElem.split("\n");
                if(logElem.length > 1){
                    if(logElem[1] == "done"){
                        return m("div", {"class": "logElem"},
                            m("i", {"class": "icon-check_circle logElemDone"}),
                            m("div", {"class": "logElemText"}, logElem[0]))
                    }
                    else if(logElem[1] == "error"){
                        return m("div", {"class": "logElem"},
                            m("i", {"class": "icon-cancel_circle logElemError"}),
                            m("div", {"class": "logElemText"}, logElem[0]))
                    }
                }
                return m("div", {"class": "logElem"},
                    m("div", {"class": "logElemRunning"}),
                    m("div", {"class": "logElemText"}, logElem[0]))
            })
        ]);
    }
};