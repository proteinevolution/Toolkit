import Vue from 'vue';
import EventBus from '@/util/EventBus';
import Logger from 'js-logger';
import {HHInfoResult} from '@/types/toolkit/results';
import {colorSequence, ssColorSequence} from '@/util/SequenceUtils';

const logger = Logger.get('SearchResultTabMixin');

const SearchResultTabMixin = Vue.extend({
    data() {
        return {
            color: true,
            info: undefined as HHInfoResult | undefined,
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
        resubmitSection([start, end]: [number, number]): void {
            if (!this.info) {
                return;
            }
            const section: string = '>' + this.info.query.accession + '\n' + this.info.query.seq.slice(start, end+1);
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
