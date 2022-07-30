<template>
    <tool-view v-if="job"
               is-job-view
               :job="job"
               @delete-job="deleteJob">
        <template #job-details>
            <small class="text-muted mr-2"
                   v-text="$t('jobs.details.jobID', {jobID})"></small>
            <i18n v-if="job.parentID"
                  path="jobs.details.parentID"
                  tag="small"
                  class="text-muted mr-2">
                <a class="cursor-pointer text-primary"
                   @click="goToParent"
                   v-text="job.parentID"></a>
            </i18n>
            <small class="text-muted"
                   v-text="$t('jobs.details.dateCreated', {dateCreated})"></small>
        </template>

        <template v-if="job.status === JobState.Done && job.views"
                  #job-tabs="{fullScreen}">
            <b-tab v-for="(jobViewOptions, index) in job.views"
                   :key="'jobview-' + index"
                   :title="$t('jobs.results.titles.' + (jobViewOptions.title || jobViewOptions.component))"
                   :active="index === 0"
                   @click="tabActivated(jobViewOptions.component)">
                <component :is="jobViewOptions.component"
                           :result-tab-name="jobViewOptions.component"
                           :job="job"
                           :view-options="jobViewOptions"
                           :full-screen="fullScreen"
                           :render-on-create="index === 0"
                           :tool="tool" />

                <tool-citation-info :tool="tool" />
            </b-tab>
        </template>
        <template v-else
                  #job-tabs>
            <b-tab :title="$t('jobs.states.' + job.status)"
                   active>
                <job-prepared-tab v-if="job.status === JobState.Prepared"
                                  :tool="tool"
                                  :job="job" />
                <job-queued-tab v-else-if="job.status === JobState.Queued"
                                :tool="tool"
                                :job="job" />
                <job-running-tab v-else-if="job.status === JobState.Running"
                                 :tool="tool"
                                 :job="job" />
                <job-error-tab v-else-if="job.status === JobState.Error"
                               :tool="tool"
                               :job="job" />
                <job-submitted-tab v-else-if="job.status === JobState.Submitted"
                                   :tool="tool"
                                   :job="job" />
                <job-pending-tab v-else-if="job.status === JobState.Pending"
                                 :tool="tool"
                                 :job="job" />
                <job-limitReached-tab v-else-if="job.status === JobState.LimitReached"
                                      :tool="tool"
                                      :job="job" />
                <span v-else>
                    Error!
                    <p class="cursor-pointer text-primary"
                       v-text="job.status"></p>                </span>
            </b-tab>
        </template>
    </tool-view>
    <not-found-view v-else
                    :error-message="errorMessage" />
</template>

