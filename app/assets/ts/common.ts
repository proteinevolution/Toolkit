function openNav() {
    document.getElementById("myNav").style.height = "100%";
}

function closeNav() {
    document.getElementById("myNav").style.height = "0%";
}

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

