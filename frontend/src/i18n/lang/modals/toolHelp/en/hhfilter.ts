/* tslint:disable:max-line-length */

export default {
    overview: `Extract a representative set of sequences from an alignment, based of pairwise sequence identity and
                minimum coverage.`,
    parameters: [
        {
            title: 'Input',
            content: `You have the option to enter a multiple sequence alignment (MSA) in FASTA, CLUSTAL, or A3M format.
                    The number of sequences is currently limited to 10,000.<br/><br/>
                @:toolHelpModals.common.msa
                @:toolHelpModals.common.a3m`,
        },
        {
            title: 'Max. sequence identity (%)',
            content: `Filter by maximum pairwise sequence identity, given in percent.`,
        },
        {
            title: 'Min. seq. identity of MSA hits with query (%)',
            content: `Filter by minimum sequence identity with query, given in percent.`,
        },
        {
            title: 'Minimal coverage with query (%)',
            content: `Filter by minimum coverage with query, given in percent.`,
        },
        {
            title: 'No. of most dissimilar sequences to extract',
            content: `Filter most diverse set of sequences, keeping at least this many sequences in each block of >50 columns.`,
        },
    ],
    references: `<p>Remmert M., Biegert A., Hauser A., Söding J. (2011)
        <b>HHblits: Lightning-fast iterative protein sequence searching by HMM-HMM alignment. </b>
        Nat Methods. 9(2):173-5. doi: 10.1038/nmeth.1818.
        <a href = https://www.ncbi.nlm.nih.gov/pubmed/22198341 target="_blank" rel="noopener">PMID: 22198341</a></p>`,
};
