<template>
    <Loading v-if="loading"
             :message="$t('loading')" />
    <div v-else>
        <div class="result-options d-flex align-items-center">
            <b-form inline>
                <label class="mr-sm-2"
                       for="layoutSelect">{{ $t('jobs.results.tree.layout') }}</label>
                <b-form-select id="layoutSelect"
                               v-model="layout"
                               :options="layoutOptions"
                               size="sm"
                               class="w-auto"
                               @input="handleRadialChanged" />

                <label class="mr-sm-2 ml-sm-3"
                       for="hStretchSlider">{{ $t('jobs.results.tree.horizontalStretch') }}</label>
                <vue-slider id="hStretchSlider"
                            v-model="hStretch"
                            :min="0.1"
                            :max="3"
                            :interval="0.1"
                            :height="6"
                            :width="100"
                            @change="handleHStretchChanged" />

                <label class="mr-sm-2 ml-sm-3"
                       for="vStretchSlider">{{ $t('jobs.results.tree.verticalStretch') }}</label>
                <vue-slider id="vStretchSlider"
                            v-model="vStretch"
                            :min="0.1"
                            :max="3"
                            :interval="0.1"
                            :height="6"
                            :width="100"
                            @change="handleVStretchChanged" />
            </b-form>

            <a class="ml-auto"
               @click="download">{{ $t('jobs.results.actions.downloadTree') }}</a>
        </div>

        <div id="treeContainer"
             ref="treeContainer"></div>
    </div>
</template>

<script lang="ts">
import ResultTabMixin from '@/mixins/ResultTabMixin';
import Loading from '@/components/utils/Loading.vue';
import {resultsService} from '@/services/ResultsService';
import Logger from 'js-logger';
import {TidyTree} from 'tidytree';
import {select} from 'd3-selection';
import VueSlider from 'vue-slider-component';
import {debounce} from 'lodash-es';

const logger = Logger.get('TreeTab');

const hStretchDefault = 0.8;
const vStretchCircularDefault = 0.8;
const vStretchHorizontalDefault = 1;

export default ResultTabMixin.extend({
    name: 'TreeTab',
    components: {
        Loading,
        VueSlider,
    },
    data() {
        return {
            tree: undefined as any,
            hStretch: hStretchDefault,
            vStretch: vStretchCircularDefault,
            layout: 'circular',
            layoutOptions: [
                {value: 'circular', text: this.$t('jobs.results.tree.circular')},
                {value: 'horizontal', text: this.$t('jobs.results.tree.horizontal')},
            ],
        };
    },
    computed: {
        downloadFilePath(): string {
            return resultsService.getDownloadFilePath(this.job.jobID, this.filename);
        },
        filename(): string {
            return `${this.job.jobID}.tree`;
        },
    },
    beforeDestroy() {
        window.removeEventListener('resize', this.handleWindowResize);
    },
    methods: {
        async init() {
            const data = await resultsService.getFile<string>(this.job.jobID, this.filename);
            this.loading = false;
            this.$nextTick(() => {
                this.tree = new TidyTree(data, {
                    parent: '#treeContainer',
                    type: 'dendrogram',
                    mode: 'square',
                    leafLabels: true,
                    branchNodes: true,
                    hStretch: this.hStretch,
                    vStretch: this.vStretch,
                    layout: this.layout,
                    margin: [20, 20, 0, 0]
                });
                const nodeStyler = (node: any) => select(node).attr('r', 4).style('fill', '#2E8C81');
                this.tree.eachLeafNode(nodeStyler);
                this.tree.eachBranchNode(nodeStyler);
                window.addEventListener('resize', this.handleWindowResize);
            });
        },
        handleWindowResize: debounce(function (this: any) {
            this.tree?.redraw().recenter();
        }, 200),
        handleRadialChanged(): void {
            const defaultVStretch = this.layout == 'circular' ? vStretchCircularDefault : vStretchHorizontalDefault;
            if (this.vStretch != defaultVStretch) {
                this.vStretch = defaultVStretch;
                this.handleVStretchChanged();
            }
            if (this.hStretch != hStretchDefault) {
                this.hStretch = hStretchDefault;
                this.handleHStretchChanged();
            }
            this.tree?.setLayout(this.layout).recenter();
        },
        handleHStretchChanged(): void {
            this.tree?.setHStretch(this.hStretch);
        },
        handleVStretchChanged(): void {
            this.tree?.setVStretch(this.vStretch);
        },
        download(): void {
            const downloadFilename = `${this.tool.name}_${this.job.jobID}.tree`;
            resultsService.downloadFile(this.job.jobID, this.filename, downloadFilename)
                .catch((e) => {
                    logger.error(e);
                });
        },
    },
    watch: {
        fullScreen() {
            this.handleWindowResize();
        },
    },
});
</script>

<style lang="scss">
#treeContainer {
  height: calc(90vh - 100px);
  min-height: 400px;
}
</style>
