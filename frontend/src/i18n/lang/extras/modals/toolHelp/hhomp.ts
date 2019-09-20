/* tslint:disable:max-line-length */

export default {
    en: {
        toolHelpModals: {
            hhomp: {
                overview: `
        <h5><b>HHomp server for outer membrane protein prediction and classification</b></h5>
        <p>HHomp is a sensitive protein homology detection and classification method of outer membrane proteins (OMP). 
        It uses HMM-HMM-comparison and an integrated beta-barrel prediction method. The output is a list of closest 
        homologs with alignments. HHomp builds a profile HMM from a query sequence and compares it with a database of 
        HMMs representing outer membrane proteins. </p>`,
                parameters: [
                    {
                        title: 'Input',
                        content: `You have the option to enter or upload either a single protein sequence or a multiple sequence
                    alignment (MSA) in FASTA, CLUSTAL, or A3M format.<br/><br/>
                @:toolHelpModals.common.singleseq
                @:toolHelpModals.common.msa
                @:toolHelpModals.common.a3m`,
                    },
                    {
                        title: 'Select HMM database',
                        content: `<p>Select a database of template HMMs against which you want to compare the query.</p>
                    <em>HHompDB_v1.0 (April 2007)</em>
                    <p>This database is built from over 22000 sequences which were found by a homology search detection 
                    based on outer membrane proteins of known structure.</p>

                    <em>HHompDB_no_hypo (no hypothetical)</em>
                    <p>This database is similar to the HHompDB. In this database, all proteins for which the presence of
                    a outer membrane beta-barrel is unclear are filtered out.</p>

                    <em>HHompDB_only_bb (only beta-barrel)</em>
                    <p>This database is similar to the HHompDB. The difference is that this database consists only of 
                    protein comprising transmembrane beta-barrels. So, this database does not contain the alpha-helical 
                    translocon Wza. </p>
                    
                    <em>HHompDB_only_canonical_bb (only canonical beta-barrel)</em>
                    <p>This database is similar to the HHompDB. The difference is that this database consists only of 
                    canonical transmembrane beta-barrels. So, this database does not contain the trimeric outer membrane 
                    effluc protein TolC and the alpha-helical translocon Wza. </p>

                    <em>scop1.71_only_bb</em>
                    <p>This database is built from sequences in the SCOP (Structural Classification of Proteins) 
                    database. SCOP is a hand-curated database with protein domains of known structure. It is 
                    hierarchically organized into families, superfamilies, folds, and classes to reflect evolutionary 
                    relationships. In our database, only proteins in the f.4 fold (transmembrane beta-barrel), in the 
                    f.5.1.1 family (outer membrane efflux proteins) and in the f.6.1.2 family (porin MspA) are taken. 
                    For each protein we build a multiple alignment with iterated PSI-BLAST searches and transform these 
                    alignments into HMMs.</p>`,
                    },
                    {
                        title: 'Maximal no. of MSA generation steps',
                        content: `This specifies the number of iterations performed by HHblits to build an
                    alignment from the input sequence or alignment. The iterations stop in any case when no
                    further sequences are found. If you want to use exactly your input alignment choose 0 here.
                    This will ensure that only predicted secondary structure is added to your input alignment.`,
                    },
                    {
                        title: 'E-value incl. threshold for MSA generation',
                        content: `All sequence matches ("hits") with an E-value better than this threshold are included 
                        in the alignment for the next MSA generation iteration or, in the last iteration, for building 
                        the HMM for HHsearch.`,
                    },
                    {
                        title: 'Min. coverage of MSA hits',
                        content: `This parameter controls what percentage of the query residues needs to be covered by 
                        the hits of the MSA algorithm in order for the hit to be included into the alignment for the 
                        next iteration. More precisely, the coverage of a sequence found by the MSA algorithm is defined 
                        as the number of residues aligned with the query divided by the length of the query.`,
                    },
                    {
                        title: 'Min. seq. identity of MSA hits with query',
                        content: `This parameter controls what percentage of residues from a hit of the MSA algorithm 
                        needs to be identical to their aligned query residues in order for the hit to be included in 
                        the MSA alignment. More precisely, the sequence identity of a hit by the MSA algorithm is 
                        defined as the number of residues identical with aligned query residues divided by the length 
                        of the MSA hit.`,
                    },
                    {
                        title: 'Alignment mode',
                        content: `In local mode, HHomp will use local alignments to score your query sequence with the
                        database sequences. In global mode, it uses global alignments with no end gap penalties 
                        (sometimes called 'global-local' alignments), i.e. the query-template alignments extend up to 
                        the beginning and end of the query or template sequence. The use of local mode is strongly 
                        recommended if you want to know which are your most likely remote homologs.`,
                    },
                    {
                        title: 'Min. probability in hit list',
                        content: `Only hits above this cut-off probability will be displayed.`,
                    },
                    {
                        title: 'No. of target sequences',
                        content: `This parameter controls how many matches will be displayed in the results.`,
                    },
                ],
                references: `<p>HHomp--prediction and classification of outer membrane proteins. <br>Remmert M, Linke D, 
                             Lupas AN, Söding J. <a href = https://www.ncbi.nlm.nih.gov/pubmed/19429691 target="_blank" 
                             rel="noopener">Nucleic Acids Res. 2009 Jul; 37:W446-51.</a></p>`,
            },
        },
    },
};
