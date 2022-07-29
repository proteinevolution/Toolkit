import {Job} from '@/types/toolkit/jobs';
import {defineStore} from 'pinia';
import {useStorage} from '@vueuse/core';
import {useRootStore} from '@/stores/root';
import Vue from 'vue';
import {jobService} from '@/services/JobService';
import {WebSocketActions} from '@/types/toolkit/enums';
import Logger from 'js-logger';
import {maxBy} from 'lodash-es';

const logger = Logger.get('JobStore');

export interface JobState {
    jobs: Job[];
}

export const useJobsStore = defineStore('jobs', {
    state: () => ({
        jobs: useStorage<Job[]>('jobs', []),
    }),
    getters: {
        watchedJobs(state): Job[] {
            return state.jobs.filter((j: Job) => j.watched);
        },
        ownedJobs(state): Job[] {
            return state.jobs.filter((j: Job) => !j.foreign);
        },
        recentJob(state): Job | undefined {
            return maxBy(state.jobs, 'dateUpdated');
        },
    },
    actions: {
        async fetchAllJobs() {
            const rootStore = useRootStore();
            rootStore.loading.jobs = true;
            this.jobs = await jobService.fetchJobs();
            rootStore.loading.jobs = false;
        },
        async loadJobDetails(jobID: string) {
            const rootStore = useRootStore();
            rootStore.loading.jobDetails = true;
            const job: Job = await jobService.fetchJob(jobID);
            this.setJob(jobID, job);
            Vue.prototype.$socket.sendObj({
                type: WebSocketActions.SET_JOB_WATCHED,
                jobID,
                watched: true,
            });
            rootStore.loading.jobDetails = true;
        },
        async setJobPublic(jobID: string, isPublic: boolean) {
            logger.info(`Setting job.isPublic to ${isPublic} for job id ${jobID}`);
            await jobService.setJobPublic(jobID, isPublic);
        },
        setJobWatched(jobID: string, watched: boolean) {
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
        setJob(jobID: string, job: Job) {
            const index: number = this.jobs.findIndex((j) => j.jobID === jobID);
            if (index < 0) {
                this.jobs.push(job);
            } else {
                Vue.set(this.jobs, index, job);
            }
        },
        removeJob(jobID: string) {
            this.jobs = this.jobs.filter((job: Job) => job.jobID !== jobID);
        },
        SOCKET_ClearJob(jobID: string) {
            this.jobs = this.jobs.filter((job: Job) => job.jobID !== jobID);
        },
        SOCKET_UpdateJob(job: Job) {
            const index: number = this.jobs.findIndex((j) => j.jobID === job.jobID);
            if (index < 0) {
                this.jobs.push(job);
            } else {
                // the websocket does not push paramValues
                if (!job.paramValues && this.jobs[index].paramValues) {
                    job.paramValues = this.jobs[index].paramValues;
                }
                Vue.set(this.jobs, index, job);
            }
        },
    }
});
