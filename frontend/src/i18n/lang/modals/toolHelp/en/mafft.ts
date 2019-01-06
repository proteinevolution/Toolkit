/* tslint:disable:max-line-length */

export default {
    overview: `Multiple alignment program for amino acid or nucleotide sequences<br/>[MAFFT v7;
    <a href = http://mafft.cbrc.jp/alignment/software target="_blank" rel="noopener">
        http://mafft.cbrc.jp/alignment/software/</a>]`,
    parameters: [
        {
            title: 'Input',
            content: `@:toolHelpModals.common.multiseq`,
        },
        {
            title: 'Output the alignment in',
            content: `Specify the order in which the aligned sequences appear within the resulting multiple sequence alignment`,
        },
        {
            title: 'Gap open penalty',
            content: `Specify the gap open penalty (numbers greater than 0 are valid).`,
        },
        {
            title: 'Offset',
            content: `This option specifies the offset parameter which works like a gap extension penalty (numbers greater than 0 are valid).`,
        },
    ],
    references: `<p>Katoh K., Misawa K., Kuma K., Miyata T. (2002)
        <b>MAFFT: a novel method for rapid multiple sequence alignment based on fast Fourier transform.</b>
        Nucleic Acid Res 30(14):3059-3066.
        <a href = https://www.ncbi.nlm.nih.gov/pubmed/12136088 target="_blank" rel="noopener">PMID: 12136088</a></p>`,
};
