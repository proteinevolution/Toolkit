(<any>window).Job404Component = {

    controller: function(args: any) {
        return {};
    },
    view: function(ctrl: any, args: any) {
        return m("div", {"class": "column error404-container"}, [
            m("div", {class: "subtitle404"}, "Job not found."),
            m("div", {id: "404msa", config: window.call404})
        ]);
    }
};