<template>
    <alignment-viewer :sequences="alignments" />
</template>

<script setup lang="ts">
import { ref, Ref, watchEffect } from 'vue';
import AlignmentViewer from '@/components/tools/AlignmentViewer.vue';
import { AlignmentItem } from '@/types/toolkit/results';
import { resultsService } from '@/services/ResultsService';
import { useEventBus } from '@vueuse/core';
import useResultTab, { defineResultTabProps } from '@/composables/useResultTab';

const props = defineResultTabProps();

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