<script lang="ts">
import Vue from 'vue';
import JobPreparedTab from './state-tabs/JobPreparedTab.vue';
import JobQueuedTab from './state-tabs/JobQueuedTab.vue';
import JobRunningTab from './state-tabs/JobRunningTab.vue';
import JobErrorTab from './state-tabs/JobErrorTab.vue';
import JobSubmittedTab from './state-tabs/JobSubmittedTab.vue';
import JobPendingTab from './state-tabs/JobPendingTab.vue';
import JobLimitReachedTab from '@/components/jobs/state-tabs/JobLimitReachedTab.vue';
import ToolView from '../tools/ToolView.vue';
import {Job} from '@/types/toolkit/jobs';
import moment from 'moment';
import {JobState} from '@/types/toolkit/enums';
import {Tool} from '@/types/toolkit/tools';
import {jobService} from '@/services/JobService';
import NotFoundView from '@/components/utils/NotFoundView.vue';
import Logger from 'js-logger';
import ToolCitationInfo from '@/components/jobs/ToolCitationInfo.vue';
import {lazyLoadView} from '@/router/routes';
import EventBus from '@/util/EventBus';
import {mapStores} from 'pinia';
import {useRootStore} from '@/stores/root';
import {useToolsStore} from '@/stores/tools';
import {useJobsStore} from '@/stores/jobs';
import {useAuthStore} from '@/stores/auth';

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
        JobLimitReachedTab,
        NotFoundView,
        ToolCitationInfo,
        clustalAlignment: () => lazyLoadView(import(
            './result-tabs/ClustalAlignmentTab.vue')),
        fastaAlignment: () => lazyLoadView(import(
            './result-tabs/FastaAlignmentTab.vue')),
        alignmentViewer: () => lazyLoadView(import(
            './result-tabs/AlignmentViewerTab.vue')),
        ngl3dStructureView: () => lazyLoadView(import(
            './result-tabs/NGL3DStructureView.vue')),
        hhompResults: () => lazyLoadView(import(
            './result-tabs/HHompResultsTab.vue')),
        hhblitsResults: () => lazyLoadView(import(
            './result-tabs/HHblitsResultsTab.vue')),
        hhpredResults: () => lazyLoadView(import(
            './result-tabs/HHpredResultsTab.vue')),
        psiblastResults: () => lazyLoadView(import(
            './result-tabs/PsiblastResultsTab.vue')),
        hmmerResults: () => lazyLoadView(import(
            './result-tabs/HmmerResultsTab.vue')),
        clansResults: () => lazyLoadView(import(
            './result-tabs/ClansResultsTab.vue')),
        patsearchResults: () => lazyLoadView(import(
            './result-tabs/PatsearchResultsTab.vue')),
        plotView: () => lazyLoadView(import(
            './result-tabs/PlotTab.vue')),
        tprpredResults: () => lazyLoadView(import(
            './result-tabs/TprpredResultsTab.vue')),
        quick2dResults: () => lazyLoadView(import(
            './result-tabs/Quick2DResultsTab.vue')),
        hhrepidResults: () => lazyLoadView(import(
            './result-tabs/HhrepidResultsTab.vue')),
        imagesView: () => lazyLoadView(import(
            './result-tabs/ImagesViewTab.vue')),
        seq2IDResults: () => lazyLoadView(import(
            './result-tabs/Seq2IDResultsTab.vue')),
        treeView: () => lazyLoadView(import(
            './result-tabs/TreeTab.vue')),
        dataView: () => lazyLoadView(import(
            './result-tabs/DataTab.vue')),
        templateSelection: () => lazyLoadView(import(
            './result-tabs/TemplateSelectionViewTab.vue')),
    },
    data() {
        return {
            JobState,
            errorMessage: '',
        };
    },
    computed: {
        jobID(): string {
            return this.$route.params.jobID;
        },
        dateCreated(): string {
            return moment(this.job.dateCreated).from(moment.utc(this.rootStore.now));
        },
        job(): Job {
            return this.jobsStore.jobs.find((job: Job) => job.jobID === this.jobID) as Job;
        },
        tool(): Tool {
            return this.toolsStore.tools.find((tool: Tool) => tool.name === this.job.tool) as Tool;
        },
        loggedIn(): boolean {
            return this.authStore.loggedIn;
        },
        ...mapStores(useRootStore, useAuthStore, useToolsStore, useJobsStore),
    },
    watch: {
        // Use a watcher here - component cannot use 'beforeRouteUpdate' because of lazy loading
        $route(to) {
            this.loadJobDetails(to.params.jobID);
        },
        loggedIn(login) {
            if (login) {
                this.loadJobDetails(this.jobID);
            } else {
                // need to handle error separately
                this.jobsStore.loadJobDetails(this.jobID)
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
    created() {
        logger.debug(`created JobView with jobID ${this.jobID}`);
        this.loadJobDetails(this.jobID);
    },
    methods: {
        deleteJob() {
            const oldJobID: string = this.jobID;
            jobService.deleteJob(oldJobID)
                .then(() => {
                    this.$router.replace('/jobmanager');
                    this.jobsStore.removeJob(oldJobID);
                })
                .catch(() => {
                    this.$alert(this.$t('errors.couldNotDeleteJob'), 'danger');
                });
        },
        loadJobDetails(jobID: string): Promise<void> {
            return this.jobsStore.loadJobDetails(jobID)
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
        tabActivated(jobView: string): void {
            EventBus.$emit('tool-tab-activated', jobView);
        },
    },
});
</script>

<style lang="scss">
.result-options {
  font-size: 0.9em;
  background: $white;
  border-bottom: 1px solid rgba(10, 10, 10, 0.1);
  margin-top: -1rem;
  margin-bottom: 1rem;
  padding: 1rem 0;
  position: sticky;
  top: 0;
  z-index: 10;
  overflow-x: auto;
  white-space: nowrap;

  a {
    cursor: pointer;
    margin-right: 1rem;
    @include media-breakpoint-up(lg) {
      margin-right: 2.5rem;
    }
    color: inherit;

    &[disabled] {
      cursor: not-allowed;
      color: $tk-gray;
    }

    &:hover,
    &.active {
      color: $primary !important;
      text-decoration: none;
    }
  }
}
</style>
