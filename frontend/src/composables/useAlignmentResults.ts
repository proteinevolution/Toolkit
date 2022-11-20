import { computed, ref, Ref } from 'vue';
import { ILogger } from 'js-logger';
import { AlignmentItem, AlignmentResultResponse } from '@/types/toolkit/results';
import { isNonNullable, isNullable } from '@/util/nullability-helpers';
import { resultsService } from '@/services/ResultsService';
import { range } from 'lodash-es';
import { ModalParams } from '@/types/toolkit/utils';
import { useEventBus } from '@vueuse/core';
import { ToolParameters } from '@/types/toolkit/tools';

interface UseAlignmentResultsArguments {
    logger: ILogger;
    jobID: string;
    toolParameters: ToolParameters | undefined;
    resultField?: Ref<string>;
}

export default function useAlignmentResults(args: UseAlignmentResultsArguments) {
    const alignments = ref<AlignmentItem[] | undefined>(undefined);
    const selected = ref<number[]>([]);
    const perPage = 50;
    const total = ref(0);
    const loadingMore = ref(false);

    const allSelected = computed(() => {
        if (isNullable(alignments.value)) {
            return false;
        }
        return alignments.value.length > 0 && selected.value.length === total.value;
    });

    function selectedChanged(num: number): void {
        if (selected.value.includes(num)) {
            selected.value = selected.value.filter((n: number) => num !== n);
        } else {
            selected.value.push(num);
        }
    }

    function toggleAllSelected(): void {
        if (isNullable(alignments.value)) {
            return;
        }
        if (allSelected.value) {
            selected.value = [];
        } else {
            selected.value = range(1, total.value + 1); // numbers are one-based
        }
    }

    async function loadHits(start: number, end: number) {
        const res: AlignmentResultResponse = await resultsService.fetchAlignmentResults(
            args.jobID,
            start,
            end,
            args.resultField?.value
        );
        total.value = res.total;
        if (isNullable(alignments.value)) {
            alignments.value = res.alignments;
        } else {
            alignments.value.push(...res.alignments);
        }
    }

    async function init() {
        await loadHits(0, perPage);
    }

    async function intersected() {
        if (!loadingMore.value && isNonNullable(alignments.value) && alignments.value.length < total.value) {
            loadingMore.value = true;
            try {
                await loadHits(alignments.value.length, alignments.value.length + perPage);
            } catch (e) {
                args.logger.error(e);
            }
            loadingMore.value = false;
        }
    }

    const showModalsBus = useEventBus<ModalParams>('show-modal');

    function forwardSelected(): void {
        if (selected.value.length > 0) {
            if (isNonNullable(args.toolParameters) && isNonNullable(alignments.value)) {
                showModalsBus.emit({
                    id: 'forwardingModal',
                    props: {
                        forwardingJobID: args.jobID,
                        forwardingApiOptionsAlignment: {
                            selectedItems: selected.value,
                            resultField: args.resultField?.value ?? 'alignment',
                        },
                        forwardingMode: args.toolParameters.forwarding,
                    },
                });
            } else {
                args.logger.error('tool parameters not loaded. Cannot forward');
            }
        }
    }

    return {
        init,
        intersected,
        alignments,
        selected,
        allSelected,
        selectedChanged,
        toggleAllSelected,
        total,
        loadingMore,
        forwardSelected,
    };
}
