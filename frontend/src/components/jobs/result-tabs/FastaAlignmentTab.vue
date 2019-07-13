<template>
    <div>
        <div class="alignment-options">
            <a @click="toggleAllSelected">
                {{$t('jobs.results.actions.' + (allSelected ? 'deselectAll' : 'selectAll'))}}
            </a>
            <a @click="forwardSelected">{{$t('jobs.results.actions.forwardSelected')}}</a>
            <a @click="downloadAlignment">{{$t('jobs.results.actions.downloadMSA')}}</a>
            <a :href="downloadFilePath" target="_blank">{{$t('jobs.results.actions.exportMSA')}}</a>
        </div>
        <hr class="mt-2">

        <Loading :message="$t('jobs.results.alignment.loadingHits')"
                 v-if="loading"/>

        <div class="alignment-results mb-4"
             v-else>
            <p v-html="$t('jobs.results.alignment.numSeqs', {num: alignment.length})"></p>
            <div class="table-responsive">
                <table>
                    <tbody>
                    <template v-for="(elem, index) in alignment">
                        <tr :key="'header' + elem.num">
                            <td class="d-flex align-items-center">
                                <b-form-checkbox :checked="selected.includes(elem.num)"
                                                 @change="selectedChanged(elem.num)"/>
                                <b v-text="index + '.'"
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

        <tool-citation-info :tool="tool"/>
    </div>
</template>

<script lang="ts">
    /** TODO:
     * - lazyloading?
     * - it is very similar to clustal alignment tab.. is abstraction possible/advised?
     * - get data one level higher? make available to other result views?
     * */
    import Vue from 'vue';
    import ToolCitationInfo from '../ToolCitationInfo.vue';
    import {AlignmentItem, Job} from '@/types/toolkit/jobs';
    import {Tool} from '@/types/toolkit/tools';
    import Loading from '@/components/utils/Loading.vue';
    import {resultsService} from '@/services/ResultsService';
    import Logger from 'js-logger';

    const logger = Logger.get('ClustalAlignmentTab');

    export default Vue.extend({
        name: 'FastaAlignmentTab',
        components: {
            ToolCitationInfo,
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
                alignment: [] as AlignmentItem[],
                selected: [] as number[],
                loading: false,
            };
        },
        computed: {
            allSelected(): boolean {
                return this.alignment.length > 0 &&
                    this.selected.length === this.alignment.length;
            },
            downloadFilePath(): string {
                return resultsService.getDownloadFilePath(this.job.jobID, 'alignment.fas');
            },
        },
        mounted() {
            this.loadAlignments();
        },
        methods: {
            loadAlignments(): void {
                this.loading = true;
                resultsService.fetchAlignmentResults(this.job.jobID)
                    .then((data: AlignmentItem[]) => {
                        this.alignment = data;
                    })
                    .catch((e: any) => {
                        logger.error(e);
                    })
                    .finally(() => {
                        this.loading = false;
                    });
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
                    this.selected = this.alignment.map((a: AlignmentItem) => a.num);
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
                alert('implement me!');
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

    .alignment-options {
        font-size: 0.9em;

        a {
            cursor: pointer;
            margin-right: 3rem;
            color: inherit;
        }

        a:hover {
            color: $primary;
            text-decoration: none;
        }
    }
</style>
