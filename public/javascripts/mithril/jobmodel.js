window.JobModel = {
    paramValues: {},
    defaultValues: {
        "num_iter": 1,
        "evalue": 10,
        "inclusion_ethresh": "0.001",
        "min_cov": "20",
        "min_seqid_query": "0",
        "msageneration": "hhblits",
        "gap_open": 11,
        "gap_ext": 1,
        "desc": 500,
        "alignmode": "local",
        "maxrounds": "1",
        "matrix": "BLOSUM62",
        "max_lines": 100,
        "pmin": 20,
        "aliwidth": 80,
        "max_seqid": 90,
        "min_seqid": "0.8",
        "min_aln_cov": "0.8",
        "min_query_cov": "0",
        "num_seqs_extract": 100,
        "protblastprogram": "psiblast",
        "seq_count": 1000,
        "codon_table": "1",
        "genetic_code": "1",
        "grammar": "Prosite_grammar"
    },
    pushMessage: function(msg) {
        return messages().push(msg);
    },
    update: function(args, value) {
        if (args.isJob) {
            return m.request({
                method: 'GET',
                url: "/api/job/" + value
            }).then(function(data) {
                JobModel.paramValues = data.paramValues;
                Job.owner = data.ownerName;
                return {
                    tool: data.toolitem,
                    isJob: true,
                    jobID: data.jobID,
                    ownerName: data.ownerName,
                    createdOn: data.createdOn,
                    jobstate: data.state,
                    views: data.views
                };
            });
        } else {
            return m.request({
                method: 'GET',
                url: "/api/tools/" + value
            }).then(function(toolitem) {
                JobModel.paramValues = {};
                return {
                    tool: toolitem,
                    isJob: false,
                    jobID: ""
                };
            });
        }
    },
    getParamValue: function(param) {
        // Update the value with the one from the local storage
        var resultcookie = localStorage.getItem("resultcookie");
        if (resultcookie) {
            JobModel.paramValues["alignment"] = resultcookie;
            localStorage.removeItem("resultcookie");
        }
        var val = JobModel.paramValues[param];
        var defVal = JobModel.defaultValues[param];
        if (val) {
            return val;
        } else if (defVal) {
            return defVal;
        } else {
            return "";
        }
    }
};