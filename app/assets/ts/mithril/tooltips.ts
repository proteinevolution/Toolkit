/**
 * Created by mlozajic on 11.04.17.
 */


//Tooltips for individual tools

$( "#hhb" ).prop('title', 'Remote homology detection method based on iterative HMM-HMM comparison');

$( "#hhp" ).prop('title', 'Homology detection & structure prediction by HMM-HMM comparison');

$( "#hmmr" ).prop('title', 'Sensitive sequence searching based on profile HMMs');

$( "#pats" ).prop('title', 'Search based on PROSITE pattern/regular expression');

$( "#pbl" ).prop('title', 'NCBI (PSI-)BLAST+');

$( "#alnviz" ).prop('title', 'BioJS multiple sequence alignment viewer');

$( "#cluo" ).prop('title', 'Multiple sequence alignment tool');

$( "#kal" ).prop('title', 'Multiple sequence alignment tool');

$( "#mft" ).prop('title', 'Multiple sequence alignment tool');

$( "#msa" ).prop('title', 'Multiple sequence alignment tool');

$( "#musc" ).prop('title', 'Multiple sequence alignment tool');

$( "#tcf" ).prop('title', 'Multiple sequence alignment tool');

$( "#a2pl" ).prop('title', 'Shows a graphical overview of hydrophobicity and side chain volume');

$( "#frp" ).prop('title', 'Prediction of functional residues in multiple sequence alignments');

$( "#hhr" ).prop('title', 'De novo identification of repeats');

$( "#mar" ).prop('title', 'Prediction of coiled coils based on HMMs');

$( "#pco" ).prop('title', 'Prediction of coiled coils');

$( "#rep" ).prop('title', 'Detects short gapless repeats');

$( "#tprp" ).prop('title', 'Detects Tetratrico Peptide Repeats (TPRs), Pentatrico Peptide Repeats (PPRs) and SEL1-like repeats');

$( "#a2d" ).prop('title', 'Plots info on secondary structure and transmembrane regions onto an MSA');

$( "#q2d" ).prop('title', 'Overview of secondary structure features like coiled coils, transmembrane helices and disordered regions');

$( "#mod" ).prop('title', 'Comparative protein structure modelling by satisfaction of spatial restraints');

$( "#sam" ).prop('title', 'Measures structural parameters of four-helical bundles');

$( "#anc" ).prop('title', 'Reconstructs ancestral protein sequences');

$( "#clan" ).prop('title', 'Clustering based on all-against-all BLAST+ similarities');

$( "#mseq" ).prop('title', 'Ultra fast and sensitive protein sequence clustering');

$( "#phym" ).prop('title', 'Infer phylogenies');

$( "#6frt" ).prop('title', 'Six-frame translation of nucleotide sequences');

$( "#bac" ).prop('title', 'Reverse translation of protein sequences into nucleotide sequences');

$( "#hhfi" ).prop('title', 'Extraction of a representative set of sequences from an alignment');

$( "#ret" ).prop('title', 'Sequence retrieval using a list of accession IDs');

$( "#s2id" ).prop('title', 'Extraction of accessions IDs');

$( "#reformat" ).prop('title', 'Sequence reformatting utility');

$('.toolsec a').tooltipster({
    theme: 'tooltipster-borderless',
    position: 'bottom',
    animation: 'fade'
});