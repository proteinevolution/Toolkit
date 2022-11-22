import { computed } from 'vue';
import { Job } from '@/types/toolkit/jobs';
import { defineStore } from 'pinia';
import { useStorage } from '@vueuse/core';
import { useRootStore } from '@/stores/root';
import { jobService } from '@/services/JobService';
import { WebSocketActions } from '@/types/toolkit/enums';
import Logger from 'js-logger';
import { maxBy } from 'lodash-es';
import useToolkitWebsocket from '@/composables/useToolkitWebsocket';

const logger = Logger.get('JobStore');

export const useJobsStore = defineStore('jobs', () => {
    const jobs = useStorage<Job[]>('jobs', []);

    const watchedJobs = computed(() => jobs.value.filter((j: Job) => j.watched));
    const ownedJobs = computed(() => jobs.value.filter((j: Job) => !j.foreign));
    const recentJob = computed<Job | undefined>(() => maxBy(jobs.value, 'dateUpdated'));

    const rootStore = useRootStore();

    async function fetchAllJobs() {
        rootStore.loading.jobs = true;
        jobs.value = await jobService.fetchJobs();
        rootStore.loading.jobs = false;
    }

    function setJob(jobID: string, job: Job) {
        const index: number = jobs.value.findIndex((j) => j.jobID === jobID);
        if (index < 0) {
            jobs.value.push(job);
        } else {
            jobs.value[index] = job;
        }
    }

    async function loadJobDetails(jobID: string) {
        rootStore.loading.jobDetails = true;
        const job: Job = await jobService.fetchJob(jobID);
        setJob(jobID, job);
        const { send } = useToolkitWebsocket();
        send({
            type: WebSocketActions.SET_JOB_WATCHED,
            jobID,
            watched: true,
        });
        rootStore.loading.jobDetails = true;
    }

    async function setJobPublic(jobID: string, isPublic: boolean) {
        logger.info(`Setting job.isPublic to ${isPublic} for job id ${jobID}`);
        await jobService.setJobPublic(jobID, isPublic);
    }

    function setJobWatched(jobID: string, watched: boolean) {
        const { send } = useToolkitWebsocket();
        send({
            type: WebSocketActions.SET_JOB_WATCHED,
            jobID,
            watched,
        });
        if (watched) {
            logger.info('unsubscribing from job');
        } else {
            logger.info('subscribing to job');
        }
    }

    function removeJob(jobID: string) {
        jobs.value = jobs.value.filter((job: Job) => job.jobID !== jobID);
    }

    return {
        jobs,
        watchedJobs,
        ownedJobs,
        recentJob,
        fetchAllJobs,
        loadJobDetails,
        setJobPublic,
        setJobWatched,
        removeJob,
    };
});
