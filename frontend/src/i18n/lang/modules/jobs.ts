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
