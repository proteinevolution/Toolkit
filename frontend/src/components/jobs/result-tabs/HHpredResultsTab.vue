<template>
    <Loading v-if="loading" :message="t('loading')" />
    <div v-else class="font-small">
        <b v-if="total === 0" v-text="t('jobs.results.hhpred.noResults')"></b>
        <div v-else>
            <div class="result-options">
                <a @click="scrollTo('visualization')">{{ t('jobs.results.hitlist.visLink') }}</a>
                <a @click="scrollTo('hits')">{{ t('jobs.results.hitlist.hitsLink') }}</a>
                <a class="mr-4" @click="scrollTo('alignments')">{{ t('jobs.results.hitlist.alnLink') }}</a>
                <a class="border-right mr-4"></a>
                <a :class="{ active: allSelected }" @click="toggleAllSelected">
                    {{ t('jobs.results.actions.selectAll') }}</a
                >
                <a @click="forward(true)">{{ t('jobs.results.actions.forward') }}</a>
                <a @click="forwardQueryA3M">{{ t('jobs.results.actions.forwardQueryA3M') }}</a>
                <a v-if="info.modeller" @click="modelSelection" v-text="t('jobs.results.actions.model')"></a>
                <a @click="download" v-text="t('jobs.results.actions.downloadHHR')"></a>
                <a :class="{ active: color }" @click="toggleColor">{{ t('jobs.results.actions.colorSeqs') }}</a>
                <a :class="{ active: wrap }" @click="toggleWrap">{{ t('jobs.results.actions.wrapSeqs') }}</a>
            </div>

            <div v-html="t('jobs.results.hhpred.numHits', { num: info.num_hits })"></div>
            <div v-html="t('jobs.results.hhpred.queryNeff', { num: info.query_neff })"></div>

            <div v-if="info.coil === '0' || info.tm > '0' || info.signal === '1'" class="mt-2">
                {{ t('jobs.results.sequenceFeatures.header') }}
                <b v-if="info.coil === '0'" v-html="t('jobs.results.sequenceFeatures.coil')"></b>
                <b v-if="info.tm > '0'" v-html="t('jobs.results.sequenceFeatures.tm')"></b>
                <b v-if="info.signal === '1'" v-html="t('jobs.results.sequenceFeatures.signal')"></b>
            </div>

            <div v-if="info.qa3m_count < '10'" class="mt-2">
                <b class="mt-2" v-html="t('jobs.results.hhpred.qa3mWarning', { num: info.qa3m_count })"></b>
                <b v-if="info.msa_gen === 'uniclust30'" v-html="t('jobs.results.hhpred.uniclustWarning')"></b>
                <b v-if="info.msa_gen === 'psiblast'" v-html="t('jobs.results.hhpred.psiblastWarning')"></b>
                <b v-if="info.msa_gen === 'custom'" v-html="t('jobs.results.hhpred.customWarning')"></b>
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
                                    <td colspan="3">
                                        <a
                                            @click="displayTemplateAlignment(al.template.accession)"
                                            v-text="t('jobs.results.hhpred.templateAlignment')"></a>
                                        <a
                                            v-if="al.structLink"
                                            class="db-list"
                                            @click="displayTemplateStructure(al.template.accession)"
                                            v-text="t('jobs.results.hhpred.templateStructure')"></a>
                                        <span v-if="al.dbLink" class="db-list" v-html="al.dbLink"></span>
                                    </td>
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
                                    <td colspan="3" v-html="t('jobs.results.hhpred.alignmentInfo', al)"></td>
                                </tr>

                                <template
                                    v-for="(alPart, alIdx) in wrapAlignments(al)"
                                    :key="'alignment-rows-' + i + '-' + alIdx">
                                    <tr class="blank-row">
                                        <td></td>
                                    </tr>
                                    <tr v-if="alPart.query.ss_pred" class="sequence">
                                        <td></td>
                                        <td>Q ss_pred</td>
                                        <td></td>
                                        <td v-html="coloredSeqSS(alPart.query.ss_pred)"></td>
                                    </tr>
                                    <tr v-if="alPart.query.seq" class="sequence">
                                        <td></td>
                                        <td v-text="'Q ' + alPart.query.name"></td>
                                        <td v-text="alPart.query.start"></td>
                                        <td v-html="coloredSeq(alPart.query.seq) + alEndRef(alPart.query)"></td>
                                    </tr>
                                    <tr v-if="alPart.query.consensus" class="sequence">
                                        <td></td>
                                        <td>Q Consensus</td>
                                        <td v-text="alPart.query.start"></td>
                                        <td v-html="alPart.query.consensus + alEndRef(alPart.query)"></td>
                                    </tr>
                                    <tr v-if="alPart.agree" class="sequence">
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                        <td class="consensus-agree" v-text="alPart.agree"></td>
                                    </tr>
                                    <tr v-if="alPart.template.consensus" class="sequence">
                                        <td></td>
                                        <td>T Consensus</td>
                                        <td v-text="alPart.template.start"></td>
                                        <td v-html="alPart.template.consensus + alEndRef(alPart.template)"></td>
                                    </tr>
                                    <tr v-if="alPart.template.seq" class="sequence">
                                        <td></td>
                                        <td v-text="'T ' + alPart.template.accession"></td>
                                        <td v-text="alPart.template.start"></td>
                                        <td v-html="coloredSeq(alPart.template.seq) + alEndRef(alPart.template)"></td>
                                    </tr>
                                    <tr v-if="alPart.template.ss_dssp" class="sequence">
                                        <td></td>
                                        <td>T ss_dssp</td>
                                        <td></td>
                                        <td v-html="coloredSeqSS(alPart.template.ss_dssp)"></td>
                                    </tr>
                                    <tr v-if="alPart.template.ss_pred" class="sequence">
                                        <td></td>
                                        <td>T ss_pred</td>
                                        <td></td>
                                        <td v-html="coloredSeqSS(alPart.template.ss_pred)"></td>
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
import Loading from '@/components/utils/Loading.vue';
import HitListTable from '@/components/jobs/result-tabs/sections/HitListTable.vue';
import HitMap from '@/components/jobs/result-tabs/sections/HitMap.vue';
import IntersectionObserver from '@/components/utils/IntersectionObserver.vue';
import { HHpredAlignmentItem, HHpredHHInfoResult } from '@/types/toolkit/results';
import { jobService } from '@/services/JobService';
import { resultsService } from '@/services/ResultsService';
import useToolkitNotifications from '@/composables/useToolkitNotifications';
import useSearchResultTab from '@/composables/useSearchResultTab';
import Logger from 'js-logger';
import { defineResultTabProps } from '@/composables/useResultTab';
import { computed, toRef } from 'vue';
import { useI18n } from 'vue-i18n';
import { isNonNullable, isNullable } from '@/util/nullability-helpers';
import { useRouter } from 'vue-router';

