<template>
    <Loading v-if="loading || !accIds" :message="t('loading')" />
    <div v-else>
        <div class="result-options">
            <a @click="forwardAll">{{ t('jobs.results.actions.forwardAll') }}</a>
            <a @click="download">{{ t('jobs.results.actions.download') }}</a>
        </div>

        <div class="file-view">
            <b v-text="t('jobs.results.seq2ID.numRetrieved', { num: accIds.length })"></b>
            <br /><br />
            <div v-for="(acc, i) in accIds" :key="'accession-' + i" v-text="acc"></div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import useResultTab, { defineResultTabProps } from '@/composables/useResultTab';
import Loading from '@/components/utils/Loading.vue';
import Logger from 'js-logger';
import { resultsService } from '@/services/ResultsService';
import { timeout } from '@/util/Utils';
import { ModalParams } from '@/types/toolkit/utils';
import { useI18n } from 'vue-i18n';
import { useEventBus } from '@vueuse/core';
import { isNonNullable } from '@/util/nullability-helpers';

const logger = Logger.get('Seq2IDResultsTab');

const { t } = useI18n();

const props = defineResultTabProps();

const accIds = ref<string[]>([]);
const maxTries = 50;
const tries = ref(0);

async function init() {
    const data: any = await resultsService.getFile(props.job.jobID, 'ids.json');
    if (data) {
        accIds.value = data.ACC_IDS;
    } else {
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
    const downloadFilename = `${props.tool.name}_${props.job.jobID}.fasta`;
    resultsService.downloadAsFile(accIds.value.join('\n'), downloadFilename);
}

const showModalsBus = useEventBus<ModalParams>('show-modal');

function forwardAll(): void {
    if (isNonNullable(props.tool.parameters)) {
        showModalsBus.emit({
            id: 'forwardingModal',
            props: {
                forwardingJobID: props.job.jobID,
                forwardingData: accIds.value.join('\n'),
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
    font-family: $font-family-monospace;
}

.fullscreen .file-view {
    height: 70vh;
}
</style>
