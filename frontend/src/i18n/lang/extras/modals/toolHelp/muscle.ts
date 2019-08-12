/* tslint:disable:max-line-length */

export default {
    en: {
        toolHelpModals: {
            muscle: {
                overview: `MUSCLE is a multiple alignment program for protein sequences. MUSCLE stands for multiple sequence
        comparison by log-expectation.<br/>[MUSCLE 3.8.31;
        <a href = http://www.drive5.com/muscle target="_blank" rel="noopener">http://www.drive5.com/muscle/</a>]`,
                parameters: [
                    {
                        title: 'Input',
                        content: `@:toolHelpModals.common.multiseq`,
                    },
                    {
                        title: 'Maximum number of iterations',
                        content: `Specify the maximum number of iterations.`,
                    },
                ],
                references: `<p>Edgar RC. (2004) <b>MUSCLE: multiple sequence alignment with high accuracy and high throughput.</b>
        Nucl Acid Res 32(5):1792-1797.
        <a href = https://www.ncbi.nlm.nih.gov/pubmed/15034147 target="_blank" rel="noopener">PMID: 15034147</a></p>
`,
            },
        },
    },
};
