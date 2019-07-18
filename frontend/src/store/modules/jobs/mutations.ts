import Vue from 'vue';
import {MutationTree} from 'vuex';
import {JobState} from '../../types';
import {Job} from '@/types/toolkit/jobs';

const mutations: MutationTree<JobState> = {
    setJobs(state, jobs) {
        state.jobs = jobs;
    },
    setJob(state, {jobID, job}) {
        const index: number = state.jobs.findIndex((j) => j.jobID === jobID);
        if (index < 0) {
            state.jobs.push(job);
        } else {
            // alignments have to be fetched separately
            if (!job.alignments && state.jobs[index].alignments) {
                job.alignments = state.jobs[index].alignments;
            }
            Vue.set(state.jobs, index, job);
        }
    },
    setJobAlignments(state, {jobID, alignments}) {
        const index: number = state.jobs.findIndex((j) => j.jobID === jobID);
        Vue.set(state.jobs[index], 'alignments', alignments);
    },
    removeJob(state, {jobID}) {
        state.jobs = state.jobs.filter((job: Job) => job.jobID !== jobID);
    },
    SOCKET_ClearJob(state, {jobID}) {
        state.jobs = state.jobs.filter((job: Job) => job.jobID !== jobID);
    },
    SOCKET_UpdateJob(state, {job}) {
        const index: number = state.jobs.findIndex((j) => j.jobID === job.jobID);
        if (index < 0) {
            state.jobs.push(job);
        } else {
            // the websocket does not push paramValues
            if (!job.paramValues && state.jobs[index].paramValues) {
                job.paramValues = state.jobs[index].paramValues;
            }
            // the websocket does not push alignments
            if (!job.alignments && state.jobs[index].alignments) {
                job.alignments = state.jobs[index].alignments;
            }
            Vue.set(state.jobs, index, job);
        }
    },
};

export default mutations;
