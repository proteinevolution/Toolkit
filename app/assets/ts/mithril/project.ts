
let ProjectComponent = {

    controller(args: any) {



    },

    view(ctrl: any, args: any) {

        return m("div", {"class" : "tiny reveal", "data-reveal" : "data-reveal", "data-animation-in":   "fade-in",
            "data-overlay":        "false",
            "transition-duration": "fast", id: "projectReveal"}, m("div", {id : "findProject"}, "Add to a project"),
                m("input", {id : "projectInput"}));
    }

};