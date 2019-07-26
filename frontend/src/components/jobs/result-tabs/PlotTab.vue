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
    import {ProbEvalList} from '@/types/toolkit/results';
    import {Chart} from 'highcharts-vue';

    const logger = Logger.get('PlotTab');

    export default mixins(ResultTabMixin).extend({
        name: 'PlotTab',
        components: {
            Loading,
            highcharts: Chart,
        },
        data() {
            return {
                results: undefined as ProbEvalList | undefined,
                loading: true,
            };
        },
        computed: {
            chartOptions(): any {
                return {
                    title: {
                        text: this.$t(`jobs.results.plot.${this.type}.title`),
                        x: -20, // center
                    },
                    xAxis: {
                        title: {
                            text: this.$t(`jobs.results.plot.${this.type}.xLabel`),
                        },
                    },
                    yAxis: {
                        title: {
                            text: this.$t(`jobs.results.plot.${this.type}.yLabel`),
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
                        name: this.$t(`jobs.results.plot.${this.type}.legend`),
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
                    for (let i = 0; i < this.results.vals.length; i++) {
                        bins.push([this.results.vals[i], i]);
                    }
                    return bins;
                }
                return undefined;
            },
            type(): string {
                if (!this.results) {
                    return '';
                }
                return this.results.type;
            },
        },
        methods: {
            async init() {
                this.results = await resultsService.getFile(this.job.jobID, 'plot_data.json') as ProbEvalList;
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
