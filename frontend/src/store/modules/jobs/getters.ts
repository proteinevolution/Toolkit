import {GetterTree} from 'vuex';
import {JobState, RootState} from '../../types';
import {Job} from '@/types/toolkit/jobs';
import {maxBy} from 'lodash-es';

const getters: GetterTree<JobState, RootState> = {
    jobs(state): Job[] {
        return state.jobs;
    },
    recentJob(state): Job | undefined {
        return maxBy(state.jobs, 'dateUpdated');
    },
};

export default getters;
