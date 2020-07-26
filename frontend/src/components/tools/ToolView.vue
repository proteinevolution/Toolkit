<template>
    <VelocityFade :duration="100">
        <div v-if="tool"
             :key="toolName + 'view'"
             class="tool-view">
            <div class="tool-header d-flex align-items-baseline">
                <h1 class="no-wrap mr-3">
                    <a class="cursor-pointer mr-1"
                       @click="refresh">
                        {{ tool.longname }}
                    </a>
                    <b-link class="help-icon"
                            @click="launchHelpModal">
                        <i class="far fa-question-circle"
                           :title="$t('jobs.help')"></i>
                    </b-link>
                </h1>
                <div class="job-details ml-auto text-muted mb-2">
                    <slot name="job-details"></slot>
                </div>
            </div>

            <LoadingWrapper :loading="$store.state.loading.toolParameters">
                <b-form class="tool-form">
                    <b-card no-body
                            :class="[fullScreen ? 'fullscreen' : '']">
                        <b-tabs v-model="tabIndex"
                                class="parameter-tabs"
                                card
                                nav-class="tabs-nav"
                                no-fade>
                            <b-tab v-for="section in parameterSections"
                                   :key="toolName + section.name"
                                   :title="section.name">
                                <div class="tabs-panel">
                                    <Section :section="section"
                                             :validation-params="tool.validationParams"
                                             :validation-errors="validationErrors"
                                             :full-screen="fullScreen"
                                             :submission="submission"
                                             :remember-params="rememberParams"/>
                                </div>

                                <b-form-group v-if="showSubmitButtons"
                                              class="submit-buttons pt-4">
                                    <b-btn class="submit-button"
                                           :class="{ 'submit-button-margin' : loggedIn }"
                                           variant="primary"
                                           :disabled="preventSubmit"
                                           @click="submitJob"
                                           v-text="$t(isJobView ? 'jobs.resubmitJob' : 'jobs.submitJob')"/>
                                    <custom-job-id-input :validation-errors="validationErrors"
                                                         :submission="submission"/>
                                    <b-btn v-if="hasRememberedParameters"
                                           class="reset-params-button"
                                           variant="secondary"
                                           :title="$t('jobs.resetParamsTitle')"
                                           @click="clearParameterRemember"
                                           v-text="$t('jobs.resetParams')"/>
                                    <email-notification-switch v-if="loggedIn"
                                                               :validation-errors="validationErrors"
                                                               :submission="submission"
                                                               class="pull-left"/>
                                </b-form-group>
                            </b-tab>

                            <!-- the job form can insert more tabs here -->
                            <slot name="job-tabs"
                                  :full-screen="fullScreen"></slot>

                            <!-- hack to show the alignment viewer tool results -->
                            <b-tab v-if="alignmentViewerSequences"
                                   :title="$t('tools.alignmentViewer.visualization')"
                                   active>
                                <alignment-viewer :sequences="alignmentViewerSequences"
                                                  :format="alignmentViewerFormat"/>
                            </b-tab>

                            <template v-slot:tabs-end>
                                <div class="ml-auto">
                                    <job-public-toggle v-if="loggedIn && (!isJobView || !job.foreign)"
                                                       :job="job"
                                                       :submission="submission"/>
                                    <i v-if="job && !job.foreign"
                                       class="tool-action tool-action-push-up fa fa-trash mr-4"
                                       :title="$t('jobs.delete')"
                                       @click="$emit('delete-job')"></i>
                                    <i class="tool-action tool-action-lg fa mr-1"
                                       :title="$t('jobs.toggleFullscreen')"
                                       :class="[fullScreen ? 'fa-compress' : 'fa-expand']"
                                       @click="toggleFullScreen"></i>
                                </div>
                            </template>
                        </b-tabs>
                    </b-card>
                </b-form>
            </LoadingWrapper>
        </div>
        <not-found-view v-else
                        error-message="errors.ToolNotFound"/>
    </VelocityFade>
