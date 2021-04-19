<template>
    <div>
        <h3 class="mb-3 h5"
            v-text="$t('jobs.stateMessages.error')"></h3>
        <p class="mb-3"
           v-text="$t('jobs.jobIDDetails', job)"></p>
        <table v-for="logElem in runningLog"
               :key="logElem.text"
               class="job-log-element mb-2">
            <tr>
                <td>
                    <i :class="logElem.class"
                       class="fas text-center mr-1"></i>
                </td>
                <td>{{ logElem.text }}</td>
            </tr>
        </table>
    </div>
</template>

<script lang="ts">
import Vue from 'vue';
import {resultsService} from '@/services/ResultsService';
import {parseProcessLog} from '@/util/Utils';
import {ProcessLogItem} from '@/types/toolkit/jobs';

export default Vue.extend({
    name: 'JobErrorTab',
    props: {
        job: {
            type: Object,
            required: true,
        },
        tool: {
            type: Object,
            required: true,
        },
    },
    data() {
        return {
            runningLog: [] as ProcessLogItem[],
        };
    },
    async created() {
        const file: string = await resultsService.getFile(this.job.jobID, 'process.log');
        this.runningLog = parseProcessLog(file);
    },
});
</script>

<style lang="scss" scoped>
.job-log-element {
  display: flex;
  align-items: center;

  i {
    width: 32px;
    height: 18px;
    margin-top: 0.2em;
    font-size: 1.3em;
  }

  .running {
    background: url('../../../assets/images/ellipsis.gif') no-repeat center center;
  }

  .done {
    color: rgba(40, 120, 111, 0.636);

    &:before {
      content: '\f058';
    }
  }

  .error {
    color: #da4453;

    &:before {
      content: '\f057';
    }
  }
}
</style>
