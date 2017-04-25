import Routes = Mithril.Routes;

let StaticRoute : any = {
    controller: function() {
        return {
            "static": m.route.param('static')
        };
    },
    view: function(controller : any) {
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

// Define the mithril routes

let mountpoint: HTMLElement = document.getElementById('main-content');


let routes : Routes = {
    '/':                 Index,
    '/tools/:toolname':  m(Toolkit, { isJob: false }),
    '/jobs/:jobID':      m(Toolkit, { isJob: true }),
    '/backend/:section': m(Backend),
    '/news':             m(News),
    '/jobmanager':       m(JobManager)
};

m.route(mountpoint, '/', routes);
// Add the job list to the off canvas element
m.mount(document.getElementById('off-canvas-joblist'), JobListComponent);