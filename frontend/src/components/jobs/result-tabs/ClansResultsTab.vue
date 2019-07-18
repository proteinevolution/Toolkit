<template>
    <div>
        <br/>
        Download the zipped CLANS file (<a :href="fileUrl" download>{{job.jobID + '.clans.zip'}}</a>) generated for your
        sequences and unzip it.
        <br/><br/>
        Download the CLANS application here: <a
            href="ftp://ftp.tuebingen.mpg.de/pub/protevo/CLANS/clans.jar">clans.jar</a>
        and visualize your unzipped CLANS file (<b>{{job.jobID + '.clans'}}</b>) using the 'Load Run' option in the
        'File'
        menu.
        <br/><br/>Please make sure you have Java Runtime Environment (JRE) installed. <br/><br/>
        If your input contains large number of sequences, please consider running CLANS on the command line:<br/>
        <em>java -Xmx4G -jar clans.jar -load {{job.jobID + '.clans'}}</em><br/><br/>

        If you face problems loading the CLANS file, please increase the maximum Java heap size (-Xmx).
        <br/><br/>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import Loading from '@/components/utils/Loading.vue';
    import {Tool} from '@/types/toolkit/tools';
    import {Job} from '@/types/toolkit/jobs';
    import {resultsService} from '@/services/ResultsService';

    export default Vue.extend({
        name: 'ClansResultsTab',
        components: {
            Loading,
        },
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
            fileUrl() {
                return resultsService.getDownloadFilePath(this.job.jobID, this.job.jobID + '.clans.zip');
            },
        },
    });
</script>

<style lang="scss" scoped>

</style>
