<template>
    <Loading :message="$t('jobs.results.alignment.loadingHits')"
             v-if="loading"/>
    <div v-else>
        <div class="result-options">
            <a @click="toggleAllSelected">{{$t('jobs.results.actions.' + (allSelected ? 'deselectAll' :
                'selectAll'))}}</a>
            <a @click="forwardSelected">{{$t('jobs.results.actions.forwardSelected')}}</a>
            <a @click="downloadAlignment">{{$t('jobs.results.actions.downloadMSA')}}</a>
            <a :href="downloadFilePath" target="_blank">{{$t('jobs.results.actions.exportMSA')}}</a>
            <a @click="toggleColor">{{$t('jobs.results.actions.colorMSA')}}</a>
        </div>
        <hr class="mt-2">

        <div class="alignment-results mb-4">
            <p v-html="$t('jobs.results.alignment.numSeqs', {num: alignments.length})"></p>
            <div class="table-responsive">
                <table>
                    <tbody>
                    <template v-for="(group, groupI) in brokenAlignments">
                        <tr v-for="elem in group"
                            :key="groupI + '-' + elem.num">
                            <td>
                                <b-form-checkbox :checked="selected.includes(elem.num)"
                                                 @change="selectedChanged(elem.num)"/>
                            </td>
                            <td class="accession">
                                <b v-text="elem.accession.slice(0, 20)"></b>
                            </td>
                            <td v-html="coloredSeq(elem.seq)"
                                class="sequence">
                            </td>
                        </tr>
                        <tr class="blank-row"
                            v-if="groupI < brokenAlignments.length - 1">
                            <td colspan="3"></td>
                        </tr>
                        <tr class="blank-row"
                            v-if="groupI < brokenAlignments.length - 1">
                            <td colspan="3"></td>
                        </tr>
                    </template>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {AlignmentItem} from '@/types/toolkit/results';
    import {Job} from '@/types/toolkit/jobs';
    import {Tool} from '@/types/toolkit/tools';
    import Loading from '@/components/utils/Loading.vue';
    import {resultsService} from '@/services/ResultsService';
    import Logger from 'js-logger';
    import {colorSequence} from '@/util/SequenceUtils';

    const logger = Logger.get('ClustalAlignmentTab');

    export default Vue.extend({
        name: 'ClustalAlignmentTab',
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
        data() {
            return {
                selected: [] as number[],
                breakAfter: 85, // clustal format breaks after n chars
                loading: false,
                color: false,
            };
        },
        computed: {
            alignments(): AlignmentItem[] {
                return this.job.alignments || [];
            },
            allSelected(): boolean {
                return this.alignments.length > 0 &&
                    this.selected.length === this.alignments.length;
            },
            brokenAlignments(): AlignmentItem[][] {
                // alignments need to be broken into pieces
                const res: AlignmentItem[][] = [];
                for (const a of this.alignments) {
                    let breakIt = 0;
                    while (breakIt * this.breakAfter < a.seq.length) {
                        if (!res[breakIt]) {
                            res[breakIt] = [];
                        }
                        res[breakIt].push(Object.assign({}, a, {
                            seq: a.seq.slice(breakIt * this.breakAfter, (breakIt + 1) * this.breakAfter),
                        }));
                        breakIt++;
                    }
                }
                return res;
            },
            downloadFilePath(): string {
                return resultsService.getDownloadFilePath(this.job.jobID, 'alignment.clustalw_aln');
            },
        },
        mounted() {
            if (!this.job.alignments) {
                this.loading = true;
                this.$store.dispatch('jobs/loadJobAlignments', this.job.jobID)
                    .catch((e: any) => {
                        logger.error(e);
                    })
                    .finally(() => {
                        this.loading = false;
                    });
            }
        },
        methods: {
            toggleColor(): void {
                this.color = !this.color;
            },
            selectedChanged(num: number): void {
                if (this.selected.includes(num)) {
                    this.selected = this.selected.filter((el: number) => el !== num);
                } else {
                    this.selected.push(num);
                }
            },
            toggleAllSelected(): void {
                if (this.allSelected) {
                    this.selected = [];
                } else {
                    this.selected = this.alignments.map((a: AlignmentItem) => a.num);
                }
            },
            coloredSeq(seq: string): string {
                if (this.color) {
                    return colorSequence(seq);
                } else {
                    return seq;
                }
            },
            downloadAlignment(): void {
                const downloadFilename = `${this.tool.name}_alignment_${this.job.jobID}.clustal`;
                resultsService.downloadFile(this.job.jobID, 'alignment.clustalw_aln', downloadFilename)
                    .catch((e) => {
                        logger.error(e);
                    });
            },
            forwardSelected(): void {
                alert('implement me!');
            },
        },
    });
</script>

<style lang="scss" scoped>
    .alignment-results {
        font-size: 0.9em;

        td {
            padding-right: 2rem;
        }

        .blank-row td {
            height: 1em;
        }

        .accession {
            font-size: 0.9em;
        }

        .sequence {
            font-family: $font-family-monospace;
            letter-spacing: 0.025em;
            font-size: 0.75rem;
            white-space: pre;
        }
    }
</style>
