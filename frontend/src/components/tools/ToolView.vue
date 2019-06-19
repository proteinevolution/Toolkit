<template>
    <VelocityFade :duration="100">
        <div class="tool-view"
             :key="toolName + 'view'"
             v-if="tool">
            <div class="tool-header d-flex align-items-baseline">
                <h1>
                    {{ tool.longname }}
                    <b-link class="help-icon" @click="launchHelpModal">
                        <i class="far fa-question-circle"></i>
                    </b-link>
                </h1>
                <div class="job-details ml-auto text-muted">
                    <slot name="job-details"></slot>
                </div>
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

                                <b-form-group v-if="showSubmitButtons"
                                              class="submit-buttons card-body">
                                    <b-btn class="submit-button"
                                           variant="primary"
                                           @click="submitJob"
                                           :disabled="preventSubmit"
                                           v-text="$t(isJobView ? 'jobs.resubmitJob' : 'jobs.submitJob')">
                                    </b-btn>
                                    <custom-job-id-input :validation-errors="validationErrors"
                                                         :submission="submission"/>
                                </b-form-group>
                            </b-tab>

                            <!-- the job form can insert more tabs here -->
                            <slot name="job-tabs"></slot>

                            <template #tabs>
                                <i class="fullscreen-toggler fa ml-auto mr-1"
                                   @click="toggleFullScreen"
                                   :class="[fullScreen ? 'fa-compress' : 'fa-expand']"></i>
                            </template>
                        </b-tabs>
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
    import Logger from 'js-logger';
    import EventBus from '@/util/EventBus';

    const logger = Logger.get('ToolView');

    export default Vue.extend({
        name: 'ToolView',
        mixins: [hasHTMLTitle],
        props: {
            isJobView: {
                type: Boolean,
                required: false,
                default: false,
            },
            jobToolName: {
                type: String,
                required: false,
                default: undefined,
            },
            jobId: {
                type: String,
                required: false,
                default: undefined,
            },
            jobParamValues: {
                type: Object,
                required: false,
                default: undefined,
            },
        },
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
                if (this.jobToolName) {
                    return this.jobToolName;
                }
                return this.$route.params.toolName;
            },
            tool(): Tool {
                return this.$store.getters['tools/tools'].find((tool: Tool) => tool.name === this.toolName);
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
        watch: {
            jobParamValues: {
                immediate: true,
                handler(value: object | undefined) {
                    if (value) {
                        this.submission = {...value};
                    }
                },
            },
            jobId: {
                immediate: true,
                handler(value: object | undefined) {
                    if (value) {
                        Vue.set(this.submission, 'parentID', value);
                    }
                },
            },
        },
        created() {
            // tool view is never reused (see App.vue), therefore loading parameters in created hook only is sufficient
            this.loadToolParameters(this.toolName);
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
                        this.$router.push(`/jobs/${response.jobID}`);
                    })
                    .catch((response) => {
                        logger.error('Could not submit job', response);
                        this.$alert(this.$t('errors.general'), 'danger');
                    });
            },
            launchHelpModal(): void {
                EventBus.$emit('show-modal', {id: 'helpModal', props: {toolName: this.toolName}});
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
            color: $tk-gray;
            font-size: 0.9em;
            margin-left: 0.15rem;
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
            padding-bottom: 0;
            padding-right: 0;

            .submit-button {
                margin-left: 1em;
                float: right;
            }

            .custom-job-id {
                float: right;
                width: 10em;
            }
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
