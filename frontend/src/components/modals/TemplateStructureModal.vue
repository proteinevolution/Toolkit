<template>
    <BaseModal id="templateStructureModal"
               :title="$t('jobs.results.templateStructure.title', {accession})"
               size="lmd"
               :static="true"
               :lazy="false"
               @shown="onShow"
               @hide="resetView">
        <Loading v-if="loading"
                 :message="$t('loading')" />

        <!-- refs are only accessible when in DOM => don't hide -->
        <div ref="viewport"
             class="stage"
             style="width: 100%; height: 500px"></div>
    </BaseModal>
</template>

<script lang="ts">
import Vue from 'vue';
import BaseModal from './BaseModal.vue';
import Loading from '@/components/utils/Loading.vue';
import Logger from 'js-logger';
import {resultsService} from '@/services/ResultsService';

const logger = Logger.get('TemplateStructureModal');

export default Vue.extend({
    name: 'TemplateStructureModal',
    components: {
        BaseModal,
        Loading,
    },
    props: {
        accession: {
            type: String,
            required: true,
        },
    },
    data() {
        return {
            loading: true,
            stage: undefined as any,
        };
    },
    beforeDestroy() {
        window.removeEventListener('resize', this.resize);
    },
    methods: {
        onShow(): void {
            this.resize();
            this.loadData();
        },
        getExtension(filename: string): string {
            return filename.split('.')[1];
        },
        resize(): void {
            const viewport: HTMLElement = (this.$refs.viewport as HTMLElement);
            if (!viewport || !this.stage) {
                return;
            }
            const width: number = (viewport.parentElement as HTMLElement).clientWidth;
            const height = 500;
            viewport.style.height = height + 'px';
            viewport.style.width = width + 'px';
            this.stage.setSize(width, height);
        },
        resetView(): void {
            (this.$refs.viewport as HTMLElement).innerHTML = '';
        },
        async loadData() {
            this.loading = true;
            try {
                const response = await resultsService.getStructureFile(this.accession);
                if (!response.filename) {
                    logger.error('Filename couldn\'t be read from axios response.');
                    this.$alert(this.$t('errors.templateStructureFailed'), 'danger');
                    return;
                }
                const ext: string = this.getExtension(response.filename);
                if (this.stage) {
                    this.stage.dispose();
                    (this.$refs.viewport as HTMLElement).innerHTML = '';
                }
                const ngl: any = await import(/* webpackChunkName: "ngl" */ 'ngl');
                this.stage = new ngl.Stage(this.$refs.viewport, {
                    backgroundColor: 'white',
                });
                this.stage.loadFile(new Blob([response.data]),
                    {defaultRepresentation: true, binary: true, sele: ':A or :B or DPPC', ext});
                window.addEventListener('resize', this.resize);
                this.resize();
            } catch (err) {
                this.$alert(this.$t('errors.templateStructureFailed'), 'danger');
                (this.$refs.viewport as HTMLElement).innerHTML = this.$t('errors.templateStructureFailed').toString();
            }
            this.loading = false;
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
