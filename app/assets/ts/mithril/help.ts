import Component = Mithril.Component;

let accordion : ElementConfig,
    accordionContent : any,
    accordionItem : ElementConfig,
    exampleContent : any,
    helpContent : any,
    helpModalReveal : ElementConfig,
    helpModalTabs : ElementConfig,
    parameterContent : any;

helpModalReveal = function(elem : any, isInit : boolean) : any {
    if (!isInit) {
        elem.setAttribute("data-reveal", "data-reveal");
        return $(elem).foundation();
    }
};

helpModalTabs = function(elem : any, isInit : boolean) : any {
    if (!isInit) {
        return $(elem).tabs();
    }
};

parameterContent = {
    "Alignment": "Enter single protein sequence or protein sequence alignment and choose its format from the dropdown menu.",
    "Database": "The selected databases are used by BLAST to perform the search. <br /> - <b>nr:</b> the non-redundant sequence database at the NCBI, updated weekly. (See NCBI's BLAST tutorial). <br /> - <b>env:</b> NCBI's env database with environnmental sequences, usually from unknown organisms. <br /> - <b>nre:</b> nr+env database. Updated weekly. <br /> - <b>nr90, nr70, env90 etc.:</b> representative sequences, filtered for a maximum pairwise sequence identity of ~90% or 70% with kClust. Updated bimonthly. <br /> - <b>nr_euk, nr_bac, nr_arc, nr_pro, nr_vir:</b> eukaryotic, bacterial, archaeal, prokaryotic, and viral sequences from the non-redundant database nr. Updated weekly. <br /> - <b>uniprot_sprot:</b> http://www.expasy.org/sprot/ Updated weekly. <br /> - <b>uniprot_trembl:</b> http://www.ebi.ac.uk/swissprot/ Updated weekly. <br /> - <b>pdb_nr: Sequences</b> from proteins whose structures have been deposited in the Protein Data Bank PDB. This database uses the more complete SEQRES records of the PDB files (not the ATOM records that contain the atomic coordinates). This is the database used to construct the HHpred PDB alignment database. Updated weekly from the PDB. <br /> ",
    "Matrix": "Specify an alternate scoring matrix for PSIBLAST.",
    "Number of Iterations": "Specify the maximum number of rounds of search. After each successive round the search alignment is updated. ",
    "E-Value": "The statistical significance threshold for reporting matches against database sequences; the default value is 10, meaning that 10 matches are expected to be found merely by chance, according to the stochastic model of Karlin and Altschul (1990). If the statistical significance ascribed to a match is greater than the e-value threshold, the match will not be reported. Lower e-value thresholds are more stringent, leading to fewer chance matches being reported. Increasing the threshold shows less stringent matches. Fractional values are acceptable.",
    "E-value inclusion threshold": "The statistical significance threshold for including a sequence in the model used by PSI-BLAST to create the PSSM on the next iteration.",
    "Filter (low complexity)": "Mask off segments of the query sequence that have low compositional complexity, as determined by the SEG program of Wootton & Federhen (Computers and Chemistry, 1993) or, for BLASTN, by the DUST program of Tatusov and Lipman (in preparation). Filtering can eliminate statistically significant but biologically uninteresting reports from the blast output (e.g., hits against common acidic-, basic- or proline-rich regions), leaving the more biologically interesting regions of the query sequence available for specific matching against database sequences. Filtering is only applied to the query sequence (or its translation products), not to database sequences. Default filtering is DUST for BLASTN, SEG for other programs. It is not unusual for nothing at all to be masked by SEG, when applied to sequences in SWISS-PROT, so filtering should not be expected to always yield an effect. Furthermore, in some cases, sequences are masked in their entirety, indicating that the statistical significance of any matches reported against the unfiltered query sequence should be suspect.",
    "Compute Smith-Waterman alignment": "Compute locally optimal Smith-Waterman alignments.",
    "Use nr70 for all but last iteration": " If this option is selected the program will use the nr70-database for all but last round. In the last round, the database you selected in the database section will be used.",
    "Alignments and descriptions": "Restricts the number of short descriptions of matching sequences reported to the number specified; default limit is 100 descriptions. Restricts also database sequences to the number specified for which high-scoring segment pairs (HSPs) are reported. If more database sequences than this happen to satisfy the statistical significance threshold for reporting, only the matches ascribed the greatest statistical significance are reported."
};

