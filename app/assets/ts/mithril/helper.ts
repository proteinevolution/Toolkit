/// <reference path="validation.ts"/>

let closeShortcut = function() {
    return $(document).keydown(function(e) {
        if (e.keyCode === 27 && $("#tool-tabs").hasClass("fullscreen")) {
            $("#collapseMe").click();
        }
    });
};

let nonJsonErrors = function(xhr : XMLHttpRequest) {
    return xhr.status > 200 ? JSON.stringify(xhr.responseText) : xhr.responseText
};

let tabulated = function(element : any, isInit : boolean) : any {
    if (!isInit) {
        return $(element).tabs({
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

let select2Single = function(elem : any, isInit : boolean) : any {
    if(!isInit) {
        $(elem).select2({
            dropdownAutoWidth : true,
            width: '17.5em',
        });
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


let hideSidebar = function (elem : any, isInit : boolean) : any {
    if (!isInit) {

        $('#sidebar').hide();
        $('#main-content').removeClass();

    }
};


let showSidebar = function (elem : any, isInit : boolean) : any {
    if (!isInit) {

        $('#sidebar').show();
        $('#main-content').removeClass().addClass('large-10 small-12 columns padded-column');
        JobListComponent.selectJob();

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

let fadesIn = function(element : any, isInitialized : boolean, context : any) {

    let url = window.location.href;
    let parts = url.split("/");
    let isJob = parts[parts.length-2] == "jobs";
    //let isTool = parts[parts.length-2] == "tools";
    if (!isInitialized && !isJob) {
        element.style.opacity = 0;
        $(element).velocity({opacity: 1, top: "50%"}, 750);
    }
};

let tooltipsterConf = function(elem: any, isInit: boolean) {
    if(!isInit){
        $('.job-button div').tooltipster({
            theme: 'tooltipster-borderless',
            position: 'bottom',
            animation: 'fade',
            debug: false
        });
    }
};
