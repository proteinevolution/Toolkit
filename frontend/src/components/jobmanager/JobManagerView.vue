<template>
    <div class="jobmanager-view">
        <div class="job-manager-header">
            <h1>Job Manager</h1>
        </div>

        <b-card>
            <vuetable ref="vuetable"
                      :api-mode="false"
                      :no-data-template="$t('jobManager.table.noData')"
                      :data="jobs"
                      :fields="fields">
                <template slot="joblist"
                          slot-scope="props">
                    <i class="fas cursor-pointer"
                       :class="[props.rowData.hidden ? 'fa-plus-circle':'fa-minus-circle']"
                       @click="toggleJobListStatus(props.rowData.jobID)"></i>
                </template>
                <template slot="actions"
                          slot-scope="props">
                    <i class="fa fa-trash cursor-pointer"
                       @click="deleteJob(props.rowData.jobID)"></i>
                </template>
            </vuetable>
        </b-card>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import hasHTMLTitle from '@/mixins/hasHTMLTitle';
    import Vuetable from 'vuetable-2/src/components/Vuetable';
    import {Job} from '@/types/toolkit/jobs';
    import moment from 'moment';
    import {Tool} from '@/types/toolkit/tools';
    import JobService from '@/services/JobService';

    export default Vue.extend({
        name: 'JobManagerView',
        mixins: [hasHTMLTitle],
        components: {Vuetable},
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
            };
        },
        computed: {
            htmlTitle() {
                return 'Jobmanager';
            },
            jobs(): Job[] {
                return this.$store.getters['jobs/jobs'].slice(0);
            },
            tools(): Tool[] {
                return this.$store.getters['tools/tools'];
            },
        },
        methods: {
            deleteJob(jobID: string): void {
                JobService.deleteJob(jobID)
                    .then(() => {
                        this.$store.commit('jobs/removeJob', {jobID});
                    })
                    .catch(() => {
                        this.$alert(this.$t('errors.couldNotDeleteJob'), '', 'danger');
                    });
            },
            toggleJobListStatus(jobID: string): void {
                this.$store.commit('jobs/toggleJobHidden', {jobID});
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
</style>
