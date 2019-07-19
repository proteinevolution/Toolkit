<template>
    <Loading :message="$t('loading')"
             v-if="loading"/>
    <div v-else>
        <div class="note" v-if="results.results.hits.length === 0">
            No hits found! You could consider picking a different target database.<br><br><br><br>
        </div>
        <div v-else>
            <div class="result-options">
                <a @click="download"
                   v-if="downloadEnabled">{{$t('jobs.results.actions.download')}}</a>
                <a @click="forwardAll"
                   v-if="forwardingEnabled">{{$t('jobs.results.actions.forwardAll')}}</a>
            </div>
            <hr class="mt-2"
                v-if="downloadEnabled || forwardingEnabled">
            Number of sequences: <b>{{results.results.hits.length}}</b>
            <br/><br/>
            <form id="alignmentResult">
                <table class="unstriped">
                    <tbody class="alignmentTBody">
                    <div v-for="hit in results.results.hits">
                        <tr class="header">
                            <td>
                                {{ hit.name }}
                            </td>
                        </tr>
                        <tr>
                            <td class="sequenceAlignment wrap"
                                v-html="colorHits(hit.seq, hit.matches, results.results.len)">
                                <!-- @{ hit.seq.map(s => Html(Common.insertMatch(s, result.len, hit.pats.getOrElse(Nil)))) -->
                            </td>
                        </tr>
                    </div>
                    <tr>
                        <td><br/></td>
                    </tr>
                    </tbody>
                </table>

            </form>
        </div>

    </div>

</template>

<script lang="ts">
    import Loading from '@/components/utils/Loading.vue';
    import {Tool} from '@/types/toolkit/tools';
    import {Job} from '@/types/toolkit/jobs';
    import Vue from 'vue';
    import {resultsService} from '@/services/ResultsService';
    import Logger from 'js-logger';
    import {PatsearchResults} from '@/types/toolkit/results';
    import {patsearchColor} from '@/util/SequenceUtils';

    const logger = Logger.get('PatsearchResultsTab');


    export default Vue.extend({
        name: 'PatsearchResultsTab',
        components: {
            Loading,
        },
        props: {
            viewOptions: {
                type: Object,
            },
            job: {
                type: Object as () => Job,
                required: true,
            },
            tool: {
                type: Object as () => Tool,
                required: true,
            },
        },
        data() {
            return {
                results: undefined as PatsearchResults | undefined,
                loading: false,
            };
        },
        computed: {
            downloadEnabled(): boolean {
                return this.viewOptions.hasOwnProperty('download');
            },
            forwardingEnabled(): boolean {
                return this.viewOptions.hasOwnProperty('forwarding');
            },
        },
        mounted() {
            this.loading = true;
            resultsService.fetchResults(this.job.jobID)
                .then((results: any) => {
                    this.results = results as PatsearchResults;
                })
                .catch((e: any) => {
                    logger.error(e);
                })
                .finally(() => {
                    this.loading = false;
                });
        },
        methods: {
            colorHits(seq: string, matches: string, len: number): string {
                return patsearchColor(seq, matches, len);
            },
        },
    });

</script>

<style lang="scss" scoped>
    .note {
        font-weight: bold;
    }

    .alignmentTBody td {
        padding: unset;
    }

    .sequenceAlignment {
        font-family: "SFMono-Regular", Consolas, "Source Code Pro", "Liberation Mono", Menlo, Courier, monospace;
        letter-spacing: 0.05em;
        font-size: 0.75rem;
    }

    .file-view {
        width: 100%;
        font-size: 12px;
        height: 50vh;
        font-family: $font-family-monospace;
    }

    .fullscreen .file-view {
        height: 85vh;
    }

    .wrap {
        word-break: break-all;
        white-space: unset;
    }

    .header {
        font-size: 0.8em;
        font-weight: bold;
        font-family: "Noto Sans", "Lucida Grande", "Lucida Sans Unicode", Geneva, Verdana, sans-serif;
    }
</style>
