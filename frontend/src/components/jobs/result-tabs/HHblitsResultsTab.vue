<template>
    <Loading :message="$t('loading')"
             v-if="loading"/>
    <div v-else
         class="font-small">
        <b v-if="total === 0"
           v-text="$t('jobs.results.hhblits.noResults')">
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
                <a @click="forwardQueryA3M">{{$t('jobs.results.actions.forwardQueryA3M')}}</a>
                <a @click="toggleColor"
                   :class="{active: color}">{{$t('jobs.results.actions.colorSeqs')}}</a>
                <a @click="toggleWrap"
                   :class="{active: wrap}">{{$t('jobs.results.actions.wrapSeqs')}}</a>
            </div>

            <div v-html="$t('jobs.results.hhblits.numHits', {num: total})"></div>
            <div class="result-section"
                 ref="visualization">
                <h4>{{$t('jobs.results.hitlist.vis')}}</h4>
                <hit-map :job="job"
                         @elem-clicked="scrollToElem"/>
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
                                    <a @click="displayTemplateAlignment(al.num)"
                                       v-text="$t('jobs.results.hhblits.templateAlignment')"></a>
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
                                    v-html="$t('jobs.results.hhblits.alignmentInfo', al)"></td>
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
                                    <td v-html="coloredSeq(alPart.query.seq) + alQEnd(alPart)"></td>
                                </tr>
                                <tr v-if="alPart.query.consensus"
                                    class="sequence">
                                    <td></td>
                                    <td></td>
                                    <td></td>
                                    <td v-html="alPart.query.consensus"></td>
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
                                    <td></td>
                                    <td></td>
                                    <td v-html="alPart.template.consensus"></td>
                                </tr>
                                <tr v-if="alPart.template.seq"
                                    class="sequence">
                                    <td></td>
                                    <td>T</td>
                                    <td v-text="alPart.template.start"></td>
                                    <td v-html="coloredSeq(alPart.template.seq) + alTEnd(alPart)"></td>
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
    import {HHblitsAlignmentItem, SearchAlignmentItem, SearchAlignmentsResponse} from '@/types/toolkit/results';
    import {colorSequence} from '@/util/SequenceUtils';
    import {resultsService} from '@/services/ResultsService';

    const logger = Logger.get('HHblitsResultsTab');

    export default mixins(ResultTabMixin).extend({
        name: 'HHblitsResultsTab',
        components: {
            Loading,
            HitListTable,
            HitMap,
            IntersectionObserver,
        },
        data() {
            return {
                alignments: undefined as HHblitsAlignmentItem[] | undefined,
                total: 100,
                loadingMore: false,
                perPage: 20,
                color: false,
                wrap: true,
                breakAfter: 85,
                selectedItems: [] as number[],
                hitListFields: [{
                    key: 'numCheck',
                    label: this.$t('jobs.results.hhblits.table.num'),
                    sortable: true,
                }, {
                    key: 'acc',
                    label: this.$t('jobs.results.hhblits.table.hit'),
                    sortable: true,
                }, {
                    key: 'name',
                    label: this.$t('jobs.results.hhblits.table.name'),
                    sortable: true,
                }, {
                    key: 'probab',
                    label: this.$t('jobs.results.hhblits.table.probHits'),
                    sortable: true,
                }, {
                    key: 'eval',
                    label: this.$t('jobs.results.hhblits.table.eVal'),
                    sortable: true,
                }, {
                    key: 'alignedCols',
                    label: this.$t('jobs.results.hhblits.table.cols'),
                    sortable: true,
                }, {
                    key: 'templateRef',
                    label: this.$t('jobs.results.hhblits.table.targetLength'),
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
                const res: SearchAlignmentsResponse<HHblitsAlignmentItem> =
                    await resultsService.fetchHHAlignmentResults(this.job.jobID, start, end);
                this.total = res.total;
                if (!this.alignments) {
                    this.alignments = res.alignments;
                } else {
                    this.alignments.push(...res.alignments);
                }
            },
            scrollTo(ref: string): void {
                if (this.$refs[ref]) {
                    const elem: HTMLElement = (this.$refs[ref] as any).length ?
                        (this.$refs[ref] as HTMLElement[])[0] : this.$refs[ref] as HTMLElement;
                    elem.scrollIntoView({
                        block: 'start',
                        behavior: 'smooth',
                    });
                }
            },
            async scrollToElem(num: number): Promise<void> {
                const loadNum: number = num + 2; // load some more for better scrolling
                if (this.alignments && this.alignments.map((a: HHblitsAlignmentItem) => a.num).includes(loadNum)) {
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
            forwardQueryA3M(): void {
                alert('implement me!');
            },
            toggleColor(): void {
                this.color = !this.color;
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
            coloredSeq(seq: string): string {
                return this.color ? colorSequence(seq) : seq;
            },
            alQEnd(al: HHblitsAlignmentItem): string {
                return ` &nbsp; ${al.query.end} (${al.query.ref})`;
            },
            alTEnd(al: HHblitsAlignmentItem): string {
                return ` &nbsp; ${al.template.end} (${al.template.ref})`;
            },
            wrapAlignments(al: HHblitsAlignmentItem): SearchAlignmentItem[] {
                if (this.wrap) {
                    const res: SearchAlignmentItem[] = [];
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
                                start: qStart,
                            },
                            template: {
                                accession: al.template.accession,
                                consensus: al.template.consensus.slice(start, end),
                                end: tEnd,
                                ref: al.template.ref,
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
            height: 2rem;
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
