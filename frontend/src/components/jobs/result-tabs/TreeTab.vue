<template>
    <Loading v-if="loading" :message="t('loading')" />
    <div v-else>
        <div class="result-options d-flex align-items-center">
            <b-form inline>
                <label class="mr-sm-2" for="layoutSelect">{{ t('jobs.results.tree.layout') }}</label>
                <b-form-select
                    id="layoutSelect"
                    v-model="layout"
                    :options="layoutOptions"
                    size="sm"
                    class="w-auto"
                    @input="handleRadialChanged" />

                <label class="mr-sm-2 ml-sm-3" for="hStretchSlider">{{
                    t('jobs.results.tree.horizontalStretch')
                }}</label>
                <vue-slider
                    id="hStretchSlider"
                    v-model="hStretch"
                    :min="0.1"
                    :max="3"
                    :interval="0.1"
                    :height="6"
                    :width="100"
                    @change="handleHStretchChanged" />

                <label class="mr-sm-2 ml-sm-3" for="vStretchSlider">{{
                    $t('jobs.results.tree.verticalStretch')
                }}</label>
                <vue-slider
                    id="vStretchSlider"
                    v-model="vStretch"
                    :min="0.1"
                    :max="3"
                    :interval="0.1"
                    :height="6"
                    :width="100"
                    @change="handleVStretchChanged" />
            </b-form>

            <a class="ml-auto" @click="download">{{ $t('jobs.results.actions.downloadTree') }}</a>
        </div>

        <div id="treeContainer" ref="treeContainer"></div>
    </div>
</template>

<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue';
import useResultTab from '@/composables/useResultTab';
import Loading from '@/components/utils/Loading.vue';
import { resultsService } from '@/services/ResultsService';
import Logger from 'js-logger';
import { useI18n } from 'vue-i18n';
import { TidyTree } from 'tidytree';
import { select } from 'd3-selection';
import VueSlider from 'vue-slider-component';
import { debounce } from 'lodash-es';
import { Job, JobViewOptions } from '@/types/toolkit/jobs';
import { Tool } from '@/types/toolkit/tools';

const logger = Logger.get('TreeTab');

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

const hStretchDefault = 0.8;
const vStretchCircularDefault = 0.8;
const vStretchHorizontalDefault = 1;
const hStretch = ref(hStretchDefault);
const vStretch = ref(vStretchCircularDefault);

type LayoutOption = 'circular' | 'horizontal';
const layout = ref<LayoutOption>('circular');
const layoutOptions = [
    { value: 'circular', text: t('jobs.results.tree.circular') },
    { value: 'horizontal', text: t('jobs.results.tree.horizontal') },
];

const tree = ref<any | undefined>(undefined);
const filename = computed(() => `${props.job.jobID}.tree`);

function handleRadialChanged(): void {
    const defaultVStretch = layout.value === 'circular' ? vStretchCircularDefault : vStretchHorizontalDefault;
    if (vStretch.value != defaultVStretch) {
        vStretch.value = defaultVStretch;
        handleVStretchChanged();
    }
    if (hStretch.value != hStretchDefault) {
        hStretch.value = hStretchDefault;
        handleHStretchChanged();
    }
    tree.value?.setLayout(layout.value).recenter();
}

function handleHStretchChanged(): void {
    tree.value?.setHStretch(hStretch.value);
}

function handleVStretchChanged(): void {
    tree.value?.setVStretch(vStretch.value);
}

const handleWindowResize = debounce(function (this: any) {
    this.tree?.redraw().recenter();
}, 200);

watch(() => props.fullScreen, handleWindowResize);

async function init() {
    const data = await resultsService.getFile<string>(props.job.jobID, filename.value);
    loading.value = false;
    await nextTick();
    tree.value = new TidyTree(data, {
        parent: '#treeContainer',
        type: 'dendrogram',
        mode: 'square',
        leafLabels: true,
        branchNodes: true,
        hStretch: hStretch.value,
        vStretch: vStretch.value,
        layout: layout.value,
        margin: [20, 20, 0, 0],
    });
    const nodeStyler = (node: any) => select(node).attr('r', 4).style('fill', '#2E8C81');
    tree.value.eachLeafNode(nodeStyler);
    tree.value.eachBranchNode(nodeStyler);
    window.addEventListener('resize', handleWindowResize);
}

const { loading } = useResultTab({ init, resultTabName: props.resultTabName, renderOnCreate: props.renderOnCreate });

function download(): void {
    const downloadFilename = `${props.tool.name}_${props.job.jobID}.tree`;
    resultsService.downloadFile(props.job.jobID, filename.value, downloadFilename).catch((e) => {
        logger.error(e);
    });
}

onBeforeUnmount(() => {
    window.removeEventListener('resize', handleWindowResize);
});
</script>

<style lang="scss">
#treeContainer {
    height: calc(90vh - 100px);
    min-height: 400px;
}
</style>
