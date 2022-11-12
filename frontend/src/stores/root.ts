import {MaintenanceState} from '@/types/toolkit/auth';
import {defineStore} from 'pinia';
import {backendService} from '@/services/BackendService';
import Vue from 'vue';
import Logger from 'js-logger';
import {pinia} from '@/stores/index';
import {Job} from '@/types/toolkit/jobs';
import {useJobsStore} from '@/stores/jobs';
import {useStorage} from '@vueuse/core';

const logger = Logger.get('Store');

export interface LoadingState {
    [key: string]: boolean;
}

export interface RootState {
    loading: LoadingState;
    offscreenMenuShow: boolean;
    tourFinished: boolean;
    maintenance: MaintenanceState;
    reconnecting: boolean;
    clusterWorkload: number;
    now: number;
}

export const useRootStore = defineStore('root', {
    state: () => ({
        loading: {
            maintenanceState: false,
            tools: false,
            toolParameters: false,
            jobs: false,
            jobDetails: false,
            alignmentTextarea: false,
            login: false,
            logout: false,
        },
        offscreenMenuShow: false,
        tourFinished: useStorage<boolean>('tourFinished', false),
        maintenance: {
            message: '',
            submitBlocked: false,
        },
        reconnecting: true,
        clusterWorkload: 0,
        // allow for update of human-readable time by updating reference point
        now: Date.now(),
    }),
    actions: {
        async fetchMaintenance() {
            this.loading.maintenanceState = true;
            this.maintenance = await backendService.fetchMaintenanceState();
            this.loading.maintenanceState = false;
        },
        SOCKET_RECONNECT(event: any) {
            logger.log('Trying to reconnect websocket', event);
        },
        SOCKET_RECONNECT_ERROR(event: any) {
            logger.error('Could not reconnect websocket', event);
        },
        SOCKET_ONOPEN(event: any) {
            Vue.prototype.$socket = event.currentTarget;
            this.reconnecting = false;
        },
        SOCKET_ONCLOSE() {
            this.reconnecting = true;
        },
        SOCKET_ONERROR(event: any) {
            logger.error('Websocket error', event);
        },
        SOCKET_UpdateLoad(message: any) {
            this.clusterWorkload = message.load;
        },
        SOCKET_MaintenanceAlert(maintenanceState: MaintenanceState) {
            // notification handled in App.vue
            logger.log('Maintenance alert', maintenanceState);
            this.maintenance = maintenanceState;
        },
        SOCKET_ONMESSAGE(message: any) {
            logger.log('Uncaught message from websocket', message);
        },
        SOCKET_Login() {
            logger.log('Logged in by websocket');
            // handled in App.vue
        },
        SOCKET_Logout() {
            logger.log('Logged out by websocket');
            // handled in App.vue
        },
        SOCKET_ShowNotification() {
            // handled in App.vue
        },
        SOCKET_ShowJobNotification() {
            // handled in App.vue
        },
        SOCKET_WatchLogFile() {
            // handled in JobRunningTab.vue
        },
        SOCKET_ClearJob({jobID}: { jobID: string }) {
            const jobsStore = useJobsStore();
            jobsStore.jobs = jobsStore.jobs.filter((job: Job) => job.jobID !== jobID);
        },
        SOCKET_UpdateJob({job}: { job: Job }) {
            const jobsStore = useJobsStore();
            const index: number = jobsStore.jobs.findIndex((j) => j.jobID === job.jobID);
            if (index < 0) {
                jobsStore.jobs.push(job);
            } else {
                // the websocket does not push paramValues
                if (!job.paramValues && jobsStore.jobs[index].paramValues) {
                    job.paramValues = jobsStore.jobs[index].paramValues;
                }
                Vue.set(jobsStore.jobs, index, job);
            }
        },
    }
});

// Need to be used outside the setup
export function useRootStoreWithout() {
    return useRootStore(pinia);
}
