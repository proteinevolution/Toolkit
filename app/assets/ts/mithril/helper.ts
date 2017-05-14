/// <reference path="validation.ts"/>

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


let helpModalAccess = function(elem : any, isInit: boolean) {
    if (!isInit) {
        return elem.setAttribute("data-open", "help-" + (this.job().tool.toolname));
    }
};



let hideSubmitButtons = function (elem : any, isInit : boolean) : any {
    if (!isInit) {
        return $(elem).on("click", function() {
            if($(this).attr('href') == "#tabpanel-Input" || $(this).attr('href') == "#tabpanel-Parameters") {
                $('.submitbuttons').show();
                setTimeout(function(){
                    validationProcess($('#alignment'),$("#toolnameAccess").val());
                }, 100);
            } else {
                $('.submitbuttons').hide();
            }
        });

    }
};


let submitModal = function(elem : any, isInit : boolean) : any {
    if (!isInit) {
        $(elem).foundation();
        return $(elem).bind('closed.zf.reveal', (function() {
            return $(".submitJob").prop("disabled", false);
        }));
    }
};


let alignment_format = function(elem : any, isInit : boolean) {

    if (!isInit) {
        $(elem).niceSelect();
    } else {
        $(elem).niceSelect('update');
    }
    if(this.length == 0) {
        $(".alignment_format").hide();
    }
};

