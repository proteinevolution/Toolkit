<template>
    <div class="job-list">
        <div class="job-list-controls">
            <div v-for="(sortCol, index) in sortColumns"
                 :key="sortCol.name"
                 class="sort"
                 :class="[selectedSort === index ? 'selected':'']"
                 @click="selectSort(index)">
                {{ $t('jobList.sortColumns.' + sortCol.name) }}
            </div>
            <div class="open-job-manager"
                 data-v-step="job-manager"
                 @click="$emit('click')">
                <router-link to="/jobmanager">
                    <i class="fas fa-list-ul"></i>
                </router-link>
            </div>
        </div>

        <div class="job-list-elements">
            <div v-if="jobs.length > itemsPerPage"
                 class="job-list-up"
                 :class="[scrollDownPossible ? '' : 'disabled']"
                 @click="scrollDown">
                <i class="fas fa-caret-up"></i>
            </div>

            <a v-for="job in sliceJobs"
               :key="'link-' + job.jobID"
               class="job-element"
               :class="['status-' + job.status, job.jobID === selectedJobID ? 'selected' : '']"
               :href="`/jobs/${job.jobID}`"
               @click.prevent="goToJob(job.jobID)">
                <span class="job-id"
                      v-text="job.jobID"></span>
                <span class="tool-code"
                      v-text="job.code.toUpperCase()"></span>
                <span class="job-delete-btn"
                      @click.stop.prevent="hideJob(job.jobID)">
                    <i class="fas fa-lg fa-times"></i>
                </span>
            </a>

            <div v-if="jobs.length > itemsPerPage"
                 class="job-list-down d-flex flex-column"
                 :class="[scrollUpPossible ? '' : 'disabled']"
                 @click="scrollUp">
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
import {Route} from 'vue-router';

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
                    return moment.utc(b.dateCreated).diff(moment.utc(a.dateCreated));
                },
            }, {
                name: 'tool',
                sort: (a: Job, b: Job) => {
                    return a.tool.localeCompare(b.tool);
                },
            }],
            selectedSort: 1,
            sortDescending: false,
            itemsPerPage: 10,
            startIndex: 0,
        };
    },
    computed: {
        selectedJobID(): string {
            return this.$route.params.jobID;
        },
        jobs(): Job[] {
            return this.$store.getters['jobs/watchedJobs'].slice(0);
        },
        sortedJobs(): Job[] {
            let sorted: Job[] = [...this.jobs].sort(this.sortColumns[this.selectedSort].sort);
            if (this.sortDescending) {
                sorted = sorted.reverse();
            }
            return sorted;
        },
        sliceJobs(): Job[] {
            return this.sortedJobs.slice(this.startIndex * this.itemsPerPage, (this.startIndex + 1) * this.itemsPerPage);
        },
        currentPage(): number {
            return this.startIndex + 1;
        },
        pageCount(): number {
            return Math.ceil(this.jobs.length / this.itemsPerPage);
        },
        scrollDownPossible(): boolean {
            return this.startIndex > 0;
        },
        scrollUpPossible(): boolean {
            return (this.startIndex + 1) * this.itemsPerPage < this.jobs.length;
        },
    },
    watch: {
        $route({name, params}: Route): void {
            if (name === 'jobs') {
                const jobID: string = params.jobID;
                const index: number = this.sortedJobs.findIndex((job: Job) => job.jobID === jobID);
                console.log(`looking for job ${jobID} at index ${index}`);
                this.startIndex = Math.max(0, Math.floor(index / this.itemsPerPage));
            }
        },
    },
    methods: {
        goToJob(jobID: string): void {
            this.$router.push(`/jobs/${jobID}`);
            this.$emit('click');
        },
        hideJob(jobID: string): void {
            this.$store.dispatch('jobs/setJobWatched', {jobID, watched: false});
        },
        scrollDown(): void {
            if (this.scrollDownPossible) {
                this.startIndex--;
            }
        },
        scrollUp(): void {
            if (this.scrollUpPossible) {
                this.startIndex++;
            }
        },
        selectSort(index: number): void {
            if (this.selectedSort === index) {
                this.sortDescending = !this.sortDescending;
            } else {
                this.selectedSort = index;
                this.sortDescending = false;
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
      font-size: 0.8em;
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
      font-size: 0.8em;
      color: $tk-gray;
      padding: 0.5rem 0;
      border: 1px solid $tk-light-gray;
      border-bottom: 0;
      width: 100%;
      filter: grayscale(40%) sepia(10%);
      text-decoration: none;

      &.selected {
        margin: 0 2px;
        box-shadow: 1px 1px 4px 1px $tk-medium-gray;
      }

      .job-id {
        width: 5.5em;
        text-overflow: ellipsis;
        overflow: hidden;
      }

      .tool-code {
        width: 3.5em;
        text-overflow: ellipsis;
        overflow: hidden;
      }

      .job-delete-btn {
        line-height: 1;
      }

      &:hover {
        filter: grayscale(40%) sepia(10%) brightness(95%);
      }
    }
  }
}
</style>