</template>

<script lang="ts">
    import Vue from 'vue';
    import Section from '@/components/tools/parameters/Section.vue';
    import CustomJobIdInput from '@/components/tools/parameters/CustomJobIdInput.vue';
    import EmailNotificationSwitch from '@/components/tools/parameters/EmailNotificationSwitch.vue';
    import JobPublicToggle from '@/components/tools/parameters/JobPublicToggle.vue';
    import {ParameterSection, Tool} from '@/types/toolkit/tools';
    import VelocityFade from '@/transitions/VelocityFade.vue';
    import hasHTMLTitle from '@/mixins/hasHTMLTitle';
    import NotFoundView from '@/components/utils/NotFoundView.vue';
    import LoadingWrapper from '@/components/utils/LoadingWrapper.vue';
    import {jobService} from '@/services/JobService';
    import {authService} from '@/services/AuthService';
    import Logger from 'js-logger';
    import EventBus from '@/util/EventBus';
    import {CustomJobIdValidationResult, Job} from '@/types/toolkit/jobs';
    import {parameterRememberService} from '@/services/ParameterRememberService';

    const logger = Logger.get('ToolView');

    export default Vue.extend({
        name: 'ToolView',
        components: {
            Section,
            VelocityFade,
            NotFoundView,
            LoadingWrapper,
            CustomJobIdInput,
            EmailNotificationSwitch,
            JobPublicToggle,
            AlignmentViewer: () => import(/* webpackChunkName: "alignment-viewer" */
                '@/components/tools/AlignmentViewer.vue'),
        },
        mixins: [hasHTMLTitle],
        props: {
            isJobView: {
                type: Boolean,
                required: false,
                default: false,
            },
            job: {
                type: Object as () => Job,
                required: false,
                default: undefined,
            },
        },
        data() {
            return {
                tabIndex: 0,
                fullScreen: false,
                validationErrors: {},
                submission: {} as any,
                rememberParams: {} as any,
                // hack to show the alignment viewer tool results
                alignmentViewerSequences: '',
                alignmentViewerFormat: '',
            };
        },
        computed: {
            toolName(): string {
                if (this.isJobView) {
                    return this.job.tool;
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
                return this.tool.parameters.sections
                    .filter((section: ParameterSection) => section.parameters.length > 0);
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
            loggedIn(): boolean {
                return this.$store.getters['auth/loggedIn'];
            },
            hasRememberedParameters(): boolean {
                return Object.keys(this.rememberParams).length > 0;
            },
        },
        watch: {
            job: {
                immediate: true,
                handler(value: Job | undefined) {
                    if (value) {
                        this.submission = {...value.paramValues};
                        Vue.set(this.submission, 'parentID', value.jobID);
                        // Take the suggested Job ID immediately when loading existing job parameters into the tool
                        this.checkJobId(value.jobID);
                    }
                },
            },
            rememberParams: {
                immediate: true,
                deep: true,
                handler() {
                    this.saveParametersToRemember(this.toolName);
                },
            },
        },
        created() {
            // tool view is never reused (see App.vue), therefore loading parameters in created hook only is sufficient
            this.loadToolParameters(this.toolName);
            EventBus.$on('alignment-viewer-result-open', this.openAlignmentViewerResults);
            EventBus.$on('resubmit-section', this.resubmitSectionReceive);
        },
        beforeDestroy() {
            EventBus.$off('alignment-viewer-result-open', this.openAlignmentViewerResults);
            EventBus.$off('resubmit-section', this.resubmitSectionReceive);
        },
        methods: {
            async loadToolParameters(toolName: string): Promise<void> {
                await this.$store.dispatch('tools/fetchToolParametersIfNotPresent', toolName);
                // wait until parameters are loaded before trying to load remembered values
                if (!this.job) {
                    if (parameterRememberService.has(this.toolName)) {
                        this.loadParameterRemember(toolName);
                    }
                }
            },
            loadParameterRemember(toolName: string): void {
                logger.debug(`loading remembered parameters for ${toolName}`);
                // We need to create a fresh object here to trigger the correct reactivity
                // (see https://vuejs.org/v2/guide/reactivity.html)
                this.submission = Object.assign({}, this.submission, parameterRememberService.load(toolName));
            },
            saveParametersToRemember(toolName: string): void {
                if (Object.keys(this.rememberParams).length > 0) {
                    parameterRememberService.save(toolName, this.rememberParams);
                }
            },
            clearParameterRemember(): void {
                parameterRememberService.reset(this.toolName);
                this.loadToolParameters(this.toolName);
                this.refresh();
            },
            toggleFullScreen(): void {
                this.fullScreen = !this.fullScreen;
                if (this.alignmentViewerSequences) {
                    EventBus.$emit('alignment-viewer-resize', this.fullScreen);
                }
            },
            submitJob(): void {
                const toolName = this.toolName;
                const submission = this.submission;
                jobService.submitJob(this.toolName, submission)
                    .then((response) => {
                        this.saveParametersToRemember(toolName);
                        this.$router.push(`/jobs/${response.jobID}`);
                    })
                    .catch((response) => {
                        logger.error('Could not submit job', response);
                        this.$alert(this.$t('errors.general'), 'danger');
                    });
            },
            openAlignmentViewerResults({sequences, format}: { sequences: string, format: string }): void {
                this.alignmentViewerSequences = sequences;
                this.alignmentViewerFormat = format;
                this.tabIndex = 1;
            },
            resubmitSectionReceive(section: string): void {
                Vue.set(this.submission, 'alignment', section);
                this.tabIndex = 0;
            },
            launchHelpModal(): void {
                EventBus.$emit('show-modal', {id: 'helpModal', props: {toolName: this.toolName}});
            },
            refresh(): void {
                if (this.$route.name === 'tools') {
                    this.$emit('refresh');
                } else {
                    this.$router.push('/tools/' + this.toolName);
                }
            },
            checkJobId(jobId: string): void {
                authService.validateJobId(jobId)
                    .then((result: CustomJobIdValidationResult) => {
                        if (result.suggested) {
                            Vue.set(this.submission, 'jobID', result.suggested);
                        }
                    });
            },
        },
    });
</script>

<style lang="scss">
    .tool-header {
        @include media-breakpoint-up(md) {
            height: 2.75rem;
        }

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
        .tool-action {
            font-size: 1.25rem;
            color: $tk-dark-gray;
            cursor: pointer;

            &.tool-action-lg {
                font-size: 1.625rem;
            }

            &.tool-action-push-up {
                position: relative;
                top: -2px;
            }

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
                margin-left: 0.5em;
                float: right;
                width: 7.9em;

                @media (max-width: 560px) {
                    width: 100%;
                }
            }

            .submit-button-margin {
                @media (max-width: 560px) {
                    margin-top: 3em;
                }
            }

            .reset-params-button {
                float: right;
                width: 7.9em;

                @media (max-width: 560px) {
                    width: 100%;
                    margin-top: 1em;
                }
            }

            .btn-secondary, .btn-secondary:active {
                color: $white;
                background-color: $secondary-1;
                border-color: $secondary-1;
            }

            .btn-secondary:hover {
                background-color: $secondary-2;
                border-color: $secondary-2;
            }

            .custom-job-id {
                margin-left: 0.5em;
                float: right;
                width: 10em;

                @media (max-width: 560px) {
                    width: 100%;
                    margin-top: 1em;
                }

                ::placeholder {
                    text-align: center;
                }
            }
        }

        .card.fullscreen {
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            z-index: 11;
            overflow-y: auto;
            border-radius: 0;
        }

        .vue-switcher__label {
            font-size: 11px;
            margin-top: -1em;
            white-space: nowrap;

            @media (max-width: 560px) {
                margin-top: -2.5em;
            }
        }

    }
</style>
