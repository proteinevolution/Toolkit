import Vue from 'vue';
import {Tool} from '@/types/toolkit/tools';
import {Job, JobViewOptions} from '@/types/toolkit/jobs';
import EventBus from '@/util/EventBus';
import Logger from 'js-logger';

const logger = Logger.get('ResultTabMixin');

const ResultTabMixin = Vue.extend({
    props: {
        job: {
            type: Object as () => Job,
            required: true,
        },
        tool: {
            type: Object as () => Tool,
            required: true,
        },
        fullScreen: {
            type: Boolean,
            required: false,
            default: false,
        },
        viewOptions: {
            type: Object as () => JobViewOptions,
            required: false,
        },
        resultTabName: {
            type: String,
            required: false,
            default: '',
        },
        renderOnCreate: {
            type: Boolean,
            required: false,
            default: true,
        },
    },
    data() {
        return {
            loading: false,
            initialized: false,
        };
    },
    created() {
        if (this.renderOnCreate) {
            this.internalInit();
        }
        EventBus.$on('tool-tab-activated', (jobView: string) => {
            if (jobView === this.resultTabName) {
                this.internalInit();
            }
        });
    },
    beforeDestroy() {
        EventBus.$off('tool-tab-activated');
    },
    methods: {
        async internalInit() {
            if (!this.initialized) {
                this.loading = true;
                try {
                    await this.init();
                    this.initialized = true;
                } catch (e) {
                    logger.error(this.resultTabName, e);
                }
                this.loading = false;
            }
        },
        // init gets called only on first render
        async init() {
            logger.debug('uncaught init called for ' + this.resultTabName);
        },
    },
});

export default ResultTabMixin;
