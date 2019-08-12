/* tslint:disable:max-line-length */

export default {
    en: {
        toolHelpModals: {
            mmseqs2: {
                overview: `MMseqs2 (Many-against-Many searching) is a software suite to search and cluster huge protein sequence sets.
            MMseqs2 can run 10000 times faster than BLAST. At 100 times its speed it achieves the same sensitivity.
            It can also perform profile searches with the same sensitivity as PSI-BLAST but at around 270 times its speed.`,
                parameters: [
                    {
                        title: 'Input',
                        content: `@:toolHelpModals.common.multiseq`,
                    },
                    {
                        title: 'Minimum sequence identity',
                        content: `List matches above this sequence identity (for clustering)`,
                    },
                    {
                        title: 'Minimum alignment coverage',
                        content: `List matches above this fraction of aligned (covered) query and target residues`,
                    },
                    {
                        title: 'Clustering mode',
                        content: `The 'normal' mode employs the slow (quadratic time), but sensitive 'cluster' pipeline offered
                    by MMseqs2. The 'linclust' mode clusters sequences in linear time, but is less sensitive than the
                    normal mode.`,
                    },
                ],
                references: `<p>MMseqs2 enables sensitive protein sequence searching for the analysis of massive data sets. Steinegger,M,
            Söding J.<a href="https://www.nature.com/articles/nbt.3988" target="_blank" rel="noopener"> Nat Biotechnol.
            2017 Nov;35(11):1026-1028.</a></p>`,
            },
        },
    },
};
