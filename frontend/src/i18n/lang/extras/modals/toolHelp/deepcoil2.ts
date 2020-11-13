/* tslint:disable:max-line-length */

export default {
    en: {
        toolHelpModals: {
            deepcoil2: {
                overview: `<p><a href="https://github.com/labstructbioinf/DeepCoil" target="_blank" rel="noopener">
                    DeepCoil2</a> is a method for the fast and accurate prediction of coiled-coil domains in
                    protein sequences.<br/><br/>
                    <b>New in version 2.0:</b>
                    <ul>
                    <li>Faster inference time by applying <a href="https://github.com/mheinzinger/SeqVec"  
                    target="_blank" rel="noopener">SeqVec embeddings</a> instead of psiblast profiles.</li>
                    <li>Heptad register prediction (a and d core positions).</li>
                    <li>No maximum sequence length limit.</li>
                    <li>Automated peak detection for improved output readability.</li>                   
                    </ul>`,
                parameters: [
                    {
                        title: 'Input',
                        content: `You have the option to enter or upload either a single protein sequence in FASTA 
                    format.<br/><br/>
                    @:toolHelpModals.common.singleseq`,
                    },
                ],
                references: 'DeepCoil - a fast and accurate prediction of coiled-coil domains in protein sequences.<br>' +
                    'Ludwiczak J, Winski A, Szczepaniak K, Alva V, Dunin-Horkawicz S.<a ' +
                    'href="https://www.ncbi.nlm.nih.gov/pubmed/?term=30601942" target="_blank" rel="noopener">\n' +
                    'Bioinformatics. 2019 Aug 15;35(16):2790-2795</a>.',
            },
        },
    },
};
