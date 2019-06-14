/* tslint:disable:max-line-length */

export default {
    en: {
        toolHelpModals: {
            pcoils: {
                overview: `<h5><b>Recommendations</b></h5>
        <p>COILS with the MTIDK matrix is specific for solvent-exposed, left-handed coiled coils. Other types of
        coiled-coil structure, such as buried coiled coils (e.g the central coiled coil in catabolite repressor
        protein, or some transmembrane domains) and right-handed coiled coils, are not detected. COILS does not
        reach yes-or-no decisions based on a threshold value. Rather, it yields a set of probabilities that
        presumably reflect the coiled-coil forming potential of a sequence. This means that even at high probabilities
        (e.g. >90%), there will be (and should be) sequences that in fact do not form a coiled coil, though they may
        have the potential to do so in a different context. COILS is biased towards hydrophilic, highly charged sequences.
        For this reason, all scans should be performed with a weighted and an unweighted matrix, and the results compared.
        Differences of more than 20-30 percentage points in the probabilities should be taken to indicate that a
        coiled-coil structure is unlikely, the elevated scores being mainly due to the high incidence of charged
        residues (note, though, that this would have marked human mannose-binding protein as a false positive).
        The MTIDK matrix assigns high probabilities to known coiled coils segments, but identifies different helices
        at high probability in a database of globular proteins. This is a surprising feature whose reason is as yet
        unclear, but which can be exploited for predictive purposes. It is therefore useful to compare the results of
        scans made with the two matrices. Again, differences of more than 20-30 percentage points in the probabilities
        should be taken to indicate that a coiled-coil structure is unlikely (note, though, that this threshold would
        make the replication terminator protein a border-line case). The resolution between globular and coiled-coil
        score distributions decreases strongly with a decreasing size of the scanning window. The prediction of new
        coiled-coil segments should therefore be made using a 28 residue window, or in special cases a 21 residue window.
        14 residue windows should normally be reserved for the analysis of local parameters (such as the frame) in
        known or predicted coiled coils. The ends of coiled-coil segments appear to be most accurately identified in
        a 21 residue window. In general, it is assumed that residues with probabilities >50% are part of a coiled-coil
        segment. Sequences with high coiled-coil probability from globular proteins rarely exceed a length of 30 residues.
        None is longer than 35 residues. Sequences with probabilities >80-90% that extend for more than 35 residues are
        therefore more likely to assume a coiled-coil structure than is indicated by the obtained probabilities.
        Where possible, sequences related to the protein of interest should also be analyzed for predicted coiled-coil
        segments (e.g. by using a sequence alignment instead of a single sequence as input). It should be kept in mind,
        though, that the sequences must be related in the region of high scores in order for the comparison to be significant.</p>
        <h5><b>PSIPRED</b></h5><p>Comparison of the coiled-coil prediction with predictions of the secondary
        structure are generally useful, particularly if multiple related sequences are available.
        PSIPRED predicts the secondary structure as described in
        <a href =https://www.ncbi.nlm.nih.gov/pubmed/10493868?dopt=Abstract target="_blank" rel="noopener">
        Jones, J.Mol.Biol. 292:195-202 (1999).</a></p>
    `,
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
                        content: `<p>Use input as given: Performs all calculations on the basis of a given sequence or a
        given sequence alignment in FASTA format. Either put in a sequence or sequence alignment of your choice.</p>
                    <p>Run PSI-BLAST: Performs a PSI-BLAST run on the given sequence (two iterations, E-value
                    for inclusion 0.0001). Hits are aligned and filtered to a minimum sequence identity of 40%
                    with the query sequence and a minimum coverage of 20%. This alignment is then the basis for
                    the input profile. In case you are not happy with the used PSI-BLAST parameters, run PSI-BLAST
                    separately with the parameters of your choice, create a multiple alignment in FASTA format,
                    and use this as COILS input.</p>
                `,
                    },
                    {
                        title: 'Matrix',
                        content: `
                    MTIDK - is a matrix derived from myosins, paramyosins, tropomyosins, intermediate filaments type I - V, desmosomal proteins and kinesins. The matrix was compiled by weighting the residue frequencies of the different protein families according to the following scheme:<br/>0.2 MYOSINS - 0.5 myosins - 0.5 paramyosins<br/>0.2 TROPOMYOSINS<br/>0.2 INTERMEDIATE FILAMENTS - 0.2 type I (keratin) - 0.2 type II (keratin) - 0.2 type III (desmin, vimentin, GFAP, peripherin) - 0.2 type IV (NF light, medium and heavy chains) - 0.2 type V (lamins A and B)<br/>0.2 DESMOSOMAL PROTEINS - 0.33 desmoplakin - 0.33 plectin - 0.33 hemidesmosomal plaque prot. (bullous pemphigoid)<br/>0.2 KINESINS<br/><br/>PDB - is a matrix derived from coiled coils in the PDB. <br/>
                `,
                    },
                    {
                        title: 'Weighting',
                        content: `Weighting: Because coiled coils are generally fibrous, solvent-exposed structures, all but
            the internal a and d positions have a high likelihood of being occupied by hydrophilic residues.
            A program that gives equal weight to all positions is therefore going to be biased towards hydrophilic,
            charge-rich sequences. While this does not pose a problem for the vast majority of natural sequences,
            some highly charged sequences obtain high coiled-coil probabilities in the obvious absence of heptad
            periodicity and coiled-coil-forming potential. An extreme case is that of polyglutamate which obtains
            a coiled-coil-forming probability > 99%. To counter this problem, COILS contains a weighting option,
            which allows the user to assign the same weight to the two hydrophobic positions a and d as to the
            five hydrophilic positions b, c, e, f and g. This leads to an only slightly worse performance of the
            program and permits the identification of false positives. It is recommended to run a weighted
            and unweighted scan and to compare the outputs. A drop of more than 20-30% in the probability is a clear
            indication of a highly-charged false positive.`,
                    },
                ],
                references: `<p>Lupas A., Van Dyke M., Stock J. (1991) <b>Predicting Coiled Coils from Protein Sequences. </b>
        Science 252:1162-1164.
        <a href = https://www.ncbi.nlm.nih.gov/pubmed/2031185 target="_blank" rel="noopener">PMID: 2031185</a></p>
        <p>Lupas A. (1996) <b>Prediction and Analysis of Coiled-Coil Structures. </b>Methods Enzymol 266:513-525.
        <a href = https://www.ncbi.nlm.nih.gov/pubmed/8743703 target="_blank" rel="noopener">PMID: 8743703</a></p>
        <p>Parry DA. (1982) <b>Coiled-Coils in Alpha-Helix-Containing Proteins: Analysis of the Residue Types
        within the Heptad Repeat and the Use of These Data in the Prediction of Coiled-Coils in Other Proteins. </b>
        Biosci Rep 2(12):1017-24.
        <a href = https://www.ncbi.nlm.nih.gov/pubmed/7165792 target="_blank" rel="noopener">PMID: 7165792</a></p>`,
            },
        },
    },
};
