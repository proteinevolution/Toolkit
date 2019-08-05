<template>
    <Loading :message="$t('loading')"
             v-if="loading"/>
    <div v-else>
        <div class="result-options"
             v-if="downloadEnabled || forwardingEnabled">
            <a @click="download"
               v-if="downloadEnabled">{{$t('jobs.results.actions.download')}}</a>
            <a @click="forwardAll"
               v-if="forwardingEnabled">{{$t('jobs.results.actions.forwardAll')}}</a>
        </div>

        <pre v-html="file"
             class="file-view"></pre>
    </div>
</template>

<script lang="ts">
    import mixins from 'vue-typed-mixins';
    import ResultTabMixin from '@/mixins/ResultTabMixin';
    import Loading from '@/components/utils/Loading.vue';
    import Logger from 'js-logger';
    import {resultsService} from '@/services/ResultsService';
    import EventBus from '@/util/EventBus';
    import {timeout} from '@/util/Utils';

    const logger = Logger.get('DataTab');

    export default mixins(ResultTabMixin).extend({
        name: 'DataTab',
        components: {
            Loading,
        },
        data() {
            return {
                file: '',
                maxTries: 50,
                tries: 0,
            };
        },
        computed: {
            filename(): string {
                if (!this.viewOptions.filename) {
                    return '';
                }
                return this.viewOptions.filename.replace(':jobID', this.job.jobID);
            },
            downloadEnabled(): boolean {
                return this.viewOptions.hasOwnProperty('download');
            },
            forwardingEnabled(): boolean {
                return this.viewOptions.hasOwnProperty('forwarding');
            },
        },
        methods: {
            async init() {
                this.file = await resultsService.getFile(this.job.jobID, this.filename);
                if (!this.file) {
                    ++this.tries;
                    if (this.tries === this.maxTries) {
                        logger.error('Couldn\'t fetch files.');
                        return;
                    }
                    await timeout(300);
                    await this.init();
                }
            },
            download(): void {
                const toolName = this.tool.name;
                const ending = toolName === 'hhpred' || toolName === 'hhomp' ? 'hhr' : 'out';
                const downloadFilename = `${toolName}_${this.job.jobID}.${ending}`;
                resultsService.downloadFile(this.job.jobID, this.filename, downloadFilename)
                    .catch((e) => {
                        logger.error(e);
                    });
            },
            forwardAll(): void {
                if (this.tool.parameters) {
                    EventBus.$emit('show-modal', {
                        id: 'forwardingModal', props: {
                            forwardingJobID: this.job.jobID,
                            forwardingData: this.file,
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
        height: 50vh;
        font-family: $font-family-monospace;
    }

    .fullscreen .file-view {
        height: 85vh;
    }
</style>
