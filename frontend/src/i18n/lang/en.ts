import modalContent from './modals/help/en';
import hhblits from './modals/toolHelp/en/hhblits';
import common from '@/i18n/lang/modals/toolHelp/en/common';
import ali2d from '@/i18n/lang/modals/toolHelp/en/ali2d';
import aln2plot from '@/i18n/lang/modals/toolHelp/en/aln2plot';
import ancescon from '@/i18n/lang/modals/toolHelp/en/ancescon';
import backtrans from '@/i18n/lang/modals/toolHelp/en/backtrans';
import deepcoil from '@/i18n/lang/modals/toolHelp/en/deepcoil';
import marcoil from '@/i18n/lang/modals/toolHelp/en/marcoil';
import mafft from '@/i18n/lang/modals/toolHelp/en/mafft';
import kalign from '@/i18n/lang/modals/toolHelp/en/kalign';
import hmmer from '@/i18n/lang/modals/toolHelp/en/hmmer';
import hhfilter from '@/i18n/lang/modals/toolHelp/en/hhfilter';
import hhpred from '@/i18n/lang/modals/toolHelp/en/hhpred';
import clans from '@/i18n/lang/modals/toolHelp/en/clans';
import hhrepid from '@/i18n/lang/modals/toolHelp/en/hhrepid';
import clustalo from '@/i18n/lang/modals/toolHelp/en/clustalo';
import mmseqs2 from '@/i18n/lang/modals/toolHelp/en/mmseqs2';
import modeller from '@/i18n/lang/modals/toolHelp/en/modeller';
import msaprobs from '@/i18n/lang/modals/toolHelp/en/msaprobs';
import muscle from '@/i18n/lang/modals/toolHelp/en/muscle';
import patsearch from '@/i18n/lang/modals/toolHelp/en/patsearch';
import tprpred from '@/i18n/lang/modals/toolHelp/en/tprpred';
import retseq from '@/i18n/lang/modals/toolHelp/en/retseq';
import pcoils from '@/i18n/lang/modals/toolHelp/en/pcoils';
import quick2d from '@/i18n/lang/modals/toolHelp/en/quick2d';
import psiblast from '@/i18n/lang/modals/toolHelp/en/psiblast';
import sixframe from '@/i18n/lang/modals/toolHelp/en/sixframe';
import phyml from '@/i18n/lang/modals/toolHelp/en/phyml';
import repper from '@/i18n/lang/modals/toolHelp/en/repper';
import samcc from '@/i18n/lang/modals/toolHelp/en/samcc';
import seq2id from '@/i18n/lang/modals/toolHelp/en/seq2id';
import tcoffee from '@/i18n/lang/modals/toolHelp/en/tcoffee';

