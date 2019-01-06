/* tslint:disable:max-line-length */

export default {
    overview: `<p>DeepCoil is a method for the accurate prediction of coiled coil domains in protein sequences. A local
        copy and further details on DeepCoil can here obtained here: <a href = https://github.com/labstructbioinf/DeepCoil
        target="_blank" rel="noopener">https://github.com/labstructbioinf/DeepCoil</a>.</p>

        <p>DeepCoil predictions can be made based on a single sequence or based on a multiple sequence alignment (MSA). Users
            are provided with the option to enter their own alignment as well as with an option to have the alignment generated
            for their input sequence ('Run_PSI-BLAST' mode). DeepCoil generates a graphical summary of the predictions and
            a text output with probability value for each residue in the sequence to be in a coiled coil domain.
        </p>

        <p>Please check the 'Input & Parameters' tab for details on allowed input formats.</p>`,
    parameters: [
        {
            title: 'Input',
            content: `You have the option to enter or upload either a single protein sequence or a multiple sequence
                    alignment (MSA) in FASTA or CLUSTAL format.<br/><br/>
                    @:toolHelpModals.common.singleseq
                    @:toolHelpModals.common.msa`,
        },
        {
            title: 'Input mode',
            content: `<p>Use_input_sequence: prediction is performed on the basis of the given sequence.</p>
                    <p>Use_input_msa: prediction is performed on the basis of the given multiple sequence alignment (MSA).</p>
                    <p>Run_PSI-BLAST: In this mode, a PSI-BLAST search, with 'evalue' set to 0.001 and 'num_iterations' to 3,
                        against the nr90 database is performed for the generation of a PSSM for the given query. </p>`,
        },
    ],
    references: ``,
};
