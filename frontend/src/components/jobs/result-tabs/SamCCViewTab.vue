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
    import mixins from 'vue-typed-mixins';
    import ResultTabMixin from '@/mixins/ResultTabMixin';
    import {resultsService} from '@/services/ResultsService';

    export default mixins(ResultTabMixin).extend({
        name: 'SamCCViewTab',
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
