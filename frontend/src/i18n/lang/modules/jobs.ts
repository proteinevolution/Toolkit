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
            probList: 'ProbList',
            probState: 'ProbState',
            predictedDomains: 'Predicted Domains',
            numericalData: 'Numerical Data',
            structure3dWithAxes: '3D Structure With Axes',
            structure3d: '3D Structure',
            plots: 'Plots',
        },
        actions: {
            selectAll: 'Select All',
            deselectAll: 'Deselect All',
            forward: 'Forward',
            forwardAll: 'Forward All',
            forwardSelected: 'Forward Selected',
            download: 'Download',
            downloadMSA: 'Download MSA',
            exportMSA: 'Export MSA',
            colorMSA: 'Color MSA',
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
