<template>
    <VelocityFade :duration="100">
        <div v-if="tool" :key="toolName + 'view'" class="tool-view">
            <div class="tool-header d-flex align-items-baseline">
                <h1 class="no-wrap mr-3">
                    <a class="cursor-pointer mr-1" @click="refresh">
                        {{ tool.longname }}
                    </a>
                    <b-link class="help-icon" data-v-step="help-modal" @click="launchHelpModal">
                        <i class="far fa-question-circle" :title="t('jobs.help')"></i>
                    </b-link>
                </h1>
                <div class="job-details ml-auto text-muted mb-2">
                    <slot name="job-details"></slot>
                </div>
            </div>

            <LoadingWrapper :loading="rootStore.loading.toolParameters">
                <b-form class="tool-form">
                    <b-card no-body :class="[fullScreen ? 'fullscreen' : '']">
                        <b-tabs v-model="tabIndex" class="parameter-tabs" card nav-class="tabs-nav" no-fade>
                            <b-tab
                                v-for="section in parameterSections"
                                :key="toolName + section.name"
                                :title-item-class="'tour-tab-' + section.name"
                                :title="section.name">
                                <div class="tabs-panel">
                                    <Section
                                        :section="section"
                                        :validation-params="tool.validationParams"
                                        :validation-errors="validationErrors"
                                        :full-screen="fullScreen"
                                        :submission="submission"
                                        :remember-params="rememberParams" />
                                </div>

                                <b-form-group v-if="showSubmitButtons" class="submit-buttons pt-4">
                                    <b-btn
                                        v-b-tooltip="submitBlocked ? t('maintenance.blockSubmit') : null"
                                        class="submit-button"
                                        :class="{ margin: loggedIn, maintenance: submitBlocked }"
                                        :disabled="preventSubmit"
                                        :data-v-step="isJobView ? '' : 'submit'"
                                        variant="primary"
                                        @click="submitJob">
                                        <loading
                                            v-if="submitLoading"
                                            :message="t(isJobView ? 'jobs.resubmitJob' : 'jobs.submitJob')"
                                            :size="20" />
                                        <span
                                            v-else
                                            v-text="t(isJobView ? 'jobs.resubmitJob' : 'jobs.submitJob')"></span>
                                    </b-btn>
                                    <custom-job-id-input
                                        data-v-step="job-id"
                                        :validation-errors="validationErrors"
                                        :submission="submission" />
                                    <b-btn
                                        v-if="hasRememberedParameters"
                                        class="reset-params-button"
                                        variant="secondary"
                                        :title="t('jobs.resetParamsTitle')"
                                        @click="clearParameterRemember"
                                        v-text="t('jobs.resetParams')" />
                                    <email-notification-switch
                                        v-if="loggedIn"
                                        :validation-errors="validationErrors"
                                        :submission="submission"
                                        class="pull-left" />
                                </b-form-group>
                            </b-tab>

                            <!-- the job form can insert more tabs here -->
                            <slot name="job-tabs" :full-screen="fullScreen"></slot>

                            <!-- hack to show the alignment viewer tool results -->
                            <b-tab v-if="alignmentViewerSequences" :title="t('tools.alignmentViewer.visualization')">
                                <alignment-viewer
                                    :sequences="alignmentViewerSequences"
                                    :format="alignmentViewerFormat" />
                            </b-tab>

                            <template #tabs-end>
                                <div class="ml-auto">
                                    <job-public-toggle
                                        v-if="loggedIn && (!isJobView || !job.foreign)"
                                        :job="job"
                                        :submission="submission" />
                                    <i
                                        v-if="job && !job.foreign"
                                        class="tool-action tool-action-push-up fa fa-trash mr-4"
                                        :title="t('jobs.delete')"
                                        @click="$emit('delete-job')"></i>
                                    <i
                                        class="tool-action tool-action-lg fa mr-1"
                                        :title="t('jobs.toggleFullscreen')"
                                        :class="[fullScreen ? 'fa-compress' : 'fa-expand']"
                                        @click="toggleFullScreen"></i>
                                </div>
                            </template>
                        </b-tabs>
                    </b-card>
                </b-form>
            </LoadingWrapper>
        </div>
        <not-found-view v-else error-message="errors.ToolNotFound" />
    </VelocityFade>
