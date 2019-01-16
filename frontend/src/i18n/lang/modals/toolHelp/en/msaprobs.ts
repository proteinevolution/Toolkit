/* tslint:disable:max-line-length */

export default {
    overview: `MSAProbs is a practical multiple alignment algorithm for protein sequences.
    The design of MSAProbs is based on a combination of pair hidden Markov models and partition
    functions to calculate posterior probabilities. Assessed using the popular benchmarks: BAliBASE,
    PREFAB, SABmark and OXBENCH, MSAProbs achieves statistically significant accuracy improvements
    over the existing top performing aligners, including ClustalW, MAFFT, MUSCLE, ProbCons and Probalign.
    Furthermore, MSAProbs is optimized for multi-core CPUs by employing a multi-threaded design, leading to
    a competitive execution time compared to other aligners.`,
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
    references: `<p>Liu Y., Schmidt B., Maskell DL. (2010) <b>MSAProbs: multiple sequence alignment based on
            pair hidden Markov models and partition function posterior probabilities.</b> Bioinformatics 26(16): 1958-64.
            <a href = https://www.ncbi.nlm.nih.gov/pubmed/20576627 target="_blank" rel="noopener">PMID: 20576627</a></p>
`,
};
