<template>
    <alignment-viewer :sequences="alignments" />
</template>

<script setup lang="ts">
import { ref, Ref, watchEffect } from 'vue';
import AlignmentViewer from '@/components/tools/AlignmentViewer.vue';
import { AlignmentItem } from '@/types/toolkit/results';
import { resultsService } from '@/services/ResultsService';
import { useEventBus } from '@vueuse/core';
import useResultTab from '@/composables/useResultTab';
import { Job, JobViewOptions } from '@/types/toolkit/jobs';
import { Tool } from '@/types/toolkit/tools';

interface ResultTabProps {
    job: Job;
    tool: Tool;
    fullScreen?: boolean;
    viewOptions?: JobViewOptions;
    resultTabName?: string;
    renderOnCreate?: boolean;
}

const props = withDefaults(defineProps<ResultTabProps>(), {
    resultTabName: '',
    renderOnCreate: true,
});

const alignments: Ref<AlignmentItem[] | undefined> = ref(undefined);

async function init() {
    const res = await resultsService.fetchAlignmentResults(props.job.jobID);
    alignments.value = res.alignments;
}

useResultTab({ init, resultTabName: props.resultTabName, renderOnCreate: props.renderOnCreate });

const alignmentViewerResizeBus = useEventBus<boolean>('alignment-viewer-resize');
watchEffect(() => {
    alignmentViewerResizeBus.emit(props.fullScreen);
});
</script>
