<template>
    <Loading v-if="loading" :message="t('loading')" />
    <div v-else>
        <div v-if="downloadEnabled || forwardingEnabled" class="result-options">
            <a v-if="downloadEnabled" @click="download">{{ t('jobs.results.actions.download') }}</a>
            <a v-if="forwardingEnabled" @click="forwardAll">{{ t('jobs.results.actions.forwardAll') }}</a>
        </div>

        <pre class="file-view" v-html="file"></pre>
    </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import useResultTab from '@/composables/useResultTab';
import Loading from '@/components/utils/Loading.vue';
import Logger from 'js-logger';
import { resultsService } from '@/services/ResultsService';
import { timeout } from '@/util/Utils';
import { ModalParams } from '@/types/toolkit/utils';
import { useEventBus } from '@vueuse/core';
import { isNonNullable, isNullable } from '@/util/nullability-helpers';
import { useI18n } from 'vue-i18n';
import { Job, JobViewOptions } from '@/types/toolkit/jobs';
import { Tool } from '@/types/toolkit/tools';

const logger = Logger.get('DataTab');
const { t } = useI18n();

interface ResultTabProps {
    job: Job;
    tool: Tool;
    fullScreen?: boolean;
    viewOptions?: JobViewOptions;
    resultTabName?: string;
    renderOnCreate?: boolean;
}

const props = withDefaults(defineProps<ResultTabProps>(), {
    resultTabName: '',
    renderOnCreate: true,
});

const downloadEnabled = computed<boolean>(() => isNonNullable(props.viewOptions) && 'download' in props.viewOptions);
const forwardingEnabled = computed<boolean>(
    () => isNonNullable(props.viewOptions) && 'forwarding' in props.viewOptions
);

const file = ref('');
const maxTries = 50;
const tries = ref(0);

const filename = computed<string>(() => {
    const name = props.viewOptions?.filename;
    if (isNullable(name)) {
        return '';
    }
    return name.replace(':jobID', props.job.jobID);
});

async function init() {
    file.value = await resultsService.getFile(props.job.jobID, filename.value);
    if (isNonNullable(file.value)) {
        ++tries.value;
        if (tries.value === maxTries) {
            logger.info("Couldn't fetch files.");
            return;
        }
        await timeout(300);
        await init();
    }
}

const { loading } = useResultTab({ init, resultTabName: props.resultTabName, renderOnCreate: props.renderOnCreate });

function download(): void {
    const toolName = props.tool.name;
    const ending = toolName === 'hhpred' || toolName === 'hhomp' ? 'hhr' : 'out';
    const downloadFilename = `${toolName}_${props.job.jobID}.${ending}`;
    resultsService.downloadFile(props.job.jobID, filename.value, downloadFilename).catch((e) => {
        logger.error(e);
    });
}

const showModalsBus = useEventBus<ModalParams>('show-modal');

function forwardAll(): void {
    if (props.tool.parameters) {
        showModalsBus.emit({
            id: 'forwardingModal',
            props: {
                forwardingJobID: props.job.jobID,
                forwardingData: file.value,
                forwardingMode: props.tool.parameters.forwarding,
            },
        });
    } else {
        logger.error('tool parameters not loaded. Cannot forward');
    }
}
</script>

<style lang="scss" scoped>
.file-view {
    width: 100%;
    font-size: 12px;
    height: 50vh;
    font-family: $font-family-monospace;
}

.fullscreen .file-view {
    height: 85vh;
}
</style>
