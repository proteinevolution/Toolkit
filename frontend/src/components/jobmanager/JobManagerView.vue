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
                        <b-form-select v-model="perPage"
                                       :options="perPageOptions"
                                       class="mx-2" />
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
                            <b-form-input v-model="filter" />
                        </div>
                    </label>
                </b-col>
            </b-row>
            <b-table id="jobmanagerTable"
                     primary-key="jobID"
                     :items="jobs"
                     :fields="fields"
                     :current-page="currentPage"
                     :per-page="perPage"
                     :filter="filter"
                     :empty-text="$t('jobManager.table.noData')"
                     :empty-filtered-text="$t('jobManager.table.noDataFiltered')"
                     sort-by="dateCreated"
                     sort-desc
                     show-empty
                     responsive
                     @filtered="onFiltered">
                <template v-slot:cell(jobID)="data">
                    <router-link :to="`/jobs/${data.item.jobID}`"
                                 class="job-link">
                        {{ data.value }}
                    </router-link>
                </template>

                <template v-slot:cell(status)="{value}">
                    <b-badge variant="light"
                             :class="'status-' + value"
                             v-text="$t('jobs.states.' + value)" />
                </template>

                <template v-slot:cell(joblist)="{item}">
                    <i class="fas cursor-pointer"
                       :class="[item.watched ? 'fa-eye':'fa-eye-slash']"
                       :title="$t('jobManager.watched.' + item.isPublic)"
                       @click="toggleJobListStatus(item.jobID, !item.watched)"></i>
                </template>

                <template v-slot:cell(actions)="{item}">
                    <i v-if="loggedIn"
                       class="fa fa-fw mr-3 hover-unlock cursor-pointer"
                       :class="[item.isPublic ? 'fa-lock-open text-primary' : 'fa-lock']"
                       :title="$t('tools.parameters.isPublic.' + item.isPublic)"
                       @click="setPublic(item.jobID, !item.isPublic)"></i>
                    <i v-if="!item.foreign"
                       class="fa fa-fw fa-trash cursor-pointer"
                       :title="$t('jobs.delete')"
                       @click="deleteJob(item.jobID)"></i>
                </template>
            </b-table>
            <div v-show="totalRows > perPage"
                 class="pagination-container">
                <span v-text="$t('jobManager.paginationInfo', {start, end, total: totalRows})"></span>
                <b-pagination
                    v-model="currentPage"
                    :total-rows="totalRows"
                    :per-page="perPage"
                    align="right"
                    class="mb-0"
                    aria-controls="jobmanagerTable" />
            </div>
        </b-card>
    </div>
</template>

<script lang="ts">
    import hasHTMLTitle from '@/mixins/hasHTMLTitle';
    import {Job} from '@/types/toolkit/jobs';
    import moment from 'moment';
    import {Tool} from '@/types/toolkit/tools';
    import {jobService} from '@/services/JobService';

    export default hasHTMLTitle.extend({
        name: 'JobManagerView',
        data() {
            return {
                fields: [{
                    key: 'joblist',
                    label: this.$t('jobManager.table.jobListStatus'),
                }, {
                    key: 'status',
                    label: this.$t('jobManager.table.status'),
                    sortable: true,
                }, {
                    key: 'jobID',
                    label: this.$t('jobManager.table.jobID'),
                    sortable: true,
                }, {
                    key: 'tool',
                    label: this.$t('jobManager.table.tool'),
                    formatter: 'translateToolName',
                    sortable: true,
                }, {
                    key: 'dateCreated',
                    label: this.$t('jobManager.table.dateCreated'),
                    formatter: 'fromNow',
                    sortable: true,
                }, {
                    key: 'actions',
                    label: this.$t('jobManager.table.actions'),
                }],
                totalRows: 1,
                filter: '',
                currentPage: 1,
                perPage: 10,
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
            tools(): Tool[] {
                return this.$store.getters['tools/tools'];
            },
            start(): number {
                if (this.totalRows === 0) {
                    return 0;
                }
                return (this.currentPage - 1) * this.perPage + 1;
            },
            end(): number {
                return Math.min(this.currentPage * this.perPage, this.totalRows);
            },
            loggedIn(): boolean {
                return this.$store.getters['auth/loggedIn'];
            },
        },
        mounted() {
            // Set the initial number of items
            this.totalRows = this.jobs.length;
        },
        methods: {
            deleteJob(jobID: string): void {
                jobService.deleteJob(jobID)
                    .then(() => {
                        this.$store.commit('jobs/removeJob', {jobID});
                        this.totalRows = this.jobs.length;
                    })
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
            translateToolName(toolName: string): string {
                const tool: Tool | undefined = this.tools.find((t: Tool) => t.name === toolName);
                return tool ? tool.longname : toolName;
            },
            fromNow(date: string): string {
                return moment(date).from(moment.utc(this.$store.state.now));
            },
            onFiltered(filteredItems: any) {
                // Trigger pagination to update the number of buttons/pages due to filtering
                this.totalRows = filteredItems.length;
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

    .job-link:not(:hover) {
        color: $gray-900;
    }
</style>
