<template>
    <Loading v-if="loading" :message="$t('loading')" />
    <div v-else class="font-small">
        <b v-if="total === 0" v-text="$t('jobs.results.psiblast.noResults')"></b>
        <div v-else>
            <div class="result-options">
                <a @click="scrollTo('visualization')">{{ $t('jobs.results.hitlist.visLink') }}</a>
                <a @click="scrollTo('hits')">{{ $t('jobs.results.hitlist.hitsLink') }}</a>
                <a class="mr-4" @click="scrollTo('alignments')">{{ $t('jobs.results.hitlist.alnLink') }}</a>
                <a class="border-right mr-4"></a>
                <a :class="{ active: allSelected }" @click="toggleAllSelected">
                    {{ $t('jobs.results.actions.selectAll') }}</a
                >
                <a @click="forward(false)">{{ $t('jobs.results.actions.forward') }}</a>
                <a @click="download">{{ $t('jobs.results.actions.downloadMSA') }}</a>
                <a :class="{ active: color }" @click="toggleColor">{{ $t('jobs.results.actions.colorSeqs') }}</a>
                <a :class="{ active: wrap }" @click="toggleWrap">{{ $t('jobs.results.actions.wrapSeqs') }}</a>
            </div>

            <div v-html="$t('jobs.results.psiblast.numHits', { num: info.num_hits })"></div>

            <div v-if="info.coil === '0' || info.tm > '0' || info.signal === '1'" class="mt-2">
                {{ $t('jobs.results.sequenceFeatures.header') }}
                <b v-if="info.coil === '0'" v-html="$t('jobs.results.sequenceFeatures.coil')"></b>
                <b v-if="info.tm > '0'" v-html="$t('jobs.results.sequenceFeatures.tm')"></b>
                <b v-if="info.signal === '1'" v-html="$t('jobs.results.sequenceFeatures.signal')"></b>
            </div>

            <div ref="visualization" class="result-section">
                <h4>{{ $t('jobs.results.hitlist.vis') }}</h4>
                <hit-map :job="job" @elem-clicked="scrollToElem" @resubmit-section="resubmitSection" />
            </div>

            <div ref="hits" class="result-section">
                <h4 class="mb-4">
                    {{ $t('jobs.results.hitlist.hits') }}
                </h4>
                <hit-list-table
                    :job="job"
                    :fields="hitListFields"
                    :selected-items="selectedItems"
                    @elem-clicked="scrollToElem" />
            </div>

            <div ref="alignments" class="result-section">
                <h4>{{ $t('jobs.results.hitlist.aln') }}</h4>

                <div ref="scrollElem" class="table-responsive">
                    <table class="alignments-table">
                        <tbody>
                            <template v-for="(al, i) in alignments">
                                <tr :key="'alignment-' + al.num" :ref="'alignment-' + al.num" class="blank-row">
                                    <td colspan="4">
                                        <hr v-if="i !== 0" />
                                    </td>
                                </tr>
                                <tr :key="'alignment-fasta-link-' + i">
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
                                <tr :key="'alignment-info-' + i">
                                    <td></td>
                                    <td colspan="3" v-html="$t('jobs.results.psiblast.alignmentInfo', al)"></td>
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
                                        <td></td>
                                        <td></td>
                                        <td class="consensus-agree" v-text="alPart.agree"></td>
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
                                    <tr :key="'alignment-' + i + '-br-' + alIdx" class="blank-row">
                                        <td></td>
                                    </tr>
                                </template>
                            </template>

                            <tr v-if="alignments.length !== total">
                                <td colspan="4">
                                    <Loading
                                        v-if="loadingMore"
                                        :message="$t('jobs.results.alignment.loadingHits')"
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

<script lang="ts">
import Loading from '@/components/utils/Loading.vue';
import Logger from 'js-logger';
import HitListTable from '@/components/jobs/result-tabs/sections/HitListTable.vue';
import HitMap from '@/components/jobs/result-tabs/sections/HitMap.vue';
import IntersectionObserver from '@/components/utils/IntersectionObserver.vue';
import { PSIBLASTAlignmentItem, PsiblastHHInfoResult, SearchAlignmentItemRender } from '@/types/toolkit/results';
import SearchResultTabMixin from '@/mixins/SearchResultTabMixin';
import { resultsService } from '@/services/ResultsService';

const logger = Logger.get('PsiblastResultsTab');

export default SearchResultTabMixin.extend({
    name: 'PsiblastResultsTab',
    components: {
        Loading,
        HitListTable,
        HitMap,
        IntersectionObserver,
    },
    data() {
        return {
            alignments: undefined as PSIBLASTAlignmentItem[] | undefined,
            info: undefined as PsiblastHHInfoResult | undefined,
            breakAfter: 90,
            hitListFields: [
                {
                    key: 'numCheck',
                    label: this.$t('jobs.results.psiblast.table.num'),
                    sortable: true,
                },
                {
                    key: 'acc',
                    label: this.$t('jobs.results.psiblast.table.accession'),
                    sortable: true,
                },
                {
                    key: 'name',
                    label: this.$t('jobs.results.psiblast.table.description'),
                    sortable: true,
                },
                {
                    key: 'eval',
                    label: this.$t('jobs.results.psiblast.table.eValue'),
                    class: 'no-wrap',
                    sortable: true,
                },
                {
                    key: 'bitScore',
                    label: this.$t('jobs.results.psiblast.table.bitscore'),
                    sortable: true,
                },
                {
                    key: 'refLen',
                    label: this.$t('jobs.results.psiblast.table.ref_len'),
                    sortable: true,
                },
                {
                    key: 'hitLen',
                    label: this.$t('jobs.results.psiblast.table.hit_len'),
                    sortable: true,
                },
            ],
        };
    },
    methods: {
        async init(): Promise<void> {
            await this.loadAlignments(0, this.perPage);
            if (this.info) {
                for (let i = 1; i <= this.info.belowEvalThreshold; i++) {
                    this.selectedItems.push(i);
                }
            }
        },
        wrapAlignments(al: PSIBLASTAlignmentItem): SearchAlignmentItemRender[] {
            if (this.wrap) {
                const res: SearchAlignmentItemRender[] = [];
                let qStart: number = al.query.start;
                let tStart: number = al.template.start;
                for (let start = 0; start < al.query.seq.length; start += this.breakAfter) {
                    const end: number = start + this.breakAfter;
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
        },
        download(): void {
            if (this.viewOptions.filename) {
                const toolName = this.tool.name;
                const downloadFilename = `${toolName}_${this.job.jobID}.aln`;
                resultsService.downloadFile(this.job.jobID, this.viewOptions.filename, downloadFilename).catch((e) => {
                    logger.error(e);
                });
            }
        },
    },
});
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
