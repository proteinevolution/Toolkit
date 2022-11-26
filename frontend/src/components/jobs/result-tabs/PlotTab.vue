<template>
    <Loading v-if="loading" :message="t('loading')" />
    <div v-else class="font-small">
        <b v-if="results.vals.length === 0" v-html="t('jobs.results.plot.noResults')"></b>
        <div v-else class="high-chart-container">
            <div v-html="t('jobs.results.plot.numHits', { num: results.vals.length })"></div>
            <br /><br />
            <chart :options="chartOptions" class="high-chart" />
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import useResultTab from '@/composables/useResultTab';
import Loading from '@/components/utils/Loading.vue';
import { resultsService } from '@/services/ResultsService';
import { ProbEvalList } from '@/types/toolkit/results';
import { useI18n } from 'vue-i18n';
import { Chart } from 'highcharts-vue';
import { isNonNullable } from '@/util/nullability-helpers';
import { Job, JobViewOptions } from '@/types/toolkit/jobs';
import { Tool } from '@/types/toolkit/tools';

const { t } = useI18n();

interface ResultTabProps {
    job: Job;
    tool: Tool;
    fullScreen?: boolean;
    viewOptions?: JobViewOptions;
    resultTabName?: string;
    renderOnCreate?: boolean;
}

const props = withDefaults(defineProps<ResultTabProps>(), {
    resultTabName: '',
    renderOnCreate: true,
});

const results = ref<ProbEvalList | undefined>(undefined);

async function init() {
    results.value = (await resultsService.getFile(props.job.jobID, 'plot_data.json')) as ProbEvalList;
}

const { loading } = useResultTab({ init, resultTabName: props.resultTabName, renderOnCreate: props.renderOnCreate });

const probabilities = computed<number[][] | undefined>(() => {
    if (isNonNullable(results.value)) {
        const bins: number[][] = [];
        for (let i = 0; i < results.value.vals.length; i++) {
            bins.push([results.value.vals[i], i]);
        }
        return bins;
    }
    return undefined;
});

const type = computed<string>(() => results.value?.type ?? '');

const chartOptions = computed(() => ({
    title: {
        text: t(`jobs.results.plot.${type.value}.title`),
        x: -20, // center
    },
    xAxis: {
        title: {
            text: t(`jobs.results.plot.${type.value}.xLabel`),
        },
    },
    yAxis: {
        title: {
            text: t(`jobs.results.plot.${type.value}.yLabel`),
        },
        plotLines: [
            {
                value: 0,
                width: 1,
                color: '#808080',
            },
        ],
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
    series: [
        {
            name: t(`jobs.results.plot.${type.value}.legend`),
            data: probabilities.value,
        },
    ],
    credits: {
        enabled: false,
    },
}));
</script>

<style lang="scss" scoped>
.high-chart-container {
    padding-left: 50px;
    @include media-breakpoint-down(md) {
        padding-left: 0;
    }

    .high-chart {
        overflow-x: auto !important;
    }
}
</style>
