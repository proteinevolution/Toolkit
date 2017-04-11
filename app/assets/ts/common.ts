
let jqueryUITabsInit = function(elem : Element, isInit : boolean) : void {
    if (!isInit) {
        $("#" + elem.id).tabs().addClass( "ui-tabs-vertical");
    }
};
let foundationInit = function(elem : Element, isInit : boolean) : void {
    if (!isInit) {
        $("#" + elem.id).foundation();
    }
};

let openNav = function(view : string = "") : void {
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

let closeNav = function() : void {
    document.getElementById("myNav").style.height = "0%";
};

let color_regex = /(?:[WYF]+|[LIVM]+|[AST]+|[KR]+|[DE]+|[QN]+|H+|C+|P+|G+)/g;


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

let aa_color_font : Map<string, string> = new Map<string, string>();
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

let aa_color_background : Map<string, string> = new Map<string, string>();
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