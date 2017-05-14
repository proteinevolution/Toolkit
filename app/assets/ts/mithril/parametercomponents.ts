let renderParameter = function(content : any, moreClasses? : any) : any {
    return m("div", { "class": moreClasses ? "parameter " + moreClasses : "parameter" }, content);
};

let mapParam = function(param : any, ctrl : any) {
    let comp = formComponents[param.paramType.type];
    return m(comp, {
        param: param,
        value: ctrl.getParamValue(param.name)
    });
};



let selectBoxAccess = function(elem : any, isInit : boolean) {
    if (!isInit) {
        return $(elem).niceSelect();
    } else {
        return $(elem).niceSelect('update');
    }
};



let ParameterSlideComponent = {
    model: function(args : any) {

    },
    controller: function(args : any){

        this.value = args.value;
        this.config = function (el : any, isInit : boolean, ctx : any) {
            if (!isInit) {
                $(el).ionRangeSlider({
                    grid: true,
                    values: [
                        0.000000000000000000000000000000000000000000000000001,
                        0.00000000000000000000000000000000000000001,
                        0.000000000000000000000000000001,
                        0.00000000000000000001,
                        0.000000000000001,
                        0.0000000001,
                        0.00000001,
                        0.000001,
                        0.0001,
                        0.001,
                        0.01,
                        0.02,
                        0.05,
                        0.1],
                    grid_snap: true,
                    keyboard: true
                })
            }
        }.bind(this)

    },
    view: function (ctrl : any, args : any) {
        let paramAttrs : any = {
            type: "range",
            id: args.param.name,
            name: args.param.name,
            value: ctrl.value,
            config: ctrl.config
        };
        // Add minimum and maximum if present
        if(args.param.paramType["max"]) {
            paramAttrs["max"] = args.param.paramType["max"];
        }
        if(args.param.paramType["min"]) {
            paramAttrs["min"] = args.param.paramType["min"];
        }
        return renderParameter([
            m("label", args.value),
            m("input", paramAttrs)
        ])

    }

};



let ParameterRadioComponent = {
    view: function(ctrl : any, args : any) {
        return renderParameter([
            m("label", {
                "for": args.param.name
            }, args.param.label), args.param.paramType.options.map(function(entry : any) {
                return m("span", [
                    m("input", {
                        type: "radio",
                        name: args.param.name,
                        value: entry[0]
                    }), entry[1]
                ]);
            })
        ]);
    }
};

let ParameterSelectComponent = {
    //not needed so far but is working
    controller: function(args : any) {
        return {

        }
    },

    view: function(ctrl : any, args : any) {
        let paramAttrs : any = {
            name: args.param.name,
            "class": "wide",
            id: args.param.name,
            config: select2Config
        };
        if(args.param.name == "hhsuitedb" || args.param.name == "proteomes") {
            paramAttrs["multiple"] = "multiple";
            paramAttrs["class"] = "inputDBs";
        }else{
            paramAttrs["config"] = selectBoxAccess;
        }
        return renderParameter([
            m("label", {
                "for": args.param.name
            }, args.param.label),
            m("select", paramAttrs,
                args.param.paramType.options.map(function(entry : any) {
                    return m("option", (args.value.indexOf(entry[0]) > -1 ? {
                        value: entry[0],
                        selected: "selected"
                    } : {
                        value: entry[0]
                    }), entry[1])
                }))
        ]);
    }
};


let ParameterNumberComponent = {
    view: function(ctrl : any, args : any) {
        let paramAttrs : any = {
            type: "number",
            id: args.param.name,
            name: args.param.name,
            value: args.value
        };
        // Add minimum and maximum if present
        if(args.param.paramType["min"] != null) {
            paramAttrs["min"] = args.param.paramType["min"];
        }
        if(args.param.paramType["max"]) {
            paramAttrs["max"] = args.param.paramType["max"];
        }
        if(args.param.paramType["step"]) {
            paramAttrs["step"] = args.param.paramType["step"];
        }
        return renderParameter([
            m("label", {
                "for": args.param.name
            }, args.param.label), m("input", paramAttrs)
        ]);
    }
};


let ParameterTextComponent = {
    view: function(ctrl : any, args : any) {
        let paramAttrs = {
            type: "text",
            id: args.param.name,
            name: args.param.name,
            value: args.value,
            config: paramValidation
        };
        return renderParameter([
            m("label", {
                "for": args.param.name
            }, args.param.label), m("input", paramAttrs)
        ]);
    }
};

let ParameterBoolComponent = {
    view: function(ctrl : any, args : any) {
        return renderParameter([
            m("label", {
                "for": args.param.name
            }, args.label), m("input", {
                type: "checkbox",
                id: args.param.name,
                name: args.param.name,
                value: args.value
            })
        ]);
    }
};




let formComponents : any = {
    1: (<any>window).ParameterAlignmentComponent,
    2: ParameterNumberComponent,
    3: ParameterSelectComponent,
    4: ParameterBoolComponent,
    5: ParameterRadioComponent,
    6: ParameterSlideComponent,
    7: ParameterTextComponent
};