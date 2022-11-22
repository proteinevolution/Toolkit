<template>
    <VelocityFade :duration="100">
        <div v-if="tool" class="tool-view">
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

            <LoadingWrapper :loading="loadingToolParameters">
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
                                        v-b-tooltip="submitBlockedMaintenance ? t('maintenance.blockSubmit') : null"
                                        class="submit-button"
                                        :class="{ margin: loggedIn, maintenance: submitBlockedMaintenance }"
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

<script setup lang="ts">
import { computed, defineAsyncComponent, onBeforeUnmount, ref, toRef, watch } from 'vue';
import Section from '@/components/tools/parameters/Section.vue';
import CustomJobIdInput from '@/components/tools/parameters/CustomJobIdInput.vue';
import EmailNotificationSwitch from '@/components/tools/parameters/EmailNotificationSwitch.vue';
import JobPublicToggle from '@/components/tools/parameters/JobPublicToggle.vue';
import { ParameterSection } from '@/types/toolkit/tools';
import VelocityFade from '@/transitions/VelocityFade.vue';
import NotFoundView from '@/components/utils/NotFoundView.vue';
import LoadingWrapper from '@/components/utils/LoadingWrapper.vue';
import { jobService } from '@/services/JobService';
import { authService } from '@/services/AuthService';
import Logger from 'js-logger';
import { Job } from '@/types/toolkit/jobs';
import Loading from '@/components/utils/Loading.vue';
import { useRootStore } from '@/stores/root';
import { useToolsStore } from '@/stores/tools';
import { useAuthStore } from '@/stores/auth';
import { useRoute, useRouter } from 'vue-router';
import useToolkitTitle from '@/composables/useToolkitTitle';
import useToolkitNotifications from '@/composables/useToolkitNotifications';
import { useEventBus, useStorage } from '@vueuse/core';
import { ModalParams } from '@/types/toolkit/utils';
import { useI18n } from 'vue-i18n';
import { isNonNullable } from '@/util/nullability-helpers';

const AlignmentViewer = defineAsyncComponent(() => import('@/components/tools/AlignmentViewer.vue'));

const logger = Logger.get('ToolView');

interface ToolViewProps {
    isJobView?: boolean;
    job?: Job;
}

const props = defineProps<ToolViewProps>();
const job = toRef(props, 'job');

const emit = defineEmits(['delete-job', 'refresh']);

const route = useRoute();
const router = useRouter();

const { t } = useI18n();

const submission = ref<Record<string, any>>({});
const submitLoading = ref(false);
const validationErrors = ref({});

const tabIndex = ref(0);
const fullScreen = ref(false);

const toolName = computed<string>(() => {
    const { isJobView } = props;
    if (isJobView && isNonNullable(job.value)) {
        return job.value.tool;
    }
    return route.params.toolName as string;
});

const tool = computed(() => toolsStore.tools.find((tool) => tool.name === toolName.value));

const parameterSections = computed<ParameterSection[] | undefined>(() =>
    tool.value?.parameters?.sections.filter((section: ParameterSection) => section.parameters.length > 0)
);

useToolkitTitle(computed(() => tool.value?.longname));

const rememberParamsAllTools = useStorage<Record<string, Record<string, any>>>(
    'remember_parameters',
    { [toolName.value]: {} },
    localStorage,
    {
        mergeDefaults: true,
    }
);
const rememberParams = computed<Record<string, any>>({
    get: (): Record<string, any> => rememberParamsAllTools.value[toolName.value],
    set: (value: Record<string, any>) => {
        rememberParamsAllTools.value[toolName.value] = value;
    },
});
const hasRememberedParameters = computed(() => Object.keys(rememberParams.value).length > 0);

const toolsStore = useToolsStore();

async function loadToolParameters(toolName: string): Promise<void> {
    await toolsStore.fetchToolParametersIfNotPresent(toolName);
    // wait until parameters are loaded before trying to load remembered values
    if (isNonNullable(props.job)) {
        logger.debug(`loading remembered parameters for ${toolName}`);
        // We override all properties of the submission object with the remembered parameters
        Object.assign(submission.value, rememberParams.value);
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
    rememberParams.value = {};
    loadToolParameters(toolName.value);
    refresh();
}

const { alert } = useToolkitNotifications();

const rootStore = useRootStore();
const loadingToolParameters = computed(() => rootStore.loading.toolParameters);

const submitBlockedMaintenance = computed(() => rootStore.maintenance.submitBlocked);
const preventSubmit = computed(() => submitLoading.value || Object.keys(validationErrors.value).length > 0);
const showSubmitButtons = computed(
    () => isNonNullable(tool.value?.parameters) && !tool.value?.parameters.hideSubmitButtons
);

async function submitJob(): Promise<void> {
    if (preventSubmit.value || submitBlockedMaintenance.value) {
        return;
    }
    submitLoading.value = true;
    try {
        const response = await jobService.submitJob(toolName.value, submission.value);
        router.push(`/jobs/${response.jobID}`);
    } catch (error) {
        logger.error('Could not submit job', error);
        alert(t('errors.general'), 'danger');
    }
    submitLoading.value = false;
}

function changeTab(index: number): void {
    tabIndex.value = index;
}

const changeToolTabBus = useEventBus<number>('change-tool-tab');
const unsubscribeChangeToolTab = changeToolTabBus.on(changeTab);

const showModalsBus = useEventBus<ModalParams>('show-modal');

function launchHelpModal(): void {
    showModalsBus.emit({ id: 'helpModal', props: { toolName: toolName.value } });
}

function resubmitSectionReceive(section: string): void {
    submission.value.alignment = section;
    tabIndex.value = 0;
}

const resubmitSectionBus = useEventBus<string>('resubmit-section');
const unsubscribeResubmitSection = resubmitSectionBus.on(resubmitSectionReceive);

const alignmentViewerResizeBus = useEventBus<boolean>('alignment-viewer-resize');
// hack to show the alignment viewer tool results
const alignmentViewerSequences = ref('');
const alignmentViewerFormat = ref('');
const alignmentViewerResultOpenBus = useEventBus<{ sequences: string; format: string }>('alignment-viewer-result-open');

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

const authStore = useAuthStore();
const loggedIn = computed(() => authStore.loggedIn);

onBeforeUnmount(() => {
    unsubscribeChangeToolTab();
    unsubscribeResubmitSection();
    unsubscribeAlignmentViewerResult();
});

async function checkJobId(jobId: string): Promise<void> {
    const result = await authService.validateJobId(jobId);
    if (result.suggested) {
        submission.value.jobID = result.suggested;
    }
}

watch(
    job,
    (value: Job | undefined) => {
        if (isNonNullable(value)) {
            submission.value = { ...value.paramValues, parentID: value.jobID };
            // Take the suggested Job ID immediately when loading existing job parameters into the tool
            checkJobId(value.jobID);
        }
    },
    { deep: true, immediate: true }
);
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
