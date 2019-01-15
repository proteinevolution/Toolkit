/* tslint:disable:max-line-length */

export default {
    overview: `Ali2D which takes a sequence alignment as input, performs PSIPRED and MEMSAT2 on each of the sequences and
        finally plots information about secondary structure, transmembrane regions and amino acids on the alignment.`,
    parameters: [
        {
            title: 'Input',
            content: `You have the option to enter a multiple sequence alignment (MSA) in FASTA or CLUSTAL format. The
                    number of sequences is currently limited to 100.<br/><br/>
                @:toolHelpModals.common.msa`,
        },
        {
            title: '% identity cutoff to invoke a new PSIPRED run',
            content: `This option specifies the percentage sequence identity below which a new Psipred run is invoked.`,
        },
    ],
    references: ``,
};