</template>

<script lang="ts">
import Vue, { computed, defineComponent, onBeforeUnmount, reactive, ref } from 'vue';
import Section from '@/components/tools/parameters/Section.vue';
import CustomJobIdInput from '@/components/tools/parameters/CustomJobIdInput.vue';
import EmailNotificationSwitch from '@/components/tools/parameters/EmailNotificationSwitch.vue';
import JobPublicToggle from '@/components/tools/parameters/JobPublicToggle.vue';
import { ParameterSection, Tool } from '@/types/toolkit/tools';
import VelocityFade from '@/transitions/VelocityFade.vue';
import NotFoundView from '@/components/utils/NotFoundView.vue';
import LoadingWrapper from '@/components/utils/LoadingWrapper.vue';
import { jobService } from '@/services/JobService';
import { authService } from '@/services/AuthService';
import Logger from 'js-logger';
import { CustomJobIdValidationResult, Job } from '@/types/toolkit/jobs';
import Loading from '@/components/utils/Loading.vue';
import { parameterRememberService } from '@/services/ParameterRememberService';
import { useRootStore } from '@/stores/root';
import { useToolsStore } from '@/stores/tools';
import { useAuthStore } from '@/stores/auth';
import { useRoute, useRouter } from 'vue-router';
import useToolkitTitle from '@/composables/useToolkitTitle';
import useToolkitNotifications from '@/composables/useToolkitNotifications';
import { useEventBus } from '@vueuse/core';
import { ModalParams } from '@/types/toolkit/utils';
import { useI18n } from 'vue-i18n';

const logger = Logger.get('ToolView');

