<template>
    <div class="text-center">
        <img :src="img"
             v-for="(img, index) in images"
             :key="'img' + index"
             class="plot-img"
             alt=""/>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {Tool} from '@/types/toolkit/tools';
    import {Job} from '@/types/toolkit/jobs';
    import {resultsService} from '@/services/ResultsService';

    export default Vue.extend({
        name: 'SamCCViewTab',
        props: {
            job: {
                type: Object as () => Job,
                required: true,
            },
            tool: {
                type: Object as () => Tool,
                required: true,
            },
        },
        computed: {
            images(): string[] {
                const jobID: string = this.job.jobID;
                return [
                    resultsService.getDownloadFilePath(jobID, 'out0.png'),
                    resultsService.getDownloadFilePath(jobID, 'out1.png'),
                    resultsService.getDownloadFilePath(jobID, 'out2.png'),
                    resultsService.getDownloadFilePath(jobID, 'out3.png'),
                ];
            },
        },
    });
</script>

<style lang="scss" scoped>
    .plot-img {
        margin-bottom: 2rem;
    }
</style>
