<template>
    <tool-view v-if="job"
               :job="job">

        <template slot="job-details">
            <small class="text-muted"
                   v-text="$t('jobs.details', {jobID, dateCreated})"></small>
        </template>

        <template slot="job-tabs">
            <b-tab title="Test">
                <div class="tabs-panel">
                    Display job data
                </div>
            </b-tab>
            <b-tab title="Test2">
                <div class="tabs-panel">
                    Display job data
                </div>
            </b-tab>
        </template>

    </tool-view>
</template>

<script lang="ts">
    import Vue from 'vue';
    import ToolView from '../tools/ToolView.vue';
    import {Job} from '@/types/toolkit/jobs';
    import moment from 'moment';

    export default Vue.extend({
        name: 'JobView',
        components: {
            ToolView,
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
        },
    });
</script>

<style lang="scss" scoped>

</style>
