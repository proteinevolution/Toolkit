import {ActionTree} from 'vuex';
import {RootState, JobState} from '../../types';
import {Job} from '@/types/toolkit/jobs';
import JobService from '@/services/JobService';

const actions: ActionTree<JobState, RootState> = {
    async fetchAllJobs(context) {
        context.commit('startLoading', 'jobs', {root: true});
        const jobs: Job[] = await JobService.fetchJobs();
        context.commit('setJobs', jobs);
        context.commit('stopLoading', 'jobs', {root: true});
    },
};

export default actions;
