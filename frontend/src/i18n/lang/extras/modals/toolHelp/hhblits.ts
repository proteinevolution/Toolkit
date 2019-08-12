/* tslint:disable:max-line-length */

export default {
    en: {
        toolHelpModals: {
            hhblits: {
                overview: `
<b>Background</b>
<p>For an accurate sequence-based prediction of protein structure and function, a highly sensitive sequence-search
    method is crucial. Today, profile-profile and HMM-HMM alignment methods are custom when it comes to identification
    and alignment of templates for 3D-homology modeling. As these methods are generally too slow for iteratively
    searching through large databases, we offer HHblits as a 'lightning-fast' HMM-HMM-based iterative sequence
    search tool. The profile-profile alignment prefilter of HHblits reduces the number of full HMM-HMM alignments
    from many millions to a few thousand, making it faster than PSI-BLAST, but still as sensitive as HHsearch.
    Prefiltering is essential for speed and sensitivity. Compared to PSI-BLAST, HHblits is faster, up to twice
    as sensitive and produces more accurate alignments. The HHblits software is part of the open source<br/>
    package <a href = https://github.com/soedinglab/hh-suite target="_blank" rel="noopener">HHsuite</a>.</p><br/>

<b>Output</b><br/>
The results of HHblits are organized into five different tabs:<br/><br/>

The 'Results' tab provides an interactive and detailed summary of the obtained hits. Here, a selection of the
found hits (either the aligned regions or the full-length sequences) or the query A3M can be forwarded to other
tools (e.g. HHpred, HHomp). A3Ms for matched templates can be retrieved in the 'Alignments' section by clicking
on the 'Template alignment' link.<br/><br/>

The 'Raw Output' tab displays the search results in text format and also offers it for download.<br/><br/>

The E-value distribution of the obtained hits is presented in the 'E-value Plot' tab.<br/><br/>

The query-template and query alignments can be visualized, downloaded, and forwarded to other tools in the
'Query Template MSA' and 'Query Alignment' tabs.<br/>
        `,
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
                        title: 'Select database',
                        content: `Database of template HMMs against which the query sequence or alignment is compared. The <a href = https://uniclust.mmseqs.com target="_blank" rel="noopener">Uniclust30 database</a> is a version of the Uniclust database clustered down to a maximum pairwise sequence identity of 30%.`,
                    },
                    {
                        title: 'E-value inclusion threshold',
                        content: `E-value cutoff for inclusion in result alignment.`,
                    },
                    {
                        title: 'Max. number of iterations',
                        content: `This specifies the number of iterations that are performed by HHblits. The iterations stop in any case when no further sequences are found.`,
                    },
                    {
                        title: 'Minimum probability in hit list (> 10%)',
                        content: `Only hits above this cut-off probability will be displayed.`,
                    },
                    {
                        title: 'No. of target sequences',
                        content: `This parameter controls how many matches will be displayed in the results.`,
                    },
                ],
                references: `
        <p>HHblits: lightning-fast iterative protein sequence searching by HMM-HMM alignment.
            Remmert M, Biegert A, Hauser A, Söding J. <a href = https://www.nature.com/articles/nmeth.1818 target="_blank" rel="noopener">Nat Methods. 2011 Dec 25;9(2):173-5</a>.</p>
    `,
            },
        },
    },
};
