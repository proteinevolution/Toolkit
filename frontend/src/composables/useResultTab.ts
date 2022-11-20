import { onBeforeUnmount, ref, Ref } from 'vue';
import { Tool } from '@/types/toolkit/tools';
import { Job, JobViewOptions } from '@/types/toolkit/jobs';
import Logger from 'js-logger';
import { useEventBus } from '@vueuse/core';
import { isNonNullable } from '@/util/nullability-helpers';

interface ResultTabProps {
    job: Job;
    tool: Tool;
    fullScreen?: boolean;
    viewOptions?: JobViewOptions;
    resultTabName?: string;
    renderOnCreate?: boolean;
}

export function defineResultTabProps() {
    return withDefaults(defineProps<ResultTabProps>(), {
        resultTabName: '',
        renderOnCreate: true,
    });
}

interface UseResultTabArguments {
    // init gets called only on first render
    init?: () => Promise<void>;
    resultTabName: string;
    renderOnCreate: boolean;
}

interface UseResultTabResult {
    loading: Ref<boolean>;
}

const logger = Logger.get('UseResultTab');

export default function useResultTab(args: UseResultTabArguments): UseResultTabResult {
    const loading = ref(false);
    let initialized = false;

    async function internalInit() {
        if (!initialized) {
            loading.value = true;
            try {
                if (isNonNullable(args.init)) {
                    await args.init();
                }
                initialized = true;
            } catch (e) {
                logger.error(args.resultTabName, e);
            }
            loading.value = false;
        }
    }

    if (args.renderOnCreate) {
        // noinspection JSIgnoredPromiseFromCall
        internalInit();
    }

    const toolTabActivatedBus = useEventBus<string>('tool-tab-activated');

    async function handleToolTabActivated(jobView: string) {
        if (jobView === args.resultTabName) {
            await internalInit();
        }
    }

    const unsubscribeToolTabActivated = toolTabActivatedBus.on(handleToolTabActivated);
    onBeforeUnmount(() => {
        unsubscribeToolTabActivated();
    });

    return { loading };
}
