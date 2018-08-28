<template>
    <b-container>
        <b-row><h3>{{ tool.longname }}</h3></b-row>
        <b-row>
            <b-col>
                <b-form class="job-form">
                    <b-tabs class="parameter-tabs" nav-class="tabs-nav">
                        <b-tab v-for="section in parameterSections"
                               :key="section.name"
                               :title="section.name">
                            <div class="tabs-panel">
                                <Section :section="section"></Section>
                            </div>
                        </b-tab>
                    </b-tabs>
                    <b-form-group class="submit-buttons">
                        <b-btn class="submit-button" variant="primary">Submit Job</b-btn>
                        <b-form-input class="custom-job-id" placeholder="Custom Job ID"></b-form-input>
                    </b-form-group>
                </b-form>
            </b-col>
        </b-row>
    </b-container>
</template>

<script lang="ts">
    import Vue from 'vue';
    import Section from '@/components/tools/parameters/Section.vue';
    import {Parameter, ParameterSection, Tool} from '@/types/toolkit/index';

    export default Vue.extend({
        name: 'ToolView',
        components: {
            Section,
        },
        computed: {
            toolName(): string {
                return this.$route.params.toolName;
            },
            tool(): Tool {
                return this.$store.getters['tools/tools'].filter((tool: Tool) => tool.name === this.toolName)[0];
            },
            parameterSections(): ParameterSection[] | undefined {
                return this.tool.parameters;
            },
        },
        created() {
            this.loadToolParameters();
        },
        beforeRouteEnter(to, from, next) {
            next((vm) => {
                if (!vm.$store.getters['tools/tools'].some((tool: Tool) => tool.name === to.params.toolName)) {
                    next({path: '/404'});
                } else {
                    next();
                }
            });
        },
        beforeRouteUpdate(to, from, next) {
            this.loadToolParameters();
            next();
        },
        methods: {
            loadToolParameters() {
                this.$store.dispatch('tools/fetchToolParametersIfNotPresent', this.toolName);
            },
        },
    });
</script>

<style>
    .parameter-tabs .tabs-nav {
        background: #eee;
    }
</style>

<style scoped>
    .submit-buttons {
        padding: 0 2em 2em 2em;
    }

    .submit-button {
        margin-left: 1em;
        float: right;
    }

    .custom-job-id {
        float: right;
        width: 10em;
    }

    .job-form {
        background: #FEFEFE;
    }

    .parameter-tabs {
        background: none;
        width: 100%;
    }

    .tabs-panel {
        padding: 2em;
        background: none;
    }
</style>