import Vue from 'vue';
import {ActionTree} from 'vuex';
import {RootState, JobState} from '../../types';
import {Job} from '@/types/toolkit/jobs';
import {AlignmentItem} from '@/types/toolkit/results';
import {jobService} from '@/services/JobService';
import {WebSocketActions} from '@/types/toolkit/enums';
import Logger from 'js-logger';
import {resultsService} from '@/services/ResultsService';

const logger = Logger.get('JobStore');

const actions: ActionTree<JobState, RootState> = {
    async fetchAllJobs(context) {
        context.commit('startLoading', 'jobs', {root: true});
        const jobs: Job[] = await jobService.fetchJobs();
        context.commit('setJobs', jobs);
        context.commit('stopLoading', 'jobs', {root: true});
    },
    async loadJobDetails(context, jobID: string) {
        context.commit('startLoading', 'jobDetails', {root: true});
        const job: Job = await jobService.fetchJob(jobID);
        context.commit('setJob', {jobID, job});
        Vue.prototype.$socket.sendObj({
            type: WebSocketActions.SET_JOB_WATCHED,
            jobID,
            watched: true,
        });
        context.commit('stopLoading', 'jobDetails', {root: true});
    },
    async loadJobAlignments(context, jobID: string) {
        const alignments: AlignmentItem[] = await resultsService.fetchAlignmentResults(jobID);
        context.commit('setJobAlignments', {jobID, alignments});
    },
    async setJobPublic(context, {jobID, isPublic}) {
        logger.info(`Setting job.isPublic to ${isPublic} for job id ${jobID}`);
        await jobService.setJobPublic(jobID, isPublic);
    },
    setJobWatched(state, {jobID, watched}) {
        Vue.prototype.$socket.sendObj({
            type: WebSocketActions.SET_JOB_WATCHED,
            jobID,
            watched,
        });
        if (watched) {
            logger.info('unsubscribing from job');
        } else {
            logger.info('subscribing to job');
        }
    },
};

export default actions;
