import modalContent from './help/en';
import tools from '@/i18n/lang/modules/tools-en';
import jobs from '@/i18n/lang/modules/jobs-en';
import auth from '@/i18n/lang/modules/auth-en';

export default {
    en: {
        back: 'Back',
        cancel: 'Cancel',
        submit: 'Submit',
        download: 'Download',
        loading: 'Loading...',
        fetching: 'Fetching...',
        reconnecting: 'Reconnecting...',
        connectionTimeout: 'Connection Timeout!',
        language: {
            lang: 'Language',
            de: 'Deutsch',
            en: 'English',
        },
        auth,
        index: {
            welcomeTitle: 'Welcome to the Bioinformatics Toolkit',
            welcomeBody: 'of the Max Planck Institute for Biology, Tübingen, Germany.',
            loadBarLabel: 'Cluster workload: {load}%',
            searchPlaceholder: 'Enter a job ID or a tool name',
        },
        jobManager: {
            table: {
                jobListStatus: 'Job List',
                jobID: 'Job ID',
                status: 'Status',
                tool: 'Tool',
                dateCreated: 'Created',
                actions: 'Actions',
                noData: "Currently you don't own any jobs.",
                noDataFiltered: 'There are no jobs matching your request.',
            },
            watched: {
                true: 'Display in Job List',
                false: 'Hide in Job List',
            },
            paginationInfo: 'Displaying {start} to {end} of {total} jobs',
            perPage: {
                show: 'Show',
                entries: 'Entries',
            },
            filter: 'Search:',
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
        },
        tools,
        jobs,
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
        cookieLaw: {
            message:
                'We use cookies to ensure you get the best experience on our website. ' +
                'By using our services you agree to our {0}.',
            privacyLink: 'Privacy Policy',
            accept: 'Got it!',
        },
        tour: {
            content: {
                toolBar: 'The available tools are organized into different sections',
                searchBar: 'Quick access to tools and jobs',
                tool: "Let's checkout HHpred",
                help: 'Find details on using HHpred, including target databases and parameters',
                input: 'Enter your query protein sequence here',
                paste: 'Alternatively, click here to add an example sequence',
                structuralDomainDatabase: 'Select structural/domain databases',
                proteomes: 'Select proteome databases',
                parametersTab: 'Change search parameters',
                jobId: 'Optionally, enter a custom Job ID',
                submit: 'Submit your job',
                jobList: 'Your running and completed jobs are listed below. Click here to access the job manager.',
                jobManager: 'Here you can see a more detailed list of your jobs',
            },
            labels: {
                buttonSkip: 'Skip tour',
                buttonPrevious: 'Previous',
                buttonNext: 'Next',
                buttonStop: 'Finish tour',
            },
            banner: {
                message: 'Welcome to the MPI Bioinformatics Toolkit! Would you like a tour?',
                ignore: 'Skip tour',
                start: 'Start',
            },
        },
        copyright: '© 2008-{currentYear}, Dept. of Protein Evolution, ' + 'Max Planck Institute for Biology, Tübingen',
        citation: `A Completely Reimplemented MPI Bioinformatics Toolkit
            with a New HHpred Server at its Core.<br>Zimmermann L, Stephens A, Nam SZ, Rau D,
            Kübler J, Lozajic M, Gabler F, Söding J, Lupas AN, Alva V.
            <a href="http://www.sciencedirect.com/science/article/pii/S0022283617305879" target="_blank" rel="noopener">
            J Mol Biol. 2018 Jul 20. S0022-2836(17)30587-9</a>.<br><br>
            Protein Sequence Analysis Using the MPI Bioinformatics Toolkit.
            <br>Gabler F, Nam SZ, Till S, Mirdita M, Steinegger M, Söding J, Lupas AN, Alva V.
            <a href="https://currentprotocols.onlinelibrary.wiley.com/doi/full/10.1002/cpbi.108" target="_blank" rel="noopener">
            Curr Protoc Bioinformatics. 2020 Dec;72(1):e108. doi: 10.1002/cpbi.108</a>.`,
        errors: {
            general: 'Error!',
            fileNotFound: 'File not found!',
            fileUnreadable: 'File could not be read.',
            PageNotFound: 'Page not found!',
            ToolNotFound: 'Tool not found!',
            JobNotFound: 'Job not found!',
            JobNotAuthorized: 'Access denied to private job.',
            couldNotDeleteJob: 'Could not delete job!',
            templateAlignmentFailed: 'Sorry, failed to fetch Template Alignment.',
            templateStructureFailed: 'Sorry, failed to fetch structure.',
            couldNotLoadForwardData: 'Sorry, failed to fetch forwarding data.',
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
        maintenance: {
            headerWarning: 'Maintenance in a few seconds!',
            blockSubmit: 'Cannot submit jobs during maintenance.',
            notificationTitle: 'Maintenance Alert',
            notificationBody: 'The Toolkit is undergoing maintenance.',
            start: 'Start Maintenance',
            end: 'End Maintenance',
        },
    },
};
