<template>
    <Loading v-if="loading || !alignments" :message="t('jobs.results.alignment.loadingHits')" />
    <div v-else>
        <div class="result-options">
            <a :class="{ active: allSelected }" @click="toggleAllSelected">
                {{ t('jobs.results.actions.selectAll') }}</a
            >
            <a :disabled="selected.length === 0" @click="forwardSelected">
                {{ t('jobs.results.actions.forwardSelected') }}</a
            >
            <a v-if="!isReduced" @click="download(downloadMSAFilename, downloadMSAFile)">
                {{ t('jobs.results.actions.downloadMSA') }}</a
            >
            <a v-if="isReduced" @click="download(downloadReducedA3MFilename, downloadReducedA3MFile)">
                {{ t('jobs.results.actions.downloadReducedA3M') }}</a
            >
            <a v-if="isReduced" @click="download(downloadFullA3MFilename, downloadFullA3MFile)">
                {{ t('jobs.results.actions.downloadFullA3M') }}</a
            >
            <a v-if="!isReduced" :href="downloadMSAFileDownloadPath" target="_blank">
                {{ t('jobs.results.actions.exportMSA') }}</a
            >
        </div>

        <div class="alignment-results mb-4">
            <p v-html="t(alignmentNumTextKey, { num: total, reduced: viewOptions.reduced })"></p>
            <div class="table-responsive">
                <table>
                    <tbody>
                        <template v-for="(elem, index) in alignments">
                            <tr :key="'header' + elem.num">
                                <td class="d-flex align-items-center">
                                    <b-form-checkbox
                                        :checked="selected.includes(elem.num)"
                                        @change="selectedChanged(elem.num)" />
                                    <b class="ml-2" v-text="index + 1 + '.'"></b>
                                </td>
                                <td class="accession">
                                    <b v-text="elem.accession"></b>
                                </td>
                            </tr>
                            <tr
                                v-for="(part, partI) in elem.seq.match(/.{1,95}/g)"
                                :key="'sequence' + elem.num + '-' + partI">
                                <td></td>
                                <td class="sequence" v-text="part"></td>
                            </tr>
                        </template>
                    </tbody>
                </table>
            </div>
            <div v-if="alignments.length !== total">
                <Loading
                    v-if="loadingMore"
                    :message="t('jobs.results.alignment.loadingHits')"
                    justify="center"
                    class="mt-4" />
                <intersection-observer @intersect="intersected" />
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { defineResultTabProps } from '@/composables/useResultTab';
import Loading from '@/components/utils/Loading.vue';
import { resultsService } from '@/services/ResultsService';
import Logger from 'js-logger';
import IntersectionObserver from '@/components/utils/IntersectionObserver.vue';
import useAlignmentResultTab from '@/composables/useAlignmentResultTab';
import { useI18n } from 'vue-i18n';
import { isNullable } from '@/util/nullability-helpers';

const logger = Logger.get('FastaAlignmentTab');

const { t } = useI18n();

const props = defineResultTabProps();
const viewOptions = props.viewOptions;

const resultField = computed(() => viewOptions?.resultField ?? 'alignment');

const {
    intersected,
    alignments,
    selected,
    allSelected,
    selectedChanged,
    toggleAllSelected,
    total,
    loading,
    loadingMore,
    forwardSelected,
} = useAlignmentResultTab({ logger, props, resultField });

const downloadMSAFile = 'alignment.fas';
const downloadMSAFileDownloadPath = computed(() =>
    resultsService.getDownloadFilePath(props.job.jobID, downloadMSAFile)
);
const downloadMSAFilename = computed(() => `${props.tool.name}_${resultField.value}_${props.job.jobID}.fasta`);

const downloadReducedA3MFile = computed(() => (isNullable(viewOptions) ? '' : viewOptions.reducedFilename + '.a3m'));
const downloadReducedA3MFilename = computed(
    () => `${props.tool.name}_${viewOptions?.reducedFilename ?? ''}_${props.job.jobID}.a3m`
);
const downloadFullA3MFile = computed(() => (isNullable(viewOptions) ? '' : viewOptions.fullFilename + '.a3m'));
const downloadFullA3MFilename = computed(
    () => `${props.tool.name}_${viewOptions?.fullFilename ?? ''}_${props.job.jobID}.a3m`
);
const isReduced = computed(() => viewOptions?.reduced);
const alignmentNumTextKey = computed(() => `jobs.results.alignment.numSeqs${isReduced.value ? 'Reduced' : ''}`);

function download(downloadFilename: string, path: string): void {
    resultsService.downloadFile(props.job.jobID, path, downloadFilename).catch((e) => {
        logger.error(e);
    });
}
</script>

<style lang="scss" scoped>
.alignment-results {
    font-size: 0.9em;

    td {
        padding-right: 0.5rem;
    }

    .accession {
        font-size: 0.9em;
    }

    .sequence {
        font-family: $font-family-monospace;
        letter-spacing: 0.025em;
        font-size: 0.75rem;
        white-space: pre;
    }
}
</style>
