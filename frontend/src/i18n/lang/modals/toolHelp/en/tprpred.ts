/* tslint:disable:max-line-length */

export default {
    overview: `TPRpred uses the profile representation of the known repeats to detect Tetratrico Peptide Repeats (TPRs),
        Pentatrico Peptide Repeats(PPRs) and SEL1-like repeats from the query sequence and computes the
        statistical significance for their occurrence.`,
    parameters: [
        {
            title: 'Input',
            content: `Enter a single protein sequence in FASTA format.<br/><br/>
                @:toolHelpModals.common.singleseq`,
        },
        {
            title: 'E-value inclusion TPR & SEL',
            content: `Max. E-value for inclusion in output for TPR and SEL.`,
        },
    ],
    references: `<p>Magis C., Taly JF., Bussotti G., Chang JM., Di Tommaso P., Erb I., Espinosa-Carrasco J., Notredame C. (2014)
        <b>T-Coffee: Tree-based consistency objective function for alignment evaluation. </b>
        Methods Mol Biol 1079:117-129.
        <a href = https://www.ncbi.nlm.nih.gov/pubmed/24170398 target="_blank" rel="noopener">PMID: 24170398</a></p>`,
};
