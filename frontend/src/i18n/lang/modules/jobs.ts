export default {
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
    results: {
        titles: {
            hitlist: 'Hitlist',
            fastaAlignment: 'FASTA Alignment',
            clustalAlignment: 'CLUSTAL Alignment',
            alignmentViewer: 'AlignmentViewer',
            treeView: 'Tree',
            summary: 'Summary',
            dataView: 'Data',
            results: 'Results',
            resultsConfidence: 'Results With Confidence',
            textOutput: 'Text Output',
            reducedSet: 'Reduced Set',
            clusters: 'Clusters',
            rawOutput: 'Raw Output',
            queryTemplateMSA: 'Query Template MSA',
            probList: 'ProbList',
            probState: 'ProbState',
            predictedDomains: 'Predicted Domains',
            numericalData: 'Numerical Data',
            structure3dWithAxes: '3D Structure With Axes',
            structure3d: '3D Structure',
            plots: 'Plots',
            ccProb: 'CC-Prob',
        },
        actions: {
            selectAll: 'Select All',
            deselectAll: 'Deselect All',
            forward: 'Forward',
            forwardAll: 'Forward All',
            forwardSelected: 'Forward Selected',
            forwardQueryA3M: 'Forward Query A3M',
            download: 'Download',
            downloadHits: 'Download Hits',
            downloadMSA: 'Download MSA',
            exportMSA: 'Export MSA',
            colorMSA: 'Color MSA',
            colorSeqs: 'Color Seqs',
            wrapSeqs: 'Wrap Seqs',
            downloadTree: 'Download Tree',
            downloadPDBFile: 'Download PDB File',
        },
        alignment: {
            numSeqs: 'Number of Sequences: <b>{num}</b>',
            loadingHits: 'Loading hits...',
        },
        seq2ID: {
            numRetrieved: 'Retrieved {num} IDs',
        },
        tprpred: {
            noResults: 'No repeats found! You could consider picking a less stringent E-value inclusion cut-off.',
        },
        patsearch: {
            noResults: 'No hits found! You could consider picking a different target database.',
        },
        hhomp: {
            noResults: 'No hits found! Please re-run HHomp with an increased number of MSA generation steps or with a custom-built ' +
                'multiple sequence alignment as input (set "Maximal no. of MSA generation steps" to 0). You could also ' +
                'consider picking a different target database and/or using different parameters.',
            numHits: 'Number of Hits: <b>{num}</b>',
            ompProb: 'Overall probability of the query to be an OMP: <b>{prob}%</b>',
        },
        imagesView: {
            noPeriodicity: 'No periodicity spectrum found!',
            noRepeats: 'No significant repeats found for given values!',
        },
        hitlist: {
            visLink: 'Vis',
            hitsLink: 'Hits',
            alnLink: 'Aln',
            vis: 'Visualization',
            hits: 'Hitlist',
            aln: 'Alignments',
        },
    },
    forwarding: {
        selectPlaceholder: 'Please select a tool',
    },
    submitJob: 'Submit Job',
    resubmitJob: 'Resubmit Job',
    delete: 'Delete Job',
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
};
