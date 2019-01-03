<template>
    <div class="d-flex flex-column my-2">
        <p v-html="$t('jobs.foundIdenticalCopy', {jobID: similarJob.jobID, createdAt: fromNow(similarJob.dateCreated)})"
           class="text-center">
        </p>
        <div class="d-flex justify-content-around mt-3 mx-5">
            <b-btn variant="primary"
                   @click="startJob">
                Start job anyways
            </b-btn>
            <b-btn variant="primary"
                   @click="loadExistingJob">
                Load existing job
            </b-btn>
            <b-btn variant="primary"
                   @click="loadExistingJobAndDelete">
                Load existing job and delete this one
            </b-btn>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import moment from 'moment';
    import JobService from '@/services/JobService';
    import {SimilarJobResult} from '@/types/toolkit/jobs';

    export default Vue.extend({
        name: 'JobPendingTab',
        props: {
            job: {
                type: Object,
                required: true,
            },
        },
        data() {
            return {
                similarJob: {
                    jobID: '',
                    dateCreated: 0,
                },
            };
        },
        created(): void {
            JobService.getSimilarJob(this.job.jobID)
                .then((similarJob: SimilarJobResult) => {
                    this.similarJob = similarJob;
                })
                .catch(() => {
                    // TODO: error-handling
                });
        },
        methods: {
            startJob() {
                JobService.startJob(this.job.jobID)
                    .catch(() => {
                        // TODO: error-handling
                    });
            },
            loadExistingJob() {
                this.$router.push(`/jobs/${this.similarJob.jobID}`);
            },
            loadExistingJobAndDelete() {
                JobService.deleteJob(this.job.jobID)
                    .then(() => {
                        this.$store.commit('jobs/removeJob', {jobID: this.job.jobID});
                        this.$router.push(`/jobs/${this.similarJob.jobID}`);
                    })
                    .catch(() => {
                        this.$alert(this.$t('errors.couldNotDeleteJob'), '', 'danger');
                    });
            },
            fromNow(date: string): string {
                return moment(date).fromNow();
            },
        },
    });
</script>

<style lang="scss" scoped>

</style>
