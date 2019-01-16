import {GetterTree} from 'vuex';
import {JobState, RootState} from '../../types';
import {Job} from '@/types/toolkit/jobs';

const getters: GetterTree<JobState, RootState> = {
    jobs(state): Job[] {
        return state.jobs;
    },
    recentJob(state): Job | undefined {
        if (state.jobs.length === 0) {
            return undefined;
        }
        return state.jobs.sort((a: Job, b: Job) => {
            if (!a.dateUpdated) {
                return 1;
            } else if (!b.dateUpdated) {
                return -1;
            } else {
                return b.dateUpdated - a.dateUpdated;
            }
        })[0];
    },
};

export default getters;
