import {MutationTree} from 'vuex';
import {JobState} from '../../types';

const mutations: MutationTree<JobState> = {
    setJobs(state, jobs) {
        state.jobs = jobs;
    },
};

export default mutations;
