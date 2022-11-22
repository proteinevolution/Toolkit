<template>
    <Loading v-if="loading || !results" :message="t('loading')" />
    <div v-else class="font-small">
        <b v-if="results.results.hits.length === 0" v-text="t('jobs.results.tprpred.noResults')"></b>
        <div v-else>
            <br />
            <div v-for="(hit, hidx) in results.results.desc" :key="hidx" class="tpr-info">
                {{ hit[0] }}: <b>{{ hit[1] }}</b>
            </div>

            <table class="alignment-table mt-4">
                <thead>
                    <tr>
                        <th>Repeat</th>
                        <th>Begin</th>
                        <th>Alignment</th>
                        <th>End</th>
                        <th>P-value</th>
                    </tr>
                </thead>
                <tbody>
                    <template v-for="(hit, hidx) in results.results.hits" :key="'rows' + hidx">
                        <tr class="sequence-alignment">
                            <td v-text="hit[1]"></td>
                            <td v-text="hit[2]"></td>
                            <td class="tpr-hit" v-html="colorSequence(hit[0])"></td>
                            <td v-text="hit[3]"></td>
                            <td v-text="hit[4]"></td>
                        </tr>
                    </template>
                </tbody>
            </table>
            <br />
        </div>
    </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import useResultTab, { defineResultTabProps } from '@/composables/useResultTab';
import Loading from '@/components/utils/Loading.vue';
import { resultsService } from '@/services/ResultsService';
import Logger from 'js-logger';
import { colorSequence } from '@/util/SequenceUtils';
import { useI18n } from 'vue-i18n';
import { TprpredResults } from '@/types/toolkit/results';
import { timeout } from '@/util/Utils';

const logger = Logger.get('TprpredResultsTab');

const { t } = useI18n();

const props = defineResultTabProps();

const results = ref<TprpredResults | undefined>(undefined);
const maxTries = 50;
const tries = ref(0);

async function init() {
    try {
        results.value = await resultsService.fetchResults(props.job.jobID);
    } catch (e) {
        ++tries.value;
        if (tries.value === maxTries) {
            logger.info("Couldn't fetch files.");
            return;
        }
        await timeout(300);
        await init();
    }
}

const { loading } = useResultTab({ init, resultTabName: props.resultTabName, renderOnCreate: props.renderOnCreate });
</script>

<style lang="scss" scoped>
.alignment-table {
    width: 100%;
    @include media-breakpoint-up(xl) {
        width: 80%;
    }
    font-size: 0.9em;

    .sequence-alignment {
        font-family: $font-family-monospace;
        letter-spacing: 0.05em;

        .tpr-hit {
            width: 27em;
        }
    }
}
</style>
