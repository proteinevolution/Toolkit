/// <reference path="validation.ts"/>
interface Window { JobModel: any; }
window.JobModel = {
    paramValues: {},
    defaultValues: {
        "evalue": "1e-3",
        "inclusion_ethresh": "0.001",
        "min_cov": "20",
        "min_seqid_query": "0",
        "gap_open": 11,
        "mafft_gap_open": "1.53",
        "desc": "250",
        "alignmode": "local",
        "maxrounds": "1",
        "matrix": "BLOSUM62",
        "use_nr70": "1",
        "max_lines": 100,
        "pmin": 20,
        "max_seqid": 90,
        "min_seqid": "0.8",
        "min_aln_cov": "0.8",
        "min_query_cov": "0",
        "num_seqs_extract": 100,
        "protblastprogram": "psiblast",
        "seq_count": 1000,
        "codon_table": "1",
        "genetic_code": "1",
        "msa_gen_max_iter": "3",
        "msa_gen_max_iter_hhrepid": "1",
        "quick_iters": "3",
        "grammar": "Prosite_grammar",
        "macmode": "off",
        "macthreshold":"0.3",
        "max_hhblits_iter":"1",
        "score_ss":"2",
        "ss_scoring":"2",
        "rep_pval_threshold":"1e-2",
        "self_aln_pval_threshold":"1e-1",
        "merge_iters":"3",
        "domain_bound_detection":"1",
        "gap_ext_kaln":"0.85",
        "gap_term":"0.45",
        "bonusscore":"0",
        "offset":"0.0",
        "output_order":"input",
        "clustering_pval_threshold":"1",
        "eval_tpr":"1e-2",
        "msa_gen_method":"hhblits",
        "hhsuitedb":"mmcif70/pdb70",
        "hhpred_incl_eval":"1e-3",
        "hhblits_incl_eval":"1e-3",
        "blast_incl_eval":"1e-3",
        "pcoils_matrix": "2",
        "pcoils_weighting":"1",
        "pcoils_input_mode":"0",
        "repper_input_mode":"0",
        "no_replicates":"0",
        "matrix_phyml":"LG",
        "eff_crick_angle":"1",
        "samcc_periodicity":"7",
        "seqcount":"500",
        "invoke_psipred":"30",
        "clans_eval":"1e-4",
        "target_psi_db":"nr90",
        "transition_probability":"1",
        "matrix_marcoil":"mtk",
        "window_size":"100",
        "periodicity_min":"2",
        "periodicity_max":"100",
        "ftwin_threshold":"6",
        "repwin_threshold":"2",
        "in_format":"a3m",
        "out_format":"fas",
        "patsearchdb":"nr50",
        "standarddb":"nr50",
        "hmmerdb":"nr50",
        "clustering_mode":"cluster"
    },
    update: function(args : any, value : string) : any {
        if (args.isJob) {
            return m.request({
                method: 'GET',
                url: "/api/job/" + value
            }).then(function(data) {
                window.JobModel.paramValues = data.paramValues;
                if(window.JobModel.paramValues.proteomes && !window.JobModel.paramValues.hhsuitedb){
                    window.JobModel.paramValues["hhsuitedb"]= "";
                }
                return {
                    tool: data.toolitem,
                    isJob: true,
                    jobID: data.jobID,
                    dateCreated: data.dateCreated,
                    jobstate: data.state,
                    views: data.views,
                    successful : true
                };
            });
        } else {
            return m.request({
                method: 'GET',
                url: "/api/tools/" + value
            }).then(function(toolitem) {
                window.JobModel.paramValues = {};
                return {
                    tool: toolitem,
                    isJob: false,
                    jobID: "",
                    successful : true
                };
            });
        }
    },
    getParamValue: function(param : any) : any {
        const val = window.JobModel.paramValues[param];
        const defVal = window.JobModel.defaultValues[param];

        if (val || val === "") {
            return val;
        } else if (defVal) {
            return defVal;
        } else {
            return "";
        }
    },
    setParamValue: function(param : string, value : any) : void {
        window.JobModel.paramValues[param] = value;
        m.redraw(true);
    }
};
