<template>
    <Loading v-if="loading" :message="t('loading')" />
    <div v-else>
        <h5>Protein ID: {{ header }}</h5>

        <div v-if="results && results.results.signal === 'yes'">
            <br />
            <span class="note"><b> We have detected a potential signal peptide in your query protein!</b></span>
        </div>

        <br />
        <br />

        <div class="table-responsive">
            <table class="alignment-table">
                <tbody>
                    <template v-for="i in brokenQuery.length" :key="'rows-' + i">
                        <tr :key="'1-' + i">
                            <td>AA_QUERY</td>
                            <td v-text="(i - 1) * breakAfter + 1"></td>
                            <td>
                                <span class="sequence" v-text="brokenQuery[i - 1]"></span>
                                <span v-text="'   ' + min(i * breakAfter, results.query.sequence.length)"></span>
                            </td>
                        </tr>
                        <tr v-for="(value, key) in filteredSubTools" :key="'2-' + i + '-' + key">
                            <td v-text="value"></td>
                            <td></td>
                            <td v-html="brokenResults[key][i - 1]"></td>
                            <td></td>
                        </tr>
                        <tr :key="'3-' + i" class="empty-row">
                            <td colspan="4"></td>
                        </tr>
                    </template>
                </tbody>
            </table>
        </div>

        <hr class="mt-0" />
        <br />

        <div class="text-center mb-5">
            SS = <span class="ss_h_b">&nbsp;&alpha;-helix&nbsp;</span
            ><span class="ss_e_b">&nbsp;&beta;-strand&nbsp;</span
            ><span class="ss_pihelix">&nbsp;&pi;-helix&nbsp;</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;CC =
            <span class="CC_b">Coiled Coils</span> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;TM =
            <span class="CC_m">Transmembrane</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;DO =
            <span class="CC_do">Disorder</span>
        </div>
    </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue';
import useResultTab, { defineResultTabProps } from '@/composables/useResultTab';
import Loading from '@/components/utils/Loading.vue';
import { resultsService } from '@/services/ResultsService';
import { useI18n } from 'vue-i18n';
import { Quick2dResults } from '@/types/toolkit/results';
import { quick2dColor } from '@/util/SequenceUtils';
import { isNullable } from '@/util/nullability-helpers';

const { t } = useI18n();

const props = defineResultTabProps();

const results = ref<Quick2dResults | undefined>(undefined);

const header = computed(() => {
    if (isNullable(results.value)) {
        return '';
    }
    return results.value.query.header.slice(1, 50);
});

async function init() {
    results.value = await resultsService.fetchResults(props.job.jobID);
}

const { loading } = useResultTab({ init, resultTabName: props.resultTabName, renderOnCreate: props.renderOnCreate });

function min(a: number, b: number): number {
    return Math.min(a, b);
}

const breakAfter = 85;

const brokenQuery = computed<string[]>(() => {
    if (isNullable(results.value)) {
        return [];
    }
    const res: string[] = [];
    let breakIt = 0;
    const value: string = results.value.query.sequence;
    while (breakIt * breakAfter < value.length) {
        res.push(value.slice(breakIt * breakAfter, (breakIt + 1) * breakAfter));
        breakIt++;
    }
    return res;
});

const subTools = {
    psipred: 'SS_PSIPRED',
    spider: 'SS_SPIDER3',
    psspred: 'SS_PSSPRED4',
    deepcnf: 'SS_DEEPCNF',
    netsurfpss: 'SS_NETSURFP2',
    pipred: 'SS_PIPRED',
    marcoil: 'CC_MARCOIL',
    coils_w28: 'CC_COILS_W28',
    pcoils_w28: 'CC_PCOILS_W28',
    tmhmm: 'TM_TMHMM',
    phobius: 'TM_PHOBIUS',
    polyphobius: 'TM_POLYPHOBIUS',
    netsurfpd: 'DO_NETSURFPD2',
    disopred: 'DO_DISOPRED',
    'spot-d': 'DO_SPOTD',
    iupred: 'DO_IUPRED',
};

const brokenResults = computed<{ [key: string]: string[] }>(() => {
    if (isNullable(results.value)) {
        return {};
    }
    // alignments need to be broken into pieces
    const res: { [key: string]: string[] } = {};
    for (const key in subTools) {
        if (key in results.value.results && results.value.results[key].length > 0) {
            res[key] = [];
            let breakIt = 0;
            const value: string = results.value.results[key];
            while (breakIt < value.length) {
                const cut: string = value.slice(breakIt, breakIt + breakAfter);
                const colored: string = quick2dColor(key, cut);
                res[key].push(colored);
                breakIt += breakAfter;
            }
        }
    }
    return res;
});

const filteredSubTools = computed<Record<string, string>>(() =>
    Object.fromEntries(Object.entries(subTools).filter(([k]) => k in brokenResults.value))
);
</script>

<style lang="scss" scoped>
.alignment-table {
    font-family: $font-family-monospace;
    font-size: 0.85em;
    white-space: pre;

    td {
        padding: 0 1.5rem 0 0;
        border-spacing: 0;
        line-height: 1.3;
    }

    .sequence {
        font-weight: 600;
        border-bottom: 0.2em solid rgba(128, 128, 128, 0.37);
    }

    .empty-row td {
        height: 4em;
    }
}
</style>