exampleContent = {
    "psiblast": ["Search with an amino acid sequence against protein databases for locally similar sequences.<br/>Similar to Protein BLAST+ but more sensitive. PSI-BLAST+ first performs a BLAST+ search and builds an alignment\nfrom the best local hits. This alignment is then used as a query for the next round of search.\nAfter each successive round the search alignment is updated.", ["Alignment", "Database", "Matrix", "Number of Iterations", "E-Value", "E-value inclusion threshold", "Filter (low complexity)", "Compute Smith-Waterman alignment", "Use nr70 for all but last iteration", "Alignments and descriptions"], "At the top of the results page is a \"View alignment\"-button, that shows the multiple alignment of all hits.<br/>Then there is a list with brief information about all hits that were found.<br/>They are listed together with their scores and e-values.<br/>There you can select the ones, you want to work with. Below this you can find more information.<br/>There are all pairwise alignments and there is also additional information about the method,<br/>the identities and the positives. There you also have the possibility to select the hits, you want to work with.<br/>At the bottom you can find more information about the database and the matrix.<br/>The \"View alignment\"-button shows the multiple alignment of all sequences that was found by PSI-BLAST. The different colours illustrate the identities between the different amino acids. This helps you to decide, whether the found sequences may be really homologue", "Alva V., Nam SZ., Söding J., Lupas AN. (2016) <b>The MPI bioinformatics Toolkit as an integrative platform for advanced protein sequence and structure analysis.</b> Nucleic Acids Res. pii: gkw348. [Epub ahead of print] <a href=https://www.ncbi.nlm.nih.gov/pubmed/27131380>PMID: 27131380</a><br/><br/>Altschul S., Madden T., Schaffer A., Zhang J., Miller W., Lipman D. (1997) <b>Gapped BLAST and PSI-BLAST: a new generation of protein database search programs.</b> Nucl Acids Res 25(17):3389-3402. <a href = https://www.ncbi.nlm.nih.gov/pmc/articles/PMC146917/>PMCID: PMC146917</a><br/><br/>Altschul SF., Gish W., Miller W., Myers EW., Lipman DJ. (1990) <b>Basic local alignment search tool.</b> J Mol Biol. 215:403-410. <a href = https://www.ncbi.nlm.nih.gov/pubmed/2231712>PMID: 2231712</a><br/><br/>Camacho C., Coulouris G., Avagyan V., Ma N., Papadopoulos J., Bealer K., Madden TL. (2008) <b>BLAST+: architecture and applications.</b> BMC Bioinformatics 10:421. <a href = https://www.ncbi.nlm.nih.gov/pubmed/20003500>PMID: 20003500</a>"],
    "hhblits": ["", [], "", "Alva V., Nam SZ., Söding J., Lupas AN. (2016) <b>The MPI bioinformatics Toolkit as an integrative platform for advanced protein sequence and structure analysis.</b> Nucleic Acids Res. pii: gkw348. [Epub ahead of print] <a href=https://www.ncbi.nlm.nih.gov/pubmed/27131380>PMID: 27131380</a><br/><br/>Remmert M., Biegert A., Hauser A., Söding J. (2011) <b>HHblits: Lightning-fast iterative protein sequence searching by HMM-HMM alignment.</b> Nat Methods. 9(2):173-5. doi: 10.1038/nmeth.1818. <a href=https://www.ncbi.nlm.nih.gov/pubmed/22198341>PMID: 22198341</a>"],
    "hhpred": ["", [], "", "Alva V., Nam SZ., Söding J., Lupas AN. (2016) <b>The MPI bioinformatics Toolkit as an integrative platform for advanced protein sequence and structure analysis.</b> Nucleic Acids Res. pii: gkw348. [Epub ahead of print] <a href=https://www.ncbi.nlm.nih.gov/pubmed/27131380>PMID: 27131380</a><br/><br/>Söding J., Biegert A., Lupas AN. (2005) <b>The HHpred interactive server for protein homology detection and structure prediction.</b> Nucleic Acids Res 33 (Web Server issue), W244-W248. <a href = https://www.ncbi.nlm.nih.gov/pubmed/15980461>PMID: 15980461</a><br/><br/>Söding J. (2005) <b>Protein homology detection by HMM-HMM comparison.</b> Bioinformatics 21: 951-960. <a href = https://www.ncbi.nlm.nih.gov/pubmed/15531603>PMID: 15531603</a><br/><br/>Hildebrand A., Remmert A., Biegert A., Söding J. (2009) <b>Fast and accurate automatic structure prediction with HHpred.</b> Proteins 77(Suppl 9):128-32. doi: 10.1002/prot.22499. <a href = https://www.ncbi.nlm.nih.gov/pubmed/19626712>PMID: 19626712</a><br/><br/>Meier A., Söding J. (2015) <b>Automatic Prediction of Protein 3D Structures by Probabilistic Multi-template Homology Modeling.</b> PLoS Comput Biol. 11(10):e1004343. doi: 10.1371/journal.pcbi.1004343. <a href = https://www.ncbi.nlm.nih.gov/pubmed/26496371>PMID: 26496371</a><br/><br/>Remmert M., Biegert A., Hauser A., Söding J. (2011) <b>HHblits: Lightning-fast iterative protein sequence searching by HMM-HMM alignment.</b> Nat Methods. 9(2):173-5. doi: 10.1038/nmeth.1818. <a href=https://www.ncbi.nlm.nih.gov/pubmed/22198341>PMID: 22198341</a>"],
    "hmmer": ["", [], "", "Alva V., Nam SZ., Söding J., Lupas AN. (2016) <b>The MPI bioinformatics Toolkit as an integrative platform for advanced protein sequence and structure analysis.</b> Nucleic Acids Res. pii: gkw348. [Epub ahead of print] <a href=https://www.ncbi.nlm.nih.gov/pubmed/27131380>PMID: 27131380</a><br/><br/>Finn RD., Clements J., Eddy SR. (2011) <b>HMMER web server: interactive sequence similarity searching.</b> Nucleic Acids Res. 39(Web Server issue): W29–W37. <a href = https://www.ncbi.nlm.nih.gov/pmc/articles/PMC3125773/>PMCID: PMC3125773</a>"],
    "patsearch": ["", [], "", "Alva V., Nam SZ., Söding J., Lupas AN. (2016) <b>The MPI bioinformatics Toolkit as an integrative platform for advanced protein sequence and structure analysis.</b> Nucleic Acids Res. pii: gkw348. [Epub ahead of print] <a href=https://www.ncbi.nlm.nih.gov/pubmed/27131380>PMID: 27131380</a>"],
    "clustalo": ["", [], "", "Alva V., Nam SZ., Söding J., Lupas AN. (2016) <b>The MPI bioinformatics Toolkit as an integrative platform for advanced protein sequence and structure analysis.</b> Nucleic Acids Res. pii: gkw348. [Epub ahead of print] <a href=https://www.ncbi.nlm.nih.gov/pubmed/27131380>PMID: 27131380</a><br/><br/>Sievers F., Wilm A., Dineen D., Gibson TJ., Karplus K., Li W., Lopez R., McWilliam H., Remmert M., Söding J., Thompson JD., Higgins DG. (2011) <b>Fast, scalable generation of high-quality protein multiple sequence alignments using Clustal Omega.</b> Mol Syst Biol 7:539. <a href = https://www.ncbi.nlm.nih.gov/pubmed/21988835>PMID: 21988835</a>"],
    "kalign": ["", [], "", "Alva V., Nam SZ., Söding J., Lupas AN. (2016) <b>The MPI bioinformatics Toolkit as an integrative platform for advanced protein sequence and structure analysis.</b> Nucleic Acids Res. pii: gkw348. [Epub ahead of print] <a href=https://www.ncbi.nlm.nih.gov/pubmed/27131380>PMID: 27131380</a><br/><br/>Lassmann T., Sonnhammer EL. (2005) <b>Kalign--an accurate and fast multiple sequence alignment algorithm.</b> BMC Bioinformatics. 6:298.<a href = https://www.ncbi.nlm.nih.gov/pubmed/16343337>PMID: 16343337</a>"],
    "mafft": ["", [], "", "Alva V., Nam SZ., Söding J., Lupas AN. (2016) <b>The MPI bioinformatics Toolkit as an integrative platform for advanced protein sequence and structure analysis.</b> Nucleic Acids Res. pii: gkw348. [Epub ahead of print] <a href=https://www.ncbi.nlm.nih.gov/pubmed/27131380>PMID: 27131380</a><br/><br/>Katoh K., Misawa K., Kuma K., Miyata T. (2002) <b>MAFFT: a novel method for rapid multiple sequence alignment based on fast Fourier transform.</b> Nucleic Acid Res 30(14):3059-3066. <a href = https://www.ncbi.nlm.nih.gov/pubmed/12136088>PMID: 12136088</a>"],
    "msaprobs": ["", [], "", "Alva V., Nam SZ., Söding J., Lupas AN. (2016) <b>The MPI bioinformatics Toolkit as an integrative platform for advanced protein sequence and structure analysis.</b> Nucleic Acids Res. pii: gkw348. [Epub ahead of print] <a href=https://www.ncbi.nlm.nih.gov/pubmed/27131380>PMID: 27131380</a><br/><br/>Liu Y., Schmidt B., Maskell DL. (2010) <b>MSAProbs: multiple sequence alignment based on pair hidden Markov models and partition function posterior probabilities.</b> Bioinformatics 26(16): 1958-64. <a href = https://www.ncbi.nlm.nih.gov/pubmed/20576627>PMID: 20576627</a>"],
    "muscle": ["", [], "", "Alva V., Nam SZ., Söding J., Lupas AN. (2016) <b>The MPI bioinformatics Toolkit as an integrative platform for advanced protein sequence and structure analysis.</b> Nucleic Acids Res. pii: gkw348. [Epub ahead of print] <a href=https://www.ncbi.nlm.nih.gov/pubmed/27131380>PMID: 27131380</a><br/><br/>Edgar RC. (2004) <b>MUSCLE: multiple sequence alignment with high accuracy and high throughput.</b> Nucl Acid Res 32(5):1792-1797. <a href = https://www.ncbi.nlm.nih.gov/pubmed/15034147>PMID: 15034147</a> "],
    "tcoffee": ["", [], "", "Alva V., Nam SZ., Söding J., Lupas AN. (2016) <b>The MPI bioinformatics Toolkit as an integrative platform for advanced protein sequence and structure analysis.</b> Nucleic Acids Res. pii: gkw348. [Epub ahead of print] <a href=https://www.ncbi.nlm.nih.gov/pubmed/27131380>PMID: 27131380</a><br/><br/>Magis C., Taly JF., Bussotti G., Chang JM., Di Tommaso P., Erb I., Espinosa-Carrasco J., Notredame C. (2014) <b>T-Coffee: Tree-based consistency objective function for alignment evaluation. </b>Methods Mol Biol 1079:117-129. <a href = https://www.ncbi.nlm.nih.gov/pubmed/24170398>PMID: 24170398</a>"],
    "aln2plot": ["", [], "", "Alva V., Nam SZ., Söding J., Lupas AN. (2016) <b>The MPI bioinformatics Toolkit as an integrative platform for advanced protein sequence and structure analysis.</b> Nucleic Acids Res. pii: gkw348. [Epub ahead of print] <a href=https://www.ncbi.nlm.nih.gov/pubmed/27131380>PMID: 27131380</a>"],
    "frpred": ["", [], "", "Alva V., Nam SZ., Söding J., Lupas AN. (2016) <b>The MPI bioinformatics Toolkit as an integrative platform for advanced protein sequence and structure analysis.</b> Nucleic Acids Res. pii: gkw348. [Epub ahead of print] <a href=https://www.ncbi.nlm.nih.gov/pubmed/27131380>PMID: 27131380</a><br/><br/>Fischer JD., Mayer CE., Söding J. (2008) <b>Prediction of Protein Functional Residues From Sequence by Probability Density Estimation. </b>Bioinformatics 24:613-620. <a href = https://www.ncbi.nlm.nih.gov/pubmed/18174181>PMID: 18174181</a><br/><br/>Mihalek I., Res I., Lichtarge O. (2004) <b>A Family of Evolution-Entropy Hybrid Methods for Ranking Protein Residues by Importance. </b>J Mol Biol 336:1265-1282. <a href = https://www.ncbi.nlm.nih.gov/pubmed/15037084>PMID: 15037084</a><br/><br/>Hannenhalli SS., Russel, RB. (2000) <b>Analysis and Prediction of Functional Sub-types from Protein Sequence Alignments. </b>J Mol Biol 303(1):61-76. <a href = https://www.ncbi.nlm.nih.gov/pubmed/11021970>PMID: 11021970</a><br/><br/>Altschul S., Madden T., Schaffer A., Zhang J., Zhang Z., Miller W., Lipman D. (1997) <b>Gapped BLAST and PSI-BLAST: a New Generation of Protein Database Search Programs. </b>Nucl Acids Res 25:3389-3402. <a href = https://www.ncbi.nlm.nih.gov/pubmed/9254694>PMID: 9254694</a><br/><br/>Kabsch W., Sander C. (1983) <b>Dictionary of Protein Secondary Structure: Pattern Recognition of Hydrogen-Bonded and Geometrical Features.</b> Biopolymers 22:2577-2637. <a href = https://www.ncbi.nlm.nih.gov/pubmed/6667333>PMID: 6667333</a><br/><br/>Adamczak R., Porollo A., Meller J. (2004) <b>Accurate Predicition of Solvent Accessibility Using Neural Networks-Based Regression.</b> Proteins 56:753-767. <a href = https://www.ncbi.nlm.nih.gov/pubmed/15281128>PMID: 15281128</a>"],
    "hhrepid": ["", [], "", "Alva V., Nam SZ., Söding J., Lupas AN. (2016) <b>The MPI bioinformatics Toolkit as an integrative platform for advanced protein sequence and structure analysis.</b> Nucleic Acids Res. pii: gkw348. [Epub ahead of print] <a href=https://www.ncbi.nlm.nih.gov/pubmed/27131380>PMID: 27131380</a><br/><br/>Biegert A., Söding J. (2008) <b>HHrepID: de novo protein repeat identification by probabilistic consistency. </b>Bioinformatics 24(6):807-814.<a href = https://academic.oup.com/bioinformatics/article/24/6/807/194276/De-novo-identification-of-highly-diverged-protein>(download pdf)</a> <a href = https://www.ncbi.nlm.nih.gov/pubmed/18245125>PMID: 18245125</a>"],
    "marcoil": ["", [], "", "Alva V., Nam SZ., Söding J., Lupas AN. (2016) <b>The MPI bioinformatics Toolkit as an integrative platform for advanced protein sequence and structure analysis.</b> Nucleic Acids Res. pii: gkw348. [Epub ahead of print] <a href=https://www.ncbi.nlm.nih.gov/pubmed/27131380>PMID: 27131380</a><br/><br/>Delorenzi M., Speed T. (2002) <b>An HMM model for coiled-coil domains and a comparison with PSSM-based predictions. </b>Bioinformatics 18(4):617-625. <a href = https://www.ncbi.nlm.nih.gov/pubmed/12016059>PMID: 12016059</a>"],
    "pcoils": ["", [], "", "Alva V., Nam SZ., Söding J., Lupas AN. (2016) <b>The MPI bioinformatics Toolkit as an integrative platform for advanced protein sequence and structure analysis.</b> Nucleic Acids Res. pii: gkw348. [Epub ahead of print] <a href=https://www.ncbi.nlm.nih.gov/pubmed/27131380>PMID: 27131380</a><br/><br/>Lupas A., Van Dyke M., Stock J. (1991) <b>Predicting Coiled Coils from Protein Sequences. </b>Science 252:1162-1164. <a href = https://www.ncbi.nlm.nih.gov/pubmed/2031185>PMID: 2031185</a><br/><br/>Lupas A. (1996) <b>Prediction and Analysis of Coiled-Coil Structures. </b>Methods Enzymol 266:513-525. <a href = https://www.ncbi.nlm.nih.gov/pubmed/8743703>PMID: 8743703</a><br/><br/>Parry DA. (1982) <b>Coiled-Coils in Alpha-Helix-Containing Proteins: Analysis of the Residue Types within the Heptad Repeat and the Use of These Data in the Prediction of Coiled-Coils in Other Proteins. </b>Biosci Rep 2(12):1017-24. <a href = https://www.ncbi.nlm.nih.gov/pubmed/7165792>PMID: 7165792</a>"],
    "repper": ["", [], "", "Alva V., Nam SZ., Söding J., Lupas AN. (2016) <b>The MPI bioinformatics Toolkit as an integrative platform for advanced protein sequence and structure analysis.</b> Nucleic Acids Res. pii: gkw348. [Epub ahead of print] <a href=https://www.ncbi.nlm.nih.gov/pubmed/27131380>PMID: 27131380</a><br/><br/>Gruber M., Söding J., Lupas AN. (2005) <b>REPPER -- Repeats and their periodicities in fibrous proteins. </b>Nucl Acids Res 33(Web Server issue): W239-W243. <a href = https://www.ncbi.nlm.nih.gov/pmc/articles/PMC1160166/>PMCID: PMC1160166</a>"],
    "tprpred": ["", [], "", "Alva V., Nam SZ., Söding J., Lupas AN. (2016) <b>The MPI bioinformatics Toolkit as an integrative platform for advanced protein sequence and structure analysis.</b> Nucleic Acids Res. pii: gkw348. [Epub ahead of print] <a href=https://www.ncbi.nlm.nih.gov/pubmed/27131380>PMID: 27131380</a><br/><br/>Karpenahalli MR., Söding J., Lupas AN. (2007) <b>TPRpred: a tool for prediction of TPR-, PPR- and SEL1-like repeats from protein sequences. </b>BMC Bioinformatics 8:2. <a href = https://www.ncbi.nlm.nih.gov/pmc/articles/PMC1774580/>PMCID: PMC1774580</a>"],
    "ali2d": ["", [], "", "Alva V., Nam SZ., Söding J., Lupas AN. (2016) <b>The MPI bioinformatics Toolkit as an integrative platform for advanced protein sequence and structure analysis.</b> Nucleic Acids Res. pii: gkw348. [Epub ahead of print] <a href=https://www.ncbi.nlm.nih.gov/pubmed/27131380>PMID: 27131380</a>"],
    "quick2d": ["", [], "", "Alva V., Nam SZ., Söding J., Lupas AN. (2016) <b>The MPI bioinformatics Toolkit as an integrative platform for advanced protein sequence and structure analysis.</b> Nucleic Acids Res. pii: gkw348. [Epub ahead of print] <a href=https://www.ncbi.nlm.nih.gov/pubmed/27131380>PMID: 27131380</a>"],
    "modeller": ["", [], "", "Alva V., Nam SZ., Söding J., Lupas AN. (2016) <b>The MPI bioinformatics Toolkit as an integrative platform for advanced protein sequence and structure analysis.</b> Nucleic Acids Res. pii: gkw348. [Epub ahead of print] <a href=https://www.ncbi.nlm.nih.gov/pubmed/27131380>PMID: 27131380</a><br/><br/>Sali A., Potterton L., Yuan F., van Vlijmen H., Karplus M. (1995) <b>Evaluation of comparative protein modeling by MODELLER. </b>Proteins 23(3):318-26. <a href = https://www.ncbi.nlm.nih.gov/pubmed/8710825>PMID: 8710825</a>"],
    "samcc": ["", [], "", "Alva V., Nam SZ., Söding J., Lupas AN. (2016) <b>The MPI bioinformatics Toolkit as an integrative platform for advanced protein sequence and structure analysis.</b> Nucleic Acids Res. pii: gkw348. [Epub ahead of print] <a href=https://www.ncbi.nlm.nih.gov/pubmed/27131380>PMID: 27131380</a><br/><br/>Dunin-Horkawicz S., Lupas AN. (2010) <b>Measuring the conformational space of square four-helical bundles with the program samCC. </b>170(2):226-35. doi: 10.1016/j.jsb.2010.01.023. <a href = https://www.ncbi.nlm.nih.gov/pubmed/20139000>PMID: 20139000</a>"],
    "ancescon": ["", [], "", "Alva V., Nam SZ., Söding J., Lupas AN. (2016) <b>The MPI bioinformatics Toolkit as an integrative platform for advanced protein sequence and structure analysis.</b> Nucleic Acids Res. pii: gkw348. [Epub ahead of print] <a href=https://www.ncbi.nlm.nih.gov/pubmed/27131380>PMID: 27131380</a><br/><br/>Cai W., Pei J., Grishin NV. (2004) <b>Reconstruction of ancestral protein sequences and its applications. </b>BMC Evol Biol. 4:33. <a href = https://www.ncbi.nlm.nih.gov/pubmed/15377393>PMID: 15377393</a>"],
    "clans": ["", [], "", "Alva V., Nam SZ., Söding J., Lupas AN. (2016) <b>The MPI bioinformatics Toolkit as an integrative platform for advanced protein sequence and structure analysis.</b> Nucleic Acids Res. pii: gkw348. [Epub ahead of print] <a href=https://www.ncbi.nlm.nih.gov/pubmed/27131380>PMID: 27131380</a><br/><br/>Frickey T., Lupas AN. (2004) <b>CLANS: a Java application for visualizing protein families based on pairwise similarity. </b>Bioinformatics 20(18):3702-3704. <a href = https://www.ncbi.nlm.nih.gov/pubmed/15284097>PMID: 15284097</a>"],
    "mmseqs2": ["", [], "", ""],
    "phylip": ["", [], "", "Alva V., Nam SZ., Söding J., Lupas AN. (2016) <b>The MPI bioinformatics Toolkit as an integrative platform for advanced protein sequence and structure analysis.</b> Nucleic Acids Res. pii: gkw348. [Epub ahead of print] <a href=https://www.ncbi.nlm.nih.gov/pubmed/27131380>PMID: 27131380</a><br/><br/>Felsenstein, J. (1991-2012) <b>Neighbor -- Neighbor-Joining and UPGMA methods.</b> Retrieved May 23, 2016, from <a href = http://evolution.genetics.washington.edu/phylip/doc/neighbor.html>http://evolution.genetics.washington.edu/phylip/doc/neighbor.html</a>"],
    "6frametranslation": ["", [], "", "Alva V., Nam SZ., Söding J., Lupas AN. (2016) <b>The MPI bioinformatics Toolkit as an integrative platform for advanced protein sequence and structure analysis.</b> Nucleic Acids Res. pii: gkw348. [Epub ahead of print] <a href=https://www.ncbi.nlm.nih.gov/pubmed/27131380>PMID: 27131380</a>"],
    "backtrans": ["", [], "", "Alva V., Nam SZ., Söding J., Lupas AN. (2016) <b>The MPI bioinformatics Toolkit as an integrative platform for advanced protein sequence and structure analysis.</b> Nucleic Acids Res. pii: gkw348. [Epub ahead of print] <a href=https://www.ncbi.nlm.nih.gov/pubmed/27131380>PMID: 27131380</a><br/><br/>Nakamura Y., Gojobori T., Ikemura T. (2000) <b>Codon usage tabulated from international DNA sequence databases: status for the year 2000. </b>Nucleic Acids Res 28(1):292. <a href = https://www.ncbi.nlm.nih.gov/pubmed/10592250>PMID: 10592250</a>"],
    "hhfilter": ["", [], "", "Alva V., Nam SZ., Söding J., Lupas AN. (2016) <b>The MPI bioinformatics Toolkit as an integrative platform for advanced protein sequence and structure analysis.</b> Nucleic Acids Res. pii: gkw348. [Epub ahead of print] <a href=https://www.ncbi.nlm.nih.gov/pubmed/27131380>PMID: 27131380</a><br/><br/>Remmert M., Biegert A., Hauser A., Söding J. (2011) <b>HHblits: Lightning-fast iterative protein sequence searching by HMM-HMM alignment. </b>Nat Methods. 9(2):173-5. doi: 10.1038/nmeth.1818. <a href = https://www.ncbi.nlm.nih.gov/pubmed/22198341>PMID: 22198341</a>"],
    "retseq": ["", [], "", ""],
    "seq2id": ["", [], "", ""]
};

