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
            <a v-if="!isReduced" @click="download(downloadFilenameMSA, downloadFileMSA)">
                {{$t('jobs.results.actions.downloadMSA')}}
            </a>
            <a v-if="isReduced" @click="download(downloadFilenameReducedA3M, downloadFileReducedA3M)">
                {{$t('jobs.results.actions.downloadReducedA3M')}}
            </a>
            <a v-if="isReduced" @click="download(downloadFilenameFullA3M, downloadFileFullA3M)">
                {{$t('jobs.results.actions.downloadFullA3M')}}
            </a>
            <a v-if="!isReduced" :href="downloadMSAFilePath"
               target="_blank">{{$t('jobs.results.actions.exportMSA')}}</a>
        </div>

        <div class="alignment-results mb-4">
            <p v-html="$t(alignmentNumTextKey, {num: total, reduced: viewOptions.reduced})"></p>
            <div class="table-responsive">
                <table>
                    <tbody>
                    <template v-for="(elem, index) in alignments">
                        <tr :key="'header' + elem.num">
                            <td class="d-flex align-items-center">
                                <b-form-checkbox :checked="selected.includes(elem.num)"
                                                 @change="selectedChanged(elem.num)"/>
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
                selected: [] as number[],
                loadingMore: false,
                perPage: 20,
                total: 0,
                downloadFileMSA: 'alignment.fas',
            };
        },
        computed: {
            resultField(): string {
                return this.viewOptions.resultField ? this.viewOptions.resultField : 'alignment';
            },
            allSelected(): boolean {
                if (!this.alignments) {
                    return false;
                }
                return this.alignments.length > 0 &&
                    this.selected.length === this.alignments.length;
            },
            downloadMSAFilePath(): string {
                return resultsService.getDownloadFilePath(this.job.jobID, this.downloadFileMSA);
            },
            downloadFilenameMSA(): string {
                return `${this.tool.name}_${this.resultField}_${this.job.jobID}.fasta`;
            },
            downloadFileReducedA3M(): string {
                return this.viewOptions.reducedFilename + '.a3m' || '';
            },
            downloadFilenameReducedA3M(): string {
                return `${this.tool.name}_${this.viewOptions.reducedFilename || ''}_${this.job.jobID}.a3m`;
            },
            downloadFileFullA3M(): string {
                return this.viewOptions.fullFilename + '.a3m' || '';
            },
            downloadFilenameFullA3M(): string {
                return `${this.tool.name}_${this.viewOptions.fullFilename || ''}_${this.job.jobID}.a3m`;
            },
            isReduced(): boolean {
                return Boolean(this.viewOptions.reduced);
            },
            alignmentNumTextKey(): string {
                return `jobs.results.alignment.numSeqs${this.isReduced ? 'Reduced' : ''}`;
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
                const res: AlignmentResultResponse = await resultsService.fetchAlignmentResults(this.job.jobID, start,
                    end, this.resultField);
                this.total = res.total;
                if (this.allSelected) {
                    res.alignments.forEach((a: AlignmentItem) => this.selected.push(a.num));
                }
                if (!this.alignments) {
                    this.alignments = res.alignments;
                } else {
                    this.alignments.push(...res.alignments);
                }
            },
            selectedChanged(num: number): void {
                if (this.selected.includes(num)) {
                    this.selected = this.selected.filter((n: number) => num !== n);
                } else {
                    this.selected.push(num);
                }
            },
            toggleAllSelected(): void {
                if (!this.alignments) {
                    return;
                }
                if (this.allSelected) {
                    this.selected = [];
                } else {
                    this.selected = this.alignments.map((al: AlignmentItem) => al.num);
                }
            },
            download(downloadFilename: string, file: string): void {
                resultsService.downloadFile(this.job.jobID, file, downloadFilename)
                    .catch((e) => {
                        logger.error(e);
                    });
            },
            forwardSelected(): void {
                if (this.selected.length > 0) {
                    if (this.tool.parameters && this.alignments) {
                        const selAl: AlignmentItem[] = this.alignments
                            .filter((al: AlignmentItem) => this.selected.includes(al.num));
                        EventBus.$emit('show-modal', {
                            id: 'forwardingModal', props: {
                                forwardingData: selAl.reduce((acc: string, cur: AlignmentItem) =>
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
