<template>
    <VelocityFade :duration="100">
        <div class="tool-view"
             :key="toolName + 'view'"
             v-if="tool">
            <div class="tool-header">
                <h1>{{ tool.longname }}</h1>
            </div>

            <LoadingWrapper :loading="$store.state.loading.toolParameters">
                <b-form class="tool-form">
                    <b-card no-body
                            :class="[fullScreen ? 'fullscreen' : '']">
                        <b-tabs class="parameter-tabs"
                                card
                                nav-class="tabs-nav">
                            <b-tab v-for="section in parameterSections"
                                   :key="toolName + section.name"
                                   :title="section.name">
                                <div class="tabs-panel">
                                    <Section :section="section"
                                             :validationParams="tool.validationParams"></Section>
                                </div>
                            </b-tab>

                            <template slot="tabs">
                                <i class="fullscreen-toggler fa ml-auto mr-1"
                                   @click="toggleFullScreen"
                                   :class="[fullScreen ? 'fa-compress' : 'fa-expand']"></i>
                            </template>
                        </b-tabs>
                        <b-form-group v-if="showSubmitButtons"
                                      class="submit-buttons card-body">
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
            </LoadingWrapper>
        </div>
        <not-found-view v-else
                        errorMessage="errors.ToolNotFound"/>
    </VelocityFade>
</template>

<script lang="ts">
    import Vue from 'vue';
    import Section from '@/components/tools/parameters/Section.vue';
    import {ParameterSection, Tool} from '@/types/toolkit/index';
    import VelocityFade from '@/transitions/VelocityFade.vue';
    import hasHTMLTitle from '@/mixins/hasHTMLTitle';
    import NotFoundView from '@/components/utils/NotFoundView.vue';
    import LoadingWrapper from '@/components/utils/LoadingWrapper.vue';

    export default Vue.extend({
        name: 'ToolView',
        mixins: [hasHTMLTitle],
        components: {
            Section,
            VelocityFade,
            NotFoundView,
            LoadingWrapper,
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
                if (!this.tool || !this.tool.parameters) {
                    return undefined;
                }
                return this.tool.parameters.sections;
            },
            showSubmitButtons(): boolean {
                return this.tool.parameters !== undefined && !this.tool.parameters.hideSubmitButtons;
            },
            htmlTitle(): string {
                if (!this.tool) {
                    return '';
                }
                return this.tool.longname;
            },
        },
        created() {
            this.loadToolParameters(this.toolName);
        },
        watch: {
            // Use a watcher here - component cannot use 'beforeRouteUpdate' because of lazy loading
            $route(to, from) {
                this.loadToolParameters(to.params.toolName);
            },
        },
        methods: {
            loadToolParameters(toolName: string) {
                this.$store.dispatch('tools/fetchToolParametersIfNotPresent', toolName);
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

        & > .card {

        }

        .card.fullscreen {
            position: fixed;
            top: 0;
            left: 0;
            width: 100vw;
            height: 100vh;
            z-index: 1;
            overflow-y: auto;
        }
    }
</style>