const logger = Logger.get('HHpredResultsTab');

const { t } = useI18n();
const { alert } = useToolkitNotifications();

const props = defineResultTabProps();
const job = toRef(props, 'job');

function alignmentItemToRenderInfo(
    al: HHpredAlignmentItem,
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
            consensus: al.query.consensus.slice(start, end),
            end: qEnd,
            name: al.query.name,
            ref: al.query.ref,
            seq: qSeq,
            ss_dssp: al.query.ss_dssp.slice(start, end),
            ss_pred: al.query.ss_pred.slice(start, end),
            start: qStart,
        },
        template: {
            accession: al.template.accession,
            consensus: al.template.consensus.slice(start, end),
            end: tEnd,
            ref: al.template.ref,
            seq: tSeq,
            ss_dssp: al.template.ss_dssp.slice(start, end),
            ss_pred: al.template.ss_pred.slice(start, end),
            start: tStart,
        },
    };
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
    coloredSeqSS,
    alEndRef,
    registerScrollRef,
    scrollTo,
    scrollToElem,
    resubmitSection,
    displayTemplateAlignment,
    forward,
    forwardQueryA3M,
    showModalsBus,
} = useSearchResultTab<HHpredAlignmentItem, HHpredHHInfoResult>({
    logger,
    props,
    breakAfter: 80,
    alignmentItemToRenderInfo,
    initialColor: true,
});

const hitListFields = [
    {
        key: 'numCheck',
        label: t('jobs.results.hhpred.table.num'),
        sortable: true,
    },
    {
        key: 'acc',
        label: t('jobs.results.hhpred.table.hit'),
        sortable: true,
    },
    {
        key: 'name',
        label: t('jobs.results.hhpred.table.name'),
        sortable: true,
    },
    {
        key: 'probab',
        label: t('jobs.results.hhpred.table.probHits'),
        sortable: true,
    },
    {
        key: 'eval',
        label: t('jobs.results.hhpred.table.eVal'),
        class: 'no-wrap',
        sortable: true,
    },
    {
        key: 'score',
        label: t('jobs.results.hhpred.table.score'),
        sortable: true,
    },
    {
        key: 'ssScore',
        label: t('jobs.results.hhpred.table.ssScore'),
        sortable: true,
    },
    {
        key: 'alignedCols',
        label: t('jobs.results.hhpred.table.cols'),
        sortable: true,
    },
    {
        key: 'templateRef',
        label: t('jobs.results.hhpred.table.targetLength'),
        sortable: true,
    },
];

function displayTemplateStructure(accession: string): void {
    showModalsBus.emit({
        id: 'templateStructureModal',
        props: { accessionStructure: accession },
    });
}

const filename = computed(() => {
    const name = props.viewOptions?.filename;
    if (isNullable(name)) {
        return '';
    }
    return name.replace(':jobID', props.job.jobID);
});

function download(): void {
    const toolName = props.tool.name;
    const downloadFilename = `${toolName}_${props.job.jobID}.hhr`;
    resultsService.downloadFile(props.job.jobID, filename.value, downloadFilename).catch((e) => {
        logger.error(e);
    });
}

const router = useRouter();

async function modelSelection(): Promise<void> {
    if (isNullable(alignments.value)) {
        return;
    }

    const selected: number[] = Array.from(selectedItems.value);
    if (selected.length < 1) {
        selected.push(alignments.value[0].num);
        alert(t('jobs.results.hhpred.modelUsingFirst'), 'warning');
    }

    if (isNonNullable(info.value)) {
        const submission: any = {
            parentID: props.job.jobID,
            templates: selected.join(' '),
            alnHash: info.value.alignmentHash,
        };
        try {
            const response = await jobService.submitJob('hhpred_manual', submission);
            router.push(`/jobs/${response.jobID}`);
        } catch (e) {
            logger.error('Could not submit job', e);
            alert(t('errors.general'), 'danger');
        }
    }
}
</script>

<style lang="scss" scoped>
.result-section {
    padding-top: 3.5rem;
}

.result-options {
    a {
        @include media-breakpoint-up(lg) {
            margin-right: 1.9rem;
        }
    }
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

.db-list {
    border-left: 1px solid;
    border-left-color: $tk-gray;
    margin-left: 0.5em;
    padding-left: 0.5em;
}
</style>
