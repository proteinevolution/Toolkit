<template>
    <div>
        <br />
        Download the zipped CLANS file (<a :href="fileUrl" download
            ><b>{{ jobID + '.clans.zip' }}</b></a
        >) generated for your sequences and unzip it. <br /><br />
        Download the CLANS application here:
        <a href="/clans/clans.jar" download><b>clans.jar</b></a>
        and visualize your unzipped CLANS file (<b>{{ jobID + '.clans' }}</b
        >) using the 'Load Run' option in the 'File' menu. Please refer to the
        <a href="/clans/clans_userguide.pdf" target="”_blank”"><b>CLANS user guide</b></a>
        for detailed instructions.

        <br /><br />Please make sure you have Java Runtime Environment (JRE) installed. <br /><br />

        CLANS can be launched from the command line as follows:<br />
        <em>java -Xmx4G -jar clans.jar -load {{ jobID + '.clans' }}</em
        ><br /><br />

        If you encounter problems loading the CLANS file, please increase the maximum Java heap size (-Xmx).
        <br /><br />
    </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import useResultTab from '@/composables/useResultTab';
import { resultsService } from '@/services/ResultsService';
import { Job, JobViewOptions } from '@/types/toolkit/jobs';
import { Tool } from '@/types/toolkit/tools';

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

useResultTab({ resultTabName: props.resultTabName, renderOnCreate: props.renderOnCreate });

const jobID = computed(() => props.job.jobID);
const fileUrl = computed(() => resultsService.getDownloadFilePath(jobID.value, jobID.value + '.clans.zip'));
</script>