export default defineComponent({
    name: 'ToolView',
    components: {
        Section,
        VelocityFade,
        NotFoundView,
        LoadingWrapper,
        CustomJobIdInput,
        EmailNotificationSwitch,
        JobPublicToggle,
        Loading,
        AlignmentViewer: () => import('@/components/tools/AlignmentViewer.vue'),
    },
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
    emits: ['delete-job', 'refresh'],
    setup(props, { emit }) {
        const route = useRoute();
        const router = useRouter();
        const toolsStore = useToolsStore();
        const rootStore = useRootStore();
        const authStore = useAuthStore();

        const { t } = useI18n();

        const submission = reactive({});
        const tabIndex = ref(0);
        const fullScreen = ref(false);

        const changeToolTabBus = useEventBus<number>('change-tool-tab');

        function changeTab(index: number): void {
            tabIndex.value = index;
        }

        const unsubscribeChangeToolTab = changeToolTabBus.on(changeTab);

        const toolName = computed<string>(() => {
            const { isJobView, job } = props;
            if (isJobView && job) {
                return job.tool;
            }
            return route.params.toolName as string;
        });

        function loadParameterRemember(toolName: string): void {
            logger.debug(`loading remembered parameters for ${toolName}`);
            // We override all properties of the submission object with the remembered parameters
            Object.assign(submission, parameterRememberService.load(toolName));
        }

        async function loadToolParameters(toolName: string): Promise<void> {
            await toolsStore.fetchToolParametersIfNotPresent(toolName);
            // wait until parameters are loaded before trying to load remembered values
            if (!props.job) {
                if (parameterRememberService.has(toolName)) {
                    loadParameterRemember(toolName);
                }
            }
        }

        // tool view is never reused (see App.vue), therefore loading parameters in setup only is sufficient
        loadToolParameters(toolName.value);

        function refresh(): void {
            if (route.name === 'tools') {
                emit('refresh');
            } else {
                router.push('/tools/' + toolName.value);
            }
        }

        function clearParameterRemember(): void {
            parameterRememberService.reset(toolName.value);
            loadToolParameters(toolName.value);
            refresh();
        }

        const tool = computed(() => toolsStore.tools.find((tool: Tool) => tool.name === toolName.value));

        useToolkitTitle(computed(() => tool.value?.longname));

        const { alert } = useToolkitNotifications();

        const showModalsBus = useEventBus<ModalParams>('show-modal');

        function launchHelpModal(): void {
            showModalsBus.emit({ id: 'helpModal', props: { toolName: toolName.value } });
        }

        const resubmitSectionBus = useEventBus<string>('resubmit-section');

        function resubmitSectionReceive(section: string): void {
            Vue.set(submission, 'alignment', section);
            tabIndex.value = 0;
        }

        const unsubscribeResubmitSection = resubmitSectionBus.on(resubmitSectionReceive);

        const alignmentViewerResizeBus = useEventBus<boolean>('alignment-viewer-resize');

        // hack to show the alignment viewer tool results
        const alignmentViewerSequences = ref('');
        const alignmentViewerFormat = ref('');
        const alignmentViewerResultOpenBus = useEventBus<{ sequences: string; format: string }>(
            'alignment-viewer-result-open'
        );

        function openAlignmentViewerResults({ sequences, format }: { sequences: string; format: string }): void {
            alignmentViewerSequences.value = sequences;
            alignmentViewerFormat.value = format;
            setTimeout(() => {
                tabIndex.value = 1;
                alignmentViewerResizeBus.emit(fullScreen.value);
            }, 100);
        }

        const unsubscribeAlignmentViewerResult = alignmentViewerResultOpenBus.on(openAlignmentViewerResults);

        function toggleFullScreen(): void {
            fullScreen.value = !fullScreen.value;
            if (alignmentViewerSequences.value !== '') {
                alignmentViewerResizeBus.emit(fullScreen.value);
            }
        }

        onBeforeUnmount(() => {
            unsubscribeChangeToolTab();
            unsubscribeResubmitSection();
            unsubscribeAlignmentViewerResult();
        });

        return {
            alert,
            alignmentViewerFormat,
            alignmentViewerSequences,
            clearParameterRemember,
            fullScreen,
            launchHelpModal,
            submission,
            tabIndex,
            toggleFullScreen,
            tool,
            toolName,
            toolsStore,
            refresh,
            t,
            rootStore,
            authStore,
        };
    },
    data() {
        return {
            submitLoading: false,
            validationErrors: {},
            rememberParams: {} as any,
        };
    },
    computed: {
        parameterSections(): ParameterSection[] | undefined {
            if (!this.tool || !this.tool.parameters) {
                return undefined;
            }
            return this.tool.parameters.sections.filter((section: ParameterSection) => section.parameters.length > 0);
        },
        showSubmitButtons(): boolean {
            return this.tool?.parameters !== undefined && !this.tool.parameters.hideSubmitButtons;
        },
        submitBlocked(): boolean {
            return this.rootStore.maintenance.submitBlocked;
        },
        preventSubmit(): boolean {
            return this.submitLoading || Object.keys(this.validationErrors).length > 0;
        },
        loggedIn(): boolean {
            return this.authStore.loggedIn;
        },
        hasRememberedParameters(): boolean {
            return Object.keys(this.rememberParams).length > 0;
        },
    },
    watch: {
        job: {
            immediate: true,
            deep: true,
            handler(value: Job | undefined) {
                if (value) {
                    this.submission = { ...value.paramValues };
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
    methods: {
        saveParametersToRemember(toolName: string): void {
            if (Object.keys(this.rememberParams).length > 0) {
                parameterRememberService.save(toolName, this.rememberParams);
            }
        },
        submitJob(): void {
            if (this.preventSubmit || this.submitBlocked) {
                return;
            }
            const toolName = this.toolName;
            const submission = this.submission;
            this.submitLoading = true;
            jobService
                .submitJob(this.toolName, submission)
                .then((response) => {
                    this.submitLoading = false;
                    this.saveParametersToRemember(toolName);
                    this.$router.push(`/jobs/${response.jobID}`);
                })
                .catch((response) => {
                    this.submitLoading = false;
                    logger.error('Could not submit job', response);
                    this.alert(this.$t('errors.general'), 'danger');
                });
        },
        checkJobId(jobId: string): void {
            authService.validateJobId(jobId).then((result: CustomJobIdValidationResult) => {
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

        .submit-button.margin {
            @media (max-width: 560px) {
                margin-top: 3em;
            }
        }

        .submit-button.maintenance {
            opacity: 0.65;
            cursor: default;
            background-color: $primary !important;
            border-color: $primary !important;
            color: $white !important;

            &:focus {
                box-shadow: none;
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

        .btn-secondary,
        .btn-secondary:active {
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
