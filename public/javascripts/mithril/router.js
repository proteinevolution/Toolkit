var StaticRoute = {
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

var ErrorRouteComponent = {
    controller: function(args) {
        var errorID      = m.route.param("errorID") ? m.route.param("errorID") : args.errorID,
            errorMessage = "Page not Found";
        return { errorID : errorID, errorMessage : errorMessage}
    },
    view: function(ctrl, args) {
        return m("div", { "class" : "error-page" },[
            m("div", { "class" : "title" }, "There was an error loading this page."),
            m("div", [
                m("div", { "class" : "element" }, [
                    m("div", "Error code: "),
                    m("div", ctrl.errorID)
                ]),
                m("div", { "class" : "element" }, [
                    m("div", "Error message: "),
                    m("div", ctrl.errorMessage)
                ])
            ]),
            m("div", { "class" : "message element" },
                "Please check if you have entered the correct URL and that you have the right jobID"
            )
        ]);
    }
};

m.route.mode = 'hash';

// Define the mithril routes

var mountpoint = document.getElementById('main-content');


var routes = {
    '/':                 m(Index),
    '/tools/:toolname':  m(Toolkit, { isJob: false }),
    '/jobs/:jobID':      m(Toolkit, { isJob: true }),
    '/backend/:section': m(Backend),
    '/news':             m(News),
    '/jobmanager':       m(JobManager),
    '/:path...' :        m.component(ErrorRouteComponent, { errorID : 404 })
};

m.route(mountpoint, '/', routes);
// Add the job list to the off canvas element
m.mount(document.getElementById('off-canvas-joblist'), JobListComponent);