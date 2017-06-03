renderParameter = function(content, moreClasses) {
    return m("div", { "class": moreClasses ? "parameter " + moreClasses : "parameter" }, content);
};

mapParam = function(param, ctrl) {

    let comp = formComponents[param.paramType.type];
    return m(comp, {
        param: param,
        value: ctrl.getParamValue(param.name)
    });
};