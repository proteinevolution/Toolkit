<template>
    <BaseModal
        id="templateStructureModal"
        :title="$t('jobs.results.templateStructure.title', { accession })"
        size="lmd"
        :static="true"
        :lazy="false"
        @shown="onShow"
        @hide="resetView">
        <Loading v-if="loading" :message="$t('loading')" />

        <!-- refs are only accessible when in DOM => don't hide -->
        <div ref="viewport" class="stage" style="width: 100%; height: 500px"></div>

        <b-btn v-if="!loading" variant="primary" class="mt-3" @click="download" v-text="$t('download')" />
    </BaseModal>
</template>

<script lang="ts">
import { defineComponent, onBeforeUnmount, ref, Ref } from 'vue';
import BaseModal from './BaseModal.vue';
import Loading from '@/components/utils/Loading.vue';
import Logger from 'js-logger';
import { resultsService } from '@/services/ResultsService';
import { Stage } from 'ngl';
import useToolkitNotifications from '@/composables/useToolkitNotifications';
import { isNullable } from '@/util/nullability-helpers';

const logger = Logger.get('TemplateStructureModal');

export default defineComponent({
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
    setup() {
        const { alert } = useToolkitNotifications();

        const stage: any = undefined;
        const viewport: Ref<HTMLElement | null> = ref(null);

        function resize(): void {
            if (isNullable(viewport.value) || isNullable(stage)) {
                return;
            }
            const width: number = (viewport.value.parentElement as HTMLElement).clientWidth;
            const height = 500;
            viewport.value.style.height = height + 'px';
            viewport.value.style.width = width + 'px';
            stage.setSize(width, height);
        }

        function resetView(): void {
            (viewport.value as HTMLElement).innerHTML = '';
        }

        onBeforeUnmount(() => {
            window.removeEventListener('resize', resize);
        });

        return { alert, resize, resetView, stage, viewport };
    },
    data() {
        return {
            loading: true,
            file: '',
        };
    },
    methods: {
        onShow(): void {
            this.resize();
            this.loadData();
        },
        getExtension(filename: string): string {
            return filename.split('.').reverse()[0];
        },
        download(): void {
            resultsService.downloadAsFile(this.file, `${this.accession}.pdb`);
        },
        async loadData() {
            this.loading = true;
            try {
                const response = await resultsService.getStructureFile(this.accession);
                if (!response.filename) {
                    logger.error("Filename couldn't be read from axios response.");
                    this.alert(this.$t('errors.templateStructureFailed'), 'danger');
                    return;
                }
                this.file = response.data;
                const ext: string = this.getExtension(response.filename);
                if (this.stage) {
                    this.stage.dispose();
                    (this.$refs.viewport as HTMLElement).innerHTML = '';
                }
                this.stage = new Stage(this.$refs.viewport, {
                    backgroundColor: 'white',
                });
                await this.stage.loadFile(new Blob([this.file]), {
                    defaultRepresentation: true,
                    binary: true,
                    sele: ':A or :B or DPPC',
                    ext,
                });
                window.addEventListener('resize', this.resize);
                this.resize();
            } catch (err) {
                this.alert(this.$t('errors.templateStructureFailed'), 'danger');
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
