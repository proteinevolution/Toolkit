/// <reference path="validation.ts"/>

const closeShortcut = function() {
    return $(document).keydown(function(e) {
        if (e.keyCode === 27 && $("#tool-tabs").hasClass("fullscreen")) {
            $("#collapseMe").click();
        }
    });
};

const nonJsonErrors = function(xhr : XMLHttpRequest) {
    return xhr.status > 200 ? JSON.stringify(xhr.responseText) : xhr.responseText
};

const tabulated = function(element : any, isInit : boolean) : any {
    if (!isInit || !$(element).hasClass("ui-tabs")) {
        if (isInit) {
            $(element).tabs("destroy"); // tabs should be initialized but arent -> refresh
        }
        return $(element).tabs({
            classes: {
                "ui-tabs": "ui-corner-top",
                "ui-tabs-nav": "ui-corner-top",
            },
            active: this.active,
            beforeLoad:
            function(event, ui){
                ui.panel.addClass("result-panel");
                let timerOverlay = setTimeout(function(){
                    $.LoadingOverlay("show")}, 300);
                    ui.jqXHR.then(function () {
                    clearTimeout(timerOverlay);$.LoadingOverlay("hide");
                });
            },
        });
    }
};

// clear input forms when clicking on a tool again

$('.toolsection').on('click', 'a', function() { /*m.route(this.href)*/ });

const select2Config = function(elem : any, isInit : boolean) : any {

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

const select2Single = function(elem : any, isInit : boolean) : any {
    if(!isInit) {
        $(elem).select2({
            dropdownAutoWidth : true,
            width: '17.5em',
        });
    }
};

const hideSubmitButtons = function (elem : any, isInit : boolean) : any {
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


const hideSidebar = function (elem : any, isInit : boolean) : any {
    if (!isInit) {
        $('#sidebar').hide();
        $('#main-content').removeClass();
    }
};


const showSidebar = function (elem : any, isInit : boolean) : any {
    if (!isInit) {
        $('#sidebar').show();
        $('#main-content').removeClass().addClass('large-10 small-12 columns padded-column');
        JobListComponent.selectJob();
    }
};

const submitModal = function(elem : any, isInit : boolean) : any {
    if (!isInit) {
        $(elem).foundation();
        return $(elem).bind('closed.zf.reveal', (function() {
            return $(".submitJob").prop("disabled", false);
        }));
    }
};


const alignment_format = function(elem : any, isInit : boolean) {

    if (!isInit) {
        $(elem).niceSelect();
    } else {
        $(elem).niceSelect('update');
    }
    if(this.length == 0) {
        $(".alignment_format").hide();
    }
};

const fadesIn = function(element : any, isInitialized : boolean, context : any) {

    let url = window.location.href;
    let parts = url.split("/");
    let isJob = parts[parts.length-2] == "jobs";
    //let isTool = parts[parts.length-2] == "tools";
    if (!isInitialized && !isJob) {
        element.style.opacity = 0;
        $(element).velocity({opacity: 1, top: "50%"}, 750);
    }
};

const tooltipConf = function(elem: any, isInit: boolean) {
    if(!isInit){
        $('.job-button div:not(.has-tip), #jobID:not(.has-tip), .helpicon:not(.has-tip), .fa-trash-alt:not(.has-tip)').attr("data-tooltip", "").foundation();
    }
};
