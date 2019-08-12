export default {
    header: 'Tools',
    sections: {
        'search': {
            title: 'Search',
        },
        'alignment': {
            title: 'Alignment',
        },
        'seqanal': {
            title: 'Sequence Analysis',
        },
        '2ary': {
            title: '2ary Structure',
        },
        '3ary': {
            title: '3ary Structure',
        },
        'classification': {
            title: 'Classification',
        },
        'utils': {
            title: 'Utils',
        },
    },
    parameters: {
        textArea: {
            pasteExample: 'Paste Example',
            uploadFile: 'Upload File',
            uploadedFile: 'Uploaded File',
            alignTwoSeqToggle: 'Align two sequences or MSAs',
        },
        select: {
            singlePlaceholder: 'Select one',
            multiplePlaceholder: 'Select options',
            maxElementsSelected: 'Max. elements selected',
            maxElementsSelectedHHpred: 'Only 4 databases may be selected at a time!',
        },
        customJobId: {
            placeholder: 'Custom Job ID',
        },
        modellerKey: {
            stored: 'MODELLER-key is stored in your profile.',
        },
        emailNotification: 'E-Mail notification',
        isPublic: {
            true: 'Job is public',
            false: 'Job is private',
        },
        labels: {
            standarddb: 'Select standard database',
            pmin: 'Min. probability in hit list (> 10%)',
            desc: 'No. of target sequences (up to 10000)',
            maxrounds: 'Max. number of iterations',
            evalue: 'E-value',
            output_order: 'Output the alignment in:',
            pcoils_input_mode: 'Input mode',
            matrix: 'Scoring Matrix',
            min_seqid_query: 'Min. seq. identity of MSA hits with query (%)',
            hhblitsdb: 'Select database',
            hhblits_incl_eval: 'E-value inclusion threshold',
            hhsuitedb: 'Select database (PDB_mmCIF70 for modeling)',
            proteomes: 'Proteomes',
            msa_gen_method: 'MSA generation method',
            msa_gen_max_iter: 'Maximal no. of MSA generation steps',
            hhpred_incl_eval: 'E-value incl. threshold for MSA generation',
            min_cov: 'Min. coverage of MSA hits (%)',
            ss_scoring: 'Secondary structure scoring',
            alignmacmode: 'Alignment Mode:Realign with MAC',
            macthreshold: 'MAC realignment threshold',
            hmmerdb: 'Select database',
            max_hhblits_iter: 'MSA enrichment iterations using HHblits',
            patsearchdb: 'Select database',
            grammar: 'Select grammar',
            seqcount: 'Maximum number of sequences to display',
            blast_incl_eval: 'E-value inclusion threshold',
            gap_open: 'Gap open penalty',
            gap_ext_kaln: 'Gap extension penalty',
            gap_term: 'Terminal gap penalty',
            bonusscore: 'Bonus Score',
            mafft_gap_open: 'Gap open penalty',
            offset: 'Offset',
            msa_gen_max_iter_hhrepid: 'Maximal no. of MSA generation steps',
            score_ss: 'Score secondary structure',
            rep_pval_threshold: 'Repeat family P-value threshold',
            self_aln_pval_threshold: 'Self-Alignment P-value threshold',
            merge_iters: 'Merge rounds',
            domain_bound_detection: 'Domain boundary detection',
            matrix_marcoil: 'Matrix',
            transition_probability: 'Transition Probability',
            pcoils_weighting: 'Weighting',
            pcoils_matrix: 'Matrix',
            repper_input_mode: 'Input mode',
            window_size: 'Window size (< sequence length)',
            periodicity_min: 'Periodicity range - Min',
            periodicity_max: 'Periodicity range - Max',
            ftwin_threshold: 'FTwin threshold',
            repwin_threshold: 'REPwin threshold',
            eval_tpr: 'E-value inclusion TPR & SEL',
            invoke_psipred: '% identity cutoff to invoke a new PSIPRED run',
            hhompdb: 'Select HMM databases',
            alignmode: 'Alignment Mode',
            target_psi_db: 'Select database for MSA generation',
            quick_iters: 'Maximal no. of MSA generation steps',
            regkey: 'Enter MODELLER-key (see help pages for details)',
            samcc_helixone: 'Definition for helix 1',
            samcc_helixtwo: 'Definition for helix 2',
            samcc_helixthree: 'Definition for helix 3',
            samcc_helixfour: 'Definition for helix 4',
            samcc_periodicity: 'Periodicity',
            eff_crick_angle: 'Effective Crick angle',
            clans_eval: 'Extract BLAST HSP\'s up to E-values of',
            min_seqid: 'Minimum sequence identity',
            min_aln_cov: 'Minimum alignment coverage',
            clustering_mode: 'Clustering mode',
            matrix_phyml: 'Model of AminoAcid replacement',
            no_replicates: 'Number of replicates',
            inc_nucl: 'Include nucleic acid sequence',
            amino_nucl_rel: 'Amino acids in relation to nucleotides',
            codon_table: 'Select codon usage table',
            inc_amino: 'Include amino acid sequence in output',
            genetic_code: 'Choose a genetic code',
            codon_table_organism: 'Use codon usage table of',
            in_format: 'Input format',
            out_format: 'Output format',
            max_seqid: 'Maximal Sequence Identity (%)',
            min_query_cov: 'Minimal coverage with query (%)',
            num_seqs_extract: 'No. of most dissimilar sequences to extract',
        },
    },
    inputPlaceholder: {
        multiProtNuclSeq: 'Enter up to 4000 protein/nucleotide sequences in FASTA format.',
        regex: 'Enter a PROSITE grammar/regular expression.',
        ali2dMSA: 'Enter a protein multiple sequence alignment with up to 100 sequences in CLUSTAL/FASTA/STOCKHOLM ' +
            'format. Runtime of ~30 mins for N=100 sequences of length L=200; scales as N*L.',
        singleProtSeq: 'Enter a protein sequence in FASTA format.',
        modellerPIR: 'Please note: MODELLER is configured to work with PIR alignments forwarded by HHpred. Run a ' +
            'HHpred search with your query, select the top-scoring templates and click ' +
            'on "Create model using selection". This will generate a PIR file that can be subsequently submitted to ' +
            'MODELLER. To obtain a key for MODELLER go to: http://salilab.org/modeller/registration.shtml.',
        pdbCoordinates: 'Enter PDB coordinates of a four-helical bundle.',
        phymlMSA: 'Enter a protein multiple sequence alignment with up to 100 sequences in ' +
            'CLUSTAL/FASTA/STOCKHOLM format.',
        dnaSeq: 'Enter a DNA sequence in FASTA format.',
        hhMSA: 'Enter a protein sequence/multiple sequence alignment in A3M/CLUSTAL/FASTA/STOCKHOLM format.',
        psiMSA: 'Enter a protein sequence/multiple sequence alignment in CLUSTAL/FASTA/STOCKHOLM format.',
        protMSA: 'Enter a protein multiple sequence alignment in CLUSTAL/FASTA/STOCKHOLM format.',
        a3mMSA: 'Enter a multiple sequence alignment in A3M/CLUSTAL/FASTA/STOCKHOLM format.',
        protIdentifiers: 'Enter a newline separated list of identifiers and choose the corresponding database.',
        protFasta: 'Enter protein sequences in FASTA format.',
        protFastaClans: 'Enter protein sequences with up to 10000 sequences in FASTA format.',
        protFastaMmseqs: 'Enter up to 20000 protein sequences in FASTA format.',
    },
    validation: {
        invalidCharacters: 'Invalid Characters.',
        autoTransformedToFasta: '{detected} detected. Auto-transformed to FASTA.',
        nucleotideError: 'Input contains nucleotide sequence(s). Expecting protein sequence(s).',
        emptyHeader: 'Empty header.',
        maxSeqNumber: 'Input contains more than {limit} sequences.',
        minSeqNumber: 'Input contains less than {limit} sequences.',
        maxSeqLength: 'Maximum allowed sequence length is {limit}.',
        minSeqLength: 'Minimum required sequence length is {limit}.',
        onlyDashes: 'Sequence contains only dots/dashes.',
        sameLength: 'Sequences should have the same length.',
        maxLength: 'Input contains over {limit} characters.',
        uniqueIDs: 'Identifiers are not unique.',
        invalidSequenceType: 'Invalid sequence type. Expected {expected}.',
        invalidSequenceFormat: 'Invalid sequence format. Expected {expected}.',
        valid: '{type} {format}',
        validRegex: 'Valid input.',
        invalidWhiteSpace: 'White spaces are not allowed!',
        maxRegexLength: 'Maximum allowed length is 200 characters.',
        invalidPDB: 'Invalid PDB input. Expected at least 21 ATOM records.',
        validPDB: 'Valid PDB input.',
        invalidAccessionID: 'Invalid input. Expecting accession ID(s).',
        validAccessionID: 'Valid input.',
        emptySequences: 'Empty sequences are not allowed.',
    },
    reformat: {
        detectedFormat: 'Found format: <b>{format}</b>',
        selectOutputFormat: 'Select Output Format',
        forwardTo: 'Forward to',
        download: 'Download',
        copyToClipboard: 'Copy to clipboard',
        copySuccess: 'Successfully copied',
        copyFailure: 'Unable to copy',
        viewAlignment: 'View Alignment',
    },
    alignmentViewer: {
        visualization: 'Visualization',
        viewAlignment: 'View Alignment',
    },
    citations: {
        intro: 'If you use {tool} on our Toolkit for your research, please cite as appropriate:',
        clustalo: 'Fast, scalable generation of high-quality protein multiple sequence ' +
            'alignments using Clustal Omega.<br>Sievers F et al. <a ' +
            'href="http://msb.embopress.org/content/7/1/539" ' +
            'target="_blank" rel="noopener">Mol Syst Biol. 2011 Oct 11;7:539</a>.',
        kalign: 'Kalign2: high-performance multiple alignment of protein and nucleotide ' +
            'sequences allowing external features.<br>Lassmann T, Frings O, Sonnhammer EL. ' +
            '<a href="https://academic.oup.com/nar/article-lookup/doi/10.1093/nar/gkn1006" ' +
            'target="_blank" rel="noopener">Nucleic Acids Res. 2009 Feb;37(3):858-65</a>.',
        mafft: 'MAFFT multiple sequence alignment software version 7: improvements in performance ' +
            'and usability.<br>Katoh K, Standley DM. <a ' +
            'href="https://academic.oup.com/mbe/article-lookup/doi/10.1093/molbev/mst010" ' +
            'target="_blank" rel="noopener">Mol Biol Evol. 2013 Apr;30(4):772-80</a>.',
        msaprobs: 'Multiple protein sequence alignment with MSAProbs.<br>' +
            'Liu Y, Schmidt B. <a href="https://link.springer.com/protocol/10.1007%2F978-1-62703-646-7_14" ' +
            'target="_blank" rel="noopener">Methods Mol Biol. 2014;1079:211-8</a>.',
        muscle: 'MUSCLE: multiple sequence alignment with high accuracy and high throughput.<br>' +
            'Edgar RC. <a href="https://academic.oup.com/nar/article-lookup/doi/10.1093/nar/gkh340" ' +
            'target="_blank" rel="noopener">Nucleic Acids Res. 2004 Mar 19;32(5):1792-7</a>.',
        tcoffee: 'A novel method for fast and accurate multiple sequence alignment.<br>' +
            'Notredame C, Higgins DG, Heringa J. <a href="https://www.ncbi.nlm.nih.gov/pubmed/10964570" ' +
            'target="_blank" rel="noopener">J Mol Biol. 2000 Sep 8;302(1):205-17</a>.',
        clans: 'CLANS: a Java application for visualizing protein families based on pairwise similarity.<br>' +
            'Frickey T, Lupas A.<a ' +
            'href="https://academic.oup.com/bioinformatics/article-lookup/doi/10.1093/bioinformatics/bth444" ' +
            'target="_blank" rel="noopener"> Bioinformatics. 2004 Dec 12;20(18):3702-4.</a><br><br>' +
            'BLAST+: architecture and applications.<br>Camacho C, Coulouris G, Avagyan V, Ma N, ' +
            'Papadopoulos J, Bealer K, Madden TL. ' +
            '<a href="https://bmcbioinformatics.biomedcentral.com/articles/10.1186/1471-2105-10-421" ' +
            'target="_blank" rel="noopener">BMC Bioinformatics. 2009 Dec 15;10:421</a>.',
        quick2d: 'PSIPRED: Protein secondary structure prediction based on position-specific scoring matrices.<br>\n' +
            'Jones DT.<a ' +
            'href="http://www.sciencedirect.com/science/article/pii/S0022283699930917?via%3Dihub" target="_blank" ' +
            'rel="noopener"> J Mol Biol. 1999 Sep 17;292(2):195-202</a>.<br><br>' +
            'SPIDER3: Capturing non-local interactions by long short-term memory bidirectional recurrent neural ' +
            'networks for improving prediction of protein secondary structure, backbone angles, contact numbers and ' +
            'solvent accessibility.<br>Heffernan R, Yang Y, Paliwal K, Zhou Y.<a ' +
            'href="https://academic.oup.com/bioinformatics/article/33/18/2842/3738544" target="_blank" ' +
            'rel="noopener"> Bioinformatics. 2017 Sep 15;33(18):2842-2849</a>.<br><br>' +
            'PSSPred: A comparative assessment and analysis of 20 representative sequence alignment methods for ' +
            'protein structure prediction.<br>Yan R, Xu D, Yang J, Walker S, Zhang Y.<a ' +
            'href="https://www.nature.com/articles/srep02619" target="_blank" rel="noopener"> ' +
            'Sci Rep. 2013;3:2619</a>.<br><br>' +
            'DeepCNF-SS: Protein Secondary Structure Prediction Using Deep Convolutional Neural Fields.<br>' +
            'Wang S, Peng J, Ma J, Xu J.<a href="https://www.nature.com/articles/srep18962" target="_blank" ' +
            'rel="noopener"> Sci Rep. 2016 Jan 11;6:18962</a>.<br><br>' +
            'NetSurfP2: NetSurfP-2.0: Improved prediction of protein structural features by integrated deep ' +
            'learning.<br>Klausen et al.<a href="https://onlinelibrary.wiley.com/doi/full/10.1002/prot.25674" ' +
            'target="_blank" rel="noopener"> Proteins. 2019 Jun;87(6):520-527</a>.<br><br>' +
            'COILS: Predicting coiled coils from protein sequences.<br>Lupas A, Van Dyke M, Stock J.' +
            '<a href="http://science.sciencemag.org/content/252/5009/1162" target="_blank" rel="noopener"> ' +
            'Science. 1991 May 24;252(5009):1162-4</a>.<br><br>' +
            'PCOILS: Comparative analysis of coiled-coil prediction methods.<br>Gruber M, Söding J, Lupas AN.' +
            '<a href="http://www.sciencedirect.com/science/article/pii/S1047847706000815?via%3Dihub" target="_blank" ' +
            'rel="noopener"> J Struct Biol. 2006 Aug;155(2):140-5</a>.<br><br>' +
            'MARCOIL: An HMM model for coiled-coil domains and a comparison with PSSM-based predictions.<br>' +
            'Delorenzi M, Speed T.<a href="https://www.ncbi.nlm.nih.gov/pubmed/12016059?dopt=Abstract" ' +
            'target="_blank" rel="noopener"> Bioinformatics. 2002 Apr;18(4):617-25</a>.<br><br>' +
            'TMHMM: Predicting transmembrane protein topology with a hidden Markov model: application to complete ' +
            'genomes.<br>Krogh A, Larsson B, von Heijne G, Sonnhammer EL.<a ' +
            'href="http://www.sciencedirect.com/science/article/pii/S0022283600943158?via%3Dihub" target="_blank" ' +
            'rel="noopener"> J Mol Biol. 2001 Jan 19;305(3):567-80</a>.<br><br>' +
            'Phobius: A combined transmembrane topology and signal peptide prediction method.<br>Käll L, Krogh A, ' +
            'Sonnhammer EL.<a href="http://www.sciencedirect.com/science/article/pii/S0022283604002943?via%3Dihub" ' +
            'target="_blank" rel="noopener"> J Mol Biol. 2004 May 14;338(5):1027-36</a>.<br><br>' +
            'PolyPhobius: An HMM posterior decoder for sequence feature prediction that includes homology ' +
            'information.<br>Käll L, Krogh A, Sonnhammer EL.<a ' +
            'href="https://academic.oup.com/bioinformatics/article-lookup/doi/10.1093/bioinformatics/bti1014" ' +
            'target="_blank" rel="noopener"> Bioinformatics. 2005 Jun;21</a>.<br><br>' +
            'DISOPRED3: precise disordered region predictions with annotated protein-binding activity.<br>Jones DT, ' +
            'Cozzetto D.<a href="https://academic.oup.com/bioinformatics/article/31/6/857/215129" target="_blank" ' +
            'rel="noopener"> Bioinformatics. 2015 Mar 15;31(6):857-63</a>.<br><br>' +
            'SPOT-Disorder: Improving protein disorder prediction by deep bidirectional long short-term memory ' +
            'recurrent neural networks.<br>Hanson J, Yang Y, Paliwal K, Zhou Y.<a ' +
            'href="https://academic.oup.com/bioinformatics/article-lookup/doi/10.1093/bioinformatics/btw678" ' +
            'target="_blank" rel="noopener"> Bioinformatics. 2017 Mar 1;33(5):685-692</a>.<br><br>' +
            'IUPred: The pairwise energy content estimated from amino acid composition discriminates between folded ' +
            'and intrinsically unstructured proteins.<br>Dosztányi Z, Csizmók V, Tompa P, Simon I.<a ' +
            'href="http://www.sciencedirect.com/science/article/pii/S0022283605001294?via%3Dihub" target="_blank" ' +
            'rel="noopener"> J Mol Biol. 2005 Apr 8;347(4):827-39</a>.<br><br>' +
            'SignalP: SignalP 5.0 improves signal peptide predictions using deep neural networks.<br>' +
            'Almagro Armenteros et al.<a href="https://www.nature.com/articles/s41587-019-0036-z" target="_blank" ' +
            'rel="noopener"> Nat Biotechnol. 2019 Apr;37(4):420-423</a>.<br><br>',
        ancescon: 'Reconstruction of ancestral protein sequences and its applications.<br>Cai W, Pei J, Grishin NV. ' +
            '<a href="https://bmcevolbiol.biomedcentral.com/articles/10.1186/1471-2148-4-33" ' +
            'target="_blank" rel="noopener">BMC Evol Biol. 2004 Sep 17;4:33</a>.',
        phyml: 'New algorithms and methods to estimate maximum-likelihood phylogenies: assessing the performance ' +
            'of PhyML 3.0.<br>Guindon S, Dufayard JF, Lefort V, Anisimova M, Hordijk W, Gascuel O. <a ' +
            'href="https://academic.oup.com/sysbio/article-lookup/doi/10.1093/sysbio/syq010" target="_blank" ' +
            'rel="noopener">Syst Biol. 2010 May;59(3):307-21</a>.',
        ali2d: 'PSIPRED: Protein secondary structure prediction based on position-specific scoring matrices.<br>' +
            'Jones DT. <a href="http://www.sciencedirect.com/science/article/pii/S0022283699930917?via%3Dihub" ' +
            'target="_blank" rel="noopener">J Mol Biol. 1999 Sep 17;292(2):195-202</a>.<br><br>' +
            'MEMSAT: Transmembrane protein topology prediction using support vector machines.<br>' +
            'Nugent T, Jones DT.<a ' +
            'href="https://bmcbioinformatics.biomedcentral.com/articles/10.1186/1471-2105-10-159" target="_blank" ' +
            'rel="noopener"> BMC Bioinformatics. 2009 May 26;10:159.</a>',
        mmseqs2: 'MMseqs2 enables sensitive protein sequence searching for the analysis of massive data sets.<br>' +
            'Steinegger,M, Söding J. <a href="https://www.nature.com/articles/nbt.3988" target="_blank" ' +
            'rel="noopener">Nat Biotechnol. 2017 Nov;35(11):1026-1028</a>.',
        sixframe: '',
        backtrans: '',
        patsearch: '',
        samcc: 'Measuring the conformational space of square four-helical bundles with the program samCC.<br>' +
            'Dunin-Horkawicz S, Lupas AN. <a href="http://www.sciencedirect.com/science/article/pii/S1047847710000353' +
            '" target="_blank" rel="noopener">J Struct Biol. 2010 May;170(2):226-35</a>.',
        modeller: 'Comparative Protein Structure Modeling Using MODELLER.<br>Webb B, Sali A. <a ' +
            'href="http://onlinelibrary.wiley.com/doi/10.1002/cpps.20/abstract" target="_blank" rel="noopener">' +
            'Curr Protoc Protein Sci. 2016 Nov 1;86:2.9.1-2.9.37</a>.',
        tprpred: 'TPRpred: a tool for prediction of TPR-, PPR- and SEL1-like repeats from protein sequences.<br>' +
            'Karpenahalli MR, Lupas AN, Söding J.<a ' +
            'href="https://bmcbioinformatics.biomedcentral.com/articles/10.1186/1471-2105-8-2" target="_blank" ' +
            'rel="noopener"> BMC Bioinformatics. 2007 Jan 3;8:2</a>.',
        hhomp: 'HHomp--prediction and classification of outer membrane proteins.<br>Remmert M, Linke D, Lupas AN, ' +
            'Söding J. <a href="https://academic.oup.com/nar/article-lookup/doi/10.1093/nar/gkp325" ' +
            'target="_blank" rel="noopener">Nucleic Acids Res. 2009 Jul;37:W446-51</a>.',
        aln2plot: '',
        deepcoil: 'DeepCoil - a fast and accurate prediction of coiled-coil domains in protein sequences.<br>' +
            'Ludwiczak J, Winski A, Szczepaniak K, Alva V, Dunin-Horkawicz S.<a ' +
            'href="https://www.ncbi.nlm.nih.gov/pubmed/?term=30601942" target="_blank" rel="noopener">\n' +
            'Bioinformatics. 2019 Jan 2</a>.',
        marcoil: 'MARCOIL: An HMM model for coiled-coil domains and a comparison with PSSM-based predictions.<br>' +
            'Delorenzi M, Speed T.<a href="https://www.ncbi.nlm.nih.gov/pubmed/12016059" target="_blank" ' +
            'rel="noopener"> Bioinformatics. 2002 Apr;18(4):617-25</a>.',
        pcoils: 'PSIPRED: Protein secondary structure prediction based on position-specific scoring matrices.<br>' +
            'Jones DT.<a href="http://www.sciencedirect.com/science/article/pii/S0022283699930917?via%3Dihub" ' +
            'target="_blank" rel="noopener"> J Mol Biol. 1999 Sep 17;292(2):195-202</a>.<br><br>' +
            'PCOILS: Comparative analysis of coiled-coil prediction methods.<br>Gruber M, Söding J, Lupas AN.' +
            '<a href="http://www.sciencedirect.com/science/article/pii/S1047847706000815?via%3Dihub" target="_blank" ' +
            'rel="noopener"> J Struct Biol. 2006 Aug;155(2):140-5</a>.',
        repper: 'REPPER--repeats and their periodicities in fibrous proteins<br>Gruber M, Söding J, Lupas AN.<a ' +
            'href="https://academic.oup.com/nar/article-lookup/doi/10.1093/nar/gki405" target="_blank" ' +
            'rel="noopener"> Nucleic Acids Res. 2005 Jul 1;33(Web Server issue):W239-43</a>.<br><br>' +
            'PSIPRED: Protein secondary structure prediction based on position-specific scoring matrices.<br>' +
            'Jones DT.<a href="http://www.sciencedirect.com/science/article/pii/S0022283699930917?via%3Dihub" ' +
            'target="_blank" rel="noopener"> J Mol Biol. 1999 Sep 17;292(2):195-202</a>.<br><br>' +
            'PCOILS: Comparative analysis of coiled-coil prediction methods.<br>Gruber M, Söding J, Lupas AN.' +
            '<a href="http://www.sciencedirect.com/science/article/pii/S1047847706000815?via%3Dihub" target="_blank" ' +
            'rel="noopener"> J Struct Biol. 2006 Aug;155(2):140-5</a>.',
        formatseq: '',
        hhfilter: '',
        retseq: '',
        seq2id: '',
        hhrepid: 'De novo identification of highly diverged protein repeats by probabilistic consistency.<br>' +
            'Biegert A, Söding J. <a ' +
            'href="https://academic.oup.com/bioinformatics/article-lookup/doi/10.1093/bioinformatics/btn039" ' +
            'target="_blank" rel="noopener">Bioinformatics. 2008 Mar 15;24(6):807-14</a>.',
        hhblits: 'HHblits: lightning-fast iterative protein sequence searching by HMM-HMM alignment.<br>Remmert M, ' +
            'Biegert A, Hauser A, Söding J.<a href="https://www.nature.com/nmeth/journal/v9/n2/full/nmeth.1818.html" ' +
            'target="_blank" rel="noopener"> Nat Methods. 2011 Dec 25;9(2):173-5</a>.',
        hmmer: 'Accelerated Profile HMM Searches.<br>Eddy SR.<a ' +
            'href="http://journals.plos.org/ploscompbiol/article?id=10.1371/journal.pcbi.1002195" target="_blank" ' +
            'rel="noopener"> PLoS Comput Biol. 2011 Oct;7(10):e1002195</a>.',
        psiblast: 'BLAST+: architecture and applications.<br>Camacho C, Coulouris G, Avagyan V, Ma N, ' +
            'Papadopoulos J, Bealer K, Madden TL.<a ' +
            'href="https://bmcbioinformatics.biomedcentral.com/articles/10.1186/1471-2105-10-421" target="_blank" ' +
            'rel="noopener"> BMC Bioinformatics. 2009 Dec 15;10:421</a>.',
        hhpred: 'Protein homology detection by HMM-HMM comparison.<br>Söding J.<a ' +
            'href="https://academic.oup.com/bioinformatics/article-lookup/doi/10.1093/bioinformatics/bti125" ' +
            'target="_blank" rel="noopener"> Bioinformatics. 2005 Apr 1;21(7):951-60</a>.<br><br>' +
            'Fast and accurate automatic structure prediction with HHpred.<br>Hildebrand A, Remmert M, Biegert A, ' +
            'Söding J.<a href="https://onlinelibrary.wiley.com/doi/full/10.1002/prot.22499" target="_blank" ' +
            'rel="noopener"> Proteins. 2009;77 Suppl 9:128-32</a>.<br><br>' +
            'Automatic Prediction of Protein 3D Structures by Probabilistic Multi-template Homology Modeling.<br>' +
            'Meier A, Söding J.<a ' +
            'href="http://journals.plos.org/ploscompbiol/article?id=10.1371/journal.pcbi.1004343" target="_blank" ' +
            'rel="noopener"> PLoS Comput Biol. 2015 Oct 23;11(10):e1004343</a>.',
        hhpred_manual: '',
    },
};
