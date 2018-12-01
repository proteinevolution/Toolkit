<template>
    <VelocityFade :duration="100">
        <div class="tool-view"
             :key="toolName + 'view'"
             v-if="tool">
            <div class="tool-header">
                <h1>
                    {{ tool.longname }}
                    <b-link class="help-icon" @click="launchHelpModal">
                        <i class="far fa-question-circle"></i>
                    </b-link>
                </h1>
            </div>

            <LoadingWrapper :loading="$store.state.loading.toolParameters">
                <b-form class="tool-form">
                    <b-card no-body
                            :class="[fullScreen ? 'fullscreen' : '']">
                        <b-tabs class="parameter-tabs"
                                card
                                nav-class="tabs-nav">
                            <b-tab v-for="section in parameterSections"
                                   v-if="section.parameters.length > 0"
                                   :key="toolName + section.name"
                                   :title="section.name">
                                <div class="tabs-panel">
                                    <Section :section="section"
                                             :validationParams="tool.validationParams"
                                             :validation-errors="validationErrors"
                                             :submission="submission"/>
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
                                   variant="primary"
                                   @click="submitJob"
                                   :disabled="preventSubmit">
                                Submit Job
                            </b-btn>
                            <custom-job-id-input :validation-errors="validationErrors"
                                                 :submission="submission"/>
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
    import CustomJobIdInput from '@/components/tools/parameters/CustomJobIdInput.vue';
    import {ParameterSection, Tool} from '@/types/toolkit/tools';
    import VelocityFade from '@/transitions/VelocityFade.vue';
    import hasHTMLTitle from '@/mixins/hasHTMLTitle';
    import NotFoundView from '@/components/utils/NotFoundView.vue';
    import LoadingWrapper from '@/components/utils/LoadingWrapper.vue';
    import JobService from '@/services/JobService';
    import HelpModal from '@/components/modals/HelpModal.vue';
    import ToolService from '@/services/ToolService';

    export default Vue.extend({
        name: 'ToolView',
        mixins: [hasHTMLTitle],
        components: {
            Section,
            VelocityFade,
            NotFoundView,
            LoadingWrapper,
            CustomJobIdInput,
        },
        data() {
            return {
                fullScreen: false,
                validationErrors: {},
                submission: {},
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
            preventSubmit(): boolean {
                return Object.keys(this.validationErrors).length > 0;
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
            loadToolParameters(toolName: string): void {
                this.$store.dispatch('tools/fetchToolParametersIfNotPresent', toolName);
            },
            toggleFullScreen(): void {
                this.fullScreen = !this.fullScreen;
            },
            submitJob(): void {
                JobService.submitJob(this.toolName, this.submission)
                    .then((response) => {
                        this.$alert(response.message);
                    });
            },
            launchHelpModal(): void {
                ToolService.fetchToolHelp(this.toolName)
                    .then((response) => {
                        this.$modal.show(HelpModal, {contents: response}, {
                            draggable: false,
                            width: '60%',
                            height: 'auto',
                            scrollable: true,
                        });
                    });
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

        .help-icon i {
            color: $tk-medium-gray;
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
            width: 100%;
            height: 100%;
            z-index: 1;
            overflow-y: auto;
            border-radius: 0;
        }
    }
</style>
