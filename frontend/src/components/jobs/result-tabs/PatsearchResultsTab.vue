<template>
    <Loading v-if="loading || !results" :message="t('loading')" />
    <div v-else class="font-small">
        <b v-if="results.results.hits.length === 0" v-text="t('jobs.results.patsearch.noResults')"></b>
        <div v-else>
            <div class="result-options">
                <a @click="download">{{ t('jobs.results.actions.downloadHits') }}</a>
                <a @click="forwardAll">{{ t('jobs.results.actions.forwardAll') }}</a>
            </div>

            <span v-html="t('jobs.results.alignment.numSeqs', { num: results.results.hits.length })"></span>

            <table class="alignment-table mt-3">
                <tbody>
                    <template v-for="(hit, i) in results.results.hits">
                        <tr :key="'hit-name-' + i">
                            <td>
                                <b v-text="hit.name"></b>
                            </td>
                        </tr>
                        <tr :key="'hit-seq-' + i">
                            <td class="sequence-alignment" v-html="patsearchColor(hit.seq, hit.matches)"></td>
                        </tr>
                    </template>
                </tbody>
            </table>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import useResultTab, { defineResultTabProps } from '@/composables/useResultTab';
import Loading from '@/components/utils/Loading.vue';
import { resultsService } from '@/services/ResultsService';
import Logger from 'js-logger';
import { PatsearchHit, PatsearchResults } from '@/types/toolkit/results';
import { patsearchColor } from '@/util/SequenceUtils';
import { ModalParams } from '@/types/toolkit/utils';
import { useEventBus } from '@vueuse/core';
import { useI18n } from 'vue-i18n';
import { isNonNullable, isNullable } from '@/util/nullability-helpers';

const logger = Logger.get('PatsearchResultsTab');

const { t } = useI18n();

const props = defineResultTabProps();

const results = ref<PatsearchResults | undefined>(undefined);

async function init() {
    results.value = await resultsService.fetchResults(props.job.jobID);
}

const { loading } = useResultTab({ init, resultTabName: props.resultTabName, renderOnCreate: props.renderOnCreate });

const filename = computed(() => {
    const name = props.viewOptions?.filename;
    if (isNullable(name)) {
        return '';
    }
    return name.replace(':jobID', props.job.jobID);
});

function download(): void {
    const toolName = props.tool.name;
    const downloadFilename = `${toolName}_${props.job.jobID}.fas`;
    resultsService.downloadFile(props.job.jobID, filename.value, downloadFilename).catch((e) => {
        logger.error(e);
    });
}

const showModalsBus = useEventBus<ModalParams>('show-modal');

function forwardAll(): void {
    if (isNonNullable(props.tool.parameters) && isNonNullable(results.value)) {
        showModalsBus.emit({
            id: 'forwardingModal',
            props: {
                forwardingJobID: props.job.jobID,
                forwardingData: results.value.results.hits.reduce(
                    (acc: string, cur: PatsearchHit) => acc + cur.name + '\n' + cur.seq + '\n',
                    ''
                ),
                forwardingMode: props.tool.parameters.forwarding,
            },
        });
    } else {
        logger.error('tool parameters not loaded. Cannot forward');
    }
}
</script>

<style lang="scss" scoped>
.alignment-table {
    font-size: 0.9em;

    .sequence-alignment {
        font-family: $font-family-monospace;
        letter-spacing: 0.05em;
        word-break: break-all;
        white-space: unset;

        .pattern-match {
            color: red;
            background-color: rgba(255, 0, 8, 0.1);
        }
    }
}
</style>

<style lang="scss">
.pattern-match {
    color: red;
    background-color: rgba(255, 0, 8, 0.1);
}
</style>
