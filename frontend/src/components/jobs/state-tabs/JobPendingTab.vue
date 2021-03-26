<template>
    <div class="d-flex flex-column my-2">
        <p class="text-center"
           v-html="$t('jobs.foundIdenticalCopy', {jobID: similarJob.jobID, createdAt: fromNow(similarJob.dateCreated)})"></p>
        <div class="d-flex flex-column flex-md-row justify-content-center mt-4 mx-md-5">
            <b-btn variant="primary"
                   class="mb-3 mb-md-0 mr-5 ml-5"
                   @click="startJob"
                   v-text="$t('jobs.startJob')" />
            <b-btn variant="primary"
                   class="mr-5 ml-5"
                   @click="loadExistingJobAndDelete"
                   v-text="$t('jobs.loadExistingJob')" />
        </div>
    </div>
</template>

<script lang="ts">
import Vue from 'vue';
import moment from 'moment';
import {jobService} from '@/services/JobService';
import {SimilarJobResult} from '@/types/toolkit/jobs';
import Logger from 'js-logger';

const logger = Logger.get('JobPendingTab');

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
        jobService.getSimilarJob(this.job.jobID)
            .then((similarJob: SimilarJobResult) => {
                this.similarJob = similarJob;
            })
            .catch(() => {
                logger.error('No similar job returned');
                this.$alert(this.$t('errors.general'), 'danger');
            });
    },
    methods: {
        startJob() {
            jobService.startJob(this.job.jobID)
                .catch(() => {
                    logger.error('Could not start job!');
                    this.$alert(this.$t('errors.general'), 'danger');
                });
        },
        loadExistingJobAndDelete() {
            const oldJobID: string = this.job.jobID;
            logger.debug(`loading existing job ${this.similarJob.jobID}, deleting job ${oldJobID}`);
            this.$router.push(`/jobs/${this.similarJob.jobID}`);
            jobService.deleteJob(oldJobID)
                .then(() => {
                    this.$store.commit('jobs/removeJob', {jobID: oldJobID});
                })
                .catch(() => {
                    logger.error('Could not delete old job!');
                    this.$alert(this.$t('errors.couldNotDeleteJob'), 'danger');
                });
        },
        fromNow(date: string): string {
            return moment(date).fromNow();
        },
    },
});
</script>
