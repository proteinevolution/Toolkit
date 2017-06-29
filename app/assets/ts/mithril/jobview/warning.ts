/// <reference path="helper.ts"/>
let JobLimitReachedComponent = {

    controller : function(args : any){
        return {

        }
    },
    view: function(ctrl : any, args : any) {
        return m("div", { "class": "warning-panel", config: foundationConfig }, [
            m('h6', { "class": "callout alert" }, "You submitted too many jobs, please try again later!")
        ]);
    }
};