export default {
    en: {
        cancel: 'Cancel',
        submit: 'Submit',
        fetching: 'Fetching...',
        reconnecting: 'Reconnecting...',
        maintenanceWarning: 'Maintenance in a few seconds!',
        auth: {
            signIn: 'Sign In',
            signOut: 'Sign Out',
            signUp: 'Sign Up',
            username: 'Username',
            eMail: 'E-Mail',
            password: 'Password',
            passwordRepeat: 'Confirm password',
            reenterPassword: 'Please re-enter your password',
            privacyAccept: ' I accept the {0}',
            firstName: 'First Name',
            lastName: 'Last Name',
            country: 'Country',
            selectCountry: 'Select a country',
            editProfile: 'Edit Profile',
        },
        index: {
            welcomeTitle: 'Welcome to the Bioinformatics Toolkit',
            welcomeBody: 'of the Max Planck Institute for Developmental Biology, Tübingen, Germany.',
            loadBarLabel: 'Cluster workload: {load}%',
            searchPlaceholder: 'Enter a job ID or a tool name',
        },
        jobManager: {
            table: {
                jobListStatus: 'Job List',
                jobID: 'Job ID',
                tool: 'Tool',
                dateCreated: 'Created',
                actions: 'Actions',
                noData: 'No jobs available.',
            },
        },
        jobList: {
            sortColumns: {
                jobID: 'ID',
                dateCreated: 'Date',
                tool: 'Tool',
            },
            pagination: 'Page {currentPage} of {pageCount}',
        },
        search: {
            nothingFound: 'Nothing found.',
            loading: 'Loading...',
        },
        tools: {
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
                },
                customJobId: {
                    placeholder: 'Custom Job ID',
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
        jobs: {
            header: 'Jobs',
            details: 'JobID: {jobID}, Created: {dateCreated}',
            states: {
                1: 'Prepared',
                2: 'Queued',
                3: 'Running',
                4: 'Error',
                5: 'Done',
                6: 'Submitted',
                7: 'Pending',
                8: 'Limit Reached',
                9: 'Deleted',
            },
            submitJob: 'Submit Job',
            resubmitJob: 'Resubmit Job',
            foundIdenticalCopy: 'We found an identical copy of your job in our database!<br>' +
                'Job ID: {jobID}, which was created {createdAt}.',
            startJob: 'Start job anyways',
            loadExistingJob: 'Load existing job',
            loadExistingJobAndDelete: 'Load existing job and delete this one',
            notifications: {
                titles: {
                    update: 'Job Update',
                },
                jobFinished: 'Your {tool} job has finished!',
            },
            citationInfo: `If you use {tool} within the Toolkit for your research, please cite:<br>
<a href="http://www.sciencedirect.com/science/article/pii/S0022283617305879" target="_blank" rel="noopener">
A Completely Reimplemented MPI Bioinformatics Toolkit with a New HHpred Server at its Core.
</a>`,
            jobIDDetails: 'Job ID: {jobID}',
            stateMessages: {
                queued: 'Your submission is queued!',
                running: 'Your submission is being processed!',
            },
        },
        footerLinkModals: {
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
        // TODO lazy load translations
        toolHelpModals: {
            common,
            ali2d,
            aln2plot,
            ancescon,
            backtrans,
            clans,
            clustalo,
            deepcoil,
            hhblits,
            hhfilter,
            hhpred,
            hhrepid,
            hmmer,
            kalign,
            mafft,
            marcoil,
            mmseqs2,
            modeller,
            msaprobs,
            muscle,
            patsearch,
            pcoils,
            phyml,
            psiblast,
            quick2d,
            repper,
            retseq,
            samcc,
            seq2id,
            sixframe,
            tcoffee,
            tprpred,
        },
        copyright: '© 2008-{currentYear}, Dept. of Protein Evolution, ' +
            'Max Planck Institute for Developmental Biology, Tübingen',
        citation: `A Completely Reimplemented MPI Bioinformatics Toolkit
with a New HHpred Server at its Core.<br>Zimmermann L, Stephens A, Nam SZ, Rau D,
Kübler J, Lozajic M, Gabler F, Söding J, Lupas AN, Alva V.
<a href="http://www.sciencedirect.com/science/article/pii/S0022283617305879" target="_blank" rel="noopener">
J Mol Biol. 2018 Jul 20. S0022-2836(17)30587-9</a>.`,
        errors: {
            general: 'Error!',
            fileNotFound: 'File not found!',
            fileUnreadable: 'File could not be read.',
            PageNotFound: 'Page not found.',
            ToolNotFound: 'Tool not found.',
            couldNotDeleteJob: 'Could not delete job!',
        },
        constraints: {
            range: 'has to be between {min} and {max}',
            format: 'wrong input format',
            notEmpty: 'must not be empty',
            invalidModellerKey: 'invalid modeller key',
            invalidCustomJobId: 'Invalid job ID. Try "{0}"',
            customerJobIdTooShort: 'needs to have more than two characters.',
            modellerNoConnection: 'could not validate the key. Try again later!',
        },
    },
};
