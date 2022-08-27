/* tslint:disable:max-line-length */

export default {
    en: {
        toolHelpModals: {
            psiblast: {
                overview: `This tools offers the BLAST+ and PSI-BLAST+ methods of NCBI for searching locally similar sequences in a chosen
            protein sequence database. The input for this tools is either a single protein sequence or a multiple
            sequence alignment. By default, one search iteration is performed, offering the BLAST+ functionality. To obtain
            the functionality of PSI-BLAST+, the hits found in the first search iteration could be forwarded to this tool
            to start the next search iteration. Alternatively, the number of iteration can also be set to a value > 1.`,
                parameters: [
                    {
                        title: 'Input',
                        content: `You have the option to enter or upload either a single protein sequence or a multiple sequence
                    alignment (MSA) in FASTA or CLUSTAL format.<br/><br/>
                @:toolHelpModals.common.singleseq
                @:toolHelpModals.common.msa`,
                    },
                    {
                        title: 'Select target database',
                        content: `<p>The selected databases are used to perform the search.</p>
                    <ul>
                        <li><b>nr:</b> the non-redundant sequence database at the NCBI. (See NCBI's BLAST tutorial).</li>
                        <li><b>nr90, nr70, etc.:</b> nr filtered for a maximum pairwise sequence identity of ~90% or 70%
                        with MMseqs2.</li>
                        <li><b>nr_euk, nr_bac, nr_arc, nr_pro, nr_vir:</b> eukaryotic, bacterial, archaeal, prokaryotic,
                        and viral sequences from the non-redundant database nr.</li>
                        <li><b>pdb_nr: </b>Sequences of proteins whose structures have been deposited in the Protein
                            Data Bank (PDB).</li>
                        <li><b>uniprot_sprot:</b> manually annotated and reviewed sequences from the
                            <a href="http://www.expasy.org/sprot/" target="_blank" rel="noopener">UniProtKB.</a></li>
                        <li><b>uniprot_trembl:</b> automatically annotated and not reviewed sequences from the
                            <a href="http://www.ebi.ac.uk/swissprot/" target="_blank" rel="noopener">UniProtKB.</a></li>
                        <li><b>uniprot_ribodb:</b> collection of ribosomal proteins from uniprot_sprot.</a></li>
                        <li><b>uniref90:</b> the
                        <a href="https://www.uniprot.org/help/uniref" target="_blank" rel="noopener">UniRef database</a>
                        offers clustered sets of sequences from the UniProtKB and selected UniParc records. UniRef90 is
                        a filtered down version of UniRef100.</li>
                        <li><b>prokaryotic_proteasome_homologs:</b> this database comprises protein sequences of several
                        newly discovered prokaryotic proteasome homologs.</li>
                        <li><b>alphafold_uniprot, alphafold_uniprot50:</b> these protein sequence databases are based on
                        the <a href="https://alphafold.ebi.ac.uk/" target="_blank" rel="noopener">AlphaFold DB</a>
                        developed by DeepMind and EMBL-EBI.</li>
                    </ul>`,
                    },
                    {
                        title: 'Scoring Matrix',
                        content: `Specify an alternate scoring matrix.`,
                    },
                    {
                        title: 'Number of iterations',
                        content: `Specify the maximum number of rounds of search.`,
                    },
                    {
                        title: 'E-value cutoff for reporting',
                        content: `The statistical significance threshold for reporting matches against database sequences. If the
                    statistical significance ascribed to a match is greater than the e-value threshold, the match will
                    not be reported. Lower e-value thresholds are more stringent, leading to fewer chance matches being
                    reported. Increasing the threshold shows less stringent matches. Fractional values are acceptable.`,
                    },
                    {
                        title: 'E-value cutoff for inclusion',
                        content: `The statistical significance threshold for including a sequence in the model used to
                    create the PSSM for the next iteration.`,
                    },
                    {
                        title: 'Max target hits',
                        content: `This parameter controls how many matches will be displayed in the results.`,
                    },
                ],
                references: `BLAST+: architecture and applications.<br> Camacho C, Coulouris G, Avagyan V, Ma N, 
                Papadopoulos J, Bealer K, Madden TL.
                <a href="https://bmcbioinformatics.biomedcentral.com/articles/10.1186/1471-2105-10-421" target="_blank" rel="noopener">
                BMC Bioinformatics. 2009 Dec 15;10:421</a>.<br><br>
                Database resources of the national center for biotechnology information.<br>Sayers EW, Bolton EE, Brister JR, et al.
                <a href="https://doi.org/10.1093/nar/gkab1112" target="_blank" rel="noopener"> 
                Nucleic Acids Res. 2022;50(D1):D20-D26.</a><br><br>
                UniProt: the universal protein knowledgebase in 2021. <br>UniProt Consortium.
                <a href="https://doi.org/10.1093/nar/gkaa1100" target="_blank" rel="noopener"> 
                Nucleic Acids Res. 2021;49(D1):D480-D489. </a><br><br>
                RCSB Protein Data Bank: powerful new tools for exploring 3D structures of biological macromolecules for 
                basic and applied research and education in fundamental biology, biomedicine, biotechnology, 
                bioengineering and energy sciences.<br> Burley SK, Bhikadiya C, Bi C, et al.
                <a href="https://doi.org/10.1093/nar/gkaa1038" target="_blank" rel="noopener"> 
                Nucleic Acids Res. 2021;49(D1):D437-D451</a><br><br>
                AlphaFold Protein Structure Database: massively expanding the structural coverage of protein-sequence 
                space with high-accuracy models.<br> Varadi M, Anyango S, Deshpande M, et al.
                <a href="https://doi.org/10.1093/nar/gkab1061" target="_blank" rel="noopener"> 
                Nucleic Acids Res. 2022;50(D1):D439-D444.`,
            },
        },
    },
};
