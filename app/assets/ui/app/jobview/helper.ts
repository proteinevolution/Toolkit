const mapParam = function(param : any, ctrl : any) {

    let comp = formComponents[param.paramType.type];
    return m(comp, {
        param: param,
        value: ctrl.getParamValue(param.name)
    });
};


const enabled = function (elem : any, isInit : boolean) : any {
    if (!isInit) {
        return $(elem).removeAttr('disabled');
    }
};
