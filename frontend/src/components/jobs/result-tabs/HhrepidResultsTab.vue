<template>
    <Loading v-if="loading || !results"
             :message="$t('loading')"/>
    <div v-else>
        <b v-if="results.results.length === 0"
           v-html="$t('jobs.results.hhrepid.noResults')"></b>
        <div v-else>
            <div class="result-options">
                <a @click="forwardQueryA3M">{{ $t('jobs.results.actions.forwardQueryA3M') }}</a>
            </div>
            <template v-for="hit in results.results.reptypes">
                <h4 class="mb-4"
                    v-text="$t('jobs.results.hhrepid.resultsForType', {type: hit.typ})"></h4>
                <img :key="hit.typ"
                     :src="getFilePath(hit.typ)"
                     class="mb-3"
                     alt="">

                <table class="alignment-table mt-2">
                    <tbody>
                        <tr>
                            <td v-text="$t('jobs.results.hhrepid.numResults', {num: hit.num})"></td>
                        </tr>
                        <tr>
                            <td v-text="$t('jobs.results.hhrepid.pValue', {pval: hit.pval})"></td>
                        </tr>
                        <tr>
                            <td v-text="$t('jobs.results.hhrepid.length', {len: hit.len})"></td>
                        </tr>
                    </tbody>
                </table>
                <table class="alignment-table mt-4">
                    <thead>
                        <tr>
                            <th>ID</th>
                            <th>Prob</th>
                            <th>P-val</th>
                            <th>Loc</th>
                            <th>Sequence</th>
                        </tr>
                    </thead>
                    <tbody>
                        <template v-for="i in breakIndices(hit.len)">
                            <tr v-for="rep in hit.reps"
                                class="sequence-alignment">
                                <td v-text="rep.id"></td>
                                <td v-text="rep.prob"></td>
                                <td v-text="rep.pval"></td>
                                <td v-text="rep.loc"></td>
                                <td v-html="coloredSeq(rep.seq.slice(i, i + breakAfter))"></td>
                            </tr>
                            <tr class="empty-row"></tr>
                        </template>
                    </tbody>
                </table>
                <br>
            </template>
        </div>
    </div>
</template>

<script lang="ts">
    import mixins from 'vue-typed-mixins';
    import ResultTabMixin from '@/mixins/ResultTabMixin';
    import Loading from '@/components/utils/Loading.vue';
    import {resultsService} from '@/services/ResultsService';
    import Logger from 'js-logger';
    import {HhrepidResults} from '@/types/toolkit/results';
    import EventBus from '@/util/EventBus';
    import {colorSequence} from '@/util/SequenceUtils';

    const logger = Logger.get('HhrepidResultsTab');

    export default mixins(ResultTabMixin).extend({
        name: 'HhrepidResultsTab',
        components: {
            Loading,
        },
        data() {
            return {
                results: undefined as HhrepidResults | undefined,
                breakAfter: 80,
                file: '',
            };
        },
        computed: {
            filename(): string {
                if (!this.viewOptions.filename) {
                    return '';
                }
                return this.viewOptions.filename.replace(':jobID', this.job.jobID);
            },
            forwardingEnabled(): boolean {
                return this.viewOptions.hasOwnProperty('forwarding');
            },
        },
        methods: {
            async init() {
                this.results = await resultsService.fetchResults(this.job.jobID);
                this.file = await resultsService.getFile(this.job.jobID, this.filename);
            },
            getFilePath(typ: string): string {
                const jobID: string = this.job.jobID;
                return resultsService.getDownloadFilePath(jobID, `query_${typ}.png`);
            },
            forwardQueryA3M(): void {
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
            breakIndices(length: number): number[] {
                const res: number[] = [];
                for (let i = 0; i < length; i += this.breakAfter) {
                    res.push(i);
                }
                return res;
            },
            coloredSeq: colorSequence,
        },
    });
</script>

<style lang="scss" scoped>

    img {
        max-width: 100%;
    }

    .alignment-table {
        width: 100%;
        @include media-breakpoint-up(xl) {
            width: 100%;
        }
        font-size: 0.85em;

        .sequence-alignment {
            font-family: $font-family-monospace;
            letter-spacing: 0.05em;
        }
    }

    .empty-row {
        height: 2em;
    }
</style>
