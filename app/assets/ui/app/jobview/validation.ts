const JobValidationComponent = function(suffix: string = "") {
    return {
        controller: function(): any {
            return null;
        },
        view: function() {
            return m("div#validOrNot" + suffix, {"class": "callout validOrNot", style: "display: none"}, "");
        }
    };
};
