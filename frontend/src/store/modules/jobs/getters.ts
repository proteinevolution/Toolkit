import {GetterTree} from 'vuex';
import {RootState, JobState} from '../../types';
import {Job} from '@/types/toolkit/jobs';

const getters: GetterTree<JobState, RootState> = {
    jobs(state): Job[] {
        return state.jobs;
    },
};

export default getters;
