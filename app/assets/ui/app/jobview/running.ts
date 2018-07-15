class JobRunningComponent {

    static jobID : string = "undefined";
    static lines : any;
    public static RunningLog : Array<string> = [];

    public static updateLog(jobID : string, lines : any){
        const path = window.location.href;
        const url = path.split("/");
        if(url[url.length - 1] == jobID && $('#tab-Input').attr('aria-expanded') != "true" && $('#tab-Parameters').attr('aria-expanded') != "true") {
            this.lines = lines;
            this.RunningLog = lines.split("#");
            m.redraw.strategy("diff");
            m.redraw();
        }
    }

    // ensure that the running tab gets terminated
    public static terminate(jobID: string){
        if(jobID == JobRunningComponent.jobID){
            m.redraw(true);
        }
    }

    public static controller(args : any) : any {
        //m.request({ method: "GET", url: "files/"+args.job().jobID+"/process.log", contentType: "charset=utf-8",
        //    deserialize: function (data) {JobRunningComponent.RunningLog = data.split('#')}});
        JobRunningComponent.jobID = args.job().jobID;
        return {}
    }
    public static view(ctrl : any, args : any) : any {
        return m("div", { "class": "running-panel" , config: foundationConfig}, [
            m('h5', "Your submission is being processed!"),
            m("div", {"class": "processCiteContainer"},
                m("span", "If you use " + args.job().tool.toolnameLong + " within the Toolkit for your research, please cite: "),
                m("a", {href: "https://www.sciencedirect.com/science/article/pii/S0022283617305879", target: "_blank"},
                    m("p", m("em", "A Completely Reimplemented MPI Bioinformatics Toolkit with a New HHpred Server at its Core. J Mol Biol. 2017 Dec 16."))
                )
            ),
            m("div", {"class": "processJobIdContainer"},
                m('p', "Job ID: " + args.job().jobID)),
                    JobRunningComponent.RunningLog.map(function(logElem : any) : any{
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
}