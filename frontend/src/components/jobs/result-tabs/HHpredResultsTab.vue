<template>
    <Loading :message="$t('loading')"
             v-if="loading"/>
    <div v-else
         class="font-small">
        <b v-if="total === 0"
           v-text="$t('jobs.results.hhpred.noResults')">
        </b>
        <div v-else>
            <div class="result-options">
                <a @click="scrollTo('visualization')">{{$t('jobs.results.hitlist.visLink')}}</a>
                <a @click="scrollTo('hits')">{{$t('jobs.results.hitlist.hitsLink')}}</a>
                <a @click="scrollTo('alignments')"
                   class="mr-4">{{$t('jobs.results.hitlist.alnLink')}}</a>
                <a class="border-right mr-4"></a>
                <a @click="toggleAllSelected" :class="{active: allSelected}">
                    {{$t('jobs.results.actions.selectAll')}}</a>
                <a @click="forward(true)">{{$t('jobs.results.actions.forward')}}</a>
                <a @click="forwardQueryA3M">{{$t('jobs.results.actions.forwardQueryA3M')}}</a>
                <a v-if="info.modeller" @click="modelSelection" v-text="$t('jobs.results.actions.model')"></a>
                <a @click="toggleColor"
                   :class="{active: color}">{{$t('jobs.results.actions.colorSeqs')}}</a>
                <a @click="toggleWrap"
                   :class="{active: wrap}">{{$t('jobs.results.actions.wrapSeqs')}}</a>
            </div>

            <div v-html="$t('jobs.results.hhpred.numHits', {num: info.num_hits})"></div>

            <div v-if="info.coil === '0' || info.tm === '1' || info.signal === '1'" class="mt-2">
                <b> Detected sequence features:</b>
                <b v-if="info.coil === '0'" v-html="$t('jobs.results.sequenceFeatures.coil')"></b>
                <b v-if="info.tm > '1'" v-html="$t('jobs.results.sequenceFeatures.tm')"></b>
                <b v-if="info.signal === '1'" v-html="$t('jobs.results.sequenceFeatures.signal')"></b>
            </div>

            <div v-if="info.qa3m_count < '10'" class="mt-2">
                <b v-html="$t('jobs.results.hhpred.qa3mWarning', {num: info.qa3m_count})" class="mt-2"></b>
                <b v-if="info.msa_gen === 'uniclust30'" v-html="$t('jobs.results.hhpred.uniclustWarning')"></b>
                <b v-if="info.msa_gen === 'psiblast'" v-html="$t('jobs.results.hhpred.psiblastWarning')"></b>
                <b v-if="info.msa_gen === 'custom'" v-html="$t('jobs.results.hhpred.customWarning')"></b>
            </div>


            <div class="result-section"
                 ref="visualization">
                <h4>{{$t('jobs.results.hitlist.vis')}}</h4>
                <hit-map :job="job"
                         @elem-clicked="scrollToElem"
                         @resubmit-section="resubmitSection"/>
            </div>

            <div class="result-section"
                 ref="hits">
                <h4 class="mb-4">{{$t('jobs.results.hitlist.hits')}}</h4>
                <hit-list-table :job="job"
                                :fields="hitListFields"
                                @elem-clicked="scrollToElem"
                                :selected-items="selectedItems"/>
            </div>
            <div class="result-section"
                 ref="alignments">
                <h4>{{$t('jobs.results.hitlist.aln')}}</h4>

                <div class="table-responsive"
                     ref="scrollElem">
                    <table class="alignments-table">
                        <tbody>
                        <template v-for="(al, i) in alignments">
                            <tr class="blank-row"
                                :key="'alignment-' + al.num"
                                :ref="'alignment-' + al.num">
                                <td colspan="4">
                                    <hr v-if="i !== 0"/>
                                </td>
                            </tr>
                            <tr>
                                <td></td>
                                <td colspan="3">
                                    <a @click="displayTemplateAlignment(al.template.accession)"
                                       v-text="$t('jobs.results.hhpred.templateAlignment')"></a>
                                    <a class="db-list"
                                       v-if="al.structLink"
                                       @click="displayTemplateStructure(al.template.accession)"
                                       v-text="$t('jobs.results.hhpred.templateStructure')"></a>
                                    <span v-if="al.dbLink"
                                          class="db-list"
                                          v-html="al.dbLink"></span>
                                </td>
                            </tr>
                            <tr class="font-weight-bold">
                                <td class="no-wrap">
                                    <b-checkbox @change="check($event, al.num)"
                                                class="d-inline"
                                                :checked="selectedItems.includes(al.num)"/>
                                    <span v-text="al.num + '.'"></span>
                                </td>
                                <td colspan="3"
                                    v-html="al.acc + ' ' + al.name"></td>
                            </tr>

                            <tr>
                                <td></td>
                                <td colspan="3"
                                    v-html="$t('jobs.results.hhpred.alignmentInfo', al)"></td>
                            </tr>

                            <template v-for="alPart in wrapAlignments(al)">
                                <tr class="blank-row">
                                    <td></td>
                                </tr>
                                <tr v-if="alPart.query.ss_pred"
                                    class="sequence">
                                    <td></td>
                                    <td>Q ss_pred</td>
                                    <td></td>
                                    <td v-html="coloredSeqSS(alPart.query.ss_pred)"></td>
                                </tr>
                                <tr v-if="alPart.query.seq"
                                    class="sequence">
                                    <td></td>
                                    <td v-text="'Q '+alPart.query.name"></td>
                                    <td v-text="alPart.query.start"></td>
                                    <td v-html="coloredSeq(alPart.query.seq) + alEndRef(alPart.query)"></td>
                                </tr>
                                <tr v-if="alPart.query.consensus"
                                    class="sequence">
                                    <td></td>
                                    <td>Q Consensus</td>
                                    <td v-text="alPart.query.start"></td>
                                    <td v-html="alPart.query.consensus  + alEndRef(alPart.query)"></td>
                                </tr>
                                <tr v-if="alPart.agree"
                                    class="sequence">
                                    <td></td>
                                    <td></td>
                                    <td></td>
                                    <td v-text="alPart.agree"></td>
                                </tr>
                                <tr v-if="alPart.template.consensus"
                                    class="sequence">
                                    <td></td>
                                    <td v-text="">Q Consensus</td>
                                    <td v-text="alPart.template.start"></td>
                                    <td v-html="alPart.template.consensus  + alEndRef(alPart.template)"></td>
                                </tr>
                                <tr v-if="alPart.template.seq"
                                    class="sequence">
                                    <td></td>
                                    <td v-text="'T '+alPart.query.name"></td>
                                    <td v-text="alPart.template.start"></td>
                                    <td v-html="coloredSeq(alPart.template.seq) + alEndRef(alPart.template)"></td>
                                </tr>
                                <tr v-if="alPart.template.ss_dssp"
                                    class="sequence">
                                    <td></td>
                                    <td>T ss_dssp</td>
                                    <td></td>
                                    <td v-html="coloredSeqSS(alPart.template.ss_dssp)"></td>
                                </tr>
                                <tr v-if="alPart.template.ss_pred"
                                    class="sequence">
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
                                <Loading :message="$t('jobs.results.alignment.loadingHits')"
                                         v-if="loadingMore"
                                         justify="center"
                                         class="mt-4"/>
                                <intersection-observer @intersect="intersected"/>
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
    import mixins from 'vue-typed-mixins';
    import Loading from '@/components/utils/Loading.vue';
    import Logger from 'js-logger';
    import HitListTable from '@/components/jobs/result-tabs/sections/HitListTable.vue';
    import HitMap from '@/components/jobs/result-tabs/sections/HitMap.vue';
    import IntersectionObserver from '@/components/utils/IntersectionObserver.vue';
    import {HHpredAlignmentItem, HHpredHHInfoResult, SearchAlignmentItemRender} from '@/types/toolkit/results';
    import EventBus from '@/util/EventBus';
    import SearchResultTabMixin from '@/mixins/SearchResultTabMixin';
    import {jobService} from '@/services/JobService';

    const logger = Logger.get('HHpredResultsTab');

    export default mixins(SearchResultTabMixin).extend({
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
        methods: {
            displayTemplateStructure(accession: string): void {
                EventBus.$emit('show-modal', {
                    id: 'templateStructureModal', props: {accessionStructure: accession},
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
                const submission: any = {
                    parentID: this.job.jobID,
                    templates: selected.join(' '),
                };
                jobService.submitJob('hhpred_manual', submission)
                    .then((response) => {
                        this.$router.push(`/jobs/${response.jobID}`);
                    })
                    .catch((response) => {
                        logger.error('Could not submit job', response);
                        this.$alert(this.$t('errors.general'), 'danger');
                    });
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

    .alignments-table {
        font-size: 0.95em;

        .blank-row {
            height: 0.9rem;
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

    .db-list {
        border-left: 1px solid;
        border-left-color: $tk-gray;
        margin-left: 0.5em;
        padding-left: 0.5em;
    }
</style>
