import Vue from 'vue';
import {MutationTree} from 'vuex';
import {JobState} from '../../types';
import {Job} from '@/types/toolkit/jobs';
import {WebSocketActions} from '@/types/toolkit/enums';
import Logger from 'js-logger';

const logger = Logger.get('JobStore');

const mutations: MutationTree<JobState> = {
    setJobs(state, jobs) {
        state.jobs = jobs;
    },
    setJob(state, {jobID, job}) {
        const index: number = state.jobs.findIndex((j) => j.jobID === jobID);
        const existingJob: Job = state.jobs[index];
        if (index < 0) {
            state.jobs.push(job);
        } else {
            Vue.set(state.jobs, index, Object.assign(existingJob, job));
        }
    },
    toggleJobWatched(state, {jobID}) {
        const job = state.jobs.find((j) => j.jobID === jobID);
        if (job) {
            if (job.watched) {
                logger.info('unsubscribing from job');
                Vue.prototype.$socket.sendObj({
                    type: WebSocketActions.UNSUBSCRIBE,
                    jobIDs: [jobID],
                });
            } else {
                logger.info('subscribing to job');
                Vue.prototype.$socket.sendObj({
                    type: WebSocketActions.SUBSCRIBE,
                    jobIDs: [jobID],
                });
            }
        }
    },
    removeJob(state, {jobID}) {
        state.jobs = state.jobs.filter((job: Job) => job.jobID !== jobID);
    },
    SOCKET_ClearJob(state, {jobID}) {
        state.jobs = state.jobs.filter((job: Job) => job.jobID !== jobID);
    },
    SOCKET_UpdateJob(state, {job}) {
        const index: number = state.jobs.findIndex((j) => j.jobID === job.jobID);
        if (index < 0) {
            state.jobs.push(job);
        } else {
            Vue.set(state.jobs, index, job);
        }
    },
};

export default mutations;
