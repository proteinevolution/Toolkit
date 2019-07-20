<template>
    <Loading :message="$t('jobs.results.alignment.loadingHits')"
             v-if="loading || !alignments"/>
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
            <p v-html="$t('jobs.results.alignment.numSeqs', {num: total})"></p>
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
            <div v-if="alignments.length !== total">
                <Loading :message="$t('jobs.results.alignment.loadingHits')"
                         v-if="loadingMore"
                         justify="center"
                         class="mt-4"/>
                <intersection-observer @intersect="intersected"/>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import mixins from 'vue-typed-mixins';
    import ResultTabMixin from '@/mixins/ResultTabMixin';
    import {AlignmentItem, AlignmentResultResponse} from '@/types/toolkit/results';
    import Loading from '@/components/utils/Loading.vue';
    import {resultsService} from '@/services/ResultsService';
    import Logger from 'js-logger';
    import EventBus from '@/util/EventBus';
    import IntersectionObserver from '@/components/utils/IntersectionObserver.vue';

    const logger = Logger.get('FastaAlignmentTab');

    export default mixins(ResultTabMixin).extend({
        name: 'FastaAlignmentTab',
        components: {
            Loading,
            IntersectionObserver,
        },
        data() {
            return {
                alignments: undefined as AlignmentItem[] | undefined,
                selected: [] as AlignmentItem[],
                loadingMore: false,
                perPage: 20,
                total: 0,
            };
        },
        computed: {
            allSelected(): boolean {
                if (!this.alignments) {
                    return false;
                }
                return this.alignments.length > 0 &&
                    this.selected.length === this.alignments.length;
            },
            downloadFilePath(): string {
                return resultsService.getDownloadFilePath(this.job.jobID, 'alignment.fas');
            },
        },
        methods: {
            async init() {
                await this.loadHits(0, this.perPage);
            },
            async intersected() {
                if (!this.loadingMore && this.alignments && this.alignments.length < this.total) {
                    this.loadingMore = true;
                    try {
                        await this.loadHits(this.alignments.length, this.alignments.length + this.perPage);
                    } catch (e) {
                        logger.error(e);
                    }
                    this.loadingMore = false;
                }
            },
            async loadHits(start: number, end: number) {
                const res: AlignmentResultResponse = await resultsService.fetchAlignmentResults(this.job.jobID, start, end);
                this.total = res.total;
                if (!this.alignments) {
                    this.alignments = res.alignments;
                } else {
                    this.alignments.push(...res.alignments);
                }
            },
            selectedChanged(al: AlignmentItem): void {
                if (this.selected.includes(al)) {
                    this.selected = this.selected.filter((el: AlignmentItem) => el !== al);
                } else {
                    this.selected.push(al);
                }
            },
            toggleAllSelected(): void {
                if (!this.alignments) {
                    return;
                }
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
