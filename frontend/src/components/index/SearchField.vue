<template>
    <div class="autocomplete">
        <input ref="searchInput"
               v-model.trim="search"
               class="form-control search-field"
               :class="targetClass"
               v-bind="$attrs"
               @input="onChange"
               @keydown.down="onArrowDown"
               @keydown.up="onArrowUp"
               @keydown.enter="onEnter"
               @focus="onFocus">
        <i class="fas fa-search search-icon"></i>
        <div v-show="isOpen"
             class="autocomplete-results">
            <div v-show="!isLoading && suggestions.jobs.length === 0 && suggestions.tools.length === 0"
                 class="autocomplete-no-results">
                {{ $t('search.nothingFound') }}
            </div>
            <div v-show="suggestions.tools.length > 0"
                 class="autocomplete-group"
                 :class="[suggestions.jobs.length > 0 ? 'mb-3' : '']">
                <h6 class="autocomplete-group-header">
                    {{ $t('tools.header') }}
                </h6>
                <div v-for="(tool, i) in suggestions.tools.slice(0, itemsPerGroup)"
                     :key="i"
                     class="autocomplete-result"
                     :class="{ 'is-active': i === arrowCounter }">
                    <a class="search-results"
                       @click="goToTool(tool)"
                       v-text="tool.longname"></a>
                </div>
                <div v-if="suggestions.tools.length > itemsPerGroup"
                     class="autocomplete-more-results">
                    ...
                </div>
            </div>
            <div v-show="!isLoading && suggestions.jobs.length > 0"
                 class="autocomplete-group">
                <h6 class="autocomplete-group-header">
                    {{ $t('jobs.header') }}
                </h6>
                <li v-if="isLoading"
                    class="autocomplete-loading">
                    {{ $t('loading') }}
                </li>
                <div v-else>
                    <div v-for="(job, i) in suggestions.jobs.slice(0, itemsPerGroup)"
                         :key="i"
                         class="autocomplete-result"
                         :class="{ 'is-active': (i + suggestions.tools.length) === arrowCounter }">
                        <a class="search-results"
                           @click="goToJob(job)">
                            {{ job.jobID }} ({{ job.tool.substr(0, 4).toUpperCase() }})
                        </a>
                    </div>
                </div>
                <div v-if="suggestions.tools.length > itemsPerGroup && !isLoading"
                     class="autocomplete-more-results">
                    ...
                </div>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
import Vue from 'vue';
import {Tool} from '@/types/toolkit/tools';
import {Job} from '@/types/toolkit/jobs';
import {sections} from '@/conf/ToolSections';
import {jobService} from '@/services/JobService';
import {mapStores} from 'pinia';
import {useToolsStore} from '@/stores/tools';

export default Vue.extend({
    name: 'SearchField',
    inheritAttrs: false,
    props: {
        targetClass: {
            type: String,
            default: '',
            required: false,
        },
    },
    data() {
        return {
            search: '',
            suggestions: {
                tools: [],
                jobs: [],
            },
            isOpen: false,
            isLoading: false,
            arrowCounter: -1,
            itemsPerGroup: 5,
        };
    },
    computed: {
        tools(): Tool[] {
            return this.toolsStore.tools.filter((t: Tool) => sections.includes(t.section));
        },
        ...mapStores(useToolsStore),
    },
    mounted() {
        document.addEventListener('click', this.handleClickOutside);
    },
    destroyed() {
        document.removeEventListener('click', this.handleClickOutside);
    },
    methods: {
        onChange() {
            // Let's warn the parent that a change was made
            this.$emit('input', this.search);
            if (this.search.length > 0) {
                this.filterResults();
            }
            this.isOpen = this.search.length > 0;
            this.arrowCounter = -1;
        },
        filterResults() {
            this.isLoading = true;
            // first uncapitalize all the things
            (this.suggestions.tools as Tool[]) = this.tools.filter((t: Tool) => {
                return t.longname.toLowerCase().indexOf(this.search.toLowerCase()) > -1;
            });
            jobService.suggestJobsForJobId(this.search)
                .then((jobs: Job[]) => {
                    (this.suggestions.jobs as Job[]) = jobs;
                })
                .finally(() => {
                    this.isLoading = false;
                });
        },
        goToTool(tool: Tool): void {
            this.$router.push('/tools/' + tool.name);
            this.clearSearch();
        },
        goToJob(job: Job): void {
            this.$router.push('/jobs/' + job.jobID);
            this.clearSearch();
        },
        clearSearch(): void {
            this.search = '';
            this.isOpen = false;
            this.arrowCounter = -1;
        },
        onArrowDown(): void {
            if (this.arrowCounter < this.suggestions.tools.length + this.suggestions.jobs.length - 1) {
                this.arrowCounter = this.arrowCounter + 1;
            }
        },
        onArrowUp(): void {
            if (this.arrowCounter > 0) {
                this.arrowCounter = this.arrowCounter - 1;
            }
        },
        onEnter(): void {
            if (this.arrowCounter < this.suggestions.tools.length) {
                this.goToTool(this.suggestions.tools[this.arrowCounter]);
            } else if (this.arrowCounter < this.suggestions.tools.length + this.suggestions.jobs.length) {
                this.goToJob(this.suggestions.jobs[this.arrowCounter - this.suggestions.tools.length]);
            }
            this.clearSearch();
        },
        onFocus(): void {
            if (this.search.length > 0) {
                this.isOpen = true;
            }
        },
        handleClickOutside(evt: any) {
            if (!this.$el.contains(evt.target)) {
                this.clearSearch();
            }
        },
    },
});
</script>

<style lang="scss" scoped>
.autocomplete {
  width: 100%;
  position: relative;
  z-index: 10;

  .search-field {
    font-size: 0.9em;
  }

  .form-control-gray:not(:focus) {
    background-color: transparent;
  }

  .search-icon {
    color: $tk-gray;
    position: absolute;
    right: 0.7rem;
    top: 0.63rem;
    pointer-events: none;
  }

  .form-control-sm ~ .search-icon {
    right: 0.6rem;
    top: 25%;
  }

  .autocomplete-results {
    border-radius: $global-radius;
    background: white;
    padding: 0.5rem 0;
    margin-top: 0.25rem;
    border: 1px solid $tk-light-gray;
    max-height: 15rem;
    overflow: auto;
    position: absolute;
    width: 100%;

    .autocomplete-group-header {
      color: $primary;
      font-weight: bold;
      padding: 0 0.6rem;
    }

    .autocomplete-more-results,
    .autocomplete-no-results {
      list-style: none;
      text-align: left;
      padding: 0.3rem 0.6rem;
    }

    .autocomplete-result {
      list-style: none;
      text-align: left;
      padding: 0.3rem 0.6rem;
      cursor: pointer;
    }

    .autocomplete-result.is-active,
    .autocomplete-result:hover {
      background-color: $primary;
      color: white;
    }
  }
}

</style>
