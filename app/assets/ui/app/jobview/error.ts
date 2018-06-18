let ErrorLog : Array<string> = [];

const JobErrorComponent = {
    updateLog: function(){
        m.redraw(true);
    },
    controller: function (args: any) {
        m.request({ method: "GET", url: "results/files/"+args.job().jobID+"/process.log", contentType: "charset=utf-8",
            deserialize: function (data) {ErrorLog = data.split('#')}});

        return {}
    },
    view: function(ctrl : any, args : any) {
        return m("div", { "class": "running-panel", config: foundationConfig }, [
            m('h5', "Your job has reached error state!"),
            m("div", {"class": "processJobIdContainer"},
                m('p', "Job ID:"),
                m('p', '' + args.job().jobID)),
            //m("h6", "Job has reached Error state"),
            //m("br"),
            //m("br"),
            ErrorLog.map(function(logElem : any) : any{
                if(logElem == "")
                    return;
                logElem = logElem.split("\n");
                // delete empty entries from array
                logElem = logElem.filter(Boolean);
                let len = logElem.length-1;
                console.log(len);
                if(len > 0){
                    if(len > 1){
                        return [m("div", {"class": "logElem"},
                            m("i", {"class": "icon-check_circle logElemDone"}),
                            m("div", {"class": "logElemText"}, logElem[0])),
                            m("div", {"class": "logElem"},
                                m("i", {"class": "icon-cancel_circle logElemError"}),
                                m("div", {"class": "logElemText"}, "Error."))
                        ]
                    }
                    else if(logElem[1] == "done"){
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