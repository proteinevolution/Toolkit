/* tslint:disable:max-line-length */

export default {
    en: {
        toolHelpModals: {
            plmblast: {
                overview: `
                <p>pLM-BLAST is a sensitive remote homology detection tool that is based on the comparison of sequence 
                embeddings obtained from the protein language model ProtT5. It is available as an easy-to-use web server 
                within the MPI Bioinformatics Toolkit but can be also used as a standalone package, which is available 
                at <a href = https://github.com/labstructbioinf/pLM-BLAST target="_blank" rel="noopener">
                https://github.com/labstructbioinf/pLM-BLAST. </a> Pre-computer databases can be downloaded from 
                <a href = http://ftp.tuebingen.mpg.de/pub/protevo/toolkit/databases/plmblast_dbs target="_blank" rel="noopener">
                 http://ftp.tuebingen.mpg.de/pub/protevo/toolkit/databases/plmblast_dbs</a>.</p>`,
                parameters: [
                    {
                        title: 'Input',
                        content: `You have the option to enter a single protein sequence FASTA.<br/><br/>
                @:toolHelpModals.common.singleseq`,
                    },
                    {
                        title: 'Select target database',
                        content: `<p>Select domain database(s) of template embeddings against which you want to compare the query.</p>
                    <em>ECOD30, ECOD50, ECOD70</em>
                    <p> These databases are versions of the <a href = http://prodata.swmed.edu/ecod/ target="_blank" rel="noopener">
                        Evolutionary Classification of Protein Domains (ECOD) database</a> filtered for a maximum of 30%,
                        50%, and 70% sequence identity, respectively.</p>`,
                    },
                    {
                        title: 'Cosine similarity percentile cut-off',
                        content: `A pre-screening procedure is used to improve the performance of database searches. 
                        First, the database of flattened (per-protein) embeddings is searched using the flattened query
                         embedding and the cosine similarity metric (this is much faster than the comparison of 
                         per-residue embeddings), and then the actual pLM-BLAST comparisons are performed only for 
                         matches above the user-provided cut-off. The cut-off is expressed as the n-th percentile of all 
                         cosine similarity scores. The higher the pre-screening cut-off, the faster and less sensitive 
                         the search will be, and vice versa.`,
                    },
                    {
                        title: 'Alignment score cut-off',
                        content: `Each local alignment is assigned a score calculated as the mean of substitution matrix 
                        values at coordinates defined by its subpath. The alignment cut-off defines the minimal score for 
                        reporting a match. The larger the cut-off, the stricter is search. Also, note that only matches 
                        that passed the pre-filtering step (see "Cosine similarity percentile cut-off") are considered.`,
                    },
                    {
                        title: 'Window length',
                        content: `A moving average is used to identify subpaths, i.e., local alignments, in the full 
                        paths defined by the traceback procedure. The window size values greater than one tends to 
                        generate longer local alignments yet may result in reduced sensitivity.`,
                    },
                    {
                        title: 'Merge hits',
                        content: `Since pLM-BLAST tends to return rather short alignments, an optional procedure may be 
                        applied in which alignments to a single database entry are merged. Such a merged match can 
                        comprise two or more un-merged hits and its score, similarity, and identity are defined as a 
                        mean of the values from the individual sub-hits.`,
                    },
                    {
                        title: 'Max target hits',
                        content: `This parameter controls how many matches will be displayed in the results.`,
                    },
                ],
                references: `<p>pLM-BLAST – distant homology detection based on direct comparison of sequence 
                                representations from protein language models.<br>
                                Kaminski K, Ludwiczak K, Alva V, Dunin-Horkawicz S. bioRxiv. 2022.</p>`,
            },
        },
    },
};
