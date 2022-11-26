<template>
    <div>
        <h3 class="mb-3 h5" v-text="t('jobs.stateMessages.running')"></h3>
        <p v-html="t('jobs.citationInfo', { tool: tool.longname })"></p>
        <p class="mb-3" v-text="t('jobs.jobIDDetails', job)"></p>
        <table v-for="logElem in runningLog" :key="logElem.text" class="job-log-element mb-2">
            <tr>
                <td>
                    <i :class="logElem.class" class="fas text-center mr-1"></i>
                </td>
                <td>{{ logElem.text }}</td>
            </tr>
        </table>
    </div>
</template>

<script setup lang="ts">
import { ref, Ref, watch } from 'vue';
import { parseProcessLog } from '@/util/Utils';
import { Job, ProcessLogItem } from '@/types/toolkit/jobs';
import useToolkitWebsocket from '@/composables/useToolkitWebsocket';
import { useI18n } from 'vue-i18n';
import { Tool } from '@/types/toolkit/tools';

interface JobRunningTabProps {
    job: Job;
    tool: Tool;
}

const props = defineProps<JobRunningTabProps>();

const { t } = useI18n();

const runningLog: Ref<ProcessLogItem[]> = ref([]);

const { data } = useToolkitWebsocket();
watch(
    data,
    (json) => {
        if (json.mutation === 'SOCKET_WatchLogFile') {
            if (json.jobID === props.job.jobID) {
                runningLog.value = parseProcessLog(json.lines);
            }
        }
    },
    { deep: false }
);
</script>

<style lang="scss" scoped>
.job-log-element {
    display: flex;
    align-items: center;

    i {
        width: 32px;
        height: 18px;
        margin-top: 0.2em;
        font-size: 1.3em;
    }

    .running {
        background: url('../../../assets/images/ellipsis.gif') no-repeat center center;
    }

    .done {
        color: rgba(40, 120, 111, 0.636);

        &:before {
            content: '\f058';
        }
    }

    .error {
        color: #da4453;

        &:before {
            content: '\f057';
        }
    }
}
</style>