helpContent = function(tool : string) {
    if (exampleContent[tool]) {
        return exampleContent[tool];
    } else {
        return ["", [], "", ""];
    }
};

accordion = function(elem : Element, isInit : boolean) : any {
    if (!isInit) {
        return elem.setAttribute("data-accordion", "data-accordion");
    }
};

accordionItem = function(elem : Element, isInit : boolean) : any {
    if (!isInit) {
        return elem.setAttribute("data-accordion-item", "data-accordion-item");
    }
};

accordionContent = function(elem : Element, isInit : boolean) {
    if (!isInit) {
        return elem.setAttribute("data-tab-content", "data-tab-content");
    }
};

(<any>window).HelpModalComponent = {
    controller: function(){},
    view: function(ctrl : any, args : any) {
        let overview : any, params : any, results : any, references : any;
        overview = helpContent(args.toolname)[0];
        params = helpContent(args.toolname)[1];
        results = helpContent(args.toolname)[2];
        references = helpContent(args.toolname)[3];
        return m("div", {
            id: "help-" + args.toolname,
            "class": "reveal",
            config: helpModalReveal
        }, m("div", {
            id: "help-tabs",
            config: helpModalTabs
        }, [
            m("ul", [
                m("li", m("a", {
                    href: "#help-tabs1"
                }, "Overview")), m("li", m("a", {
                    href: "#help-tabs2"
                }, "Parameters")), m("li", m("a", {
                    href: "#help-tabs3"
                }, "Results")), m("li", m("a", {
                    href: "#help-tabs4"
                }, "References")), m("li", {
                    "class": "toolname"
                }, m("a", {
                    href: "#",
                    "class": "not-active"
                }, args.toolnameLong))
            ]), m("div", {
                id: "help-tabs1"
            }, m("div", m.trust(overview))), m("div", {
                id: "help-tabs2"
            }, m("ul", {
                "class": "accordion",
                config: accordion
            }, params.map(function(param : any) {
                return m("li", {
                    "class": "accordion-item",
                    config: accordionItem
                }, [
                    m("a", {
                        href: "#",
                        "class": "accordion-title"
                    }, param), m("div", {
                        "class": "accordion-content",
                        config: accordionContent
                    }, [ m.trust(parameterContent[param]) ])
                ]);
            }))), m("div", {
                id: "help-tabs3"
            }, m("div", m.trust(results)), m("img", {
                src: '/assets/images/psiblast.png',
                style: {
                    marginTop: '2em'
                }
            })), m("div", {
                id: "help-tabs4"
            }, m("div", m.trust(references)))
        ]));
    }
};