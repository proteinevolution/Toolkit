<template>
    <div class="autocomplete">
        <input ref="searchInput"
               class="form-control search-field"
               :class="targetClass"
               v-bind="$attrs"
               v-model.trim="search"
               @input="onChange"
               @keydown.down="onArrowDown"
               @keydown.up="onArrowUp"
               @keydown.enter="onEnter"/>
        <div class="autocomplete-results"
             v-show="isOpen">
            <li class="autocomplete-loading"
                v-if="isLoading">
                Loading...
            </li>
            <div class="autocomplete-group"
                 v-show="!isLoading && suggestions.tools.length > 0">
                <h6 class="autocomplete-group-header">{{ $t('tools.header') }}</h6>
                <div class="autocomplete-result"
                     v-for="(tool, i) in suggestions.tools"
                     :class="{ 'is-active': i === arrowCounter }"
                     :key="i">
                    <a class="search-results"
                       @click="goToTool(tool.name)"
                       v-text="tool.longname"></a>
                </div>
            </div>
            <div class="autocomplete-group"
                 v-show="!isLoading && suggestions.jobs.length > 0">
                <h6 class="autocomplete-group-header">{{ $t('jobs.header') }}</h6>
            </div>
        </div>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {Tool} from '@/types/toolkit/tools';
    import {sections} from '@/conf/ToolSections';

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
            };
        },
        computed: {
            tools(): Tool[] {
                return this.$store.getters['tools/tools'].filter((t: Tool) => sections.includes(t.section));
            },
        },
        mounted() {
            // configure datasource for the suggestions (i.e. Bloodhound)
            // this.suggestions = {
            //     engine: new Bloodhound({
            //         remote: {
            //             url: '/search/suggest/%QUERY%',
            //             wildcard: '%QUERY%',
            //         },
            //         datumTokenizer: Bloodhound.tokenizers.whitespace('q'),
            //         queryTokenizer: Bloodhound.tokenizers.whitespace,
            //     }),
            // };
            //
            // // get the input element and init typeahead on it
            // const inputEl = this.$refs.searchInput;
            // inputEl.typeahead({
            //     highlight: true,
            //     minLength: 1,
            //     autoselect: 'first',
            // }, [{
            //     source: this.suggestions.engine.ttAdapter(),
            //     name: 'jobList',
            //     limit: 30,
            //     displayKey: 'jobID',
            //     templates: {
            //         //empty: '<div class="list-group search-results-dropdown"><div class="list-group-item-notfound">Nothing found.</div></div>',
            //         suggestion: function(data: any) {
            //             if (data != null) {
            //                 return '<div class="list-group-item"><a class="search-results" data-typeahead-id=\'' + data.jobID + '\' data-link="/jobs/' + data.jobID + '" name="' + data.jobID + ' - ' + data.toolnameLong + '">' +
            //                     '<span class="search-result-jobid">' + data.jobID + '</span> <span class="search-result-tool"> ' +
            //                     '(' + data.toolnameLong + ')</span> <span class="search-result-tool-short"> (' + data.tool.substr(0, 4).toUpperCase() + ')</span></a></div>';
            //             } else {
            //                 return '';
            //             }
            //         },
            //     },
            // },
            // ]);
            document.addEventListener('click', this.handleClickOutside);
        },
        destroyed() {
            document.removeEventListener('click', this.handleClickOutside);
        },
        methods: {
            onChange() {
                // Let's warn the parent that a change was made
                this.$emit('input', this.search);
                this.filterResults();
                this.isOpen = true;
            },
            filterResults() {
                this.isLoading = true;
                // first uncapitalize all the things
                this.suggestions.tools = this.tools.filter((t: Tool) => {
                    return t.longname.toLowerCase().indexOf(this.search.toLowerCase()) > -1;
                });
                this.isLoading = false;
            },
            goToTool(toolName: string): void {
                this.$router.push('/tools/' + toolName);
            },
            onArrowDown() {
                if (this.arrowCounter < this.suggestions.tools.length) {
                    this.arrowCounter = this.arrowCounter + 1;
                }
            },
            onArrowUp() {
                if (this.arrowCounter > 0) {
                    this.arrowCounter = this.arrowCounter - 1;
                }
            },
            onEnter() {
                if (this.arrowCounter < this.suggestions.tools.length) {
                    this.goToTool(this.suggestions.tools[this.arrowCounter].name);
                }
                this.isOpen = false;
                this.arrowCounter = -1;
            },
            handleClickOutside(evt) {
                if (!this.$el.contains(evt.target)) {
                    this.isOpen = false;
                    this.arrowCounter = -1;
                }
            },
        },
    });
</script>

<style lang="scss" scoped>
    .search-field {
        font-size: 0.9em;
    }

    .form-control-gray:not(:focus) {
        background-color: transparent;
    }

    .autocomplete {
        width: 100%;

        .autocomplete-results {
            border-radius: $global-radius;
            background: white;
            padding: 0.5rem 0;
            margin-top: 0.25rem;
            border: 1px solid $tk-light-gray;
            max-height: 120px;
            overflow: auto;
            position: absolute;
            width: 100%;

            .autocomplete-group-header {
                color: $primary;
                font-weight: bold;
                padding: 0 0.6rem;
            }

            .autocomplete-result {
                list-style: none;
                text-align: left;
                padding: 4px 0.6rem;
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
