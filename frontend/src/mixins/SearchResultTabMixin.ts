import EventBus from '@/util/EventBus';
import Logger from 'js-logger';
import {HHInfoResult, SearchAlignmentItem} from '@/types/toolkit/results';
import {colorSequence, ssColorSequence} from '@/util/SequenceUtils';
import ResultTabMixin from '@/mixins/ResultTabMixin';
import mixins from 'vue-typed-mixins';
import {resultsService} from '@/services/ResultsService';

const logger = Logger.get('SearchResultTabMixin');

const SearchResultTabMixin = mixins(ResultTabMixin).extend({
    data() {
        return {
            color: true,
            info: undefined as HHInfoResult | undefined,
            alignments: undefined as SearchAlignmentItem[] | undefined,
        };
    },
    methods: {
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
        forward(): void {
            alert('implement me!');
        },
        async forwardQueryA3M() {
            const a3mData: any = await resultsService.getFile(this.job.jobID, 'reduced.a3m');
            EventBus.$emit('show-modal', {
                id: 'forwardingModal', props: {
                    forwardingJobID: this.job.jobID,
                    forwardingData: a3mData,
                    forwardingMode: {
                        alignment: ['formatseq', 'hhblits', 'hhpred', 'hhomp', 'hhrepid'],
                    },
                },
            });
        },
        resubmitSection([start, end]: [number, number]): void {
            if (!this.info) {
                return;
            }
            const section: string = '>' + this.info.query.accession + '\n' + this.info.query.seq.slice(start, end + 1);
            EventBus.$emit('resubmit-section', section);
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
