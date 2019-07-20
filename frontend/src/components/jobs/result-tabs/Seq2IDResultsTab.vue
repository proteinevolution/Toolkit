<template>
    <Loading :message="$t('loading')"
             v-if="loading"/>
    <div v-else>
        <div class="result-options">
            <a @click="forwardAll">{{$t('jobs.results.actions.forwardAll')}}</a>
            <a @click="download">{{$t('jobs.results.actions.download')}}</a>
        </div>
        <hr class="mt-2">

        <div class="file-view">
            <b v-text="$t('jobs.results.seq2ID.numRetrieved', {num: accIds.length})"></b>
            <br><br>
            <div v-for="acc in accIds"
                 v-text="acc"></div>
        </div>
    </div>
</template>

<script lang="ts">
    import mixins from 'vue-typed-mixins';
    import ResultTabMixin from '@/mixins/ResultTabMixin';
    import Loading from '@/components/utils/Loading.vue';
    import Logger from 'js-logger';
    import {resultsService} from '@/services/ResultsService';
    import EventBus from '@/util/EventBus';

    const logger = Logger.get('Seq2IDResultsTab');

    export default mixins(ResultTabMixin).extend({
        name: 'Seq2IDResultsTab',
        components: {
            Loading,
        },
        data() {
            return {
                accIds: [],
                loading: false,
            };
        },
        computed: {
            filename(): string {
                return 'ids.json';
            },
        },
        methods: {
            async init() {
                const data: any = await resultsService.getFile(this.job.jobID, this.filename);
                this.accIds = data.ACC_IDS;
            },
            download(): void {
                const downloadFilename = `${this.tool.name}_${this.job.jobID}.fasta`;
                resultsService.downloadAsFile(this.accIds.join('\n'), downloadFilename);
            },
            forwardAll(): void {
                if (this.tool.parameters) {
                    EventBus.$emit('show-modal', {
                        id: 'forwardingModal', props: {
                            forwardingData: this.accIds.join('\n'),
                            forwardingMode: this.tool.parameters.forwarding,
                        },
                    });
                } else {
                    logger.error('tool parameters not loaded. Cannot forward');
                }
            },
        },
    });
</script>

<style lang="scss" scoped>
    .file-view {
        width: 100%;
        font-size: 12px;
        font-family: $font-family-monospace;
    }

    .fullscreen .file-view {
        height: 70vh;
    }
</style>
