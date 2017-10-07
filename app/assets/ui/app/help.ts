const helpModalReveal : ElementConfig = function(elem : any, isInit : boolean) : any {
    if (!isInit) {
        elem.setAttribute("data-reveal", "data-reveal");
        return $(elem).foundation();
    }
};

(<any>window).HelpModalComponent = {

    view: function(ctrl : any, args : any) {
        return m("div", {
            id: "help-" + args.toolname,
            "class": "reveal helpModal",
            config: helpModalReveal
        });
    }
};