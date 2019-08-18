<template>
    <Loading :message="$t('loading')"
             v-if="loading || !alignments"/>
    <div v-else
         class="font-small">
        <b v-if="total === 0"
           v-text="$t('jobs.results.hhomp.noResults')">
        </b>
        <div v-else>
            <div class="result-options">
                <a @click="scrollTo('visualization')">{{$t('jobs.results.hitlist.visLink')}}</a>
                <a @click="scrollTo('hits')">{{$t('jobs.results.hitlist.hitsLink')}}</a>
                <a @click="scrollTo('alignments')"
                   class="mr-4">{{$t('jobs.results.hitlist.alnLink')}}</a>
                <a class="border-right mr-4"></a>
                <a @click="forwardQueryA3M">{{$t('jobs.results.actions.forwardQueryA3M')}}</a>
                <a @click="toggleColor"
                   :class="{active: color}">{{$t('jobs.results.actions.colorSeqs')}}</a>
                <a @click="toggleWrap"
                   :class="{active: wrap}">{{$t('jobs.results.actions.wrapSeqs')}}</a>
            </div>

            <div v-html="$t('jobs.results.hhomp.numHits', {num: total})"></div>
            <div v-html="$t('jobs.results.hhomp.probOMP', {num: info.probOMP})"></div>


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
                                @elem-clicked="scrollToElem"/>
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
                                       v-text="$t('jobs.results.hhomp.templateAlignment')"></a>
                                </td>
                            </tr>
                            <tr class="font-weight-bold">
                                <td v-text="al.num + '.'"></td>
                                <td colspan="3"
                                    v-text="al.acc + ' ' + al.name"></td>
                            </tr>
                            <tr>
                                <td></td>
                                <td colspan="3"
                                    v-html="$t('jobs.results.hhomp.alignmentInfo', al)"></td>
                            </tr>

                            <template v-for="alPart in wrapAlignments(al)">
                                <tr class="blank-row">
                                    <td></td>
                                </tr>
                                <tr v-if="alPart.query.ss_conf"
                                    class="sequence">
                                    <td></td>
                                    <td>Q ss_conf</td>
                                    <td></td>
                                    <td v-html="alPart.query.ss_conf"></td>
                                </tr>
                                <tr v-if="alPart.query.ss_dssp"
                                    class="sequence">
                                    <td></td>
                                    <td>Q ss_pred</td>
                                    <td></td>
                                    <td v-html="coloredSeqSS(alPart.query.ss_dssp)"></td>
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
                                    <td v-text="'Q ' + alPart.query.name"></td>
                                    <td v-text="alPart.query.start"></td>
                                    <td v-html="coloredSeq(alPart.query.seq) + alEndRef(alPart.query)"></td>
                                </tr>
                                <tr v-if="alPart.query.consensus"
                                    class="sequence">
                                    <td></td>
                                    <td>Q Consensus</td>
                                    <td v-text="alPart.query.start"></td>
                                    <td v-html="alPart.query.consensus + alEndRef(alPart.query)"></td>
                                </tr>
                                <tr v-if="alPart.agree"
                                    class="sequence">
                                    <td></td>
                                    <td></td>
                                    <td></td>
                                    <td class="consensus-agree" v-text="alPart.agree"></td>
                                </tr>
                                <tr v-if="alPart.template.consensus"
                                    class="sequence">
                                    <td></td>
                                    <td>T Consensus</td>
                                    <td v-text="alPart.template.start"></td>
                                    <td v-html="alPart.template.consensus + alEndRef(alPart.template)"></td>
                                </tr>
                                <tr v-if="alPart.template.seq"
                                    class="sequence">
                                    <td></td>
                                    <td v-text="'Q ' + alPart.template.accession"></td>
                                    <td v-text="alPart.template.start"></td>
                                    <td v-html="coloredSeq(alPart.template.seq) + alEndRef(alPart.template)"></td>
                                </tr>
                                <tr v-if="alPart.template.ss_pred"
                                    class="sequence">
                                    <td></td>
                                    <td>T ss_pred</td>
                                    <td></td>
                                    <td v-html="coloredSeqSS(alPart.template.ss_pred)"></td>
                                </tr>
                                <tr v-if="alPart.template.ss_dssp"
                                    class="sequence">
                                    <td></td>
                                    <td>T ss_dssp</td>
                                    <td></td>
                                    <td v-html="coloredSeqSS(alPart.template.ss_dssp)"></td>
                                </tr>
                                <tr v-if="alPart.template.ss_conf"
                                    class="sequence">
                                    <td></td>
                                    <td>T ss_conf</td>
                                    <td></td>
                                    <td v-text="alPart.template.ss_conf"></td>
                                </tr>
                                <tr v-if="alPart.template.bb_pred"
                                    class="sequence">
                                    <td></td>
                                    <td>T bb_pred</td>
                                    <td></td>
                                    <td v-text="alPart.template.bb_pred"></td>
                                </tr>
                                <tr v-if="alPart.template.bb_conf"
                                    class="sequence">
                                    <td></td>
                                    <td>T bb_conf</td>
                                    <td></td>
                                    <td v-text="alPart.template.bb_conf"></td>
                                </tr>
                                <tr class="blank-row">
                                    <td>
                                    </td>
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
    import {HHompAlignmentItem, HHompHHInfoResult, SearchAlignmentItemRender} from '@/types/toolkit/results';
    import SearchResultTabMixin from '@/mixins/SearchResultTabMixin';

    const logger = Logger.get('HHompResultsTab');

    export default mixins(SearchResultTabMixin).extend({
        name: 'HHompResultsTab',
        components: {
            Loading,
            HitListTable,
            HitMap,
            IntersectionObserver,
        },
        data() {
            return {
                alignments: undefined as HHompAlignmentItem[] | undefined,
                info: undefined as HHompHHInfoResult | undefined,
                breakAfter: 70,
                hitListFields: [{
                    key: 'num',
                    label: this.$t('jobs.results.hhomp.table.num'),
                    sortable: true,
                }, {
                    key: 'acc',
                    label: this.$t('jobs.results.hhomp.table.hit'),
                    sortable: true,
                }, {
                    key: 'name',
                    label: this.$t('jobs.results.hhomp.table.name'),
                    sortable: true,
                }, {
                    key: 'probabHit',
                    label: this.$t('jobs.results.hhomp.table.probHits'),
                    sortable: true,
                }, {
                    key: 'probabOMP',
                    label: this.$t('jobs.results.hhomp.table.probOMP'),
                    sortable: true,
                }, {
                    key: 'eval',
                    label: this.$t('jobs.results.hhomp.table.eVal'),
                    class: 'no-wrap',
                    sortable: true,
                }, {
                    key: 'ssScore',
                    label: this.$t('jobs.results.hhomp.table.ssScore'),
                    sortable: true,
                }, {
                    key: 'alignedCols',
                    label: this.$t('jobs.results.hhomp.table.cols'),
                    sortable: true,
                }, {
                    key: 'templateRef',
                    label: this.$t('jobs.results.hhomp.table.targetLength'),
                    sortable: true,
                }],
            };
        },
        methods: {
            wrapAlignments(al: HHompAlignmentItem): SearchAlignmentItemRender[] {
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
                                ss_conf: al.query.ss_conf.slice(start, end),
                                ss_dssp: al.query.ss_dssp.slice(start, end),
                                ss_pred: al.query.ss_pred.slice(start, end),
                                start: qStart,
                            },
                            template: {
                                accession: al.template.accession,
                                bb_conf: al.template.bb_conf.slice(start, end),
                                bb_pred: al.template.bb_pred.slice(start, end),
                                consensus: al.template.consensus.slice(start, end),
                                end: tEnd,
                                ref: al.template.ref,
                                seq: tSeq,
                                ss_conf: al.template.ss_conf.slice(start, end),
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
