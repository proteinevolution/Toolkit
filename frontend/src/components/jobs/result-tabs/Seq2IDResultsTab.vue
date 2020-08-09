<template>
    <Loading v-if="loading || !accIds"
             :message="$t('loading')" />
    <div v-else>
        <div class="result-options">
            <a @click="forwardAll">{{ $t('jobs.results.actions.forwardAll') }}</a>
            <a @click="download">{{ $t('jobs.results.actions.download') }}</a>
        </div>

        <div class="file-view">
            <b v-text="$t('jobs.results.seq2ID.numRetrieved', {num: accIds.length})"></b>
            <br><br>
            <div v-for="(acc, i) in accIds"
                 :key="'accession-' + i"
                 v-text="acc"></div>
        </div>
    </div>
</template>

<script lang="ts">
    import ResultTabMixin from '@/mixins/ResultTabMixin';
    import Loading from '@/components/utils/Loading.vue';
    import Logger from 'js-logger';
    import {resultsService} from '@/services/ResultsService';
    import EventBus from '@/util/EventBus';
    import {timeout} from '@/util/Utils';

    const logger = Logger.get('Seq2IDResultsTab');

    export default ResultTabMixin.extend({
        name: 'Seq2IDResultsTab',
        components: {
            Loading,
        },
        data() {
            return {
                accIds: [],
                len: 0,
                maxTries: 50,
                tries: 0,
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
                if (data) {
                    this.accIds = data.ACC_IDS;
                } else {
                    ++this.tries;
                    if (this.tries === this.maxTries) {
                        logger.info('Couldn\'t fetch files.');
                        return;
                    }
                    await timeout(300);
                    await this.init();
                }
            },
            download(): void {
                const downloadFilename = `${this.tool.name}_${this.job.jobID}.fasta`;
                resultsService.downloadAsFile(this.accIds.join('\n'), downloadFilename);
            },
            forwardAll(): void {
                if (this.tool.parameters) {
                    EventBus.$emit('show-modal', {
                        id: 'forwardingModal', props: {
                            forwardingJobID: this.job.jobID,
                            forwardingData: this.accIds.join('\n'),
                            forwardingMode: this.tool.parameters.forwarding,
                        },
                    });
                } else {

                    logger.error('tool parameters not loaded. Cannot forward');
                }
            },
        },
    })
    ;
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
