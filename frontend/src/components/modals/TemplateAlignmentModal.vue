<template>
    <BaseModal :title="$t('jobs.results.templateAlignment.title')"
               id="templateAlignmentModal"
               size="lg">
        <Loading v-if="loading"/>
        <div v-else>
            <b-form-select v-model="selectedOption"
                           :options="forwardingOptions"
                           class="select">
                <template slot="first">
                    <option :value="null"
                            v-text="$t('jobs.results.templateAlignment.forwardTo')"></option>
                </template>
            </b-form-select>
            <b-form-textarea v-model="data"
                             readonly
                             class="file-view">
            </b-form-textarea>
        </div>
    </BaseModal>
</template>

<script lang="ts">
    import Vue from 'vue';
    import BaseModal from './BaseModal.vue';
    import Loading from '@/components/utils/Loading.vue';
    import Logger from 'js-logger';
    import {resultsService} from '@/services/ResultsService';

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
        },
        data() {
            return {
                loading: true,
                data: '',
                selectedOption: null,
                forwardingOptions: [
                    // TODO: Implement correct forwarding options
                    {value: 'TODO', text: 'TODO'},
                    {value: 'TODO', text: 'TODO'},
                ],
            };
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
                this.loading = true;
                try {
                    await resultsService.generateTemplateAlignment(this.jobID, this.accession);
                    const res: any = await resultsService.getFile(this.jobID, this.accession);
                    this.data = String(res);
                } catch (err) {
                    this.$alert(this.$t('errors.templateAlignmentFailed'), 'danger');
                } finally {
                    this.loading = false;
                }
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
</style>
