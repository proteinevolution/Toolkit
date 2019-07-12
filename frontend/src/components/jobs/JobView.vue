<template>
    <tool-view v-if="job"
               isJobView
               :job="job"
               @delete-job="deleteJob">

        <template #job-details>
            <small class="text-muted mr-2"
                   v-text="$t('jobs.details.jobID', {jobID})"></small>
            <i18n path="jobs.details.parentID"
                  tag="small"
                  class="text-muted mr-2"
                  v-if="job.parentID">
                <a class="cursor-pointer text-primary"
                   @click="goToParent"
                   v-text="job.parentID"></a>
            </i18n>
            <small class="text-muted"
                   v-text="$t('jobs.details.dateCreated', {dateCreated})"></small>
        </template>

        <template #job-tabs>

            <b-tab v-if="job.status === JobState.Done && job.views"
                   v-for="(jobView, index) in job.views"
                   :key="'jobview-' + index"
                   :title="$t('jobs.results.titles.' + jobView)"
                   :active="index === 0">
                <component :is="jobView"
                           :job="job"
                           :tool="tool"></component>
            </b-tab>

            <b-tab :title="$t('jobs.states.' + job.status)"
                   v-else
                   active>
                <job-prepared-tab v-if="job.status === JobState.Prepared"
                                  :tool="tool"
                                  :job="job"/>
                <job-queued-tab v-else-if="job.status === JobState.Queued"
                                :tool="tool"
                                :job="job"/>
                <job-running-tab v-else-if="job.status === JobState.Running"
                                 :tool="tool"
                                 :job="job"/>
                <job-error-tab v-else-if="job.status === JobState.Error"
                               :tool="tool"
                               :job="job"/>
                <job-submitted-tab v-else-if="job.status === JobState.Submitted"
                                   :tool="tool"
                                   :job="job"/>
                <job-pending-tab v-else-if="job.status === JobState.Pending"
                                 :tool="tool"
                                 :job="job"/>
                <span v-else>
                    Error!
                </span>
            </b-tab>

        </template>

    </tool-view>
    <not-found-view v-else
                    :errorMessage="errorMessage"/>
</template>

<script lang="ts">
    import Vue from 'vue';
    import ClustalAlignmentTab from './result-tabs/ClustalAlignmentTab.vue';
    import FastaAlignmentTab from './result-tabs/FastaAlignmentTab.vue';
    import AlignmentViewerTab from './result-tabs/AlignmentViewerTab.vue';
    import DataTab from './result-tabs/DataTab.vue';
    import TreeTab from './result-tabs/TreeTab.vue';
    import HitlistTab from './result-tabs/HitlistTab.vue';
    import ResultsTab from './result-tabs/ResultsTab.vue';
    import SummaryTab from './result-tabs/SummaryTab.vue';
    import JobPreparedTab from './state-tabs/JobPreparedTab.vue';
    import JobQueuedTab from './state-tabs/JobQueuedTab.vue';
    import JobRunningTab from './state-tabs/JobRunningTab.vue';
    import JobErrorTab from './state-tabs/JobErrorTab.vue';
    import JobSubmittedTab from './state-tabs/JobSubmittedTab.vue';
    import JobPendingTab from './state-tabs/JobPendingTab.vue';
    import ToolView from '../tools/ToolView.vue';
    import {Job} from '@/types/toolkit/jobs';
    import moment from 'moment';
    import {JobState} from '@/types/toolkit/enums';
    import {Tool} from '@/types/toolkit/tools';
    import JobService from '@/services/JobService';
    import NotFoundView from '@/components/utils/NotFoundView.vue';
    import Logger from 'js-logger';

    const logger = Logger.get('JobView');

    export default Vue.extend({
        name: 'JobView',
        components: {
            ToolView,
            JobPreparedTab,
            JobQueuedTab,
            JobRunningTab,
            JobErrorTab,
            JobSubmittedTab,
            JobPendingTab,
            NotFoundView,
            clustalAlignment: ClustalAlignmentTab,
            fastaAlignment: FastaAlignmentTab,
            alignmentViewer: AlignmentViewerTab,
            hitlist: HitlistTab,
            results: ResultsTab,
            treeView: TreeTab,
            summaryView: SummaryTab,
            dataView: DataTab,
        },
        data() {
            return {
                JobState,
                errorMessage: 'errors.JobNotFound',
            };
        },
        computed: {
            jobID(): string {
                return this.$route.params.jobID;
            },
            dateCreated(): string {
                return moment(this.job.dateCreated).from(moment.utc(this.$store.state.now));
            },
            job(): Job {
                return this.$store.getters['jobs/jobs'].find((job: Job) => job.jobID === this.jobID);
            },
            tool(): Tool {
                return this.$store.getters['tools/tools'].find((tool: Tool) => tool.name === this.job.tool);
            },
            loggedIn(): boolean {
                return this.$store.getters['auth/loggedIn'];
            },
        },
        created() {
            this.loadJobDetails(this.jobID);
        },
        watch: {
            // Use a watcher here - component cannot use 'beforeRouteUpdate' because of lazy loading
            $route(to, from) {
                this.loadJobDetails(to.params.jobID);
            },
            loggedIn(login) {
                if (login) {
                    this.loadJobDetails(this.jobID);
                } else {
                    // need to handle error separately
                    this.$store.dispatch('jobs/loadJobDetails', this.jobID)
                        .catch((err) => {
                            logger.info('Error when getting jobs!', err);
                            if (err.request.status === 401) {
                                logger.info('Redirecting to index');
                                this.$router.push('/');
                            }
                        });
                }
            },
        },
        methods: {
            deleteJob() {
                JobService.deleteJob(this.jobID)
                    .then(() => {
                        this.$router.replace('/jobmanager');
                    })
                    .catch(() => {
                        this.$alert(this.$t('errors.couldNotDeleteJob'), 'danger');
                    });
            },
            loadJobDetails(jobID: string): Promise<Job> {
                return this.$store.dispatch('jobs/loadJobDetails', jobID)
                    .catch((err) => {
                        logger.warn('Error when getting jobs', err);
                        if (err.request.status === 401) {
                            this.errorMessage = 'errors.JobNotAuthorized';
                        } else {
                            this.errorMessage = 'errors.JobNotFound';
                        }
                    });
            },
            goToParent() {
                this.$router.push(`/jobs/${this.job.parentID}`);
            },
        },
    });
</script>

<style lang="scss" scoped>

</style>
