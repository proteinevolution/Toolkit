import modalContent from './help/en';

export default {
    en: {
        cancel: 'Cancel',
        submit: 'Submit',
        fetching: 'Fetching...',
        reconnecting: 'Reconnecting...',
        maintenanceWarning: 'Maintenance in a few seconds!',
        auth: {
            username: 'Username',
            firstName: 'First Name',
            firstNameEnter: 'Enter your first name',
            lastName: 'Last Name',
            lastNameEnter: 'Enter your last name',
            eMail: 'E-Mail',
            country: 'Country',
            countrySelect: 'Select a country',
            signIn: 'Sign In',
            signOut: 'Sign Out',
            signUp: 'Sign Up',
            profile: 'Profile',
            settings: 'Settings',
            password: 'Password',
            passwordRepeat: 'Confirm password',
            reenterPassword: 'Please re-enter your password',
            changePassword: 'Change Password',
            enterNewPassword: 'Please enter a new password.',
            resetPassword: 'Reset Password',
            oldPassword: 'Old Password',
            newPassword: 'New Password',
            confirmPassword: 'Confirm Password',
            forgotPassword: 'Forgot Password?',
            forgotPasswordInstructions: 'Provide your account email address or username to receive an email to ' +
                'reset your password.',
            eMailOrUsername: 'E-Mail or Username',
            privacyAccept: ' I accept the {0}',
            editProfile: 'Edit Profile',
            verification: {
                succeeded: 'Verification succeeded',
                failed: 'Verification failed',
                loading: 'Verifying...',
            },
            resetPasswordModal: {
                loading: 'Loading...',
            },
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
                noData: 'Currently you don\'t own any jobs.',
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
                modellerKey: {
                    stored: 'MODELLER-key is stored in your profile.',
                },
                emailNotification: 'E-Mail notification',
                isPublic: {
                    true: 'Job is public',
                    false: 'Job is private',
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
                invalidSequenceFormat: 'Invalid sequence format. Expected {expected}.',
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
            details: {
                jobID: 'Job ID: {jobID},',
                parentID: 'Parent Job ID: {0},',
                dateCreated: 'Created: {dateCreated}',
            },
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
            JobNotFound: 'Job not found.',
            couldNotDeleteJob: 'Could not delete job!',
        },
        constraints: {
            range: 'has to be between {min} and {max}',
            lengthMax: 'Has to be shorter than {max} characters',
            username: 'Must be between 6 and 40 characters long and only contain alphanumerical characters',
            email: 'Has to be a valid email address',
            password: 'Must be between 8 and 128 characters long',
            passwordsMatch: 'Passwords do not match',
            format: 'wrong input format',
            notEmpty: 'must not be empty',
            invalidModellerKey: 'invalid modeller key',
            invalidCustomJobId: 'Invalid job ID. Try "{0}"',
            customerJobIdTooShort: 'needs to have more than two characters.',
            modellerNoConnection: 'could not validate the key. Try again later!',
        },
    },
};
