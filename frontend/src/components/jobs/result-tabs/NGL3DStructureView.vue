<template>
    <div>
        <Loading v-if="loading" :message="t('loading')" />
        <div v-else>
            <div class="result-options">
                <a @click="downloadPdb">{{ t('jobs.results.actions.downloadPDBFile') }}</a>
            </div>
        </div>

        <!-- refs are only accessible when in DOM => don't hide -->
        <div ref="viewport" class="stage" style="width: 100%; height: 500px"></div>
    </div>
</template>

<script setup lang="ts">
import { onBeforeUnmount, ref, watch } from 'vue';
import useResultTab from '@/composables/useResultTab';
import { resultsService } from '@/services/ResultsService';
import Loading from '@/components/utils/Loading.vue';
import { useI18n } from 'vue-i18n';
import { Stage } from 'ngl';
import { isNonNullable, isNullable } from '@/util/nullability-helpers';
import { Job, JobViewOptions } from '@/types/toolkit/jobs';
import { Tool } from '@/types/toolkit/tools';

const { t } = useI18n();

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

const viewport = ref<HTMLElement | null>(null);
const stage = ref<Stage | undefined>(undefined);
const file = ref<string | undefined>(undefined);

function resize(fullScreen: boolean): void {
    if (isNullable(viewport.value) || isNullable(stage.value)) {
        return;
    }
    const width = (viewport.value.parentElement as HTMLElement).clientWidth + 'px';
    const height = (fullScreen ? window.innerHeight - 300 : 500) + 'px';
    viewport.value.style.height = height;
    viewport.value.style.width = width;
    stage.value.setSize(width, height);
}

watch(
    () => props.fullScreen,
    () => resize(props.fullScreen ?? false),
    { immediate: true }
);

function windowResized(): void {
    resize(props.fullScreen ?? false);
}

async function init() {
    file.value = (await resultsService.getFile(props.job.jobID, `${props.job.jobID}.pdb`)) as string;
    stage.value = new Stage(viewport.value as HTMLElement, {
        backgroundColor: 'white',
    });
    await stage.value.loadFile(new Blob([file.value as string], { type: 'text/plain' }), {
        defaultRepresentation: true,
        ext: 'pdb',
    });
    window.addEventListener('resize', windowResized);
    windowResized();
}

const { loading } = useResultTab({ init, resultTabName: props.resultTabName, renderOnCreate: props.renderOnCreate });

onBeforeUnmount(() => {
    window.removeEventListener('resize', windowResized);
});

function downloadPdb(): void {
    if (isNonNullable(file.value)) {
        const downloadFilename = `${props.tool.name}_${props.job.jobID}.pdb`;
        resultsService.downloadAsFile(file.value, downloadFilename);
    }
}
</script>

<style lang="scss">
.stage {
    margin: 0 auto;

    canvas {
        border: 1px solid lightgray;
    }
}
</style>
