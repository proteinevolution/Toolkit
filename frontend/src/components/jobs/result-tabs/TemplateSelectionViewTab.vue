<template>
    <Loading v-if="loading" :message="t('loading')" />
    <div v-else>
        <pre class="file-view" v-html="file"></pre>
        <div class="result-options">
            <b-btn type="button" variant="primary" class="submit-button float-right" @click="forwardToModeller">
                {{ t('jobs.results.actions.forwardToModeller') }}
            </b-btn>
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import useResultTab, { defineResultTabProps } from '@/composables/useResultTab';
import Loading from '@/components/utils/Loading.vue';
import { resultsService } from '@/services/ResultsService';
import { useI18n } from 'vue-i18n';
import { useRouter } from 'vue-router';
import { useEventBus } from '@vueuse/core';

const { t } = useI18n();
const router = useRouter();

const props = defineResultTabProps();

const file = ref('');

async function init() {
    file.value = await resultsService.getFile(props.job.jobID, 'tomodel.pir');
}

const { loading } = useResultTab({ init, resultTabName: props.resultTabName, renderOnCreate: props.renderOnCreate });

const pasteAreaLoadedBus = useEventBus<void>('paste-area-loaded');
const forwardDataBus = useEventBus<{ data: string; jobID: string }>('forward-data');

function pasteForwardData(): void {
    pasteAreaLoadedBus.off(pasteForwardData);
    forwardDataBus.emit({ data: file.value, jobID: props.job.jobID });
}

function forwardToModeller(): void {
    router.push('/tools/modeller');
    pasteAreaLoadedBus.on(pasteForwardData);
}
</script>

<style lang="scss" scoped>
.result-options {
    border-bottom: none;
    border-top: 1px solid rgba(10, 10, 10, 0.1);
}

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
