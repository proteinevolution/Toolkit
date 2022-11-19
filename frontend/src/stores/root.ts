import { MaintenanceState } from '@/types/toolkit/auth';
import { defineStore } from 'pinia';
import { backendService } from '@/services/BackendService';
import { useStorage } from '@vueuse/core';
import Vue, { computed, reactive, ref, watch } from 'vue';
import useToolkitWebsocket from '@/composables/useToolkitWebsocket';
import { Job } from '@/types/toolkit/jobs';
import { useJobsStore } from '@/stores/jobs';

export interface LoadingState {
    [key: string]: boolean;
}

export const useRootStore = defineStore('root', () => {
    const loading: LoadingState = reactive({
        maintenanceState: false,
        tools: false,
        toolParameters: false,
        jobs: false,
        jobDetails: false,
        alignmentTextarea: false,
        login: false,
        logout: false,
    });
    const offscreenMenuShow = ref(false);
    const tourFinished = useStorage<boolean>('tourFinished', false);
    const maintenance: MaintenanceState = reactive({
        message: '',
        submitBlocked: false,
    });
    const clusterWorkload = ref(0);
    // allow for update of human-readable time by updating reference point
    const now = ref(Date.now());

    async function fetchMaintenance() {
        loading.maintenanceState = true;
        Object.assign(maintenance, await backendService.fetchMaintenanceState());
        loading.maintenanceState = false;
    }

    // Store related websocket methods, others can be found in App.vue
    const jobsStore = useJobsStore();
    const { data, status } = useToolkitWebsocket();
    const reconnecting = computed(() => status.value !== 'OPEN');
    watch(
        data,
        (json) => {
            switch (json.mutation) {
                case 'SOCKET_UpdateLoad':
                    clusterWorkload.value = json.load;
                    break;
                case 'SOCKET_MaintenanceAlert':
                    Object.assign(maintenance, json.maintenanceState);
                    break;
                case 'SOCKET_ClearJob':
                    jobsStore.jobs = jobsStore.jobs.filter((job: Job) => job.jobID !== json.jobID);
                    break;
                case 'SOCKET_UpdateJob': {
                    const { job } = json;
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
                    break;
                }
            }
        },
        { deep: false }
    );

    return {
        loading,
        offscreenMenuShow,
        tourFinished,
        maintenance,
        fetchMaintenance,
        reconnecting,
        clusterWorkload,
        now,
    };
});
