<template>
    <Loading v-if="loading" :message="t('loading')" />
    <div v-else class="font-small">
        <b v-if="total === 0" v-text="t('jobs.results.psiblast.noResults')"></b>
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
                <a @click="download">{{ t('jobs.results.actions.downloadMSA') }}</a>
                <a :class="{ active: color }" @click="toggleColor">{{ t('jobs.results.actions.colorSeqs') }}</a>
                <a :class="{ active: wrap }" @click="toggleWrap">{{ t('jobs.results.actions.wrapSeqs') }}</a>
            </div>

            <div v-html="t('jobs.results.psiblast.numHits', { num: info.num_hits })"></div>

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
                            <template v-for="(al, i) in alignments" :key="'rows-' + al.num">
                                <tr :ref="registerScrollRef('alignment-' + al.num)" class="blank-row">
                                    <td colspan="4">
                                        <hr v-if="i !== 0" />
                                    </td>
                                </tr>
                                <tr>
                                    <td></td>
                                    <td colspan="3" v-html="al.fastaLink"></td>
                                </tr>
                                <tr class="font-weight-bold">
                                    <td class="no-wrap">
                                        <b-checkbox
                                            class="d-inline"
                                            :checked="selectedItems.includes(al.num)"
                                            @change="check($event, al.num)" />
                                        <span v-text="al.num + '.'"></span>
                                    </td>
                                    <td colspan="3" v-html="al.acc + ' ' + al.name"></td>
                                </tr>
                                <tr>
                                    <td></td>
                                    <td colspan="3" v-html="t('jobs.results.psiblast.alignmentInfo', al)"></td>
                                </tr>

                                <template
                                    v-for="(alPart, alIdx) in wrapAlignments(al)"
                                    :key="'alignment-rows-' + i + '-' + alIdx">
                                    <tr class="blank-row">
                                        <td></td>
                                    </tr>
                                    <tr v-if="alPart.query.seq" class="sequence">
                                        <td></td>
                                        <td>Q</td>
                                        <td v-text="alPart.query.start"></td>
                                        <td v-html="coloredSeq(alPart.query.seq) + alEnd(alPart.query)"></td>
                                    </tr>
                                    <tr v-if="alPart.agree" class="sequence">
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                        <td class="consensus-agree" v-text="alPart.agree"></td>
                                    </tr>
                                    <tr v-if="alPart.template.seq" class="sequence">
                                        <td></td>
                                        <td>T</td>
                                        <td v-text="alPart.template.start"></td>
                                        <td v-html="coloredSeq(alPart.template.seq) + alEnd(alPart.template)"></td>
                                    </tr>
                                    <tr class="blank-row">
                                        <td></td>
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
import { toRef } from 'vue';
import Loading from '@/components/utils/Loading.vue';
import HitListTable from '@/components/jobs/result-tabs/sections/HitListTable.vue';
import HitMap from '@/components/jobs/result-tabs/sections/HitMap.vue';
import IntersectionObserver from '@/components/utils/IntersectionObserver.vue';
import { PSIBLASTAlignmentItem, PsiblastHHInfoResult } from '@/types/toolkit/results';
import { resultsService } from '@/services/ResultsService';
import useSearchResultTab from '@/composables/useSearchResultTab';
import Logger from 'js-logger';
import { defineResultTabProps } from '@/composables/useResultTab';
import { useI18n } from 'vue-i18n';
import { isNonNullable } from '@/util/nullability-helpers';

const logger = Logger.get('PsiblastResultsTab');

const { t } = useI18n();

const props = defineResultTabProps();
const job = toRef(props, 'job');

function alignmentItemToRenderInfo(
    al: PSIBLASTAlignmentItem,
    start: number,
    end: number,
    qStart: number,
    qEnd: number,
    tStart: number,
    tEnd: number,
    qSeq: string,
    tSeq: string
) {
    return {
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
    };
}

function onInitialized(): void {
    if (info.value) {
        for (let i = 1; i <= info.value.belowEvalThreshold; i++) {
            selectedItems.value.push(i);
        }
    }
}

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
    wrapAlignments,
    color,
    toggleColor,
    coloredSeq,
    alEnd,
    registerScrollRef,
    scrollTo,
    scrollToElem,
    resubmitSection,
    forward,
} = useSearchResultTab<PSIBLASTAlignmentItem, PsiblastHHInfoResult>({
    logger,
    props,
    breakAfter: 90,
    alignmentItemToRenderInfo,
    onInitialized,
});

const hitListFields = [
    {
        key: 'numCheck',
        label: t('jobs.results.psiblast.table.num'),
        sortable: true,
    },
    {
        key: 'acc',
        label: t('jobs.results.psiblast.table.accession'),
        sortable: true,
    },
    {
        key: 'name',
        label: t('jobs.results.psiblast.table.description'),
        sortable: true,
    },
    {
        key: 'eval',
        label: t('jobs.results.psiblast.table.eValue'),
        class: 'no-wrap',
        sortable: true,
    },
    {
        key: 'bitScore',
        label: t('jobs.results.psiblast.table.bitscore'),
        sortable: true,
    },
    {
        key: 'refLen',
        label: t('jobs.results.psiblast.table.ref_len'),
        sortable: true,
    },
    {
        key: 'hitLen',
        label: t('jobs.results.psiblast.table.hit_len'),
        sortable: true,
    },
];

function download(): void {
    const name = props.viewOptions?.filename;
    if (isNonNullable(name)) {
        const toolName = props.tool.name;
        const downloadFilename = `${toolName}_${props.job.jobID}.aln`;
        resultsService.downloadFile(props.job.jobID, name, downloadFilename).catch((e) => {
            logger.error(e);
        });
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

    .sequence {
        td {
            word-break: keep-all;
            white-space: nowrap;
            font-family: $font-family-monospace;
            padding: 0 1rem 0 0;
        }

        .consensus-agree {
            white-space: pre-wrap;
        }
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
