/* tslint:disable:max-line-length */

export default {
    en: {
        toolHelpModals: {
            hhpred: {
                overview: `
        <h5><b>HHpred server for protein remote homology detection and 3D structure prediction</b></h5>
        <p>HHpred is a method for sequence database searching and structure prediction that is as easy to use as BLAST
            or PSI-BLAST and that is at the same time much more sensitive in finding remote homologs.
            In fact, HHpred's sensitivity is competitive with the most powerful servers for structure prediction currently
            available.</p>
        <p>HHpred is based on the pairwise comparison of profile hidden Markov models (HMMs). Whereas most conventional
            sequence search methods search sequence databases such as UniProt or the NR, HHpred searches alignment
            databases, like Pfam or SMART. This greatly simplifies the list of hits to a number of sequence families
            instead of a clutter of single sequences. All major publicly available profile and alignment databases
            are available through HHpred.</p>
        <p>HHpred accepts a single query sequence or a multiple alignment as input. Within only a few minutes it returns
            the search results in an easy-to-read format similar to that of PSI-BLAST. Search options include local or
            global alignment and scoring secondary structure similarity. HHpred can produce pairwise query-template
            sequence alignments, merged query-template multiple alignments, as well as 3D structural models calculated
            by the MODELLER software from HHpred alignments.</p>
        <h5><b>When can HHpred be useful for you?</b></h5>
        <p>It is well known that sequence search methods such as BLAST, PSI-BLAST, and/or HMMER are of prime importance
            for biological research because functional information of a protein or gene can be inferred from
            homologous proteins or genes identified in a sequence search. But quite often no significant relationship
            to a protein of known function can be established. This is certainly the case for the most interesting
            groups of proteins, for which no ortholog has yet been studied.</p>
        <p>When conventional sequence search methods fail, HHpred often allows the detection of remote relationships,
            enabling derivation of hypotheses about possible mechanisms, active site positions and residues, or the class
            of substrate bound. When a homologous protein with known structure can be identified, its structure can be
            used as a template to model the 3D structure for the protein of interest. The 3D model may then help to
            generate hypotheses to guide experiments.</p>
        <h5><b>What is HMM-HMM comparison and why is it so powerful?</b></h5>
        <p>When searching for remote homologs, it is wise to make use of as much information about the query and
            database proteins as possible in order to better distinguish true from false positives and to produce
            optimal alignments. This is the reason why sequence-sequence comparison is inferior to profile-sequence
            comparison. Sequence profiles contain for each column of a multiple alignment the frequencies of the 20
            amino acids. They therefore contain detailed information about the conservation of each residue position,
            i.e. how important each position is for defining other members of the protein family, and about the preferred
            amino acids. Profile Hidden Markov Models (HMMs) are similar to simple sequence profiles,
            but in addition to the amino acid frequencies in the columns of a multiple sequence alignment
            they contain information about the frequency of inserts and deletions at each column.
            Using profile HMMs in place of simple sequence profiles should therefore further improve sensitivity. Using
            HMMs both on the query and the database side greatly enhances the sensitivity and selectivity over
            sequence-profile based methods such as PSI-BLAST.</p>
        <h5><b>Structure prediction with HHpred</b></h5>
        <p>The most successful techniques for protein structure prediction rely on identifying homologous sequences
            with known structure to be used as template. This works so well because structures diverge much more slowly
            than sequences and homologous proteins may have very similar structures even when their sequences have
            diverged beyond recognition. But sensitivity in homology detection is crucial for success since many
            proteins have only remote relatives in the structure database.</p>
            <p>To generates a homology model, please chose PDB_mmCIF70 or PDB_mmCIF30 as the target database. Once the
            search has run through, analyse the obtained hits and select the best hits as templates for modelling.
            Next, click on the 'Model using selection' option located at the top of the Results page.</p>

        <h5><b>How can I verify if a database match is homologous?</b></h5>
        <p>Here is a list of things to check if a database match really is at least locally homologous.</p>
        <p><b>Check probability and E-value:</b> HHsearch can detect homologous relationships far
            beyond the twilight zone, i.e., below 20% sequence identity. Sequence identity is therefore not an
            appropriate measure of relatedness anymore. The estimated probability of the template to be (at least partly)
            homologous to your query sequence is the most important criterion to decide whether a template HMM is actually
            homologous or just a high-scoring chance hit. When it is larger than 95%, say, the homology is nearly certain.
            Roughly speaking, one should give a hit serious consideration (i.e., check the other points in this list)
            whenever (1) the hit has > 50% probability, or (2) it has > 30% probability and is among the top three hits.
            The E-value is an alternative measure of statistical significance. It tells you how many chance hits with a
            score better than this would be expected if the database contained only hits unrelated to the query. At
            E-values below one, matches start to get marginally significant. Contrary to the probability, when
            calculating the E-value HHsearch and HHblits do not take into account the secondary structure similarity.
            Therefore, the probability is a more sensitive measure than the E-value.</p>

        <p><b>Check if homology is biologically suggestive or at least reasonable:</b> Does the database hit have a
            function you would expect also for your query? Does it come from an organism that is likely to contain a
            homolog of your query protein?</p>

        <p><b>Check secondary structure similarity:</b> If the secondary structure of query
            and template is very different or you can’t see how they could fit together in 3D, then this is a reason
            to distrust the hit. Note however that if the query alignment contains only a single sequence, the secondary
            structure prediction is quite unreliable and confidence values are overestimated.</p>

        <p><b>Check relationship among top hits:</b> If several of the top hits are homologous to each other,
            (e.g. when they are members of the same SCOPe superfamily), then this will considerably reduce the
            chances of all of them being chance hits, especially if these related hits are themselves not very
            similar to each other. Searching the SCOP database is very useful precisely for this reason, since
            the SCOPe family identifier (e.g. a.118.8.2) allows to tell immediately if two templates are likely
            homologs.</p>

        <p><b>Check for possible conserved motifs:</b> Most homologous pairs of alignments will have at least
            one (semi-)conserved motif in common. You can identify such putative (semi-)conserved motifs by
            the agglomeration of three or more well-matching columns (marked with a ’|’ sign between the
            aligned HMMs) occurring within a few residues, as well as by matching consensus sequences. Some
            false positive hits have decent scores due to a similar amino acid composition of the template. In
            these cases, the alignments tend to be long and to lack conserved motifs.</p>

        <p><b>Check residues and role of conserved motifs:</b> If you can identify possible conserved motifs,
            are the corresponding conserved template residues involved in binding or enzymatic function?</p>

        <p><b>Check query and template alignments:</b> A corrupted query or template alignment is the
            main source of high-scoring false positives. The two most common sources of corruption in an
            alignment are (1) non-homologous sequences, especially repetitive or low-complexity sequences in
            the alignment, and (2) non-homologous fragments at the ends of the aligned database sequences.
            Check the query and template MSAs in an alignment viewer.</p>

        <p><b>Realign with other parameters:</b> change the alignment parameters. Choose global instead
            of local mode, for instance, if you expect your query to be globally homologous to the putative
            homolog. Try to improve the probability by changing the values for minimum coverage or minimum
            sequence identity. You can also run the query HMM against other databases.</p>

        <p><b>Build the query and/or database MSAs more aggressively:</b> If your query (or template)
            MSA is not diverse enough, you could increase sensitivity substantially by trying to include more
            remotely homologous sequences into the MSA. You can, for instance, make the MSA building criteria less
            stringent, or search with PSI-BLAST over nr70.</p>

        <p><b>Verify predictions experimentally:</b> The ultimate confirmation of a homologous relationship
            or structural model is, of course, the experimental verification of some of its key predictions, such
            as validating the binding to certain ligands by binding assays, measuring biochemical activity, or
            comparing the knock-out phenotype with the one obtained when the putative functional residues
            are mutated.</p>`,
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
                        title: 'Align two sequences/MSAs',
                        content: `Enable this option to compare two sequences or alignments with each other.`,
                    },
                    {
                        title: 'Select structural/domain databases',
                        content: `<p>Select domain database(s) of template HMMs against which you want to compare the query.</p>
                    <em>PDB_mmCIF70, PDB_mmCIF30</em>
                    <p>A search against our PDB_mmCIF70 database is the first step when trying to predict a structure
                        with HHpred. It is based on the <a href = https://www.rcsb.org/pdb/home/home.do target="_blank"
                        rel="noopener">Protein Data Bank (PDB)</a> which contains all publicly available
                        3D structures of proteins. For each protein chain in the PDB, we build a multiple alignment
                        with iterated HHblits searches over Uniprot20 and transform these alignments into HMMs.
                        HHpred can compare the query HMM to the database of HMMs based on PDB chains and generate
                        query-template alignments. We offer two filtered versions with a maximum sequence identity of
                        70% (PDB_mmCIF70 ) and 30% (PDB_mmCIF30).</p>

                    <em>SCOPe95, SCOPe70</em>
                    <p>The SCOPe95 and SCOPe70 profile HMM databases are built from sequences in the
                        <a href = http://scop.berkeley.edu/ target="_blank" rel="noopener">SCOPe database.</a></p>

                    <em>ECOD_F70</em>
                    <p> This database is a version of the <a href = http://prodata.swmed.edu/ecod/ target="_blank" rel="noopener">
                        Evolutionary Classification of Protein Domains (ECOD) database</a> filtered for a maximum of 70%
                        sequence identity.</p>
                    <em>CATH_S40</em>
                    <p> This database is a version of the <a href = https://www.cathdb.info target="_blank" rel="noopener">
                        CATH database</a> filtered for a maximum pairwise sequence identity of 40%.</p>
                        

                    <em>COG_KOG</em>
                    <p>The <a href = https://www.ncbi.nlm.nih.gov/COG/ target="_blank" rel="noopener">Clusters of
                        Orthologous Groups (COG) and EuKaryotic Orthologous Groups (KOG) databases</a>
                        comprise profile HMMs of conserved domains encoded in complete genomes.</p>

                    <em>Pfam-A</em>
                    <p><a href = http://pfam.xfam.org target="_blank" rel="noopener">PfamA</a> is a large collection
                        of curated multiple sequence alignments covering many common protein domains and families. Many
                        PfamA families are well documented with links to literature, and about a third of them have at
                        least one member with known structure.</p>

                    <em>NCBI_Conserved_Domains(CD)</em>
                    <p>This database is based on <a href = https://www.ncbi.nlm.nih.gov/Structure/cdd/cdd_help.shtml#CDSource_NCBI_curated
                    target="_blank" rel="noopener"> domains curated by NCBI using 3D-structure information.</a></p>

                    <em>SMART</em>
                    <p><a href = http://smart.embl-heidelberg.de target="_blank" rel="noopener">SMART</a> is a database
                        of curated alignments of genetically mobile domains found in signalling, extracellular and
                        chromatin-associated proteins, predominantly in metazoans. These domains are extensively
                        annotated with respect to phylogenetic distributions, functional class, tertiary structures, and
                        functionally important residues.</p>

                    <em>TIGRFAM</em>
                    <p><a href = http://www.jcvi.org/cgi-bin/tigrfams/index.cgi target="_blank" rel="noopener">TIGRFAMs</a>
                        is a collection of protein families, featuring curated multiple sequence alignments, hidden
                        Markov models (HMMs) and annotation, which provides a tool for identifying functionally related
                        proteins based on sequence homology.</p>

                    <em>PRK</em>
                    <p><a href = http://www.jcvi.org/cgi-bin/tigrfams/index.cgi target="_blank" rel="noopener">
                        PRotein K(c)lusters (PRK)</a> is an NCBI collection of related protein sequences (clusters)
                        consisting of Reference Sequence proteins encoded by complete prokaryotic and chloroplast
                        plasmids and genomes.</p>
                        
                    <em>UniProt-SwissProt-viral70</em>
                    <p>This database comprises profile HMMs for expert curated viral protein sequences from the UniProt database,
                    filtered for a maximum pairwise sequence identity of 70%.</p>    

                    <em>Prokaryotic_proteasome_homologs</em>
                    <p>This database comprises profile HMMs for several newly discovered prokaryotic proteasome homologs.</p>
                    
                    <em>PHROGs</em>
                    <p> This database is based on the <a href = https://phrogs.lmge.uca.fr/ target="_blank" rel="noopener">
                        Prokaryotic Virus Remote Homologous Groups (PHROGs) database</a>.</p>`,
                    },
                    {
                        title: 'Select proteomes',
                        content: `<p>Here you can select databases of HMMs representing each of the proteins of an organism.</p>`,
                    },
                    {
                        title: 'MSA generation method',
                        content: `This option sets the multiple sequence alignment (MSA) generation method used by HHpred. The query MSA
                        could be built with HHblits over Uniprot20 or Uniclust30, or with PSI-BLAST over nr70, which is
                        a version of the NCBI nonredundant and environmental databases reduced to a maximum
                        pairwise sequence identity of 70%.`,
                    },
                    {
                        title: 'MSA generation iterations',
                        content: `This specifies the number of iterations performed by the MSA generation method, to build an
                    alignment from the input sequence or input alignment. The iterations stop in any case when no
                    further sequences are found. If you want to use exactly your input alignment choose 0 here.
                    This will ensure that only predicted secondary structure is added to your input alignment.`,
                    },
                    {
                        title: 'E-value cutoff for MSA generation',
                        content: `All sequence matches ("hits") with an E-value better than this threshold are included in the alignment for
                    the next MSA generation iteration or, in the last iteration, for building the HMM for HHsearch.`,
                    },
                    {
                        title: 'Min seq identity of MSA hits with query (%)',
                        content: `This parameter controls what percentage of residues from a hit of the MSA algorithm needs to be
                    identical to their aligned query residues in order for the hit to be included in the MSA alignment.
                    More precisely, the sequence identity of a hit by the MSA algorithm is defined as the number of
                    residues identical with aligned query residues divided by the length of the MSA hit.`,
                    },
                    {
                        title: 'Min coverage of MSA hits (%)',
                        content: `This parameter controls what percentage of the query residues needs to be covered by the hits of the
                    MSA algorithm in order for the hit to be included into the alignment for the next iteration. More
                    precisely, the coverage of a sequence found by the MSA algorithm is defined as the number of
                    residues aligned with the query divided by the length of the query.`,
                    },
                    {
                        title: 'Secondary structure scoring',
                        content: `If you choose "none", no secondary structure scoring will be used. If you choose any of the other options,
                    the secondary structure of the query will be predicted with PSIPRED and compared to the actual or the predicted
                    secondary structure of the database sequences. If the actual structure of the database sequence is
                    unknown, the PSIPRED-predicted secondary structure will be used. To force HHpred to use predicted
                    instead of actual secondary structure, choose "predicted vs predicted only". Scoring the secondary
                    structure similarity improves sensitivity and also alignment quality significantly. Since the weight
                    of the secondary structure score is small (15% of the amino acid similarity score), the risk
                    for high-scoring non-homologous matches is still small.`,
                    },
                    {
                        title: 'Alignment Mode:Realign with MAC',
                        content: `In local mode, HHpred will use local alignments to score your query sequence with the
                        database sequences. In global mode, it uses global alignments with no end gap penalties
                        (sometimes called 'global-local' alignments), i.e. the query-template alignments extend up to
                        the beginning and end of the query or template sequence. The use of local mode is strongly
                        recommended if you want to know which are your most likely remote homologs. If you have already
                        identified your template with a previous run in local mode, you can use the global mode to get
                        global alignments with an already identified template. HHpred was shown to be significantly more
                        sensitive to detect remote homologs in local than in global mode. The reason is that most remote
                        homologs are really only homologous in a common core that define their superfamily. This core has
                        been adorned by other secondary structure elements in different ways in different families.
                        Also, the statistical evaluation is more error-prone for the global alignment mode, since the
                        negatives score distribution for global alignments does not follow an extreme-value distribution.
                        You may generate global alignments and at the same time score alignments with the more sensitive
                        local Viterbi alignment by selecting the 'local' option in combination with the option
                        'Realign with MAC' set to 'realign' and setting the 'MAC realignment threshold' to 0.0.<br/><br/>
                        After the search with the standard Viterbi algorithm, the 'Realign with MAC' option will cause
                        HHpred to realign all query-template alignments with the more accurate Maximum Accuracy alignment
                        algorithm. Since for technical reasons the scores, E-values, and probabilities from the Viterbi
                        alignment and NOT the MAC alignment are displayed and used for ranking, the MAC realignment
                        option may lead to high-scoring alignments of length 1. This happens when the MAC algorithm
                        cannot find any acceptable alignment path, which is an indication for a non-homologous
                        relationship. When the MAC realignment option is activated, the 'global' and 'local' options now
                        refer to both the Viterbi search stage as well as the MAC realignment stage.`,
                    },
                    {
                        title: 'MAC realignment threshold',
                        content: `Only active when the 'Realign with MAC algorithm' and the 'local' option is active. This parameter
                    controls the MAC alignment algorithm's greediness. More precisely, the MAC algorithm finds the
                    alignment that maximizes the sum of posterior probabilities minus this threshold for each aligned
                    pair. Global alignments are generated with 0, whereas 0.5 will produce quite conservative local
                    alignments with average probabilities for each aligned pair of residues above 50%. Default value is
                    0.3, which produces alignments of roughly the same length as the Viterbi algorithm.`,
                    },
                    {
                        title: 'Max target hits',
                        content: `This parameter controls how many matches will be displayed in the results.`,
                    },
                    {
                        title: 'Min probability in hitlist',
                        content: `Only hits above this cut-off probability will be displayed.`,
                    },
                ],
                references: ` <p>Söding J. (2005) Protein homology detection by HMM-HMM comparison. Bioinformatics 21: 951-960.
        <a href = https://www.ncbi.nlm.nih.gov/pubmed/15531603 target="_blank" rel="noopener">PMID: 15531603</a></p>
        <p>Hildebrand A., Remmert A., Biegert A., Söding J. (2009). Fast and accurate automatic structure prediction
            with HHpred. Proteins 77(Suppl 9):128-32. doi: 10.1002/prot.22499.
            <a href = https://www.ncbi.nlm.nih.gov/pubmed/19626712 target="_blank" rel="noopener">PMID: 19626712</a></p>
        <p>Meier A., Söding J. (2015). Automatic Prediction of Protein 3D Structures by Probabilistic Multi-template
        Homology Modeling. PLoS Comput Biol. 11(10):e1004343. doi: 10.1371/journal.pcbi.1004343.
        <a href = https://www.ncbi.nlm.nih.gov/pubmed/26496371 target="_blank" rel="noopener">PMID: 26496371</a></p>
    `,
            },
        },
    },
};
