<template>
    <Loading v-if="loading" :message="t('loading')" />
    <div v-else class="font-small">
        <b v-if="total === 0" v-text="t('jobs.results.hmmer.noResults')"></b>
        <div v-else>
            <div class="result-options">
                <a @click="scrollTo('visualization')">{{ t('jobs.results.hitlist.visLink') }}</a>
                <a @click="scrollTo('hits')">{{ t('jobs.results.hitlist.hitsLink') }}</a>
                <a class="mr-4" @click="scrollTo('alignments')">{{ t('jobs.results.hitlist.alnLink') }}</a>
                <a class="border-right mr-4"></a>
                <a :class="{ active: allSelected }" @click="toggleAllSelected">
                    {{ t('jobs.results.actions.selectAll') }}</a
                >
                <a @click="forward(false)">{{ t('jobs.results.actions.forward') }}</a>
                <a :class="{ active: color }" @click="toggleColor">{{ t('jobs.results.actions.colorSeqs') }}</a>
                <a :class="{ active: wrap }" @click="toggleWrap">{{ t('jobs.results.actions.wrapSeqs') }}</a>
            </div>

            <div v-html="t('jobs.results.hmmer.numHits', { num: total })"></div>

            <div v-if="info.coil === '0' || info.tm > '0' || info.signal === '1'" class="mt-2">
                {{ t('jobs.results.sequenceFeatures.header') }}
                <b v-if="info.coil === '0'" v-html="t('jobs.results.sequenceFeatures.coil')"></b>
                <b v-if="info.tm > '0'" v-html="t('jobs.results.sequenceFeatures.tm')"></b>
                <b v-if="info.signal === '1'" v-html="t('jobs.results.sequenceFeatures.signal')"></b>
            </div>

            <div :ref="registerScrollRef('visualization')" class="result-section">
                <h4>{{ t('jobs.results.hitlist.vis') }}</h4>
                <hit-map :job="job" @elem-clicked="scrollToElem" @resubmit-section="resubmitSection" />
            </div>

            <div :ref="registerScrollRef('hits')" class="result-section">
                <h4 class="mb-4">
                    {{ t('jobs.results.hitlist.hits') }}
                </h4>
                <hit-list-table
                    :job="job"
                    :fields="hitListFields"
                    :selected-items="selectedItems"
                    @elem-clicked="scrollToElem" />
            </div>

            <div :ref="registerScrollRef('alignments')" class="result-section">
                <h4>{{ t('jobs.results.hitlist.aln') }}</h4>

                <div ref="scrollElem" class="table-responsive">
                    <table class="alignments-table">
                        <tbody>
                            <template v-for="(al, i) in alignments">
                                <tr
                                    :key="'alignment-' + al.num"
                                    :ref="registerScrollRef('alignment-' + al.num)"
                                    class="blank-row">
                                    <td colspan="4">
                                        <hr v-if="i !== 0" />
                                    </td>
                                </tr>
                                <tr :key="'alignment-fasta-' + i">
                                    <td></td>
                                    <td colspan="3" v-html="al.fastaLink"></td>
                                </tr>
                                <tr :key="'alignment-num-' + i" class="font-weight-bold">
                                    <td class="no-wrap">
                                        <b-checkbox
                                            class="d-inline"
                                            :checked="selectedItems.includes(al.num)"
                                            @change="check($event, al.num)" />
                                        <span v-text="al.num + '.'"></span>
                                    </td>
                                    <td colspan="3" v-html="al.acc + ' ' + al.name"></td>
                                </tr>
                                <tr :key="'alignment-alinf-' + i">
                                    <td></td>
                                    <td colspan="3" v-html="t('jobs.results.hmmer.alignmentInfo', al)"></td>
                                </tr>

                                <template v-for="(alPart, alIdx) in wrapAlignments(al)">
                                    <tr :key="'alignment-' + i + '-blank-' + alIdx" class="blank-row">
                                        <td></td>
                                    </tr>
                                    <tr
                                        v-if="alPart.query.seq"
                                        :key="'alignment-' + i + '-seq-' + alIdx"
                                        class="sequence">
                                        <td></td>
                                        <td>Q</td>
                                        <td v-text="alPart.query.start"></td>
                                        <td v-html="coloredSeq(alPart.query.seq) + alEnd(alPart.query)"></td>
                                    </tr>
                                    <tr
                                        v-if="alPart.agree"
                                        :key="'alignment-' + i + '-agree-' + alIdx"
                                        class="sequence">
                                        <td></td>
                                        <td>PP</td>
                                        <td></td>
                                        <td v-text="alPart.agree"></td>
                                    </tr>
                                    <tr
                                        v-if="alPart.template.seq"
                                        :key="'alignment-' + i + '-tplseq-' + alIdx"
                                        class="sequence">
                                        <td></td>
                                        <td>T</td>
                                        <td v-text="alPart.template.start"></td>
                                        <td v-html="coloredSeq(alPart.template.seq) + alEnd(alPart.template)"></td>
                                    </tr>
                                </template>
                            </template>

                            <tr v-if="alignments.length !== total">
                                <td colspan="4">
                                    <Loading
                                        v-if="loadingMore"
                                        :message="t('jobs.results.alignment.loadingHits')"
                                        justify="center"
                                        class="mt-4" />
                                    <intersection-observer @intersect="intersected" />
                                </td>
                            </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup lang="ts">
