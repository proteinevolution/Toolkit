<template>
    <div class="tool-view">
        <div class="tool-header">
            <h1>{{ tool.longname }}</h1>
        </div>

        <b-row>
            <b-col>
                <b-form class="tool-form">
                    <b-card no-body>
                        <b-tabs class="parameter-tabs"
                                card
                                nav-class="tabs-nav">
                            <b-tab v-for="section in parameterSections"
                                   :key="section.name"
                                   :title="section.name">
                                <div class="tabs-panel">
                                    <Section :section="section"></Section>
                                </div>
                            </b-tab>

                            <template slot="tabs">
                                <i class="fullscreen-toggler fa ml-auto mr-1"
                                   @click="toggleFullScreen"
                                   :class="[fullScreen ? 'fa-compress' : 'fa-expand']"></i>
                            </template>
                        </b-tabs>
                        <b-form-group class="submit-buttons card-body">
                            <b-btn class="submit-button"
                                   variant="primary">
                                Submit Job
                            </b-btn>
                            <b-form-input class="custom-job-id"
                                          placeholder="Custom Job ID">

                            </b-form-input>
                        </b-form-group>
                    </b-card>
                </b-form>
            </b-col>
        </b-row>
    </div>
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
        data() {
            return {
                fullScreen: false,
            };
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
            toggleFullScreen() {
                this.fullScreen = !this.fullScreen;
            },
        },
    });
</script>

<style lang="scss">
    .tool-header {
        height: 2.75rem;

        h1 {
            color: $primary;
            font-weight: bold;
            font-size: 1.25em;
            line-height: 1.6;
        }
    }

    .tool-form {
        .fullscreen-toggler {
            font-size: 1.625rem;
            color: $tk-dark-gray;
            cursor: pointer;
        }

        .parameter-tabs {
            .nav-link {
                font-size: 0.9em;
                color: $tk-dark-gray;
            }
        }

        .submit-buttons {
            margin-bottom: 0;

            .submit-button {
                margin-left: 1em;
                float: right;
            }
            .custom-job-id {
                float: right;
                width: 10em;
            }
        }
    }
</style>