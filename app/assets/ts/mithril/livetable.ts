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



let JobTable = {
    controller: function(args : any) {

        let ctrl = this;
        ctrl.totalJobs = -1;
        ctrl.lastJob = {};
        m.request({method: "GET", url: "/api/jobs"})
            .then(function(jobs) {

                if(jobs.length > 0){
                    //ctrl.totalJobs = jobs.length;
                    ctrl.lastJob = jobs.slice(-1)[0];
                    JobListComponent.lastUpdatedJob = ctrl.lastJob;
                } else {
                    //ctrl.totalJobs = 0;
                    ctrl.lastJob = {
                        "jobID": -1,
                        "toolnameLong": ""
                    };
                }

            }).catch(function(e) {
            //ctrl.totalJobs = 0;
            ctrl.lastJob = {
                "jobID": -1,
                "toolnameLong": ""
            };

        });

        m.request({method: "GET", url: "count"})
            .then(function(count) {

                if(count > 0){
                    ctrl.totalJobs = count;

                } else {
                    ctrl.totalJobs = 0;

                }

            }).catch(function(e) {
            ctrl.totalJobs = 0;

        });

    },
    view: function(ctrl : any, args : any) {
        return m('div', {"class" : "row"}, [
            m('div', {"class" : "clusterLoad column large-4"}, ""),
            m('table.liveTable', {"class" : "column large-8"}, [
                m('thead', [
                    m('tr', [
                        m('th#currentLoad', 'Cluster load'),
                        m('th#lastJob', 'Last own job'),
                        m('th#lastJobs', 'Total own jobs')
                    ])]
                )], [
                m('tbody',
                    [m('tr', [
                        m('td#currentLoad', ''),
                        m('td#lastJobName', m('a[href="/#/jobs/' + ctrl.lastJob.jobID + '"]', ctrl.lastJob.toolnameLong)),
                        m('td', ctrl.totalJobs)
                    ])]
                )])
        ]);
    }
};

let LiveTable = {
    controller: function(args : any) {
        let ctrl = this;

    },
    view: function(ctrl : any, args : any) {
        return m('div', [
            m.component(JobTable, {})
        ]);
    }
};