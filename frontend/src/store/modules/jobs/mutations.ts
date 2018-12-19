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
};

export default mutations;
