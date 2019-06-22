<template>
    <div class="jobmanager-view">
        <div class="job-manager-header">
            <h1>Job Manager</h1>
        </div>

        <b-card>
            <b-row class="mb-2">
                <b-col cols="12"
                       sm="5"
                       md="3"
                       lg="4"
                       xl="3">
                    <label class="d-flex align-items-center">
                        <span v-text="$t('jobManager.perPage.show')"></span>
                        <b-form-select v-model="perPage" :options="perPageOptions"
                                       class="mx-2"></b-form-select>
                        <span v-text="$t('jobManager.perPage.entries')"></span>
                    </label>
                </b-col>
                <b-col cols="12"
                       sm="7"
                       md="6"
                       offset-md="3"
                       offset-lg="2"
                       xl="4"
                       offset-xl="5">
                    <label class="d-flex align-items-center justify-content-end">
                        <span v-text="$t('jobManager.filter')"></span>
                        <div class="ml-3 flex-grow-1">
                            <b-form-input v-model="filter"></b-form-input>
                        </div>
                    </label>
                </b-col>
            </b-row>
            <vue-table id="jobmanagerTable"
                       :api-mode="false"
                       :no-data-template="$t('jobManager.table.noData')"
                       :data="pagedJobs"
                       :fields="fields">
                <template #joblist="{rowData}">
                    <i class="fas cursor-pointer"
                       :class="[rowData.watched ? 'fa-eye':'fa-eye-slash']"
                       :title="$t('jobManager.watched.' + rowData.isPublic)"
                       @click="toggleJobListStatus(rowData.jobID, !rowData.watched)"></i>
                </template>
                <template #actions="{rowData}">
                    <i class="fa fa-fw mr-3 hover-unlock cursor-pointer"
                       :class="[rowData.isPublic ? 'fa-lock-open text-primary' : 'fa-lock']"
                       :title="$t('tools.parameters.isPublic.' + rowData.isPublic)"
                       @click="setPublic(rowData.jobID, !rowData.isPublic)"></i>
                    <i class="fa fa-fw fa-trash cursor-pointer"
                       v-if="!rowData.foreign"
                       :title="$t('jobs.delete')"
                       @click="deleteJob(rowData.jobID)"></i>
                </template>
            </vue-table>
            <div class="d-flex justify-content-between align-items-center">
                <span v-text="$t('jobManager.paginationInfo', {start, end, total: filteredJobs.length})"></span>
                <b-pagination
                        v-model="currentPage"
                        :total-rows="filteredJobs.length"
                        :per-page="perPage"
                        align="right"
                        class="mb-0"
                        aria-controls="jobmanagerTable"
                ></b-pagination>
            </div>
        </b-card>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import hasHTMLTitle from '@/mixins/hasHTMLTitle';
    import VueTable from 'vuetable-2/src/components/Vuetable';
    import {Job} from '@/types/toolkit/jobs';
    import moment from 'moment';
    import {Tool} from '@/types/toolkit/tools';
    import JobService from '@/services/JobService';

    export default Vue.extend({
        name: 'JobManagerView',
        mixins: [hasHTMLTitle],
        components: {
            VueTable,
        },
        data() {
            return {
                fields: [{
                    name: '__slot:joblist',
                    title: this.$t('jobManager.table.jobListStatus'),
                }, {
                    name: 'jobID',
                    title: this.$t('jobManager.table.jobID'),
                }, {
                    name: 'tool',
                    title: this.$t('jobManager.table.tool'),
                    callback: 'translateToolName',
                }, {
                    name: 'dateCreated',
                    title: this.$t('jobManager.table.dateCreated'),
                    callback: 'fromNow',
                }, {
                    name: '__slot:actions',
                    title: this.$t('jobManager.table.actions'),
                }],
                filter: '',
                currentPage: 1,
                perPage: 5,
                perPageOptions: [
                    5,
                    10,
                    25,
                    50,
                ],
            };
        },
        computed: {
            htmlTitle() {
                return 'Jobmanager';
            },
            jobs(): Job[] {
                return this.$store.getters['jobs/ownedJobs'].slice(0);
            },
            filteredJobs(): Job[] {
                const filterString = this.filter.toLowerCase();
                if (!filterString) {
                    return this.jobs;
                }
                return this.jobs.filter((job: Job) => {
                    return job.jobID.toLowerCase().includes(filterString) ||
                        job.tool.toLowerCase().includes(filterString);
                });
            },
            sortedJobs(): Job[] {
                return this.filteredJobs;
            },
            pagedJobs(): Job[] {
                const start = (this.currentPage - 1) * this.perPage;
                return this.sortedJobs.slice(start, start + this.perPage);
            },
            tools(): Tool[] {
                return this.$store.getters['tools/tools'];
            },
            start(): number {
                return (this.currentPage - 1) * this.perPage + 1;
            },
            end(): number {
                return Math.min(this.currentPage * this.perPage, this.filteredJobs.length);
            },
        },
        methods: {
            deleteJob(jobID: string): void {
                JobService.deleteJob(jobID)
                    .catch(() => {
                        this.$alert(this.$t('errors.couldNotDeleteJob'), 'danger');
                    });
            },
            setPublic(jobID: string, isPublic: boolean): void {
                this.$store.dispatch('jobs/setJobPublic', {jobID, isPublic});
            },
            toggleJobListStatus(jobID: string, watched: boolean): void {
                this.$store.dispatch('jobs/setJobWatched', {jobID, watched});
            },
            fromNow(date: string): string {
                return moment(date).fromNow();
            },
            translateToolName(toolName: string): string {
                const tool: Tool | undefined = this.tools.find((t: Tool) => t.name === toolName);
                return tool ? tool.longname : toolName;
            },
        },
    });
</script>

<style lang="scss" scoped>
    .job-manager-header {
        height: 2.75rem;

        h1 {
            color: $primary;
            font-weight: bold;
            font-size: 1.25em;
            line-height: 1.6;
        }
    }

    .hover-unlock.fa-lock:hover::before {
        content: "\f09c";
    }
</style>
