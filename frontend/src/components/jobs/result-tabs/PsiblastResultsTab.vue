<template>
    <Loading :message="$t('loading')"
             v-if="loading"/>
    <div v-else
         class="font-small">
        <b v-if="total === 0"
           v-text="$t('jobs.results.psiblast.noResults')">
        </b>
        <div v-else>
            <div class="result-options">
                <a @click="scrollTo('visualization')">{{$t('jobs.results.hitlist.visLink')}}</a>
                <a @click="scrollTo('hits')">{{$t('jobs.results.hitlist.hitsLink')}}</a>
                <a @click="scrollTo('alignments')"
                   class="mr-4">{{$t('jobs.results.hitlist.alnLink')}}</a>
                <a class="border-right mr-4"></a>
                <a @click="toggleAllSelected">{{$t('jobs.results.actions.' + (allSelected ? 'deselectAll' :
                    'selectAll'))}}</a>
                <a @click="forwardQuery">{{$t('jobs.results.actions.forward')}}</a>
                <a @click="forwardQuery">{{$t('jobs.results.actions.downloadMSA')}}</a>
                <a @click="toggleColor"
                   :class="{active: color}">{{$t('jobs.results.actions.colorSeqs')}}</a>
                <a @click="toggleWrap"
                   :class="{active: wrap}">{{$t('jobs.results.actions.wrapSeqs')}}</a>
            </div>

            <div v-html="$t('jobs.results.psiblast.numHits', {num: info.num_hits})"></div>

            <div v-if="info.coil === '0' || info.tm === '1' || info.signal === '1'">
                Detected sequence features:
                <b v-if="info.coil === '0'" v-html="$t('jobs.results.sequenceFeatures.coil')"></b>
                <b v-if="info.tm === '1'" v-html="$t('jobs.results.sequenceFeatures.tm')"></b>
                <b v-if="info.signal === '1'" v-html="$t('jobs.results.sequenceFeatures.signal')"></b>
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
                                <td colspan="3"
                                    v-html="al.fastaLink">
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
                                    v-html="$t('jobs.results.psiblast.alignmentInfo', al)"></td>
                            </tr>

                            <template v-for="alPart in wrapAlignments(al)">
                                <tr class="blank-row">
                                    <td></td>
                                </tr>
                                <tr v-if="alPart.query.seq"
                                    class="sequence">
                                    <td></td>
                                    <td>Q</td>
                                    <td v-text="alPart.query.start"></td>
                                    <td v-html="coloredSeq(alPart.query.seq) + alEnd(alPart.query)"></td>
                                </tr>
                                <tr v-if="alPart.agree"
                                    class="sequence">
                                    <td></td>
                                    <td></td>
                                    <td></td>
                                    <td v-text="alPart.agree"></td>
                                </tr>
                                <tr v-if="alPart.template.seq"
                                    class="sequence">
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
    import ResultTabMixin from '@/mixins/ResultTabMixin';
    import Loading from '@/components/utils/Loading.vue';
    import Logger from 'js-logger';
    import HitListTable from '@/components/jobs/result-tabs/sections/HitListTable.vue';
    import HitMap from '@/components/jobs/result-tabs/sections/HitMap.vue';
    import IntersectionObserver from '@/components/utils/IntersectionObserver.vue';
    import handyScroll from 'handy-scroll';
    import {
        PSIBLASTAlignmentItem,
        PsiblastHHInfoResult,
        SearchAlignmentItemRender,
        SearchAlignmentsResponse,
    } from '@/types/toolkit/results';
    import {resultsService} from '@/services/ResultsService';
    import SearchResultTabMixin from '@/mixins/SearchResultTabMixin';

    const logger = Logger.get('PsiblastResultsTab');

    export default mixins(ResultTabMixin, SearchResultTabMixin).extend({
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
                total: 100,
                loadingMore: false,
                perPage: 20,
                wrap: true,
                breakAfter: 90,
                selectedItems: [] as number[],
                hitListFields: [{
                    key: 'numCheck',
                    label: this.$t('jobs.results.psiblast.table.num'),
                    sortable: true,
                }, {
                    key: 'acc',
                    label: this.$t('jobs.results.psiblast.table.accession'),
                    sortable: true,
                }, {
                    key: 'name',
                    label: this.$t('jobs.results.psiblast.table.description'),
                    sortable: true,
                }, {
                    key: 'eval',
                    label: this.$t('jobs.results.psiblast.table.eValue'),
                    sortable: true,
                }, {
                    key: 'bitScore',
                    label: this.$t('jobs.results.psiblast.table.bitscore'),
                    sortable: true,
                }, {
                    key: 'refLen',
                    label: this.$t('jobs.results.psiblast.table.ref_len'),
                    sortable: true,
                }, {
                    key: 'hitLen',
                    label: this.$t('jobs.results.psiblast.table.hit_len'),
                    sortable: true,
                }],
            };
        },
        computed: {
            allSelected(): boolean {
                if (!this.total) {
                    return false;
                }
                return this.total > 0 &&
                    this.selectedItems.length === this.total;
            },
        },
        beforeDestroy(): void {
            handyScroll.destroy(this.$refs.scrollElem);
        },
        methods: {
            async init(): Promise<void> {
                await this.loadAlignments(0, this.perPage);
            },
            async intersected(): Promise<void> {
                if (!this.loadingMore && this.alignments && this.alignments.length < this.total) {
                    this.loadingMore = true;
                    try {
                        await this.loadAlignments(this.alignments.length, this.alignments.length + this.perPage);
                    } catch (e) {
                        logger.error(e);
                    }
                    this.loadingMore = false;
                }
            },
            async loadAlignments(start: number, end: number): Promise<void> {
                const res: SearchAlignmentsResponse<PSIBLASTAlignmentItem, PsiblastHHInfoResult> =
                    await resultsService.fetchHHAlignmentResults(this.job.jobID, start, end);
                this.total = res.total;
                this.info = res.info;
                if (!this.alignments) {
                    this.alignments = res.alignments;
                } else {
                    this.alignments.push(...res.alignments);
                }
            },
            async scrollToElem(num: number): Promise<void> {
                const loadNum: number = num + 2; // load some more for better scrolling
                if (this.alignments && this.alignments.map((a: PSIBLASTAlignmentItem) => a.num).includes(loadNum)) {
                    this.scrollTo('alignment-' + num);
                } else if (this.alignments) {
                    await this.loadAlignments(this.alignments.length, loadNum);
                    this.scrollTo('alignment-' + num);
                }
            },
            toggleAllSelected(): void {
                if (!this.total) {
                    return;
                }
                if (this.allSelected) {
                    this.selectedItems = [];
                } else {
                    this.selectedItems = [];
                    for (let i = 1; i <= this.total; i++) {
                        this.selectedItems.push(i);
                    }
                }
            },
            check(val: boolean, num: number): void {
                if (val && !this.selectedItems.includes(num)) {
                    this.selectedItems.push(num);
                } else {
                    const i: number = this.selectedItems.indexOf(num);
                    if (i > -1) {
                        this.selectedItems.splice(i, 1);
                    }
                }
            },
            displayTemplateAlignment(num: number): void {
                alert('implement me!' + num);
            },
            forwardQuery(): void {
                alert('implement me!');
            },
            toggleWrap(): void {
                this.wrap = !this.wrap;
                this.$nextTick(() => {
                    if (!handyScroll.mounted(this.$refs.scrollElem)) {
                        handyScroll.mount(this.$refs.scrollElem);
                    } else {
                        handyScroll.update(this.$refs.scrollElem);
                    }
                });
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
        },

    });
</script>

<style lang="scss" scoped>
    .huge {
        height: 500px;
    }

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
</style>
