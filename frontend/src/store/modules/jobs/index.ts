import {Module} from 'vuex';
import getters from './getters';
import actions from './actions';
import mutations from './mutations';
import {RootState, JobState} from '../../types';

export const state: JobState = {
    jobs: [],
};

const namespaced: boolean = true;

const jobs: Module<JobState, RootState> = {
    namespaced,
    state,
    getters,
    actions,
    mutations,
};

export default jobs;
