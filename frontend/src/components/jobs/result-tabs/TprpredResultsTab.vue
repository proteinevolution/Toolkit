<template>
    <Loading :message="$t('loading')"
             v-if="loading || !results"/>
    <div v-else
         class="font-small">
        <b v-if="results.results.hits.length === 0"
           v-text="$t('jobs.results.tprpred.noResults')">
        </b>
        <div v-else>
            <div v-for="hit in results.results.desc"
                 class="tpr-info">
                {{ hit[0]}}: <b>{{hit[1]}}</b>
            </div>

            <table class="alignment-table mt-4">
                <thead>
                <tr>
                    <th>Repeat</th>
                    <th>Begin</th>
                    <th>Alignment</th>
                    <th>End</th>
                    <th>P-value</th>
                </tr>
                </thead>
                <tbody>
                <template v-for="hit in results.results.hits">
                    <tr class="sequence-alignment">
                        <td v-text="hit[1]"></td>
                        <td v-text="hit[2]"></td>
                        <td class="tpr-hit"
                            v-html="coloredSeq(hit[0])"></td>
                        <td v-text="hit[3]"></td>
                        <td v-text="hit[4]"></td>
                    </tr>
                </template>
                </tbody>
            </table>
            <br>
        </div>
    </div>
</template>

<script lang="ts">
    import mixins from 'vue-typed-mixins';
    import ResultTabMixin from '@/mixins/ResultTabMixin';
    import Loading from '@/components/utils/Loading.vue';
    import {resultsService} from '@/services/ResultsService';
    import Logger from 'js-logger';
    import {colorSequence} from '@/util/SequenceUtils';
    import {TprpredResults} from '@/types/toolkit/results';

    const logger = Logger.get('PatsearchResultsTab');

    export default mixins(ResultTabMixin).extend({
        name: 'PatsearchResultsTab',
        components: {
            Loading,
        },
        data() {
            return {
                results: undefined as TprpredResults | undefined,
            };
        },
        methods: {
            async init() {
                this.results = await resultsService.fetchResults(this.job.jobID);
            },
            coloredSeq: colorSequence,
        },
    });
</script>

<style lang="scss" scoped>
    .alignment-table {
        width: 100%;
        @include media-breakpoint-up(xl) {
            width: 80%;
        }
        font-size: 0.9em;

        .sequence-alignment {
            font-family: $font-family-monospace;
            letter-spacing: 0.05em;

            .tpr-hit {
                width: 27em;
            }
        }
    }
</style>
