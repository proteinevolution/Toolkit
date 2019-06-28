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
            Vue.set(state.jobs, index, job);
        }
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
            Vue.set(state.jobs, index, job);
        }
    },
};

export default mutations;
