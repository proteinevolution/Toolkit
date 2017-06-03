mapParam = function(param, ctrl) {

    let comp = formComponents[param.paramType.type];
    return m(comp, {
        param: param,
        value: ctrl.getParamValue(param.name)
    });
};