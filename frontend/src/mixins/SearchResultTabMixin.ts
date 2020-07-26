import EventBus from '@/util/EventBus';
import Logger from 'js-logger';
import {HHInfoResult, SearchAlignmentItem, SearchAlignmentsResponse} from '@/types/toolkit/results';
import {colorSequence, ssColorSequence} from '@/util/SequenceUtils';
import ResultTabMixin from '@/mixins/ResultTabMixin';
import {resultsService} from '@/services/ResultsService';
import handyScroll from 'handy-scroll';
import {debounce} from 'lodash-es';

const logger = Logger.get('SearchResultTabMixin');

const SearchResultTabMixin = ResultTabMixin.extend({
    data() {
        return {
            total: 100,
            info: undefined as HHInfoResult | undefined,
            alignments: undefined as SearchAlignmentItem[] | undefined,
            selectedItems: [] as number[],
            perPage: 50,
            color: false,
            wrap: true,
            loadingMore: false,
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
    created() {
        (this as any).debouncedUpdateHandyScroll = debounce(this.updateHandyScroll.bind(this), 100);
    },
    beforeDestroy(): void {
        handyScroll.destroy(this.$refs.scrollElem);
        window.removeEventListener('resize', (this as any).debouncedUpdateHandyScroll);
    },
    methods: {
        async init(): Promise<void> {
            await this.loadAlignments(0, this.perPage);
            window.addEventListener('resize', (this as any).debouncedUpdateHandyScroll);
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
            const res: SearchAlignmentsResponse<SearchAlignmentItem, HHInfoResult> =
                await resultsService.fetchHHAlignmentResults(this.job.jobID, start, end);
            this.total = res.total;
            this.info = res.info;
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
            if (this.alignments && this.alignments.map((a: SearchAlignmentItem) => a.num).includes(loadNum)) {
                this.scrollTo('alignment-' + num);
            } else if (this.alignments) {
                await this.loadAlignments(this.alignments.length, loadNum);
                this.scrollTo('alignment-' + num);
            }
        },
        displayTemplateAlignment(accession: string): void {
            if (this.tool.parameters) {
                EventBus.$emit('show-modal', {
                    id: 'templateAlignmentModal', props: {
                        jobID: this.job.jobID,
                        accession,
                        forwardingMode: this.tool.parameters.forwarding,
                    },
                });
            } else {
                logger.error('tool parameters not loaded. Cannot forward');
            }
        },
        forward(disableSequenceLengthSelect: boolean = false): void {
            if (this.tool.parameters) {
                EventBus.$emit('show-modal', {
                    id: 'forwardingModal', props: {
                        forwardingJobID: this.job.jobID,
                        forwardingMode: this.tool.parameters.forwarding,
                        forwardingApiOptions: {
                            disableSequenceLengthSelect,
                            selectedItems: this.selectedItems,
                        },
                    },
                });
            } else {
                logger.error('tool parameters not loaded. Cannot forward');
            }
        },
        async forwardQueryA3M() {
            const a3mData: any = await resultsService.getFile(this.job.jobID, 'reduced.a3m');
            EventBus.$emit('show-modal', {
                id: 'forwardingModal', props: {
                    forwardingJobID: this.job.jobID,
                    forwardingData: a3mData,
                    forwardingMode: {
                        alignment: ['hhpred', 'formatseq', 'hhblits', 'hhomp', 'hhrepid'],
                    },
                },
            });
        },
        resubmitSection([start, end]: [number, number]): void {
            if (!this.info) {
                return;
            }
            const section: string = '>' + this.info.query.accession + '\n' + this.info.query.seq.slice(start - 1, end);
            EventBus.$emit('resubmit-section', section);
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
        toggleWrap(): void {
            this.wrap = !this.wrap;
            this.$nextTick(this.updateHandyScroll.bind(this));
        },
        updateHandyScroll(): void {
            const scrollElem: Element = this.$refs.scrollElem as Element;
            if (scrollElem) {
                if (!handyScroll.mounted(scrollElem)) {
                    handyScroll.mount(scrollElem);
                } else {
                    handyScroll.update(scrollElem);
                }
            }
        },
        toggleColor(): void {
            this.color = !this.color;
        },
        coloredSeq(seq: string): string {
            return this.color ? colorSequence(seq) : seq;
        },
        coloredSeqSS(seq: string): string {
            return this.color ? ssColorSequence(seq) : seq;
        },
        alEnd(a: { end: number }): string {
            return ` &nbsp; ${a.end}`;
        },
        alEndRef(a: { end: number, ref: number }): string {
            return ` &nbsp; ${a.end} (${a.ref})`;
        },
    },
});

export default SearchResultTabMixin;
