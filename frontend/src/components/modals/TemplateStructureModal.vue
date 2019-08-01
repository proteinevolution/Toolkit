<template>
    <BaseModal :title="$t('jobs.results.templateStructure.title')"
               id="templateStructureModal"
               size="lg">
        <Loading :message="$t('loading')"
                 v-if="loading"/>
        <h6 v-else
            class='structureAccession'>
            3D Structure: {{accession}}
        </h6>

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
    import {Stage} from 'ngl';

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
        watch: {
            accession: {
                immediate: false,
                async handler(value: string) {
                    if (value) {
                        this.loadData();
                    }
                },
            },
        },
        methods: {
            getExtension(filename: string): string {
                return filename.split('.')[1];
            },
            async loadData() {
                this.loading = true;
                try {
                    const response = await resultsService.getStructureFile(this.accession);
                    if (response.filename === undefined) {
                        logger.error('Filename couldn\'t be read from axios response.');
                        this.$alert(this.$t('errors.templateStructureFailed'), 'danger');
                        return;
                    }
                    const ext: string = this.getExtension(response.filename);
                    this.stage = new Stage(this.$refs.viewport, {
                        backgroundColor: 'white',
                    });
                    this.stage.loadFile(new Blob([response.data]),
                        {defaultRepresentation: true, binary: true, sele: ':A or :B or DPPC', ext});
                    this.loading = false;
                } catch (err) {
                    this.$alert(this.$t('errors.templateStructureFailed'), 'danger');
                }
            },
        },
    });
</script>

<style lang="scss" scoped>
</style>
