/* tslint:disable:max-line-length */

export default {
    en: {
        toolHelpModals: {
            seq2id: {
                overview: `<p>Seq2ID is a utility that extracts all GI-numbers from a given file. Each sequence has to be new line separated. eg.:</p>
                <p>Sequence 1<br/>Sequence 2<br/>sequence 3<br/>...</p>`,
                parameters: [
                    {
                        title: 'Input',
                        content: `<p>Enter a file with GI-numbers. This tool finds only numbers with the format: <b>gi|number</b> !</p>
                    <p>Exception: If a line starts with <b>>NR20|</b>, the <b>gi</b> prefix is not necessary.</p>`,
                    },
                ],
                references: ``,
            },
        },
    },
};
