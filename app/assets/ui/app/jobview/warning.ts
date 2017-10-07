/// <reference path="helper.ts"/>
let JobLimitReachedComponent = {

    controller : function(args : any){
        return {

        }
    },
    view: function(ctrl : any, args : any) {
        return m("div", { "class": "warning-panel", config: foundationConfig }, [
            m('h6', { "class": "callout alert" }, "You have submitted too many jobs recently. Please try again later!")
        ]);
    }
};