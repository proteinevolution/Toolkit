import { ILogger } from 'js-logger';
import { HHInfoResult, SearchAlignmentItem } from '@/types/toolkit/results';
import { colorSequence, ssColorSequence } from '@/util/SequenceUtils';
import useResultTab, { ResultTabPropsWithDefaults } from '@/composables/useResultTab';
import { resultsService } from '@/services/ResultsService';
import handyScroll from 'handy-scroll';
import { debounce, range } from 'lodash-es';
import { ModalParams } from '@/types/toolkit/utils';
import { useEventBus } from '@vueuse/core';
import { computed, nextTick, onBeforeUnmount, ref, Ref } from 'vue';
import { isNonNullable, isNullable } from '@/util/nullability-helpers';

interface UseSearchResultTabArguments {
    logger: ILogger;
    // We want to pass through the props themselves because they are reactive
    props: Readonly<ResultTabPropsWithDefaults>;
    onInitialized?: () => void;
    initialColor?: boolean;
}

export default function useSearchResultTab<T extends SearchAlignmentItem, S extends HHInfoResult>({
    logger,
    props,
    onInitialized,
    initialColor = false,
}: UseSearchResultTabArguments) {
    const alignments = ref<T[] | undefined>(undefined) as Ref<T[] | undefined>;
    const info = ref<S | undefined>(undefined) as Ref<S | undefined>;
    const selectedItems = ref<number[]>([]);
    const perPage = 50;
    const total = ref(100);
    const loadingMore = ref(false);
    const color = ref(initialColor);
    const wrap = ref(true);

    const allSelected = computed(() => {
        if (isNullable(alignments.value)) {
            return false;
        }
        return total.value > 0 && selectedItems.value.length === total.value;
    });

    function toggleAllSelected(): void {
        if (isNullable(alignments.value)) {
            return;
        }
        if (allSelected.value) {
            selectedItems.value = [];
        } else {
            selectedItems.value = range(1, total.value + 1); // numbers are one-based
        }
    }

    async function loadAlignments(start: number, end: number): Promise<void> {
        const res = await resultsService.fetchHHAlignmentResults<T, S>(props.job.jobID, start, end);
        total.value = res.total;
        info.value = res.info;
        if (isNullable(alignments.value)) {
            alignments.value = res.alignments;
        } else {
            alignments.value.push(...res.alignments);
        }
    }

    const scrollElem = ref<HTMLElement | null>(null);

    function updateHandyScroll(): void {
        if (isNonNullable(scrollElem.value)) {
            if (!handyScroll.mounted(scrollElem.value)) {
                handyScroll.mount(scrollElem.value);
            } else {
                handyScroll.update(scrollElem.value);
            }
        }
    }

    const debouncedUpdateHandyScroll = debounce(updateHandyScroll, 100);

    async function init(): Promise<void> {
        await loadAlignments(0, perPage);
        window.addEventListener('resize', debouncedUpdateHandyScroll);
        onInitialized?.();
    }

    onBeforeUnmount(() => {
        if (isNonNullable(scrollElem.value)) {
            handyScroll.destroy(scrollElem.value);
        }
        window.removeEventListener('resize', debouncedUpdateHandyScroll);
    });

    const { loading } = useResultTab({
        init,
        resultTabName: props.resultTabName,
        renderOnCreate: props.renderOnCreate,
    });

    async function intersected(): Promise<void> {
        if (!loadingMore.value && isNonNullable(alignments.value) && alignments.value.length < total.value) {
            loadingMore.value = true;
            try {
                await loadAlignments(alignments.value.length, alignments.value.length + perPage);
            } catch (e) {
                logger.error(e);
            }
            loadingMore.value = false;
        }
    }

    const scrollRefs = ref<Record<string, HTMLElement>>({});

    const registerScrollRef = (key: string) => (el: HTMLElement) => (scrollRefs.value[key] = el);

    function scrollTo(ref: string): void {
        scrollRefs.value[ref]?.scrollIntoView({
            block: 'start',
            behavior: 'smooth',
        });
    }

    async function scrollToElem(num: number): Promise<void> {
        const loadNum: number = num + 2; // load some more for better scrolling
        if (isNonNullable(alignments.value)) {
            if (!alignments.value.map((a: SearchAlignmentItem) => a.num).includes(loadNum)) {
                await loadAlignments(alignments.value.length, loadNum);
            }
            scrollTo('alignment-' + num);
        }
    }

    function check(val: boolean, num: number): void {
        if (val && !selectedItems.value.includes(num)) {
            selectedItems.value.push(num);
        } else {
            const i: number = selectedItems.value.indexOf(num);
            if (i > -1) {
                selectedItems.value.splice(i, 1);
            }
        }
    }

    async function toggleWrap(): Promise<void> {
        wrap.value = !wrap.value;
        await nextTick();
        updateHandyScroll();
    }

    function toggleColor(): void {
        color.value = !color.value;
    }

    function coloredSeq(seq: string): string {
        return color.value ? colorSequence(seq) : seq;
    }

    function coloredSeqSS(seq: string): string {
        return color.value ? ssColorSequence(seq) : seq;
    }

    function alEnd(a: { end: number }): string {
        return ` &nbsp; ${a.end}`;
    }

    function alEndRef(a: { end: number; ref: number }): string {
        return ` &nbsp; ${a.end} (${a.ref})`;
    }

    const resubmitSectionBus = useEventBus<string>('resubmit-section');

    function resubmitSection([start, end]: [number, number]): void {
        if (isNullable(info.value)) {
            return;
        }
        const section: string = '>' + info.value.query.accession + '\n' + info.value.query.seq.slice(start - 1, end);
        resubmitSectionBus.emit(section);
    }

    const showModalsBus = useEventBus<ModalParams>('show-modal');

    function displayTemplateAlignment(accession: string): void {
        if (isNonNullable(props.tool.parameters)) {
            showModalsBus.emit({
                id: 'templateAlignmentModal',
                props: {
                    jobID: props.job.jobID,
                    accession,
                    forwardingMode: props.tool.parameters.forwarding,
                },
            });
        } else {
            logger.error('tool parameters not loaded. Cannot forward');
        }
    }

    function forward(disableSequenceLengthSelect: boolean = false): void {
        if (isNonNullable(props.tool.parameters)) {
            showModalsBus.emit({
                id: 'forwardingModal',
                props: {
                    forwardingJobID: props.job.jobID,
                    forwardingMode: props.tool.parameters.forwarding,
                    forwardingApiOptions: {
                        disableSequenceLengthSelect,
                        selectedItems: selectedItems.value,
                    },
                },
            });
        } else {
            logger.error('tool parameters not loaded. Cannot forward');
        }
    }

    async function forwardQueryA3M() {
        const a3mData: any = await resultsService.getFile(props.job.jobID, 'reduced.a3m');
        showModalsBus.emit({
            id: 'forwardingModal',
            props: {
                forwardingJobID: props.job.jobID,
                forwardingData: a3mData,
                forwardingMode: {
                    alignment: ['hhpred', 'formatseq', 'hhblits', 'hhomp', 'hhrepid'],
                },
            },
        });
    }

    return {
        alignments,
        info,
        total,
        loading,
        loadingMore,
        selectedItems,
        allSelected,
        toggleAllSelected,
        intersected,
        check,
        wrap,
        toggleWrap,
        color,
        toggleColor,
        coloredSeq,
        coloredSeqSS,
        alEnd,
        alEndRef,
        registerScrollRef,
        scrollTo,
        scrollToElem,
        resubmitSection,
        displayTemplateAlignment,
        forward,
        forwardQueryA3M,
        showModalsBus,
    };
}
