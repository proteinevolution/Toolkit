<template>
    <div>
        <Loading :message="$t('loading')"
                 v-if="loading"/>
        <div v-else>
            <div class="result-options">
                <a @click="downloadPdb">{{$t('jobs.results.actions.downloadPDBFile')}}</a>
            </div>
            <hr class="mt-2">
        </div>

        <!-- refs are only accessible when in DOM => don't hide -->
        <div ref="viewport"
             class="stage"
             style="width: 100%; height: 500px"></div>
    </div>
</template>

<script lang="ts">
    import mixins from 'vue-typed-mixins';
    import ResultTabMixin from '@/mixins/ResultTabMixin';
    import Logger from 'js-logger';
    import {resultsService} from '@/services/ResultsService';
    import {Stage} from 'ngl';
    import Loading from '@/components/utils/Loading.vue';

    const logger = Logger.get('NGL3DStructureView');

    export default mixins(ResultTabMixin).extend({
        name: 'NGL3DStructureView',
        components: {
            Loading,
        },
        data() {
            return {
                stage: undefined as any,
                file: undefined as string | undefined,
            };
        },
        beforeDestroy() {
            window.removeEventListener('resize', this.windowResized);
        },
        watch: {
            fullScreen: {
                immediate: true,
                handler(value: boolean): void {
                    this.resize(value);
                },
            },
        },
        methods: {
            async init() {
                this.file = await resultsService.getFile(this.job.jobID, `${this.job.jobID}.pdb`);
                this.stage = new Stage(this.$refs.viewport, {
                    backgroundColor: 'white',
                });
                this.stage.loadFile(new Blob([this.file], {type: 'text/plain'}),
                    {defaultRepresentation: true, ext: 'pdb'});
                window.addEventListener('resize', this.windowResized);
                this.windowResized();
            },
            windowResized(): void {
                this.resize(this.fullScreen);
            },
            resize(fullScreen: boolean): void {
                const viewport: HTMLElement = (this.$refs.viewport as HTMLElement);
                if (!viewport) {
                    return;
                }
                const width: number = (viewport.parentElement as HTMLElement).clientWidth;
                const height: number = fullScreen ? window.innerHeight - 300 : 500;
                viewport.style.height = height + 'px';
                viewport.style.width = width + 'px';
                this.stage.setSize(width, height);
            },
            downloadPdb(): void {
                if (this.file) {
                    const downloadFilename = `${this.tool.name}_${this.job.jobID}.pdb`;
                    resultsService.downloadAsFile(this.file, downloadFilename);
                }
            },
        },
    });
</script>

<style lang="scss">
    .stage {
        margin: 0 auto;

        canvas {
            border: 1px solid lightgray;
        }
    }
</style>
