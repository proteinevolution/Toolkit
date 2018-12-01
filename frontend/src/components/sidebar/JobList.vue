<template>
    <div class="job-list">
        <div class="job-list-controls">
            <div v-for="(sortCol, index) in sortColumns"
                 :key="sortCol.name"
                 class="sort"
                 @click="selectedSort = index"
                 :class="[selectedSort === index ? 'selected':'']">
                {{ $t('jobList.sortColumns.' + sortCol.name) }}
            </div>
            <div class="open-job-manager">
                <router-link to="/jobmanager">
                    <i class="fas fa-list-ul"></i>
                </router-link>
            </div>
        </div>

        <div class="job-list-elements">
            <div class="job-list-up"
                 @click="scrollDown"
                 v-if="jobs.length > itemsPerPage"
                 :class="[startIndex > 0 ? '' : 'disabled']">
                <i class="fas fa-caret-up"></i>
            </div>

            <div class="job-element"
                 v-for="job in sortedJobs"
                 :class="['status-' + job.status, job.jobID === selectedJobID ? 'selected' : '']"
                 @click="goToJob(job.jobID)">
                <span v-text="job.jobID"></span>
                <span v-text="job.code.toUpperCase()"></span>
                <i class="fas fa-times"
                   @click.stop="deleteJob(job.jobID)"></i>
            </div>

            <div class="job-list-down d-flex flex-column"
                 @click="scrollUp"
                 v-if="jobs.length > itemsPerPage"
                 :class="[startIndex + itemsPerPage < jobs.length ? '' : 'disabled']">
                <small class="text-muted"
                       v-text="$t('jobList.pagination', {currentPage, pageCount})"></small>
                <i class="fas fa-caret-down"></i>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {Job} from '@/types/toolkit/jobs';
    import moment from 'moment';

    export default Vue.extend({
        name: 'JobList',
        data() {
            return {
                sortColumns: [{
                    name: 'jobID',
                    sort: (a: Job, b: Job) => {
                        return a.jobID.localeCompare(b.jobID);
                    },
                }, {
                    name: 'dateCreated',
                    sort: (a: Job, b: Job) => {
                        return moment.utc(a.dateCreated).diff(moment.utc(b.dateCreated));
                    },
                }, {
                    name: 'tool',
                    sort: (a: Job, b: Job) => {
                        return a.tool.localeCompare(b.tool);
                    },
                }],
                selectedSort: 1,
                itemsPerPage: 10,
                startIndex: 0,
            };
        },
        computed: {
            selectedJobID(): string {
                return this.$route.params.jobID;
            },
            jobs(): Job[] {
                return this.$store.getters['jobs/jobs'].slice(0);
            },
            sortedJobs(): Job[] {
                return this.jobs.sort(this.sortColumns[this.selectedSort].sort).slice(this.startIndex, this.startIndex + this.itemsPerPage);
            },
            currentPage(): number {
                return Math.floor(this.startIndex / this.itemsPerPage) + 1;
            },
            pageCount(): number {
                return Math.ceil(this.jobs.length / this.itemsPerPage);
            },
        },
        methods: {
            goToJob(jobID: string): void {
                this.$router.push(`/jobs/${jobID}`);
            },
            deleteJob(jobID: string): void {
                // TODO
                this.$alert('implement me!!', '', 'warning');
            },
            scrollDown(): void {
                if (this.startIndex > 0) {
                    this.startIndex--;
                }
            },
            scrollUp(): void {
                if (this.startIndex + this.itemsPerPage < this.jobs.length) {
                    this.startIndex++;
                }
            },
        },
    });
</script>

<style lang="scss" scoped>
    .job-list {
        margin-top: 1rem;

        .job-list-controls {
            display: flex;
            justify-content: space-between;
            text-align: center;
            width: 100%;
            margin-bottom: 1rem;
            box-shadow: 1px 1px 2px 1px $tk-light-gray;
            background-color: transparent;
            cursor: pointer;
            border: 1px solid rgba($black, .125);
            border-radius: $global-radius;

            .sort {
                font-weight: bold;
                color: $tk-dark-gray;
                font-size: 0.7em;
                padding: 0.4rem 6%;

                &.selected {
                    color: $primary;
                }
            }

            .open-job-manager {
                padding: 0.3rem 0.7rem;
                font-size: 0.9em;
                color: $primary;
            }
        }

        .job-list-up, .job-list-down {
            line-height: 1.5;
            padding: 0.25rem;
            text-align: center;
            border: 1px solid $tk-light-gray;
            cursor: pointer;
            color: $tk-gray;

            &.disabled {
                color: $tk-medium-gray;
                cursor: default;
            }
        }

        .job-list-up {
            border-top-left-radius: $global-radius;
            border-top-right-radius: $global-radius;
        }

        .job-list-down {
            border-bottom-left-radius: $global-radius;
            border-bottom-right-radius: $global-radius;
        }

        .job-list-elements {
            box-shadow: 2px 2px 15px -5px #999;

            .job-element {
                display: flex;
                align-items: baseline;
                justify-content: space-around;
                cursor: pointer;
                font-size: 0.7em;
                color: $tk-gray;
                padding: 0.5rem 0;
                border: 1px solid $tk-light-gray;
                border-bottom: 0;
                width: 100%;

                &.selected {
                    margin: 0 2px;
                    box-shadow: 1px 1px 6px 1px #8a8a8a;
                }

                // prepared
                &.status-1 {
                    background-color: #f2f2f2;
                }

                // queued, pending
                &.status-2, &.status-7 {
                    background-color: #c0b5bf;
                }

                // running
                &.status-3 {
                    background-color: #FFFF94;
                }

                // error, limit reached
                &.status-4, &.status-8 {
                    background-color: #FFDDDD;
                }

                // done
                &.status-5 {
                    background-color: #dbdbff;
                }

                // submitted
                &.status-6 {
                    background-color: #f2f2f2;
                }

                // deleted
                &.status-9 {
                    background-color: #DBFFDB;
                }
            }
        }
    }
</style>
