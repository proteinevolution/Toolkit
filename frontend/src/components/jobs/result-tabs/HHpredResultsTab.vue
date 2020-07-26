<template>
    <Loading v-if="loading"
             :message="$t('loading')" />
    <div v-else
         class="font-small">
        <b v-if="total === 0"
           v-text="$t('jobs.results.hhpred.noResults')"></b>
        <div v-else>
            <div class="result-options">
                <a @click="scrollTo('visualization')">{{ $t('jobs.results.hitlist.visLink') }}</a>
                <a @click="scrollTo('hits')">{{ $t('jobs.results.hitlist.hitsLink') }}</a>
                <a class="mr-4"
                   @click="scrollTo('alignments')">{{ $t('jobs.results.hitlist.alnLink') }}</a>
                <a class="border-right mr-4"></a>
                <a :class="{active: allSelected}"
                   @click="toggleAllSelected">
                    {{ $t('jobs.results.actions.selectAll') }}</a>
                <a @click="forward(true)">{{ $t('jobs.results.actions.forward') }}</a>
                <a @click="forwardQueryA3M">{{ $t('jobs.results.actions.forwardQueryA3M') }}</a>
                <a v-if="info.modeller"
                   @click="modelSelection"
                   v-text="$t('jobs.results.actions.model')"></a>
                <a @click="download"
                   v-text="$t('jobs.results.actions.downloadHHR')"></a>
                <a :class="{active: color}"
                   @click="toggleColor">{{ $t('jobs.results.actions.colorSeqs') }}</a>
                <a :class="{active: wrap}"
                   @click="toggleWrap">{{ $t('jobs.results.actions.wrapSeqs') }}</a>
            </div>

            <div v-html="$t('jobs.results.hhpred.numHits', {num: info.num_hits})"></div>

            <div v-if="info.coil === '0' || info.tm > '0' || info.signal === '1'"
                 class="mt-2">
                {{ $t('jobs.results.sequenceFeatures.header') }}
                <b v-if="info.coil === '0'"
                   v-html="$t('jobs.results.sequenceFeatures.coil')"></b>
                <b v-if="info.tm > '0'"
                   v-html="$t('jobs.results.sequenceFeatures.tm')"></b>
                <b v-if="info.signal === '1'"
                   v-html="$t('jobs.results.sequenceFeatures.signal')"></b>
            </div>

            <div v-if="info.qa3m_count < '10'"
                 class="mt-2">
                <b class="mt-2"
                   v-html="$t('jobs.results.hhpred.qa3mWarning', {num: info.qa3m_count})"></b>
                <b v-if="info.msa_gen === 'uniclust30'"
                   v-html="$t('jobs.results.hhpred.uniclustWarning')"></b>
                <b v-if="info.msa_gen === 'psiblast'"
                   v-html="$t('jobs.results.hhpred.psiblastWarning')"></b>
                <b v-if="info.msa_gen === 'custom'"
                   v-html="$t('jobs.results.hhpred.customWarning')"></b>
            </div>


            <div ref="visualization"
                 class="result-section">
                <h4>{{ $t('jobs.results.hitlist.vis') }}</h4>
                <hit-map :job="job"
                         @elem-clicked="scrollToElem"
                         @resubmit-section="resubmitSection" />
            </div>

            <div ref="hits"
                 class="result-section">
                <h4 class="mb-4">
                    {{ $t('jobs.results.hitlist.hits') }}
                </h4>
                <hit-list-table :job="job"
                                :fields="hitListFields"
                                :selected-items="selectedItems"
                                @elem-clicked="scrollToElem" />
            </div>
            <div ref="alignments"
                 class="result-section">
                <h4>{{ $t('jobs.results.hitlist.aln') }}</h4>

                <div ref="scrollElem"
                     class="table-responsive">
                    <table class="alignments-table">
                        <tbody>
                            <template v-for="(al, i) in alignments">
                                <tr :key="'alignment-' + al.num"
                                    :ref="'alignment-' + al.num"
                                    class="blank-row">
                                    <td colspan="4">
                                        <hr v-if="i !== 0">
                                    </td>
                                </tr>
                                <tr :key="'alignment-acc-' + i">
                                    <td></td>
                                    <td colspan="3">
                                        <a @click="displayTemplateAlignment(al.template.accession)"
                                           v-text="$t('jobs.results.hhpred.templateAlignment')"></a>
                                        <a v-if="al.structLink"
                                           class="db-list"
                                           @click="displayTemplateStructure(al.template.accession)"
                                           v-text="$t('jobs.results.hhpred.templateStructure')"></a>
                                        <span v-if="al.dbLink"
                                              class="db-list"
                                              v-html="al.dbLink"></span>
                                    </td>
                                </tr>
                                <tr :key="'alignment-check-' + i"
                                    class="font-weight-bold">
                                    <td class="no-wrap">
                                        <b-checkbox class="d-inline"
                                                    :checked="selectedItems.includes(al.num)"
                                                    @change="check($event, al.num)" />
                                        <span v-text="al.num + '.'"></span>
                                    </td>
                                    <td colspan="3"
                                        v-html="al.acc + ' ' + al.name"></td>
                                </tr>

                                <tr :key="'alignment-alInf-' + i">
                                    <td></td>
                                    <td colspan="3"
                                        v-html="$t('jobs.results.hhpred.alignmentInfo', al)"></td>
                                </tr>

                                <template v-for="(alPart, alIdx) in wrapAlignments(al)">
                                    <tr :key="'alignment-blank-' + i + '-' + alIdx"
                                        class="blank-row">
                                        <td></td>
                                    </tr>
                                    <tr v-if="alPart.query.ss_pred"
                                        :key="'alignment-ss_pred-' + i + '-' + alIdx"
                                        class="sequence">
                                        <td></td>
                                        <td>Q ss_pred</td>
                                        <td></td>
                                        <td v-html="coloredSeqSS(alPart.query.ss_pred)"></td>
                                    </tr>
                                    <tr v-if="alPart.query.seq"
                                        :key="'alignment-seq-' + i + '-' + alIdx"
                                        class="sequence">
                                        <td></td>
                                        <td v-text="'Q '+alPart.query.name"></td>
                                        <td v-text="alPart.query.start"></td>
                                        <td v-html="coloredSeq(alPart.query.seq) + alEndRef(alPart.query)"></td>
                                    </tr>
                                    <tr v-if="alPart.query.consensus"
                                        :key="'alignment-consensus-' + i + '-' + alIdx"
                                        class="sequence">
                                        <td></td>
                                        <td>Q Consensus</td>
                                        <td v-text="alPart.query.start"></td>
                                        <td v-html="alPart.query.consensus + alEndRef(alPart.query)"></td>
                                    </tr>
                                    <tr v-if="alPart.agree"
                                        :key="'alignment-agree-' + i + '-' + alIdx"
                                        class="sequence">
                                        <td></td>
                                        <td></td>
                                        <td></td>
                                        <td class="consensus-agree"
                                            v-text="alPart.agree"></td>
                                    </tr>
                                    <tr v-if="alPart.template.consensus"
                                        :key="'alignment-tplcons-' + i + '-' + alIdx"
                                        class="sequence">
                                        <td></td>
                                        <td>
                                            T Consensus
                                        </td>
                                        <td v-text="alPart.template.start"></td>
                                        <td v-html="alPart.template.consensus + alEndRef(alPart.template)"></td>
                                    </tr>
                                    <tr v-if="alPart.template.seq"
                                        :key="'alignment-seq-' + i + '-' + alIdx"
                                        class="sequence">
                                        <td></td>
                                        <td v-text="'T '+alPart.template.accession"></td>
                                        <td v-text="alPart.template.start"></td>
                                        <td v-html="coloredSeq(alPart.template.seq) + alEndRef(alPart.template)"></td>
                                    </tr>
                                    <tr v-if="alPart.template.ss_dssp"
                                        :key="'alignment-ss_dssp-' + i + '-' + alIdx"
                                        class="sequence">
                                        <td></td>
                                        <td>T ss_dssp</td>
                                        <td></td>
                                        <td v-html="coloredSeqSS(alPart.template.ss_dssp)"></td>
                                    </tr>
                                    <tr v-if="alPart.template.ss_pred"
                                        :key="'alignment-ss_pred-' + i + '-' + alIdx"
                                        class="sequence">
                                        <td></td>
                                        <td>T ss_pred</td>
                                        <td></td>
                                        <td v-html="coloredSeqSS(alPart.template.ss_pred)"></td>
                                    </tr>
                                    <tr :key="'alignment-br-' + i + '-' + alIdx"
                                        class="blank-row">
                                        <td></td>
                                    </tr>
                                </template>
                            </template>

                            <tr v-if="alignments.length !== total">
                                <td colspan="4">
                                    <Loading v-if="loadingMore"
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
    import {HHpredAlignmentItem, HHpredHHInfoResult, SearchAlignmentItemRender} from '@/types/toolkit/results';
    import EventBus from '@/util/EventBus';
    import SearchResultTabMixin from '@/mixins/SearchResultTabMixin';
    import {jobService} from '@/services/JobService';
    import {resultsService} from '@/services/ResultsService';

    const logger = Logger.get('HHpredResultsTab');

    export default SearchResultTabMixin.extend({
        name: 'HHpredResultsTab',
        components: {
            Loading,
            HitListTable,
            HitMap,
            IntersectionObserver,
        },
        data() {
            return {
                alignments: undefined as HHpredAlignmentItem[] | undefined,
                info: undefined as HHpredHHInfoResult | undefined,
                color: true,
                breakAfter: 80,
                hitListFields: [{
                    key: 'numCheck',
                    label: this.$t('jobs.results.hhpred.table.num'),
                    sortable: true,
                }, {
                    key: 'acc',
                    label: this.$t('jobs.results.hhpred.table.hit'),
                    sortable: true,
                }, {
                    key: 'name',
                    label: this.$t('jobs.results.hhpred.table.name'),
                    sortable: true,
                }, {
                    key: 'probab',
                    label: this.$t('jobs.results.hhpred.table.probHits'),
                    sortable: true,
                }, {
                    key: 'eval',
                    label: this.$t('jobs.results.hhpred.table.eVal'),
                    class: 'no-wrap',
                    sortable: true,
                }, {
                    key: 'ssScore',
                    label: this.$t('jobs.results.hhpred.table.ssScore'),
                    sortable: true,
                }, {
                    key: 'alignedCols',
                    label: this.$t('jobs.results.hhpred.table.cols'),
                    sortable: true,
                }, {
                    key: 'templateRef',
                    label: this.$t('jobs.results.hhpred.table.targetLength'),
                    sortable: true,
                }],
            };
        },
        computed: {
            filename(): string {
                if (!this.viewOptions.filename) {
                    return '';
                }
                return this.viewOptions.filename.replace(':jobID', this.job.jobID);
            },

        },
        methods: {
            displayTemplateStructure(accession: string): void {
                EventBus.$emit('show-modal', {
                    id: 'templateStructureModal', props: {accessionStructure: accession},
                });
            },
            download(): void {
                const toolName = this.tool.name;
                const downloadFilename = `${toolName}_${this.job.jobID}.hhr`;
                resultsService.downloadFile(this.job.jobID, this.filename, downloadFilename)
                    .catch((e) => {
                        logger.error(e);
                    });
            },
            modelSelection(): void {
                if (!this.alignments) {
                    return;
                }

                const selected: number[] = Array.from(this.selectedItems);
                if (selected.length < 1) {
                    selected.push(this.alignments[0].num);
                    this.$alert(this.$t('jobs.results.hhpred.modelUsingFirst'), 'warning');
                }

                if (this.info) {
                    const submission: any = {
                        parentID: this.job.jobID,
                        templates: selected.join(' '),
                        alnHash: this.info.alignmentHash,
                    };
                    jobService.submitJob('hhpred_manual', submission)
                        .then((response) => {
                            this.$router.push(`/jobs/${response.jobID}`);
                        })
                        .catch((response) => {
                            logger.error('Could not submit job', response);
                            this.$alert(this.$t('errors.general'), 'danger');
                        });
                }
            },
            wrapAlignments(al: HHpredAlignmentItem): SearchAlignmentItemRender[] {
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
                        });
                        qStart = qEnd + 1;
                        tStart = tEnd + 1;
                    }
                    return res;
                } else {
                    return [al];
                }
            },
        },
    });
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
