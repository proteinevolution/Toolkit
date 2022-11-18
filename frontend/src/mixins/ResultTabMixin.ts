import { defineComponent, onBeforeUnmount, ref } from 'vue';
import { Tool } from '@/types/toolkit/tools';
import { Job, JobViewOptions } from '@/types/toolkit/jobs';
import Logger from 'js-logger';
import { useEventBus } from '@vueuse/core';

const logger = Logger.get('ResultTabMixin');

const ResultTabMixin = defineComponent({
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
    setup(props) {
        const loading = ref(false);
        let initialized = false;

        // init gets called only on first render
        const init = async () => {
            logger.debug('uncaught init called for ' + props.resultTabName);
        };

        const internalInit = async () => {
            if (!initialized) {
                loading.value = true;
                try {
                    await init();
                    initialized = true;
                } catch (e) {
                    logger.error(props.resultTabName, e);
                }
                loading.value = false;
            }
        };

        if (props.renderOnCreate) {
            // noinspection JSIgnoredPromiseFromCall
            internalInit();
        }

        const toolTabActivatedBus = useEventBus<string>('tool-tab-activated');
        const handleToolTabActivated = async (jobView: string) => {
            if (jobView === props.resultTabName) {
                await internalInit();
            }
        };
        const unsubscribeToolTabActivated = toolTabActivatedBus.on(handleToolTabActivated);

        onBeforeUnmount(() => {
            unsubscribeToolTabActivated();
        });

        return { init, loading };
    },
});

export default ResultTabMixin;
