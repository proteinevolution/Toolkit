import {MutationTree} from 'vuex';
import {JobState} from '../../types';
import Vue from 'vue';

const mutations: MutationTree<JobState> = {
    setJobs(state, jobs) {
        state.jobs = jobs;
    },
    setJob(state, {jobID, job}) {
        const index: number = state.jobs.findIndex((j) => j.jobID === jobID);
        Vue.set(state.jobs, index < 0 ? state.jobs.length : index, job);
    },
};

export default mutations;
