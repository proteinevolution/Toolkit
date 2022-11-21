<template>
    <Loading v-if="loading || !results" :message="t('loading')" />
    <div v-else>
        <b v-if="results.results.length === 0" v-html="t('jobs.results.hhrepid.noResults')"></b>
        <div v-else>
            <div class="result-options">
                <a @click="forwardQueryA3M">{{ t('jobs.results.actions.forwardQueryA3M') }}</a>
            </div>
            <template v-for="(hit, i) in results.results.reptypes" :key="'hit-results-' + i">
                <h4
                    :key="'hit-' + i"
                    class="mb-4"
                    v-text="t('jobs.results.hhrepid.resultsForType', { type: hit.typ })"></h4>
                <img :key="hit.typ" :src="getFilePath(hit.typ)" class="mb-3" alt="" />

                <table :key="'hit-table-' + i" class="alignment-table mt-2">
                    <tbody>
                        <tr>
                            <td v-text="t('jobs.results.hhrepid.numResults', { num: hit.num })"></td>
                        </tr>
                        <tr>
                            <td v-text="t('jobs.results.hhrepid.pValue', { pval: hit.pval })"></td>
                        </tr>
                        <tr>
                            <td v-text="t('jobs.results.hhrepid.length', { len: hit.len })"></td>
                        </tr>
                    </tbody>
                </table>
                <table :key="'hit-table2-' + i" class="alignment-table mt-4">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Prob</th>
                            <th>P-val</th>
                            <th>Loc</th>
                            <th>Sequence</th>
                        </tr>
                    </thead>
                    <tbody>
                        <template
                            v-for="hitIdx in breakIndices(hit.len)"
                            :key="'hit-' + i + '-rows-' + hitIdx + '-' + rep.id">
                            <tr
                                v-for="rep in hit.reps"
                                :key="'hit-' + i + '-seqal-' + hitIdx + '-' + rep.id"
                                class="sequence-alignment">
                                <td v-text="rep.id"></td>
                                <td v-text="rep.prob"></td>
                                <td v-text="rep.pval"></td>
                                <td v-text="rep.loc"></td>
                                <td v-html="colorSequence(rep.seq.slice(hitIdx, hitIdx + breakAfter))"></td>
                            </tr>
                            <tr :key="'br-' + hitIdx" class="empty-row"></tr>
                        </template>
                    </tbody>
                </table>
                <br :key="'hit-br-' + i" />
            </template>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import useResultTab, { defineResultTabProps } from '@/composables/useResultTab';
import Loading from '@/components/utils/Loading.vue';
import { resultsService } from '@/services/ResultsService';
import Logger from 'js-logger';
import { HhrepidResults } from '@/types/toolkit/results';
import { colorSequence } from '@/util/SequenceUtils';
import { ModalParams } from '@/types/toolkit/utils';
import { useI18n } from 'vue-i18n';
import { useEventBus } from '@vueuse/core';
import { isNullable } from '@/util/nullability-helpers';

const logger = Logger.get('HhrepidResultsTab');

const { t } = useI18n();

const props = defineResultTabProps();

const results = ref<HhrepidResults | undefined>(undefined);
const file = ref('');

const filename = computed(() => {
    const name = props.viewOptions?.filename;
    if (isNullable(name)) {
        return '';
    }
    return name.replace(':jobID', props.job.jobID);
});

async function init() {
    results.value = await resultsService.fetchResults(props.job.jobID);
    file.value = await resultsService.getFile(props.job.jobID, filename.value);
}

const { loading } = useResultTab({ init, resultTabName: props.resultTabName, renderOnCreate: props.renderOnCreate });

function getFilePath(type: string): string {
    return resultsService.getDownloadFilePath(props.job.jobID, `query_${type}.png`);
}

const showModalsBus = useEventBus<ModalParams>('show-modal');

function forwardQueryA3M(): void {
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

const breakAfter = 80;

function breakIndices(length: number): number[] {
    const res: number[] = [];
    for (let i = 0; i < length; i += breakAfter) {
        res.push(i);
    }
    return res;
}
</script>

<style lang="scss" scoped>
img {
    max-width: 100%;
}

.alignment-table {
    width: 100%;
    @include media-breakpoint-up(xl) {
        width: 100%;
    }
    font-size: 0.85em;

    .sequence-alignment {
        font-family: $font-family-monospace;
        letter-spacing: 0.05em;
    }
}

.empty-row {
    height: 2em;
}
</style>
