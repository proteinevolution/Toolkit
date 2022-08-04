<template>
    <Loading v-if="loading"
             :message="$t('loading')" />
    <div v-else>
        <div class="result-options d-flex align-items-center">
            <b-form-select v-model="radial"
                           :options="layoutOptions"
                           size="sm"
                           class="w-auto"
                           @input="handleRadialChanged" />
            <a class="ml-auto"
               @click="download">{{ $t('jobs.results.actions.downloadTree') }}</a>
        </div>

        <div id="treeContainer"
             ref="treeContainer"
             class="tree-widget"></div>
    </div>
</template>

<script lang="ts">
import ResultTabMixin from '@/mixins/ResultTabMixin';
import Loading from '@/components/utils/Loading.vue';
import {resultsService} from '@/services/ResultsService';
import Logger from 'js-logger';
import {phylotree as Phylotree} from 'phylotree/dist/phylotree.js';

import "phylotree/dist/phylotree.css";

const logger = Logger.get('TreeTab');

export default ResultTabMixin.extend({
    name: 'TreeTab',
    components: {
        Loading,
    },
    data() {
        return {
            tree: undefined as any,
            radial: true,
            layoutOptions: [
                {value: true, text: 'Radial'},
                {value: false, text: 'Vertical'},
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
                this.tree = new Phylotree(data);
                this.tree.render({
                  container: '#treeContainer',
                  'is-radial': this.radial,
                  'left-right-spacing': 'fit-to-size',
                  node_circle_size: () => 4,
                  'node-styler': (element: any) => element.selectAll('circle').style('fill','#2E8C81').style('stroke', 'black'),
                  logger,
                });
                const containerRef = this.$refs.treeContainer as HTMLDivElement;
                containerRef.innerHTML = '';
                containerRef.appendChild(this.tree.display.show());
                this.handleWindowResize();
                window.addEventListener('resize', this.handleWindowResize);
            });
        },
        handleWindowResize(): void {
            this.tree?.display
                .set_size(this.getTreeSize())
                .update(false);
        },
        handleRadialChanged(): void {
            this.tree?.display
                .radial(this.radial)
                .set_size(this.getTreeSize())
                .update(false);
        },
        getTreeSize(): number[] {
            return [1, (this.$refs.treeContainer as HTMLElement).clientWidth];
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
.tree-widget {
  margin-bottom: 2rem;
  margin-top: 1.5rem;
}
</style>
