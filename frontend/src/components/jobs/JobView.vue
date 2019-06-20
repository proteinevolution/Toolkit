<template>
    <tool-view v-if="job"
               isJobView
               :job="job">

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
                <job-done-tab v-else-if="job.status === JobState.Done"
                              :tool="tool"
                              :job="job"/>
                <job-submitted-tab v-else-if="job.status === JobState.Submitted"
                                   :tool="tool"
                                   :job="job"/>
                <job-pending-tab v-else-if="job.status === JobState.Pending"
                                 :tool="tool"
                                 :job="job"/>
                <span v-else>
                    Not yet implemented
                </span>
            </b-tab>
        </template>

    </tool-view>
</template>

<script lang="ts">
    import Vue from 'vue';
    import JobPreparedTab from './JobPreparedTab.vue';
    import JobQueuedTab from './JobQueuedTab.vue';
    import JobRunningTab from './JobRunningTab.vue';
    import JobErrorTab from './JobErrorTab.vue';
    import JobDoneTab from './JobDoneTab.vue';
    import JobSubmittedTab from './JobSubmittedTab.vue';
    import JobPendingTab from './JobPendingTab.vue';
    import ToolView from '../tools/ToolView.vue';
    import {Job} from '@/types/toolkit/jobs';
    import moment from 'moment';
    import {JobState} from '@/types/toolkit/enums';
    import {Tool} from '@/types/toolkit/tools';

    export default Vue.extend({
        name: 'JobView',
        components: {
            ToolView,
            JobPreparedTab,
            JobQueuedTab,
            JobRunningTab,
            JobErrorTab,
            JobDoneTab,
            JobSubmittedTab,
            JobPendingTab,
        },
        data() {
            return {
                JobState,
            };
        },
        computed: {
            jobID(): string {
                return this.$route.params.jobID;
            },
            dateCreated(): string {
                return moment(this.job.dateCreated).format('lll');
            },
            job(): Job {
                return this.$store.getters['jobs/jobs'].find((job: Job) => job.jobID === this.jobID);
            },
            tool(): Tool {
                return this.$store.getters['tools/tools'].find((tool: Tool) => tool.name === this.job.tool);
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
        },
        methods: {
            loadJobDetails(jobID: string): void {
                this.$store.dispatch('jobs/loadJobDetails', jobID);
            },
            goToParent() {
                this.$router.push(`/jobs/${this.job.parentID}`);
            },
        },
    });
</script>

<style lang="scss" scoped>

</style>
