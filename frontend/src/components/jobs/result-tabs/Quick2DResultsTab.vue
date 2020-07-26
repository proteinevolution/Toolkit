<template>
    <Loading v-if="loading"
             :message="$t('loading')" />
    <div v-else>
        <h5>Protein ID: {{ header }}</h5>

        <div v-if="results && results.results.signal === 'yes'">
            <br>
            <span class="note"><b> We have detected a potential signal peptide in your query protein!</b></span>
        </div>

        <br>
        <br>

        <div class="table-responsive">
            <table class="alignment-table">
                <tbody>
                    <template v-for="i in brokenQuery.length">
                        <tr>
                            <td>AA_QUERY</td>
                            <td v-text="(i - 1) * breakAfter + 1"></td>
                            <td>
                                <span class="sequence"
                                      v-text="brokenQuery[i - 1]"></span>
                                <span v-text="'   ' + min(i * breakAfter, results.query.sequence.length)"></span>
                            </td>
                        </tr>
                        <tr v-for="(value, key) in subTools"
                            v-if="brokenResults[key]">
                            <td v-text="value"></td>
                            <td></td>
                            <td v-html="brokenResults[key][i - 1]"></td>
                            <td></td>
                        </tr>
                        <tr class="empty-row">
                            <td colspan="4"></td>
                        </tr>
                    </template>
                </tbody>
            </table>
        </div>

        <hr class="mt-0">
        <br>

        <div class="text-center mb-5">
            SS = <span class="ss_h_b">&nbsp;&alpha;-helix&nbsp;</span><span
                class="ss_e_b">&nbsp;&beta;-strand&nbsp;</span><span
                class="ss_pihelix">&nbsp;&pi;-helix&nbsp;</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;CC
            = <span class="CC_b">Coiled Coils</span>
            &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;TM = <span class="CC_m">Transmembrane</span>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;DO
            = <span class="CC_do">Disorder</span>
        </div>
    </div>
</template>

<script lang="ts">
    import mixins from 'vue-typed-mixins';
    import ResultTabMixin from '@/mixins/ResultTabMixin';
    import Loading from '@/components/utils/Loading.vue';
    import Logger from 'js-logger';
    import {resultsService} from '@/services/ResultsService';
    import {Quick2dResults} from '@/types/toolkit/results';
    import {quick2dColor} from '@/util/SequenceUtils';

    const logger = Logger.get('Quick2DResultsTab');

    export default mixins(ResultTabMixin).extend({
        name: 'Quick2DResultsTab',
        components: {
            Loading,
        },
        data() {
            return {
                results: undefined as Quick2dResults | undefined,
                breakAfter: 85,
                subTools: {
                    'psipred': 'SS_PSIPRED',
                    'spider': 'SS_SPIDER3',
                    'psspred': 'SS_PSSPRED4',
                    'deepcnf': 'SS_DEEPCNF',
                    'netsurfpss': 'SS_NETSURFP2',
                    'pipred': 'SS_PIPRED',
                    'marcoil': 'CC_MARCOIL',
                    'coils_w28': 'CC_COILS_W28',
                    'pcoils_w28': 'CC_PCOILS_W28',
                    'tmhmm': 'TM_TMHMM',
                    'phobius': 'TM_PHOBIUS',
                    'polyphobius': 'TM_POLYPHOBIUS',
                    'netsurfpd': 'DO_NETSURFPD2',
                    'disopred': 'DO_DISOPRED',
                    'spot-d': 'DO_SPOTD',
                    'iupred': 'DO_IUPRED',
                },
            };
        },
        computed: {
            header(): string {
                if (!this.results) {
                    return '';
                }
                return this.results.query.header.slice(1, 50);
            },
            brokenQuery(): string[] {
                if (!this.results) {
                    return [];
                }
                const res: string[] = [];
                let breakIt = 0;
                const value: string = this.results.query.sequence;
                while (breakIt * this.breakAfter < value.length) {
                    res.push(value.slice(breakIt * this.breakAfter, (breakIt + 1) * this.breakAfter));
                    breakIt++;
                }
                return res;
            },
            brokenResults(): { [key: string]: string[] } {
                if (!this.results) {
                    return {};
                }
                // alignments need to be broken into pieces
                const res: { [key: string]: string[] } = {};
                for (const key in this.subTools) {
                    if (this.results.results.hasOwnProperty(key)
                        && this.results.results[key].length > 0) {
                        res[key] = [];
                        let breakIt = 0;
                        const value: string = this.results.results[key];
                        while (breakIt < value.length) {
                            const cut: string = value.slice(breakIt, breakIt + this.breakAfter);
                            const colored: string = quick2dColor(key, cut);
                            res[key].push(colored);
                            breakIt += this.breakAfter;
                        }
                    }
                }
                return res;
            },
        },
        methods: {
            async init() {
                this.results = await resultsService.fetchResults(this.job.jobID);
            },
            min(a: number, b: number): number {
                return Math.min(a, b);
            },
        },
    });
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
