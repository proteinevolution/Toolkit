<template>
    <Loading :message="$t('loading')"
             v-if="loading || !results"/>
    <div v-else>
        <div class="note" v-if="results.results.hits.length === 0">
            No repeats found! You could consider picking a less stringent E-value inclusion cut-off.<br><br><br><br>
        </div>
        <div v-else class="tprResults">
            <br>
            <div v-for="hit in results.results.desc" class="tprInfo">
                {{ hit[0]}}: <span> {{hit[1]}} </span>
            </div>
            <br><br><br>

            <table class="unstriped">
                <tbody class="alignmentTBody">
                <tr class="header">
                    <td>Repeat</td>
                    <td>Begin</td>
                    <td>Alignment</td>
                    <td>End</td>
                    <td>P-value</td>
                </tr>
                <template v-for="hit in results.results.hits">
                    <tr class="sequenceAlignment">
                        <td v-text="hit[1]"></td>
                        <td v-text="hit[2]"></td>
                        <td class="tprHit" v-html="coloredSeq(hit[0])"></td>
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
        computed: {},
        methods: {
            async init() {
                this.results = await resultsService.fetchResults(this.job.jobID);
            },
            coloredSeq(seq: string): string {
                return colorSequence(seq);
            },
        },

    });

</script>

<style lang="scss" scoped>
    .note {
        font-weight: bold;
        font-size: 0.9em;
    }

    .alignmentTBody td {
        padding: unset;
    }

    .tprHit {
        width: 27em;
        text-align: left;
    }

    .tprResults table {
        width: 80%;
    }

    .tprInfo {
        font-size: 0.9em !important;
    }

    .tprInfo span {
        font-weight: bold;
    }

    .sequenceAlignment {
        font-family: "SFMono-Regular", Consolas, "Source Code Pro", "Liberation Mono", Menlo, Courier, monospace;
        letter-spacing: 0.05em;
        font-size: 0.75rem;
    }

    .header {
        font-weight: bold;
    }

</style>
