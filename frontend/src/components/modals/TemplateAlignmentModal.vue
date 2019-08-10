<template>
    <BaseModal :title="$t('jobs.results.templateAlignment.title')"
               id="templateAlignmentModal"
               size="lmd">

        <b-form-select v-else v-model="selectedTool"
                       v-if="forwardingEnabled"
                       :options="toolOptions"
                       value-field="name"
                       text-field="longname"
                       class="select"
                       @change="forward"
                       :disabled="nodata">
            <template slot="first">
                <option :value="null"
                        v-text="$t('jobs.results.templateAlignment.forwardTo')"></option>
            </template>
        </b-form-select>

        <Loading :message="$t('loading')"
                 class="float-right"
                 :size="24"
                 v-if="loading"/>

        <b-form-textarea v-model="data"
                         readonly
                         class="file-view mb-2">
        </b-form-textarea>
    </BaseModal>
</template>

<script lang="ts">
    import Vue from 'vue';
    import BaseModal from './BaseModal.vue';
    import Loading from '@/components/utils/Loading.vue';
    import Logger from 'js-logger';
    import {resultsService} from '@/services/ResultsService';
    import {ForwardingMode, Tool} from '@/types/toolkit/tools';
    import EventBus from '@/util/EventBus';

    const logger = Logger.get('TemplateAlignmentModal');

    export default Vue.extend({
        name: 'TemplateAlignmentModal',
        components: {
            BaseModal,
            Loading,
        },
        props: {
            jobID: {
                type: String,
                required: true,
            },
            accession: {
                type: String,
                required: true,
            },
            forwardingMode: {
                type: Object as () => ForwardingMode,
                required: true,
            },
        },
        data() {
            return {
                loading: true,
                data: '',
                nodata: false,
                selectedTool: null,
            };
        },
        computed: {
            tools(): Tool[] {
                return this.$store.getters['tools/tools'];
            },
            forwardingEnabled(): boolean {
                return Boolean(this.forwardingMode.templateAlignment);
            },
            toolOptions(): Tool[] {
                if (this.forwardingEnabled) {
                    const options: string[] = (this.forwardingMode.templateAlignment as string[]);
                    return this.tools.filter((t: Tool) => options.includes(t.name))
                        .sort((t1: Tool, t2: Tool) => {
                            const t1Name = t1.longname.toLowerCase();
                            const t2Name = t2.longname.toLowerCase();
                            if (t1Name < t2Name) { // sort string ascending
                                return -1;
                            } else if (t1Name > t2Name) {
                                return 1;
                            }
                            return 0;
                        });
                }
                return [];
            },
        },
        watch: {
            jobID: {
                immediate: false,
                async handler(value: string) {
                    if (value) {
                        this.loadData();
                    }
                },
            },
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
            async loadData() {
                this.data = '';
                this.loading = true;
                await resultsService.generateTemplateAlignment(this.jobID, this.accession);
                this.data = await resultsService.getFile(this.jobID, this.accession);
                if (!this.data) {
                    this.$alert(this.$t('errors.templateAlignmentFailed'), 'danger');
                    this.data = 'Sorry, failed to fetch Template Alignment.';
                    this.nodata = true;
                }
                this.loading = false;
            },
            forward() {
                if (this.selectedTool) {
                    this.$router.push('/tools/' + this.selectedTool, () => {
                        EventBus.$on('paste-area-loaded', this.pasteForwardData);
                    });
                    EventBus.$emit('hide-modal', 'templateAlignmentModal');
                    this.resetData();
                } else {
                    logger.log('no tool selected');
                }
            },
            pasteForwardData() {
                EventBus.$off('paste-area-loaded', this.pasteForwardData);
                EventBus.$emit('forward-data', {data: this.data, jobID: this.jobID});
            },
            resetData() {
                this.selectedTool = null;
            },
        },
    });
</script>

<style lang="scss" scoped>
    .file-view {
        background: white !important;
        font-size: 0.7rem;
        height: 50vh;
        font-family: $font-family-monospace;
    }

    .select {
        width: 12em;
        margin-bottom: 1em;
    }

    .vue-simple-spinner {
        margin-left: 20rem !important;
    }
</style>
