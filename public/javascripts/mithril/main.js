var StaticRoute;

StaticRoute = {
    controller: function() {
        return {
            "static": m.route.param('static')
        };
    },
    view: function(controller) {
        return $.ajax({
            type: "GET",
            url: "/static/get/" + controller["static"]
        }).done(function(data) {
            if (['sitemap', 'reformat', 'alnvizfrontend', 'patSearch'].indexOf(controller['static']) >= 0) {
                $('#content').empty().prepend(data);
            } else {
                $('body').empty().prepend(data);
            }
            $(document).foundation();
            return $("html, body").animate({
                scrollTop: 0
            }, "fast");
        });
    }
};

m.route.mode = 'hash';


m.route(document.getElementById('main-content'), '/', {
    '/': Index,
    '/tools/:toolname': m(Toolkit, {
        isJob: false
    }),
    '/jobs/:jobID': m(Toolkit, {
        isJob: true
    }),
    '/backend/:section': m(Backend),
    '/news': m(News)
});

window.onfocus = function() {
    return titlenotifier.reset();
};

window.onclick = function() {
    return titlenotifier.reset();
};


