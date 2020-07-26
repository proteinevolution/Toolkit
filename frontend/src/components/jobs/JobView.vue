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
                           :tool="tool"/>

                <tool-citation-info :tool="tool"/>
            </b-tab>
        </template>
        <template v-else
                  #job-tabs>
            <b-tab :title="$t('jobs.states.' + job.status)"
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
                    :error-message="errorMessage"/>
</template>

<script lang="ts">
    import Vue from 'vue';
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
    import {jobService} from '@/services/JobService';
    import NotFoundView from '@/components/utils/NotFoundView.vue';
    import Logger from 'js-logger';
    import ToolCitationInfo from '@/components/jobs/ToolCitationInfo.vue';
    import {lazyLoadView} from '@/router/routes';
    import EventBus from '@/util/EventBus';

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
            ToolCitationInfo,
            clustalAlignment: () => lazyLoadView(import(/* webpackChunkName: "clustal-views" */
                './result-tabs/ClustalAlignmentTab.vue')),
            fastaAlignment: () => lazyLoadView(import(/* webpackChunkName: "clustal-views" */
                './result-tabs/FastaAlignmentTab.vue')),
            alignmentViewer: () => lazyLoadView(import(/* webpackChunkName: "alignment-viewer-tab" */
                './result-tabs/AlignmentViewerTab.vue')),
            ngl3dStructureView: () => lazyLoadView(import(/* webpackChunkName: "ngl3d-viewer" */
                './result-tabs/NGL3DStructureView.vue')),
            hhompResults: () => lazyLoadView(import(/* webpackChunkName: "hhomp-results" */
                './result-tabs/HHompResultsTab.vue')),
            hhblitsResults: () => lazyLoadView(import(/* webpackChunkName: "hhblits-results" */
                './result-tabs/HHblitsResultsTab.vue')),
            hhpredResults: () => lazyLoadView(import(/* webpackChunkName: "hhpred-results" */
                './result-tabs/HHpredResultsTab.vue')),
            psiblastResults: () => lazyLoadView(import(/* webpackChunkName: "psiblast-results" */
                './result-tabs/PsiblastResultsTab.vue')),
            hmmerResults: () => lazyLoadView(import(/* webpackChunkName: "hmmer-results" */
                './result-tabs/HmmerResultsTab.vue')),
            clansResults: () => lazyLoadView(import(/* webpackChunkName: "clans-results" */
                './result-tabs/ClansResultsTab.vue')),
            patsearchResults: () => lazyLoadView(import(/* webpackChunkName: "patsearch-results" */
                './result-tabs/PatsearchResultsTab.vue')),
            plotView: () => lazyLoadView(import(/* webpackChunkName: "probability-plot" */
                './result-tabs/PlotTab.vue')),
            tprpredResults: () => lazyLoadView(import(/* webpackChunkName: "tprpred-results" */
                './result-tabs/TprpredResultsTab.vue')),
            quick2dResults: () => lazyLoadView(import(/* webpackChunkName: "quick2d-results" */
                './result-tabs/Quick2DResultsTab.vue')),
            hhrepidResults: () => lazyLoadView(import(/* webpackChunkName: "hhrepid-results" */
                './result-tabs/HhrepidResultsTab.vue')),
            imagesView: () => lazyLoadView(import(/* webpackChunkName: "images-view" */
                './result-tabs/ImagesViewTab.vue')),
            seq2IDResults: () => lazyLoadView(import(/* webpackChunkName: "seq2id-results" */
                './result-tabs/Seq2IDResultsTab.vue')),
            treeView: () => lazyLoadView(import(/* webpackChunkName: "tree-view" */
                './result-tabs/TreeTab.vue')),
            dataView: () => lazyLoadView(import(/* webpackChunkName: "data-view" */
                './result-tabs/DataTab.vue')),
            templateSelection: () => lazyLoadView(import(/* webpackChunkName: "data-view" */
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
                        this.$store.commit('jobs/removeJob', {jobID: oldJobID});
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
