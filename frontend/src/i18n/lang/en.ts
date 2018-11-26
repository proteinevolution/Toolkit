import modalContent from './modals/en';

export default {
    en: {
        helpContents: 'Some help text',
        fetching: 'Fetching...',

        index: {
            welcomeTitle: 'Welcome to the Bioinformatics Toolkit',
            welcomeBody: 'of the Max Planck Institute for Developmental Biology, Tübingen, Germany.',
            loadBarLabel: 'Cluster workload: {load}%',
            searchPlaceholder: 'Enter a job ID or a tool name',
        },
        jobList: {
            sortColumns: {
                id: 'ID',
                date: 'Date',
                tool: 'Tool',
            },
        },
        tools: {
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
                },
            },
            validation: {
                invalidCharacters: 'Invalid Characters.',
                autoTransformedToFasta: '{detected} detected. Auto-transformed to FASTA.',
                nucleotideError: 'Input contains nucleotide sequence(s). Expecting protein sequence(s).',
                emptyHeader: 'Empty header.',
                maxSeqNumber: 'Input contains more than {limit} sequences.',
                minSeqNumber: 'Input contains less than {limit} sequences.',
                maxSeqLength: 'Maximum allowed sequence length is {limit}.',
                minSeqLength: 'Minimum allowed sequence length is {limit}.',
                onlyDashes: 'Sequence contains only dots/dashes.',
                sameLength: 'Sequences should have the same length.',
                maxLength: 'Input contains over {limit} characters.',
                uniqueIDs: 'Identifiers are not unique.',
                invalidSequenceType: 'Invalid sequence type. Expected {expected}.',
                valid: '{type} {format}',
                validRegex: 'Valid input.',
                validPDB: 'Valid input.',
                validAccessionID: 'Valid input.',
                emptySequences: 'Empty sequences are not allowed.',
            },
            reformat: {
                inputPlaceholder: 'Enter a sequence...',
                detectedFormat: 'Found format: <b>{format}</b>',
                selectOutputFormat: 'Select Output Format',
                forwardTo: 'Forward to',
                download: 'Download',
                copyToClipboard: 'Copy to clipboard',
                copySuccess: 'Successfully copied',
                copyFailure: 'Unable to copy',
            },
        },
        helpModals: {
            names: {
                help: 'Help',
                faq: 'FAQ',
                privacy: 'Privacy Policy',
                imprint: 'Imprint',
                contact: 'Contact Us',
                cite: 'Cite Us',
                updates: 'Recent Updates',
            },
            titles: {
                help: 'Welcome to the MPI Bioinformatics Toolkit',
                faq: 'FAQ',
                privacy: 'Datenschutzhinweis (Privacy Policy<sup>*</sup>)',
                imprint: 'Imprint',
                contact: 'Contact Us',
                cite: 'Cite Us',
                updates: 'Recent Updates',
            },
            content: modalContent,
        },
        copyright: '© 2008-{currentYear}, Dept. of Protein Evolution, ' +
            'Max Planck Institute for Developmental Biology, Tübingen',
        errors: {
            fileNotFound: 'File not found!',
            fileUnreadable: 'File could not be read.',
            PageNotFound: 'Page not found.',
            ToolNotFound: 'Tool not found.',
        },
        constraints: {
            range: 'has to be between {min} and {max}',
            notEmpty: 'must not be empty',
            invalidModellerKey: 'invalid modeller key',
            modellerNoConnection: 'could not validate the key. Try again later!',
        },
    },
};
