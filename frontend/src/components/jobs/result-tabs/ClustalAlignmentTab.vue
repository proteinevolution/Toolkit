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
            <a @click="downloadAlignment"> {{ t('jobs.results.actions.downloadMSA') }}</a>
            <a :href="downloadFilePath" target="_blank"> {{ t('jobs.results.actions.exportMSA') }}</a>
            <a :class="{ active: color }" @click="toggleColor"> {{ t('jobs.results.actions.colorMSA') }}</a>
        </div>

        <div class="alignment-results mb-4">
            <p v-html="t('jobs.results.alignment.numSeqs', { num: total })"></p>
            <div class="table-responsive">
                <table>
                    <tbody>
                        <template v-for="(group, groupI) in brokenAlignments">
                            <tr v-for="elem in group" :key="groupI + '-' + elem.num">
                                <td>
                                    <b-form-checkbox
                                        :checked="selected.includes(elem.num)"
                                        @change="selectedChanged(elem.num)" />
                                </td>
                                <td class="accession">
                                    <b v-text="elem.accession.slice(0, 20)"></b>
                                </td>
                                <td class="sequence" v-html="coloredSeq(elem.seq)"></td>
                            </tr>
                            <tr :key="'hits-' + groupI">
                                <td v-if="groupI === 0 && alignments.length !== total" colspan="3">
                                    <Loading
                                        v-if="loadingMore"
                                        :message="t('jobs.results.alignment.loadingHits')"
                                        justify="center"
                                        class="mt-4" />
                                    <intersection-observer @intersect="intersected" />
                                </td>
                            </tr>

                            <tr v-if="groupI < brokenAlignments.length - 1" :key="'blank-' + groupI" class="blank-row">
                                <td colspan="3"></td>
                            </tr>
                        </template>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import { defineResultTabProps } from '@/composables/useResultTab';
import { AlignmentItem } from '@/types/toolkit/results';
import Loading from '@/components/utils/Loading.vue';
import { resultsService } from '@/services/ResultsService';
import Logger from 'js-logger';
import { colorSequence } from '@/util/SequenceUtils';
import IntersectionObserver from '@/components/utils/IntersectionObserver.vue';
import { isNullable } from '@/util/nullability-helpers';
import { useI18n } from 'vue-i18n';
import useAlignmentResultTab from '@/composables/useAlignmentResultTab';

const logger = Logger.get('ClustalAlignmentTab');

const { t } = useI18n();

const props = defineResultTabProps();

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
} = useAlignmentResultTab({ logger, props });

const color = ref(false);

function toggleColor(): void {
    color.value = !color.value;
}

function coloredSeq(seq: string): string {
    if (color.value) {
        return colorSequence(seq);
    } else {
        return seq;
    }
}

function downloadAlignment(): void {
    const downloadFilename = `${props.tool.name}_alignment_${props.job.jobID}.clustal`;
    resultsService.downloadFile(props.job.jobID, 'alignment.clustalw_aln', downloadFilename).catch((e) => {
        logger.error(e);
    });
}

const downloadFilePath = computed(() => resultsService.getDownloadFilePath(props.job.jobID, 'alignment.clustalw_aln'));

const breakAfter = 85; // clustal format breaks after n chars
const brokenAlignments = computed<AlignmentItem[][]>(() => {
    if (isNullable(alignments.value)) {
        return [];
    }
    // alignments need to be broken into pieces
    const res: AlignmentItem[][] = [];
    for (const a of alignments.value) {
        let breakIt = 0;
        while (breakIt * breakAfter < a.seq.length) {
            if (!res[breakIt]) {
                res[breakIt] = [];
            }
            res[breakIt].push(
                Object.assign({}, a, {
                    seq: a.seq.slice(breakIt * breakAfter, (breakIt + 1) * breakAfter),
                })
            );
            breakIt++;
        }
    }
    return res;
});
</script>

<style lang="scss" scoped>
.alignment-results {
    font-size: 0.9em;

    td {
        padding: 0 2rem 0 0;
    }

    .blank-row td {
        height: 2.5rem;
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
