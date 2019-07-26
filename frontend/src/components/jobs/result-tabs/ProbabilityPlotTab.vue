<template>
    <Loading :message="$t('loading')"
             v-if="loading"/>
    <div class="hcontainer" v-else>
        <highcharts :options="chartOptions"></highcharts>
    </div>
</template>

<script lang="ts">
    import mixins from 'vue-typed-mixins';
    import ResultTabMixin from '@/mixins/ResultTabMixin';
    import Loading from '@/components/utils/Loading.vue';
    import {resultsService} from '@/services/ResultsService';
    import Logger from 'js-logger';
    import {HHPredHit, HHPredResults} from '@/types/toolkit/results';
    import {Chart} from 'highcharts-vue';

    const logger = Logger.get('ProbabilityPlotTab');

    export default mixins(ResultTabMixin).extend({
        name: 'ProbabilityPlotTab',
        components: {
            Loading,
            highcharts: Chart,
        },
        data() {
            return {
                results: undefined as HHPredResults | undefined,
                loading: true,
            };
        },
        computed: {
            chartOptions(): any {
                return {
                    title: {
                        text: 'Probability Distribution',
                        x: -20, // center
                    },
                    xAxis: {
                        title: {
                            text: 'Probability',
                        },
                    },
                    yAxis: {
                        title: {
                            text: 'No. of matches',
                        },
                        plotLines: [{
                            value: 0,
                            width: 1,
                            color: '#808080',
                        }],
                    },
                    tooltip: {},
                    legend: {
                        layout: 'vertical',
                        align: 'right',
                        verticalAlign: 'middle',
                        borderWidth: 0,
                    },
                    chart: {
                        width: 750,
                    },
                    series: [{
                        name: 'No. HSPs',
                        data: this.probabilities,
                    }],
                    credits: {
                        enabled: false,
                    },
                };
            },
            probabilities(): number[][] | undefined {
                if (this.results !== undefined) {
                    const bins: number[][] = [];
                    for (let i = 0; i < this.results.results.hits.length; i++) {
                        bins.push([this.results.results.hits[i].prob, i]);
                    }
                    return bins;
                }
                return undefined;
            },
        },
        methods: {
            async init() {
                this.results = await resultsService.fetchResults(this.job.jobID) as HHPredResults;
                this.loading = false;
            },
        },
    });
</script>

<style lang="scss" scoped>
    .hcontainer {
        padding-left: 50px;
        @include media-breakpoint-down(md) {
            padding-left: 0;
        }
    }
</style>

