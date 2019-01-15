/* tslint:disable:max-line-length */

export default {
    overview: `<p>ANCESCON is a package for distance-based phylogenetic inference and reconstruction of ancestral protein
            sequences that takes into account the observed variation of evolutionary rates between positions that
            more precisely describes the evolution of protein families.</p>
        <p>[Reconstruction of ancestral protein sequences and its applications, Wei Cai, Jimin Pei, and Nick V Grishin<br/>
            BMC Evol Biol. 2004; 4: 33. doi: 10.1186/1471-2148-4-33.
            <a href = https://www.ncbi.nlm.nih.gov/pmc/articles/PMC522809 target="_blank" rel="noopener">Published
                online 2004 September 17</a>.]</p>`,
    parameters: [
        {
            title: 'Input',
            content: `You have the option to enter a multiple sequence alignment (MSA) in FASTA or CLUSTAL format. The
                    number of sequences is currently limited to 2000.<br/><br/>
                @:toolHelpModals.common.msa`,
        },
    ],
    references: `<p>Cai W., Pei J., Grishin NV. (2004) <b>Reconstruction of ancestral protein sequences and its applications.
    </b>BMC Evol Biol. 4:33. <a href = https://www.ncbi.nlm.nih.gov/pubmed/15377393 target="_blank" rel="noopener">PMID: 15377393</a></p>`,
};