import Loading from '@/components/utils/Loading.vue';
import HitListTable from '@/components/jobs/result-tabs/sections/HitListTable.vue';
import HitMap from '@/components/jobs/result-tabs/sections/HitMap.vue';
import IntersectionObserver from '@/components/utils/IntersectionObserver.vue';
import { HMMERAlignmentItem, HMMERHHInfoResult, SearchAlignmentItemRender } from '@/types/toolkit/results';
import useSearchResultTab from '@/composables/useSearchResultTab';
import Logger from 'js-logger';
import { defineResultTabProps } from '@/composables/useResultTab';
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';

const logger = Logger.get('HmmerResultsTab');

const { t } = useI18n();

const props = defineResultTabProps();

const job = computed(() => props.job);

const {
    alignments,
    info,
    total,
    loading,
    loadingMore,
    selectedItems,
    allSelected,
    toggleAllSelected,
    intersected,
    check,
    wrap,
    toggleWrap,
    color,
    toggleColor,
    coloredSeq,
    alEnd,
    registerScrollRef,
    scrollTo,
    scrollToElem,
    resubmitSection,
    forward,
} = useSearchResultTab<HMMERAlignmentItem, HMMERHHInfoResult>({ logger, props });

const hitListFields = [
    {
        key: 'numCheck',
        label: t('jobs.results.hmmer.table.num'),
        sortable: true,
    },
    {
        key: 'acc',
        label: t('jobs.results.hmmer.table.accession'),
        sortable: true,
    },
    {
        key: 'name',
        label: t('jobs.results.hmmer.table.description'),
        sortable: true,
    },
    {
        key: 'fullEval',
        label: t('jobs.results.hmmer.table.full_evalue'),
        class: 'no-wrap',
        sortable: true,
    },
    {
        key: 'eval',
        label: t('jobs.results.hmmer.table.eValue'),
        class: 'no-wrap',
        sortable: true,
    },
    {
        key: 'bitScore',
        label: t('jobs.results.hmmer.table.bitscore'),
        sortable: true,
    },
    {
        key: 'hitLen',
        label: t('jobs.results.hmmer.table.hit_len'),
        sortable: true,
    },
];

const breakAfter = 90;

function wrapAlignments(al: HMMERAlignmentItem): SearchAlignmentItemRender[] {
    if (wrap.value) {
        const res: SearchAlignmentItemRender[] = [];
        let qStart: number = al.query.start;
        let tStart: number = al.template.start;
        for (let start = 0; start < al.query.seq.length; start += breakAfter) {
            const end: number = start + breakAfter;
            const qSeq: string = al.query.seq.slice(start, end);
            const tSeq: string = al.template.seq.slice(start, end);
            const qEnd: number = qStart + qSeq.length - (qSeq.match(/[-.]/g) || []).length - 1;
            const tEnd: number = tStart + tSeq.length - (tSeq.match(/[-.]/g) || []).length - 1;
            res.push({
                agree: al.agree.slice(start, end),
                query: {
                    end: qEnd,
                    seq: qSeq,
                    start: qStart,
                },
                template: {
                    end: tEnd,
                    seq: tSeq,
                    start: tStart,
                },
            });
            qStart = qEnd + 1;
            tStart = tEnd + 1;
        }
        return res;
    } else {
        return [al];
    }
}
</script>

<style lang="scss" scoped>
.result-section {
    padding-top: 3.5rem;
}

.alignments-table {
    font-size: 0.95em;

    .blank-row {
        height: 0.8rem;
    }

    .sequence td {
        word-break: keep-all;
        white-space: nowrap;
        font-family: $font-family-monospace;
        padding: 0 1rem 0 0;
    }

    a {
        cursor: pointer;
        color: $primary;

        &:hover {
            color: $tk-dark-green;
        }
    }
}
</style>
