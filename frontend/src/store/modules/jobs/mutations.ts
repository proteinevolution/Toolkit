import {MutationTree} from 'vuex';
import {JobState} from '../../types';
import Vue from 'vue';
import {Job} from '@/types/toolkit/jobs';

const mutations: MutationTree<JobState> = {
    setJobs(state, jobs) {
        state.jobs = jobs;
    },
    setJob(state, {jobID, job}) {
        const index: number = state.jobs.findIndex((j) => j.jobID === jobID);
        Vue.set(state.jobs, index < 0 ? state.jobs.length : index, job);
    },
    setJobHidden(state, {jobID, hidden}) {
        const job = state.jobs.find((j) => j.jobID === jobID);
        if (job) {
            Vue.set(job, 'hidden', hidden);
        }
    },
    toggleJobHidden(state, {jobID}) {
        const job = state.jobs.find((j) => j.jobID === jobID);
        if (job) {
            Vue.set(job, 'hidden', !job.hidden);
        }
    },
    removeJob(state, {jobID}) {
        state.jobs = state.jobs.filter((job: Job) => job.jobID !== jobID);
    },
    SOCKET_UpdateJob(state, message) {
        console.log(message);
    },
};

export default mutations;
