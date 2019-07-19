<template>
    <Loading :message="$t('jobs.results.alignment.loadingHits')"
             v-if="loading"/>
    <div v-else>
        <div class="result-options">
            <a @click="toggleAllSelected">
                {{$t('jobs.results.actions.' + (allSelected ? 'deselectAll' : 'selectAll'))}}
            </a>
            <a @click="forwardSelected"
               :disabled="selected.length === 0">{{$t('jobs.results.actions.forwardSelected')}}</a>
            <a @click="downloadAlignment">{{$t('jobs.results.actions.downloadMSA')}}</a>
            <a :href="downloadFilePath" target="_blank">{{$t('jobs.results.actions.exportMSA')}}</a>
        </div>
        <hr class="mt-2">

        <div class="alignment-results mb-4">
            <p v-html="$t('jobs.results.alignment.numSeqs', {num: alignments.length})"></p>
            <div class="table-responsive">
                <table>
                    <tbody>
                    <template v-for="(elem, index) in alignments">
                        <tr :key="'header' + elem.num">
                            <td class="d-flex align-items-center">
                                <b-form-checkbox :checked="selected.includes(elem)"
                                                 @change="selectedChanged(elem)"/>
                                <b v-text="index+1 + '.'"
                                   class="ml-2"></b>
                            </td>
                            <td class="accession">
                                <b v-text="elem.accession"></b>
                            </td>
                        </tr>
                        <tr v-for="(part, partI) in elem.seq.match(/.{1,95}/g)"
                            :key="'sequence' + elem.num + '-' + partI">
                            <td></td>
                            <td v-text="part"
                                class="sequence">
                            </td>
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
    import {Job} from '@/types/toolkit/jobs';
    import {AlignmentItem} from '@/types/toolkit/results';
    import {Tool} from '@/types/toolkit/tools';
    import Loading from '@/components/utils/Loading.vue';
    import {resultsService} from '@/services/ResultsService';
    import Logger from 'js-logger';
    import EventBus from '@/util/EventBus';

    const logger = Logger.get('ClustalAlignmentTab');

    export default Vue.extend({
        name: 'FastaAlignmentTab',
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
                selected: [] as AlignmentItem[],
                loading: false,
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
            downloadFilePath(): string {
                return resultsService.getDownloadFilePath(this.job.jobID, 'alignment.fas');
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
            selectedChanged(al: AlignmentItem): void {
                if (this.selected.includes(al)) {
                    this.selected = this.selected.filter((el: AlignmentItem) => el !== al);
                } else {
                    this.selected.push(al);
                }
            },
            toggleAllSelected(): void {
                if (this.allSelected) {
                    this.selected = [];
                } else {
                    this.selected = this.alignments;
                }
            },
            downloadAlignment(): void {
                const downloadFilename = `${this.tool.name}_alignment_${this.job.jobID}.fasta`;
                resultsService.downloadFile(this.job.jobID, 'alignment.fas', downloadFilename)
                    .catch((e) => {
                        logger.error(e);
                    });
            },
            forwardSelected(): void {
                if (this.selected.length > 0) {
                    if (this.tool.parameters) {
                        EventBus.$emit('show-modal', {
                            id: 'forwardingModal', props: {
                                forwardingData: this.selected.reduce((acc: string, cur: AlignmentItem) =>
                                    acc + '>' + cur.accession + '\n' + cur.seq + '\n', ''),
                                forwardingMode: this.tool.parameters.forwarding,
                            },
                        });
                    } else {
                        logger.error('tool parameters not loaded. Cannot forward');
                    }
                }
            },
        },
    });
</script>

<style lang="scss" scoped>
    .alignment-results {
        font-size: 0.9em;

        td {
            padding-right: 0.5rem;
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
