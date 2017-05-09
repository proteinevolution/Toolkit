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

// clear input forms when clicking on a tool again

$('.toolsec').on('click', 'a', function() { /*m.route(this.href)*/ });



let select2Config = function(elem : any, isInit : boolean) : any {

    if(!isInit) {

        $(elem).select2({
            dropdownAutoWidth : true,
            width: 'auto'
        });

        return  $(elem).on("change", function () {

            if ($('#hhsuitedb').val().length == 0 && $('#proteomes').val().length == 0 ) {
                $('#hhsuitedb').prop("required", true);
                $('#proteomes').prop("required", true);
            } else {
                $('#hhsuitedb').prop("required", false);
                $('#proteomes').prop("required", false);
            }

        });

    }

};