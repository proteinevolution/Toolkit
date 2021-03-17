<template>
    <div>
        <br>
        Download the zipped CLANS file (<a :href="fileUrl"
                                           download><b>{{ job.jobID + '.clans.zip' }}</b></a>) generated
        for your
        sequences and unzip it.
        <br><br>
        Download the CLANS application here: <a
            href="ftp://ftp.tuebingen.mpg.de/pub/protevo/CLANS/clans.jar"><b>clans.jar</b></a>
        and visualize your unzipped CLANS file (<b>{{ job.jobID + '.clans' }}</b>) using the 'Load Run' option in the
        'File' menu. Please refer to the
        <a href="/clans/clans_userguide.pdf"
           target="”_blank”"><b>CLANS user guide</b></a>
        for detailed instructions.

        <br><br>Please make sure you have Java Runtime Environment (JRE) installed. <br><br>

        CLANS can be launched from the command line as follows:<br>
        <em>java -Xmx4G -jar clans.jar -load {{ job.jobID + '.clans' }}</em><br><br>

        If you encounter problems loading the CLANS file, please increase the maximum Java heap size (-Xmx).
        <br><br>
    </div>
</template>

<script lang="ts">
import ResultTabMixin from '@/mixins/ResultTabMixin';
import {resultsService} from '@/services/ResultsService';

export default ResultTabMixin.extend({
  name: 'ClansResultsTab',
  computed: {
    fileUrl() {
      return resultsService.getDownloadFilePath(this.job.jobID, this.job.jobID + '.clans.zip');
    },
  },
});
</script>
