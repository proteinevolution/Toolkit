/* tslint:disable:max-line-length */

export default {
    overview: `A clustering is performed based on all against all BLAST+ similarities and the detected groups are returned.`,
    parameters: [
        {
            title: 'Input',
            content: `@:toolHelpModals.common.multiseq`,
        },
        {
            title: 'Scoring Matrix',
            content: `Specify the scoring matrix that is used for PSI-BLAST.`,
        },
        {
            title: 'Extract BLAST HSP\'s up to E-values of',
            content: `Specifies the cut-off value for BLAST E-values. HSPs with E-value larger than this are not being
                    extracted.`,
        },
    ],
    references: `<p>Frickey T., Lupas AN. (2004) <b>CLANS: a Java application for visualizing protein families based on
        pairwise similarity. </b>Bioinformatics 20(18):3702-3704.
        <a href = https://www.ncbi.nlm.nih.gov/pubmed/15284097 target="_blank" rel="noopener">PMID: 15284097</a></p>`,
};
