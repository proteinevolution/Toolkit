<template>
    <Loading v-if="loading || !results"
             :message="$t('loading')" />
    <div v-else
         class="font-small">
        <b v-if="results.results.hits.length === 0"
           v-text="$t('jobs.results.patsearch.noResults')"></b>
        <div v-else>
            <div class="result-options">
                <a @click="download">{{ $t('jobs.results.actions.downloadHits') }}</a>
                <a @click="forwardAll">{{ $t('jobs.results.actions.forwardAll') }}</a>
            </div>

            <span v-html="$t('jobs.results.alignment.numSeqs', {num: results.results.hits.length})"></span>

            <table class="alignment-table mt-3">
                <tbody>
                    <template v-for="(hit, i) in results.results.hits">
                        <tr :key="'hit-name-' + i">
                            <td>
                                <b v-text="hit.name"></b>
                            </td>
                        </tr>
                        <tr :key="'hit-seq-' + i">
                            <td class="sequence-alignment"
                                v-html="colorHits(hit.seq, hit.matches)">
                            <!-- @{ hit.seq.map(s => Html(Common.insertMatch(s, result.len, hit.pats.getOrElse(Nil)))) -->
                            </td>
                        </tr>
                    </template>
                </tbody>
            </table>
        </div>
    </div>
</template>

<script lang="ts">
    import ResultTabMixin from '@/mixins/ResultTabMixin';
    import Loading from '@/components/utils/Loading.vue';
    import {resultsService} from '@/services/ResultsService';
    import Logger from 'js-logger';
    import {PatsearchHit, PatsearchMatch, PatsearchResults} from '@/types/toolkit/results';
    import {patsearchColor} from '@/util/SequenceUtils';
    import EventBus from '@/util/EventBus';

    const logger = Logger.get('PatsearchResultsTab');

    export default ResultTabMixin.extend({
        name: 'PatsearchResultsTab',
        components: {
            Loading,
        },
        data() {
            return {
                results: undefined as PatsearchResults | undefined,
            };
        },
        computed: {
            filename(): string {
                if (!this.viewOptions.filename) {
                    return '';
                }
                return this.viewOptions.filename.replace(':jobID', this.job.jobID);
            },
        },
        methods: {
            async init() {
                this.results = await resultsService.fetchResults(this.job.jobID);
            },
            colorHits(seq: string, matches: PatsearchMatch[]): string {
                return patsearchColor(seq, matches);
            },
            download(): void {
                const toolName = this.tool.name;
                const downloadFilename = `${toolName}_${this.job.jobID}.fas`;
                resultsService.downloadFile(this.job.jobID, this.filename, downloadFilename)
                    .catch((e) => {
                        logger.error(e);
                    });
            },
            forwardAll(): void {
                if (this.tool.parameters && this.results) {
                    EventBus.$emit('show-modal', {
                        id: 'forwardingModal', props: {
                            forwardingJobID: this.job.jobID,
                            forwardingData: this.results.results.hits.reduce((acc: string, cur: PatsearchHit) =>
                                acc + cur.name + '\n' + cur.seq + '\n', ''),
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
    .alignment-table {
        font-size: 0.9em;

        .sequence-alignment {
            font-family: $font-family-monospace;
            letter-spacing: 0.05em;
            word-break: break-all;
            white-space: unset;

            .pattern-match {
                color: red;
                background-color: rgba(255, 0, 8, 0.1);
            }
        }
    }
</style>

<style lang="scss">
    .pattern-match {
        color: red;
        background-color: rgba(255, 0, 8, 0.1);
    }
</style>
