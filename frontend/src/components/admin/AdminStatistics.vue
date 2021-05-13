<template>
    <div>
        <div class="row g-3">
            <div class="col-md-6 col-sm-8">
                <label>From</label>
                <b-form-datepicker id="from-datepicker"
                                   v-model="fromDate"
                                   view-mode="year"
                                   class="mb-2 " />
            </div>
            <div class="col-md-6 col-sm-8">
                <label>To</label>
                <b-form-datepicker id="to-datepicker"
                                   v-model="toDate"
                                   view-mode="year"
                                   class="mb-2" />
            </div>
            <div class="col-md-6 col-sm-8">
                <div class="input-group-append">
                    <b-button variant="primary"
                              class="mr-2"
                              @click="loadStatistics">
                        Load Statistics
                    </b-button>
                </div>
            </div>
        </div>

        <div v-if="showCharts">
            <highcharts :options="BarChartOptions"
                        class="high-chart" />
            <highcharts :options="WeeklyChartOptions"
                        class="high-chart" />
            <highcharts :options="MonthlyChartOptions"
                        class="high-chart" />
        </div>
    </div>
</template>

<script lang="ts">
import Vue from 'vue';
import {Chart} from 'highcharts-vue';
import {Statistics} from '@/types/toolkit/admin';
import moment from 'moment';
import {backendService} from '@/services/BackendService';

export default Vue.extend({
    name: "AdminStatistics",
    components: {
        highcharts: Chart,
    },
    data() {
        return {
            showCharts: false,
            fromDate: '',
            toDate: '',
            statistics: {} as Statistics
        };
    },
    computed: {
        BarChartOptions(): any {
            return {
                title: {
                    text: 'Tool Stats',
                },
                xAxis: {
                    title: {
                        text: 'Tools',
                    },
                    categories: this.totalChartLabels,
                },
                yAxis: {
                    title: {
                        text: 'Tool count',
                    },
                },
                tooltip: {
                    formatter: function (): string {
                        return `${this.x}: ${this.y}`;
                    },
                },
                series: [{
                    name: 'Total Total Tool Count',
                    data: this.totalChartData,
                }],
                chart: {
                    type: 'column',
                },
                credits: {
                    enabled: false,
                }
            };
        },
        WeeklyChartOptions(): any {
            return {
                title: {
                    text: 'Weekly Total Tool Stats',
                },
                xAxis: {
                    title: {
                        text: 'Tools',
                    },
                    categories: this.weeklyChartLabels,
                },
                yAxis: {
                    title: {
                        text: 'Tool count',
                    },
                },
                tooltip: {
                    formatter: function (): string {
                        return `${this.x}: ${this.y}`;
                    },
                },
                series: [{
                    name: 'Total Tool Count',
                    data: this.weeklyChartData,
                }],
                chart: {
                    type: 'line',
                },
                credits: {
                    enabled: false,
                }
            };
        },
        MonthlyChartOptions(): any {
            return {
                title: {
                    text: 'Monthly Tool Stats',
                },
                xAxis: {
                    title: {
                        text: 'Tools',
                    },
                    categories: this.monthlyChartLabels,
                },
                yAxis: {
                    title: {
                        text: 'Tool count',
                    },
                },
                tooltip: {
                    formatter: function (): string {
                        return `${this.x}: ${this.y}`;
                    },
                },
                series: [{
                    name: 'Total Tool Count',
                    data: this.monthlyChartData,
                }],
                chart: {
                    type: 'line',
                },
                credits: {
                    enabled: false,
                }
            };
        },
        totalChartLabels(): string[] {
            if (this.statistics.totalToolStats) {
                return this.statistics.totalToolStats.singleToolStats.map(stats => stats.toolName);
            } else {
                return [];
            }
        },
        totalChartData(): number[] {
            if (this.statistics.totalToolStats) {
                return this.statistics.totalToolStats.singleToolStats.map(stats => stats.count);
            } else {
                return [];
            }
        },
        weeklyChartLabels(): string[] {
            if (this.statistics.weeklyToolStats) {
                return this.statistics.weeklyToolStats.map(stats => `${stats.year} - ${stats.week}`);
            } else {
                return [];
            }
        },
        weeklyChartData(): number[] {
            if (this.statistics.weeklyToolStats) {
                return this.statistics.weeklyToolStats.map(stats => stats.toolStats.summary.count)
            } else {
                return [];
            }
        },
        monthlyChartLabels(): string[] {
            if (this.statistics.monthlyToolStats) {
                return this.statistics.monthlyToolStats.map(stats => `${stats.year} - ${stats.month}`);
            } else {
                return [];
            }
        },
        monthlyChartData(): number[] {
            if (this.statistics.monthlyToolStats) {
                return this.statistics.monthlyToolStats.map(stats => stats.toolStats.summary.count)
            } else {
                return [];
            }
        },
    },
    methods: {
        loadStatistics(): void {
            const fromDate = this.fromDate === '' ? '2000-01-01' : this.fromDate;
            const toDate = this.toDate === '' ? moment().format('YYYY-MM-DD') : this.toDate;
            backendService.fetchStatistics(fromDate, toDate).then((statistics) => {
                console.log(statistics);
                this.statistics = statistics;
                this.showCharts = true;

                // sort tools by usage
                this.statistics.totalToolStats.singleToolStats.sort((a, b) => b.count - a.count);
            });
        },
    }
});
</script>

<style scoped>

</style>