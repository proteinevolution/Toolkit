import Vue from 'vue';
import EventBus from '@/util/EventBus';
import Logger from 'js-logger';
import {HHInfoResult} from '@/types/toolkit/results';

const logger = Logger.get('SearchResultTabMixin');

const SearchResultTabMixin = Vue.extend({
    data() {
        return {
            info: undefined as HHInfoResult | undefined,
        };
    },
    methods: {
        resubmitSection([start, end]: [number, number]): void {
            if (!this.info) {
                return;
            }
            const section: string = '>' + this.info.query.accession + '\n' + this.info.query.seq.slice(start, end);
            EventBus.$emit('resubmit-section', section);
        },
    },
});

export default SearchResultTabMixin;
