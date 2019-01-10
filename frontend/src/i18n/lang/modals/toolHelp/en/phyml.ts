/* tslint:disable:max-line-length */

export default {
    overview: `PhyML is a software that estimates maximum likelihood phylogenies from alignments of amino acid sequences.`,
    parameters: [
        {
            title: 'Input',
            content: `You have the option to enter a multiple sequence alignment (MSA) in FASTA or CLUSTAL format.<br/><br/>
                @:toolHelpModals.common.msa`,
        },
        {
            title: 'Number of replicates',
            content: `Number of resampled datasets asked by
            <a href = http://evolution.genetics.washington.edu/phylip/doc/seqboot.html target="_blank" rel="noopener">SEQBOOT</a>.`,
        },
        {
            title: 'Model of AminoAcid replacement',
            content: `Amino acid substitution model that is used for the computation of the distance matrix by
            <a href = http://evolution.genetics.washington.edu/phylip/doc/protdist.html target="_blank" rel="noopener">PROTDIST</a>
            (Please have a look at the link for a more detailed description).`,
        },
    ],
    references: `<p>Guindon S., Dufayard J.F., Lefort V., Anisimova M., Hordijk W., Gascuel O.
        <b>New Algorithms and Methods to Estimate Maximum-Likelihood Phylogenies: Assessing the Performance of PhyML 3.0.</b>
        Molecular Biology and Evolution, msx149, 2017.</p>
`,
};
