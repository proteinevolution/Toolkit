let closeShortcut = function() {
    return $(document).keydown(function(e) {
        if (e.keyCode === 27 && $("#tool-tabs").hasClass("fullscreen")) {
            $("#collapseMe").click();
        }
    });
};


let tabulated = function(element : any, isInit : boolean) : any {
    if (!isInit) { return $(element).tabs({ active: this.active, beforeLoad: function(event, ui){ ui.panel.addClass("result-panel")}});}
};