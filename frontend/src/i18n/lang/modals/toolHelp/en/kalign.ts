/* tslint:disable:max-line-length */

export default {
    overview: `<p>Kalign - an accurate and fast multiple sequence alignment algorithm.</p>
        <p>Timo Lassmann and Erik LL Sonnhammer (2005) Kalign - an accurate and fast multiple sequence alignment algorithm
        <br/>BMC Bioinformatics 2005, 6:298 doi:10.1186/1471-2105-6-298</p>`,
    parameters: [
        {
            title: 'Input',
            content: `@:toolHelpModals.common.multiseq`,
        },
        {
            title: 'Output the alignment in',
            content: `Specify the order in which the aligned sequences appear within the resulting multiple sequence alignment`,
        },
        {
            title: 'Gap open penalty',
            content: `Specify the gap open penalty (numbers greater than 0 are valid).`,
        },
        {
            title: 'Gap extension penalty',
            content: `Specify the gap extension penalty.`,
        },
        {
            title: 'Terminal gap penalties',
            content: `Specify the terminal gap penalties.`,
        },
        {
            title: 'Bonus score',
            content: `Here you can specify a constant added to the substitution matrix.`,
        },
    ],
    references: `<p>Lassmann T., Sonnhammer EL. (2005) <b>Kalign--an accurate and fast multiple sequence alignment algorithm.</b>
        BMC Bioinformatics. 6:298.
        <a href = https://www.ncbi.nlm.nih.gov/pubmed/16343337 target="_blank" rel="noopener">PMID: 16343337</a></p>`,
};
