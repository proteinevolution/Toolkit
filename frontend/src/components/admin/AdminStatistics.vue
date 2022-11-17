<template>
    <div>
        <div class="row g-3">
            <div class="col-md-6 col-sm-8">
                <label>From</label>
                <b-form-datepicker id="from-datepicker" v-model="fromDate" view-mode="year" class="mb-2" />
            </div>
            <div class="col-md-6 col-sm-8">
                <label>To</label>
                <b-form-datepicker id="to-datepicker" v-model="toDate" view-mode="year" class="mb-2" />
            </div>
            <div class="col-md-6 col-sm-8">
                <div class="input-group-append">
                    <b-button variant="primary" class="mr-2" @click="loadStatistics"> Load Statistics </b-button>
                </div>
            </div>
        </div>

        <div v-if="showCharts">
            <highcharts :options="BarChartOptions" class="high-chart" />
            <highcharts :options="WeeklyChartOptions" class="high-chart" />
            <highcharts :options="MonthlyChartOptions" class="high-chart" />
        </div>
    </div>
</template>

<script lang="ts">
import { defineComponent } from 'vue';
import { Chart } from 'highcharts-vue';
import { DateTime } from 'luxon';
import { SingleToolStats, Statistics } from '@/types/toolkit/admin';
import { backendService } from '@/services/BackendService';
import { Options, SeriesBarOptions } from 'highcharts';

export default defineComponent({
    name: 'AdminStatistics',
    components: {
        highcharts: Chart,
    },
    data() {
        return {
            showCharts: false,
            fromDate: DateTime.now().minus({ years: 1 }).toISODate(),
            toDate: DateTime.now().toISODate(),
            statistics: {} as Statistics,
        };
    },
    computed: {
        BarChartOptions(): Options {
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
                series: [
                    {
                        name: 'Total Tool Count',
                        data: this.totalChartData('count'),
                    },
                    {
                        name: 'Internal Tool Count',
                        data: this.totalChartData('internalCount'),
                    },
                ] as SeriesBarOptions[],
                chart: {
                    type: 'column',
                },
                credits: {
                    enabled: false,
                },
            };
        },
        WeeklyChartOptions(): Options {
            return {
                title: {
                    text: 'Weekly Tool Stats',
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
                series: [
                    {
                        name: 'Total Tool Count',
                        data: this.weeklyChartData('count'),
                    },
                    {
                        name: 'Internal Tool Count',
                        data: this.weeklyChartData('internalCount'),
                    },
                ] as SeriesBarOptions[],
                chart: {
                    type: 'line',
                },
                credits: {
                    enabled: false,
                },
            };
        },
        MonthlyChartOptions(): Options {
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
                series: [
                    {
                        name: 'Total Tool Count',
                        data: this.monthlyChartData('count'),
                    },
                    {
                        name: 'Internal Tool Count',
                        data: this.monthlyChartData('internalCount'),
                    },
                ] as SeriesBarOptions[],
                chart: {
                    type: 'line',
                },
                credits: {
                    enabled: false,
                },
            };
        },
        totalChartLabels(): string[] {
            if (this.statistics.totalToolStats) {
                return this.statistics.totalToolStats.singleToolStats.map((stats) => stats.toolName);
            } else {
                return [];
            }
        },
        weeklyChartLabels(): string[] {
            if (this.statistics.weeklyToolStats) {
                return this.statistics.weeklyToolStats.map((stats) => `${stats.year} - ${stats.week}`);
            } else {
                return [];
            }
        },
        monthlyChartLabels(): string[] {
            if (this.statistics.monthlyToolStats) {
                return this.statistics.monthlyToolStats.map((stats) => `${stats.year} - ${stats.month}`);
            } else {
                return [];
            }
        },
    },
    methods: {
        loadStatistics(): void {
            backendService.fetchStatistics(this.fromDate, this.toDate).then((statistics) => {
                console.log(statistics);
                this.statistics = statistics;
                this.showCharts = true;

                // sort tools by usage
                this.statistics.totalToolStats.singleToolStats.sort((a, b) => b.count - a.count);
            });
        },
        totalChartData(dataType: keyof SingleToolStats): number[] {
            if (this.statistics.totalToolStats) {
                return this.statistics.totalToolStats.singleToolStats.map((stats) => stats[dataType] as number);
            } else {
                return [];
            }
        },
        weeklyChartData(dataType: keyof SingleToolStats): number[] {
            if (this.statistics.weeklyToolStats) {
                return this.statistics.weeklyToolStats.map((stats) => stats.toolStats.summary[dataType] as number);
            } else {
                return [];
            }
        },
        monthlyChartData(dataType: keyof SingleToolStats): number[] {
            if (this.statistics.monthlyToolStats) {
                return this.statistics.monthlyToolStats.map((stats) => stats.toolStats.summary[dataType] as number);
            } else {
                return [];
            }
        },
    },
});
</script>
