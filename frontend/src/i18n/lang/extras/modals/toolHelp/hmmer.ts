/* tslint:disable:max-line-length */

export default {
    en: {
        toolHelpModals: {
            hmmer: {
                overview: `<a href = https://www.ncbi.nlm.nih.gov/pmc/articles/PMC3125773 target="_blank" rel="noopener">HMMER</a> is a
        tool for fast and sensitive homology searching based on profile Hidden Markov models. It accepts a single
        sequence or a multiple alignment in aligned FASTA/CLUSTAL format as input. In case the input is a single sequence,
        HHblits is run over Uniprot20 to build an alignment required for the calculation of query HMM.`,
                parameters: [
                    {
                        title: 'Input',
                        content: `You have the option to enter or upload either a single protein sequence or a multiple sequence
                    alignment (MSA) in FASTA or CLUSTAL format.<br/><br/>
                @:toolHelpModals.common.singleseq
                @:toolHelpModals.common.msa`,
                    },
                    {
                        title: 'Select database',
                        content: `The selected database is used by HMMER to perform the search.
                        <ul>
                            <li><b>nr50, nr70, nr90:</b> NCBI's nonredundant protein sequence database filtered for a
                                maximum pairwise sequence identity of 50%, 70% and 90%, respectively.</li>
                            <li><b>env70, env90:</b> NCBI's environmental sequence database, usually from unknown organisms,
                                filtered for a maximum pairwise sequence identity of 70% and 90%, respectively.</li>
                            <li><b>nr_euk*, nr_bac*, nr_arc*, nr_pro*, nr_vir*:</b> eukaryotic, bacterial, archaeal,
                                prokaryotic, and viral sequences from the nonredundant database nr.</li>
                            <li><b>pdb_nr: </b>Sequences from proteins whose structures have been deposited in the Protein
                                Data Bank PDB.</li>
                            <li><b>Uniprot_sprot:</b> manually annotated and reviewed sequences from the
                                <a href="http://www.expasy.org/sprot/" target="_blank" rel="noopener">UniProtKB.</a></li>
                            <li><b>Uniprot_trembl:</b> automatically annotated and not reviewed sequences from the
                                <a href="http://www.ebi.ac.uk/swissprot/" target="_blank" rel="noopener">UniProtKB.</a></li>
                            <li><b>alphafold_uniprot, alphafold_uniprot50:</b> these protein sequence databases are based on
                            the <a href="https://alphafold.ebi.ac.uk/" target="_blank" rel="noopener">AlphaFold DB</a>
                                developed by DeepMind and EMBL-EBI.</li>
                        </ul>`,
                    },
                    {
                        title: 'MSA enrichment iterations using HHblits',
                        content: `Specifies the number of HHblits iterations over Uniprot20 to build an MSA when the input is a single sequence.`,
                    },
                    {
                        title: 'E-value cutoff for reporting',
                        content: `Report target sequences with an E-value of <= the specified value.`,
                    },
                    {
                        title: 'Max target hits',
                        content: `This parameter controls how many matches will be displayed in the results.`,
                    },
                ],
                references: `<p>HMMER web server: interactive sequence similarity searching.<br>Finn RD., Clements J., Eddy SR.
                <a href = https://doi.org/10.1093/nar/gkr367 target="_blank" rel="noopener"> 
                Nucleic Acids Res. 2011;39(Web Server issue):W29-W37.</a></p>
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
