/// <reference path="jobview/input.ts"/>

const renderParameter = function(content : any, moreClasses? : any) : any {
    return m("div", { "class": moreClasses ? "parameter " + moreClasses : "parameter" }, content);
};

const selectBoxAccess = function(elem : any, isInit : boolean) {
    if (!isInit) {

        $("#alignmode").on('change', function(){

            if ($("#alignmode").val() === 'glob'){
                $("#macmode").prop("value", "-realign");
                $('#macmode option[value="-norealign"]').attr("disabled", true);
                $("#macmode").niceSelect('update');
            }
            else {
                $('#macmode option[value="-norealign"]').attr("disabled", false);
                $("#macmode").niceSelect('update');
            }
        });
        return $(elem).niceSelect();
    } else {
        return $(elem).niceSelect('update');
    }
};

const ParameterSlideComponent = {
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



const ParameterRadioComponent = {
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

const ParameterSelectComponent = {

    view: function(ctrl : any, args : any) {
        let paramAttrs : any = {
            name: args.param.name,
            "class": "wide",
            id: args.param.name,
            config: select2Config
        };


        let multiselect : boolean = args.param.name === "hhsuitedb" || args.param.name === "proteomes";

        if(args.param.name === "patsearchdb"){
            paramAttrs["config"] = select2Single;
            paramAttrs["class"] = "wide inputDBs";
        }
        else if (multiselect) {

            paramAttrs["multiple"] = "multiple";
            paramAttrs["class"] = "inputDBs";
            args.value = args.value.split(/\s+/);

        } else {
            paramAttrs["config"] = selectBoxAccess;
        }

        return renderParameter([
            m("label", {
                "for": args.param.name
            }, args.param.label),
            m("select", paramAttrs,
                args.param.paramType.options.map(function(entry : any) {

                    return m("option", ( (multiselect ? args.value.indexOf(entry[0]) > -1  : args.value === entry[0]) ? {
                        value: entry[0],
                        selected: "selected"
                    } : {
                        value: entry[0]
                    }), entry[1])
                }))
        ]);
    }
};


const ParameterNumberComponent = {
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

const ParameterTextComponent = {
    view: function(ctrl : any, args : any) {
        let paramAttrs = {
            type: "text",
            id: args.param.name,
            name: args.param.name,
            value: args.value,
            placeholder: args.param.paramType.placeholder,
            config: paramValidation
        };
        return renderParameter([
            m("label", {
                "for": args.param.name
            }, args.param.label), m("input", paramAttrs)
        ]);
    }
};

const ParameterBoolComponent = {
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



const ParameterModellerKeyComponent = {
    keyStored: false,
    value: "",
    validate: function(val: string, checkLen?: boolean){
        ParameterModellerKeyComponent.value = val;
        if(checkLen || val.length >= 11) {
            m.request({method: "POST", url: "/validate/modeller?input="+val}).then(function (response) {
                ParameterModellerKeyComponent.keyStored = response.isValid;
                if(ParameterModellerKeyComponent.keyStored){
                    validationProcess($('#alignment'),"modeller");
                }
            });
        }
    },
    controller: function(){
        ParameterModellerKeyComponent.validate("", true);
        return {}
    },
    view: function(ctrl : any, args : any) {
        let paramAttrs = {
            type: "text",
            id: args.param.name,
            value: ParameterModellerKeyComponent.value,
            onkeyup: m.withAttr("value", ParameterModellerKeyComponent.validate),
            config: paramValidation,
            "class": "modellerKey invalid"

        };

        return renderParameter([
            ParameterModellerKeyComponent.keyStored ?  m("text",{"class": "modellerKey valid"},"MODELLER-key is stored in your profile.") : [
                    m("label", {
                        "for": args.param.name,
                    }, args.param.label), m("input", paramAttrs)]
        ]);
    }
};


const formComponents : any = {
    1: ParameterAlignmentComponent,
    2: ParameterNumberComponent,
    3: ParameterSelectComponent,
    4: ParameterBoolComponent,
    5: ParameterRadioComponent,
    6: ParameterSlideComponent,
    7: ParameterTextComponent,
    8: ParameterModellerKeyComponent

};