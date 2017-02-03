var jobNoteArea, tooltipSearch;

tooltipSearch = function(elem, isInit) {
    if (!isInit) {
        elem.setAttribute("data-tooltip", "data-tooltip");
        elem.setAttribute("aria-haspopup", "true");
        elem.setAttribute("data-disable-hover", "false");
        return elem.setAttribute("title", "Search for job");
    }
};

jobNoteArea = function(elem, isInit) {
    if (!isInit) {
        $.ajax({
            url: '/api/jobs/getnotes/' + $(elem).attr('id').substring(7),
            type: 'get',
            success: function(data) {
                $(elem).html(data);
            }
        });
        return $(elem).keyup(function(e) {
            var contentString;
            contentString = $(this).val();
            $.post(jsRoutes.controllers.Jobs.annotation($(this).attr('id').substring(7), contentString), function(response) {
                console.log('Response: ' + response);
            });
        });
    }
};

window.JobListComponent = {
    model: function() {},
    controller: function() {},
    view: function(ctrl, args) {
        return m("div", {
            id: "joblist"
        }, [
            m("form", {
                id: "jobsearchform"
            }, m("div", [
                m("input", {
                    type: "text",
                    placeholder: "Search by JobID",
                    id: "jobsearch"
                }), m("span", {
                    "class": "bar"
                })
            ])), m("div", {
                "class": "job-button"
            }, [
                m("div", {
                    "class": "idsort textcenter",
                    onclick: Job.sortToolname
                }, "ID"), m("div", {
                    "class": "toolsort textcenter",
                    onclick: Job.sortJobID
                }, "Tool")
            ]), m("div", {
                id: "joblistbottom"
            }, args.jobs().map(function(job, idx) {
                return m("div", {
                    "class": ("job " + a[job.state]).concat(job.jobID === args.selected ? " selected" : "")
                }, [
                    m("div", {
                        "class": "jobid"
                    }, m('a[href="/#/jobs/' + job.jobID + '"]', job.jobID)), m("span", {
                        "class": "toolname"
                    }, job.toolname.substr(0, 4).toUpperCase()), m("a", {
                        "class": "boxclose",
                        onclick: args.clear.bind(ctrl, idx)
                    })
                ]);
            })), document.cookie.split("&username=")[1] === args.owner ? [
                    m("div", {
                        "class": "notesheader"
                    }, "Notes")
                ] : [], document.cookie.split("&username=")[1] === args.owner ? [
                    m("div", {
                        "class": "jobnotes"
                    }, [
                        m("textarea", {
                            id: "notepad" + args.selected,
                            placeholder: "Type private notes here",
                            spellcheck: false,
                            config: jobNoteArea
                        })
                    ])
                ] : []
        ]);
    }
};