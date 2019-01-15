/* tslint:disable:max-line-length */

export default {
    overview: `T-Coffee (Tree-based Consistency Objective Function for alignment Evaluation) is one of
        the most accurate multiple sequence alignment methods for protein and nucleotide sequences.
        <p>[T-Coffee 11.0; <a href = "http://www.tcoffee.org/" target="_blank" rel="noopener">http://www.tcoffee.org/</a>]</p>`,
    parameters: [
        {
            title: 'Input',
            content: `@:toolHelpModals.common.multiseq`,
        },
        {
            title: 'Output the alignment in:',
            content: `Specify the order in which the aligned sequences appear within the resulting multiple sequence alignment`,
        },
    ],
    references: `<p>Magis C., Taly JF., Bussotti G., Chang JM., Di Tommaso P., Erb I., Espinosa-Carrasco J., Notredame C. (2014)
            <b>T-Coffee: Tree-based consistency objective function for alignment evaluation. </b>
            Methods Mol Biol 1079:117-129.
            <a href = https://www.ncbi.nlm.nih.gov/pubmed/24170398 target="_blank" rel="noopener">PMID: 24170398</a></p>`,
};
