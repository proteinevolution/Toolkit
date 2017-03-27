import ElementConfig = Mithril.ElementConfig;


let clusterLoadConfig = function(elem : Element, isInit : boolean, ctx : any) : any {

    if(!isInit) {

        let n = 40,
            random = d3.randomNormal(0, .2),
            data = d3.range(n).map(random);
        let svg = d3.select("svg#loadGraph"),
            margin = {top: 20, right: 20, bottom: 20, left: 40},
            width = +svg.attr("width") - margin.left - margin.right,
            height = +svg.attr("height") - margin.top - margin.bottom,
            g = svg.append("g").attr("transform", "translate(" + margin.left + "," + margin.top + ")");
        let x = d3.scaleLinear()
            .domain([0, n - 1])
            .range([0, width]);
        let y = d3.scaleLinear()
            .domain([-1, 1])
            .range([height, 0]);
        let line = d3.line()
            .x(function(d, i) { return x(i); })
            .y(function(d, i) { return y(d); });
        g.append("defs").append("clipPath")
            .attr("id", "clip")
            .append("rect")
            .attr("width", width)
            .attr("height", height);
        g.append("g")
            .attr("class", "axis axis--x")
            .attr("transform", "translate(0," + y(0) + ")")
            .call(d3.axisBottom(x));
        g.append("g")
            .attr("class", "axis axis--y")
            .call(d3.axisLeft(y));
        g.append("g")
            .attr("clip-path", "url(#clip)")
            .append("path")
            .datum(data)
            .attr("class", "line")
            .transition()
            .duration(500)
            .ease(d3.easeLinear)
            .on("start", tick);

        return g;

        function tick() {
            // Push a new data point onto the back.
            data.push(random());
            // Redraw the line.
            d3.select(this)
                .attr("d", line)
                .attr("transform", null);
            // Slide it to the left.
            d3.active(this)
                .attr("transform", "translate(" + x(-1) + ",0)")
                .transition()
                .on("start", tick);
            // Pop the old data point off the front.
            data.shift();
        }
    }

};


let trafficBarConfig = function(lastJob : any) {
    return function (elem : any, isInit : Boolean) {
        if (lastJob != null && !isInit) {
            elem.setAttribute("data-disable-hover", "false");
            //elem.setAttribute("data-tooltip", "data-tooltip");
            elem.setAttribute("title", "Click to view last job: " + lastJob.jobID);
            console.log("Traffic bar sees status " + lastJob.state);
            if (lastJob.state === -1) {
                return console.log("Hide Trafficbar");
            } else if (lastJob.state === 2) {
                console.log("Traffic Bar goes to queued");
                return $(elem).css({
                    'background': '#c0b5bf',
                    'box-shadow': '0 1 6px #9192af'
                });
            } else if (lastJob.state === 5) {
                console.log("Traffic Bar goes to done");
                return $(elem).css({
                    'background': 'green',
                    'box-shadow': '0 1 6px #C3FFC3'
                });
            } else if (lastJob.state === 4) {
                console.log("Traffic Bar goes to error");
                return $(elem).css({
                    'background': '#ff0000',
                    'box-shadow': '0 1 6px #FFC5C5'
                });
            } else if (lastJob.state === 3) {
                console.log("Traffic Bar goes to running");
                return $(elem).css({
                    'background': '#ffff00',
                    'box-shadow': '0 1 6px #FFF666'
                });
            }
        }
    };
};

class LiveTable {
    static lastJob     : Object = null;
    static totalJobs   : Number = 0;
    static load        : Number = 0.5;
    static updateJobInfo () : void {
        m.request({method: "GET", url: "indexPageInfo"})
            .then(function(pageInfo) {
                console.log(pageInfo);
                LiveTable.lastJob   = pageInfo.lastJob;
                LiveTable.totalJobs = pageInfo.totalJobs;
            }).catch(function(error){console.log(error);});
    }
    static updateLoad (load : Number) : void {
        LiveTable.load = load;
    }
    static controller (args : any) : any {
        if (args) {
            LiveTable.lastJob   = args.lastJob   ? args.lastJob          : LiveTable.lastJob;
            LiveTable.totalJobs = args.totalJobs ? args.totalJobs        : LiveTable.totalJobs;
            LiveTable.load      = args.load      ? parseFloat(args.load) : LiveTable.load;

            if(args.totalJobs == null && args.lastJob == null) {
                LiveTable.updateJobInfo();
            }
        }
        //console.log("register load..");
        //(<any>window).sendMessage({type: "RegisterLoad"}); // <- TODO this wont work for some reason
        //console.log("...sent!");
        return {}
    }
    static view (ctrl : any, args : any) : any {
        let loadString  : string = (LiveTable.load * 100).toPrecision(4) + "%",
            colorString : string = LiveTable.load < 0.7 ? "green;" : LiveTable.load < 0.9 ? "yellow;" : "red;";

        return m('div', [
            //m('div', {"class" : "clusterLoad column large-4"}, ""),
            m('table', {class : "liveTable column large-12"}, [
                m('thead', [
                    m('tr', [
                        m('th', { id: 'currentLoadLabel'} , 'Cluster load'),
                        m('th', { id: 'lastJobLabel'}, 'Last own job'),
                        m('th', { id: 'lastJobsLabel'}, 'Total own jobs')
                    ])]
                )], [
                m('tbody',
                    [m('tr', [
                        m('td', { id: 'currentLoad', style: "color: " + colorString}, "" + loadString),
                        m('td', { id: 'lastJobName' },
                            LiveTable.lastJob != null ?
                                m('a', { href: "/#/jobs/" + LiveTable.lastJob.jobID}, LiveTable.lastJob.toolnameLong) :
                                m('b', "No Jobs")
                        ),
                        m('td',
                            m('a', { href: "/#/joblist/", style: "font-weight: bold;" }, "" + LiveTable.totalJobs)
                        )
                    ])]
                )
            ]),
            m("div", { id: "trafficbar",
                       class: "trafficbar",
                       config: trafficBarConfig(LiveTable.lastJob),
                       onclick: function () { m.route("/jobs/" + LiveTable.lastJob.jobID); }
            })
        ]);
    }
}