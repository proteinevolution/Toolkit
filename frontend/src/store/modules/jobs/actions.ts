import {ActionTree} from 'vuex';
import {RootState, JobState} from '../../types';

const actions: ActionTree<JobState, RootState> = {
    async fetchAllJobs(context) {
        context.commit('startLoading', 'jobs', {root: true});
        // todo fetch jobs
        context.commit('stopLoading', 'jobs', {root: true});
    },
};

export default actions;
