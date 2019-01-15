/* tslint:disable:max-line-length */

export default {
    overview: `Quick2D gives you an overview of secondary structure features like alpha-helices, extended beta-sheets,
    coiled coils, transmembrane helices and disorder regions.`,
    parameters: [
        {
            title: 'Input',
            content: `You have the option to enter or upload either a single protein sequence or a multiple sequence
                    alignment (MSA) in FASTA or CLUSTAL format.<br/><br/>
                @:toolHelpModals.common.singleseq
                @:toolHelpModals.common.msa`,
        },
        {
            title: 'Select database for MSA generation',
            content: `Select the database that should be used for MSA generation. You can select between:
                    UniRef90,
                    UniRef90,
                    nr90, and
                    nr70`,
        },
        {
            title: 'Maximal no. of MSA generation steps',
            content: `The maximal number of PSI-BLAST for generating the MSA.`,
        },
        {
            title: 'E-value incl. threshold for MSA generation',
            content: `Alignments are extended using the jump-start option of PSI-BLAST.
                    Only include hits with E-value below this threshold into the resulting multiple sequence alignment.`,
        },
    ],
    references: ``,
};
