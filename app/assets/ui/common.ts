/// <reference path="websocket.ts"/>
const jqueryUITabsInit = function(elem : Element, isInit : boolean) : void {
    if (!isInit) {
        $("#" + elem.id).tabs().addClass( "ui-tabs-vertical");
    }
};
const foundationInit = function(elem : Element, isInit : boolean) : void {
    if (!isInit) {
        $("#" + elem.id).foundation();
    }
};

const openNav = function(view : string = "") : void {
    let lt = $("#login-tabs");
    let pt = $("#profile-tabs");
    switch(view) {
        case "": break;
        case "signin":
            lt.tabs('option', 'active', 0);
            break;
        case "signup":
            lt.tabs('option', 'active', 1);
            break;
        case "forgot":
            lt.tabs('option', 'active', 2);
            break;
        case "user":
            pt.tabs('option', 'active', 0);
            break;
        case "profile":
            pt.tabs('option', 'active', 1);
            break;
        case "password":
            pt.tabs('option', 'active', 2);
            break;
        case "passwordReset":

            break;
        default:
    }
    document.getElementById("myNav").style.height = "100%";
};

const closeNav = function() : void {
    document.getElementById("myNav").style.height = "0%";
};

const color_regex = /(?:[WYF]+|[LIVM]+|[AST]+|[KR]+|[DE]+|[QN]+|H+|C+|P+|G+)/g;


function color_regex_replacer(match : string) {
    return ["<span class=\"aa_", match.charAt(0), "\">", match, "</span>"].join('');
}

// scroll arrow
$(window).scroll(function() {
    if ($(window).scrollTop() > 200) {
        $('#scrollTop').show();
    } else {
        $('#scrollTop').hide();
    }
});


$('#scrollTop').click(function() {
    $("html, body").animate({ scrollTop: 0 }, 200);
    return false;
});

const aa_color_font : Map<string, string> = new Map<string, string>();
    aa_color_font.set("aa_W", "#808080");
    aa_color_font.set("aa_Y", "#808080");
    aa_color_font.set("aa_F", "#808080");
    aa_color_font.set("aa_L", "#00ff00");
    aa_color_font.set("aa_I", "#00ff00");
    aa_color_font.set("aa_V", "#00ff00");
    aa_color_font.set("aa_M", "#00ff00");
    aa_color_font.set("aa_A", "#404040");
    aa_color_font.set("aa_S", "#404040");
    aa_color_font.set("aa_T", "#404040");
    aa_color_font.set("aa_K", "red");
    aa_color_font.set("aa_R", "red");
    aa_color_font.set("aa_D", "blue");
    aa_color_font.set("aa_E", "blue");
    aa_color_font.set("aa_Q", "#d000a0");
    aa_color_font.set("aa_N", "#d000a0");
    aa_color_font.set("aa_H", "#E06000");
    aa_color_font.set("aa_C", "#A08000");
    aa_color_font.set("aa_P", "#191919");
    aa_color_font.set("aa_G", "#404040");

const aa_color_background : Map<string, string> = new Map<string, string>();
    aa_color_background.set("aa_W", "#00c000");
    aa_color_background.set("aa_Y", "#00c000");
    aa_color_background.set("aa_F", "#00c000");
    aa_color_background.set("aa_L", "#02ff02");
    aa_color_background.set("aa_I", "#02ff02");
    aa_color_background.set("aa_V", "#02ff02");
    aa_color_background.set("aa_M", "#02ff02");
    aa_color_background.set("aa_A", "#404040");
    aa_color_background.set("aa_S", "#404040");
    aa_color_background.set("aa_T", "#404040");
    aa_color_background.set("aa_K", "#ff0000");
    aa_color_background.set("aa_R", "#ff0000");
    aa_color_background.set("aa_D", "#6080ff");
    aa_color_background.set("aa_E", "#6080ff");
    aa_color_background.set("aa_Q", "#e080ff");
    aa_color_background.set("aa_N", "#e080ff");
    aa_color_background.set("aa_H", "#ff8000");
    aa_color_background.set("aa_C", "#ffff00;");
    aa_color_background.set("aa_P", "#a0a0a0");
    aa_color_background.set("aa_G", "#404040");

    // custom css for cluo

    $("#cluo").html("Clustal<span style='letter-spacing: 2px; padding-left: 2px;'>\u03A9</span>");

           window.addEventListener("load", function(){
               (<any>window).cookieconsent.initialise({
                        "palette": {
                            "popup": {
                                "background": "#28786f"
                            },
                            "button": {
                                "background": "#e2e2e2"
                            }
                        },
                        "content": {
                            "link": '<a data-open="privacyPolicyModal" style="color:#15423b; text-decoration: underline;">Learn More</a>',
                            "message": "We use cookies to ensure you get the best experience on our website. By using our services you agree to our privacy policies.",
                        },
                    })});

    // toggle sidebar so that the joblist is only present when there is no static content

    const toggleSidebar = function(){
        let path = window.location.href;
        let url = path.split("/");
        let $mc = $('#main-content');
        let $sb = $('#sidebar');

        if(url[url.length - 2] == 'tools' || url[url.length - 2] == 'jobs' || url[url.length - 1] == 'jobmanager'){

            // all logic to make the sidebar visible is located in the configs in mithril/helpers.ts

        }
        else {
            // this code is used to smooth out the transition TO the index page
            $mc.removeClass();
            $sb.hide();
        }
    };

    window.addEventListener("hashchange", toggleSidebar, false);

    const reloadJobList = function() {
        setTimeout(function(){ jobList.redraw(true); }, 200);
    };

    //close off canvas menu on click
    $('.vertical ul').on('click', function () {
        $('.off-canvas').foundation('close');
    });

