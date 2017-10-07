class JobRunningComponent {

    static jobID : string = "undefined";
    static lines : any;
    public static RunningLog : Array<string> = [];

    public static updateLog(jobID : string, lines : any){

        const path = window.location.href;
        const url = path.split("/");

        if(url[url.length - 1] == jobID && $('#tab-Input').attr('aria-expanded') != "true" && $('#tab-Parameters').attr('aria-expanded') != "true") {
            //console.log(lines);
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
                m("span", "If you use the Toolkit for your research, please cite: "),
                m("a", {href: "https://academic.oup.com/nar/article-lookup/doi/10.1093/nar/gkw348", target: "_blank"},
                m("a", "Alva et al. NAR (2016).")),
                m("p"," ")
            ),
            m("div", {"class": "processJobIdContainer"},
                m('p', "Job ID:"),
                m('p',  {style: "margin-left: 5px"}, ' ' + args.job().jobID)),
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