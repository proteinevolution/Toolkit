/* tslint:disable:max-line-length */

export default {
    en: {
        toolHelpModals: {
            modeller: {
                overview: `<p>Modeller is one of the most popular programs for homology modelling. The goal of homology modelling
        (also called comparative modelling) is to obtain a 3D structure for a protein sequence of interest (the target).
        One starts from a sequence alignment of the target sequence with one or more template sequences for which the structures are known.
        Sequences with recognizable sequence similarity (e.g. above 30%) will in general have quite similar 3D structures.
        Therefore, an alignment with a homologous template of known structure can, in principle, be used to directly predict the
        main chain coordinates by simply mapping the target sequence into the template main chain structure. Modeller goes a step
        further in deriving constraints from the multiple alignment of the target with the templates. In addition to a-priori constraints
        like bond lengths et cetera, these constraints may refer to Calpha-Calpha distances, main-chain dihedral angles, or side-chain
        dihedral angles. The constraints are formulated probabilistically in terms of probability density functions, that are derived
        from a statistical analysis of a great number of pairwise structural alignments. Modeller then tries find the parameters
        (i.e. the model structure) that maximizes the total probability density obtained by combining all constraints. Modeller, as
        most (if not all) other available programs, has a few important limitations:</p>
        <ul>
            <li>It cannot correct a wrong or suboptimal target-template alignment. Errors in the alignment
            will directly translate into errors in the model structures. The alignment errors are the leading
            source of errors for homology models when the sequence similarity is below ~50%.</li>
            <li>The model is, on average, not more similar to the real target structure than the template used.
            (This sounds frustrating, but it means that normally, the improvements which the homology modelling
            software achieves do not fully compensate the alignment errors, on average). For this reason, the
            other limiting factor in model quality, beside the alignment quality, is the selection of the best
            template. This is a very difficult task for which a large number of methods and servers have been
            developed. (See our HHpred server and the links in the section on tertiary structure prediction.)</li>
            <li>Side chain placement gets unreliable below sequence identities of 70%, simply because the
            position of side chains is conserved less and less below this degree of similarity.</li>
            <li>Loops of more than three or four residues for which no corresponding template atoms are
            available can not be reliably estimated. They are more or less guessed by the programs.
            There exist specialized loop modelling methods, but they are very time-consuming and can not
            be offered over the web. Also, loops of more than 8 to 12 residues can not be modelled reliably.</li>
        </ul>
        <p>In summary, homology modelling software can not correct errors in the alignments or templates.
        What you get out is almost what you put in.</p>`,
                parameters: [
                    {
                        title: 'Input',
                        content: `Currently, the input format must be a PIR alignment forwarded by HHpred.`,
                    },
                    {
                        title: 'Modeller key',
                        content: `The MODELLER license requires you to enter a MODELLER-key to use this tool.
                This key is freely available for academic users and easily obtainable at:
                <a href = "http://salilab.org/modeller/registration.shtml" target="_blank" rel="noopener">
                http://salilab.org/modeller/registration.shtml</a>.
                `,
                    },
                ],
                references: `<p>Sali A., Potterton L., Yuan F., van Vlijmen H., Karplus M. (1995)
        <b>Evaluation of comparative protein modeling by MODELLER. </b>Proteins 23(3):318-26.
        <a href = "https://www.ncbi.nlm.nih.gov/pubmed/8710825" target="_blank" rel="noopener">PMID: 8710825</a></p>`,
            },
        },
    },
};
