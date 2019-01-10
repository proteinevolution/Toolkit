/* tslint:disable:max-line-length */

export default {
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
            title: 'Select standard database',
            content: `<p>The selected databases are used to perform the search.</p>
                    <ul>
                        <li><b>nr:</b> the non-redundant sequence database at the NCBI, updated weekly. (See NCBI's BLAST tutorial).</li>
                        <li><b>env:</b> NCBI's env database with environmental sequences, usually from unknown organisms.</li>
                        <li><b>nre:</b> nr+env database. Updated weekly.</li>
                        <li><b>nr90, nr70, env90 etc.:</b> representative sequences, filtered for a maximum pairwise sequence identity of ~90% or 70% with kClust. Updated bimonthly.</li>
                        <li><b>nr_euk, nr_bac, nr_arc, nr_pro, nr_vir:</b> eukaryotic, bacterial, archaeal, prokaryotic, and viral sequences from the non-redundant database nr. Updated weekly.</li>
                        <li><b>pdb_nr: </b>Sequences from proteins whose structures have been deposited in the Protein
                            Data Bank PDB.</li>
                        <li><b>Uniprot_sprot:</b> manually annotated and reviewed sequences from the
                            <a href="http://www.expasy.org/sprot/" target="_blank" rel="noopener">UniProtKB.</a></li>
                        <li><b>Uniprot_trembl:</b> automatically annotated and not reviewed sequences from the
                            <a href="http://www.ebi.ac.uk/swissprot/" target="_blank" rel="noopener">UniProtKB.</a></li>
                    </ul>`,
        },
        {
            title: 'Scoring Matrix',
            content: `Specify an alternate scoring matrix.`,
        },
        {
            title: 'Max. number of iterations',
            content: `Specify the maximum number of rounds of search.`,
        },
        {
            title: 'E-value',
            content: `The statistical significance threshold for reporting matches against database sequences. If the
                    statistical significance ascribed to a match is greater than the e-value threshold, the match will
                    not be reported. Lower e-value thresholds are more stringent, leading to fewer chance matches being
                    reported. Increasing the threshold shows less stringent matches. Fractional values are acceptable.`,
        },
        {
            title: 'E-value inclusion threshold',
            content: `The statistical significance threshold for including a sequence in the model used to
                    create the PSSM for the next iteration.`,
        },
        {
            title: 'No. of target sequences',
            content: `This parameter controls how many matches will be displayed in the results.`,
        },
    ],
    references: `<p>Altschul S., Madden T., Schaffer A., Zhang J., Miller W., Lipman D. (1997) Gapped BLAST and PSI-BLAST: a new
            generation of protein database search programs. Nucl Acids Res 25(17):3389-3402.
            <a href = https://www.ncbi.nlm.nih.gov/pmc/articles/PMC146917 target="_blank" rel="noopener">PMCID: PMC146917</a></p>
        <p>Altschul SF., Gish W., Miller W., Myers EW., Lipman DJ. (1990) Basic local alignment search tool.
            J Mol Biol. 215:403-410. <a href = https://www.ncbi.nlm.nih.gov/pubmed/2231712 target="_blank" rel="noopener">PMID: 2231712</a></p>
        <p>Camacho C., Coulouris G., Avagyan V., Ma N., Papadopoulos J., Bealer K., Madden TL. (2008) BLAST+:
            architecture and applications. BMC Bioinformatics 10:421. <a href = https://www.ncbi.nlm.nih.gov/pubmed/20003500
        target="_blank" rel="noopener">PMID: 20003500</a></p>`,
};
