/* tslint:disable:max-line-length */

export default {
    overview: `MARCOIL is a hidden Markov model-based program that predicts existence and location of potential coiled-coil
        domains in protein sequences. The external MARCOIL Homepage can be found
        <a href = http://bioinf.wehi.edu.au/folders/mauro/Marcoil/index.html target="_blank" rel="noopener">here</a>.`,
    parameters: [
        {
            title: 'Input',
            content: `Enter a single protein sequence in FASTA format.<br/>
                @:toolHelpModals.common.singleseq`,
        },
        {
            title: 'Matrix',
            content: `<p>The three matrices accessible through the web interface are the one used in the paper and trained
                    on 9 "families" of proteins (9FAM). This is a matrix of amino-acid probabilities derived from a large
                    dataset of coiled-coil domains. It is unspecific, as the dataset contains all kind of domains and these
                    differ in the number of helices, the orientation, the length and the hydrophobicity. The matrix is meant
                    for first-pass genomic screenings.</p>
                    <p>It generalises the two matrices proposed by A. Lupas and collaborators and used by the program COILS.
                    These matrices are MTIDK, derived from 5 and MTK derived from three "families" of proteins. Those are
                    matrices of frequency ratios and we computed from them, by using an estimate of absolute amino-acid
                    frequencies, the other two matrices that can be used.</p>
                `,
        },
        {
            title: 'Transition Probability',
            content: `Specify whether to use high or low transition probability.`,
        },
    ],
    references: `<p>Delorenzi M., Speed T. (2002) <b>An HMM model for coiled-coil domains and a comparison with PSSM-based
        predictions. </b>Bioinformatics 18(4):617-625.
        <a href = https://www.ncbi.nlm.nih.gov/pubmed/12016059 target="_blank" rel="noopener">PMID: 12016059</a></p>`,
};
