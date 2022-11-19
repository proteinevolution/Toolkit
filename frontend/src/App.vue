<template>
    <div class="toolkit">
        <VelocityFade>
            <LoadingView v-if="rootStore.loading.tools || rootStore.loading.maintenanceState" />
            <b-container v-else class="main-container">
                <OffscreenMenu />
                <b-row>
                    <Header />
                </b-row>
                <b-row class="pt-3 mb-2 main-content" :class="[showJobList ? 'job-list-visible' : '']">
                    <b-col class="job-list-col d-none d-lg-block" lg="3" xl="2">
                        <SideBar />
                    </b-col>
                    <b-col :class="[showJobList ? 'col-lg-9 col-xl-10' : '']">
                        <router-view
                            :key="$route.fullPath + refreshCounter"
                            v-slot="{ Component }"
                            @refresh="refreshCounter++">
                            <VelocityFade :duration="1000">
                                <component :is="Component" />
                            </VelocityFade>
                        </router-view>
                    </b-col>
                </b-row>
                <b-row>
                    <Footer />
                </b-row>
            </b-container>
        </VelocityFade>

        <div>
            <!-- Place modals here -->
            <AuthModal />
            <FooterLinkModal :modal="modalProps.modal" />
            <UpdatesModal />
            <HelpModal :tool-name="modalProps.toolName" />
            <ForwardingModal
                :forwarding-data="modalProps.forwardingData"
                :forwarding-mode="modalProps.forwardingMode"
                :forwarding-job-i-d="modalProps.forwardingJobID"
                :forwarding-api-options="modalProps.forwardingApiOptions"
                :forwarding-api-options-alignment="modalProps.forwardingApiOptionsAlignment"
                @hidden="clearForwardingModalData" />
            <TemplateAlignmentModal
                :job-i-d="modalProps.jobID"
                :accession="modalProps.accession"
                :forwarding-mode="modalProps.forwardingMode" />
            <TemplateStructureModal :accession="modalProps.accessionStructure" />
            <VerificationModal />
            <ResetPasswordModal />
        </div>

        <v-tour
            name="toolkitTour"
            :steps="steps"
            :options="options"
            :callbacks="{ onSkip: setTourFinished, onFinish: setTourFinished }" />

        <scroll-top-button />

        <notifications animation-type="velocity" />

        <vue-cookie-accept-decline type="bar">
            <template #message>
                <i18n-t keypath="cookieLaw.message" tag="div" class="Cookie__content">
                    <b
                        class="cursor-pointer"
                        @click="showModal({ id: 'footerLink', props: { modal: 'privacy' } })"
                        v-text="$t('cookieLaw.privacyLink')"></b>
                </i18n-t>
            </template>

            <template #acceptContent>
                <div class="Cookie__buttons">
                    <button class="Cookie__button" @click="props.accept" v-text="$t('cookieLaw.accept')"></button>
                </div>
            </template>
        </vue-cookie-accept-decline>
    </div>
</template>

<script lang="ts">
import { computed, defineComponent, getCurrentInstance, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue';
import Header from '@/components/navigation/Header.vue';
import Footer from '@/components/navigation/Footer.vue';
import SideBar from '@/components/sidebar/SideBar.vue';
import VelocityFade from '@/transitions/VelocityFade.vue';
import LoadingView from '@/components/utils/LoadingView.vue';
import { Job } from '@/types/toolkit/jobs';
import Logger from 'js-logger';
import { ForwardingApiOptions, ForwardingApiOptionsAlignment, Tool } from '@/types/toolkit/tools';
import FooterLinkModal from '@/components/modals/FooterLinkModal.vue';
import UpdatesModal from '@/components/modals/UpdatesModal.vue';
import HelpModal from '@/components/modals/HelpModal.vue';
import AuthModal from '@/components/modals/AuthModal.vue';
import ForwardingModal from '@/components/modals/ForwardingModal.vue';
import TemplateAlignmentModal from '@/components/modals/TemplateAlignmentModal.vue';
import TemplateStructureModal from '@/components/modals/TemplateStructureModal.vue';
import { ModalParams } from '@/types/toolkit/utils';
import VerificationModal from '@/components/modals/VerificationModal.vue';
import ResetPasswordModal from '@/components/modals/ResetPasswordModal.vue';
import VueCookieAcceptDecline from 'vue-cookie-accept-decline';
import ScrollTopButton from '@/components/utils/ScrollTopButton.vue';
import OffscreenMenu from '@/components/navigation/OffscreenMenu.vue';
import { useRootStore } from '@/stores/root';
import { useToolsStore } from '@/stores/tools';
import { useJobsStore } from '@/stores/jobs';
import { useAuthStore } from '@/stores/auth';
import { Tour } from 'v3-tour';
import { useRoute } from 'vue-router';
import { useEventBus } from '@vueuse/core';
import { useGlobalTitleState } from '@/composables/useToolkitTitle';
import useToolkitNotifications from '@/composables/useToolkitNotifications';
import useToolkitTour from '@/composables/useToolkitTour';
import useToolkitWebsocket from './composables/useToolkitWebsocket';
import { useI18n } from 'vue-i18n';

const logger = Logger.get('App');

export default defineComponent({
    name: 'App',
    components: {
        OffscreenMenu,
        Header,
        SideBar,
        Footer,
        VelocityFade,
        LoadingView,
        FooterLinkModal,
        UpdatesModal,
        HelpModal,
        VerificationModal,
        ResetPasswordModal,
        ForwardingModal,
        TemplateAlignmentModal,
        TemplateStructureModal,
        AuthModal,
        VueCookieAcceptDecline,
        ScrollTopButton,
    },
    setup() {
        const { alert } = useToolkitNotifications();
        const { t } = useI18n();
        const rootStore = useRootStore();
        const authStore = useAuthStore();
        const toolsStore = useToolsStore();
        const jobsStore = useJobsStore();
        const { options, steps, setTourFinished } = useToolkitTour();
        const route = useRoute();

        rootStore.fetchMaintenance();
        toolsStore.fetchAllTools();
        jobsStore.fetchAllJobs();
        // this also makes sure the session id is set
        authStore.fetchUserData();

        const showJobList = computed(() => route.meta.showJobList);
        const openJobId = computed(() => route.params.jobID);

        const showNotification = (title: string, text: string, args: any): void => {
            logger.debug('Notification received.', title, text, args);
            alert({
                title: t(title, args),
                text: t(text, args),
                useBrowserNotifications: true,
            });
        };

        const showJobNotification = (jobID: string, title: string, body: string): void => {
            if (jobID === openJobId.value) {
                const job = jobsStore.jobs.find((j: Job) => j.jobID === jobID) as Job;
                const tool = toolsStore.tools.find((t: Tool) => t.name === job.tool) as Tool;
                showNotification(title, body, { tool: tool.longname });
                const { alert } = useGlobalTitleState();
                alert.value = true;
            }
        };

        // Modals logic
        const root = getCurrentInstance();
        const modalProps = reactive({
            modal: 'help', // for Simple Modal
            toolName: '', // for Help Modal
            sequences: '', // for AlignmentViewerModal
            format: '', // for AlignmentViewerModal
            forwardingMode: {}, // for ForwardingModal and TemplateAlignmentModal
            forwardingData: '', // for ForwardingModal
            forwardingJobID: '', // for ForwardingModal
            forwardingApiOptions: undefined as ForwardingApiOptions | undefined, // for ForwardingModal
            forwardingApiOptionsAlignment: undefined as ForwardingApiOptionsAlignment | undefined, // for ForwardingModal
            jobID: '', // for TemplateAlignmentModal
            accession: '', // for TemplateAlignmentModal
            // care: Don't share the accession properties between modals, otherwise they react to the wrong updates!
            accessionStructure: '', // for TemplateStructureModal
        });

        const showModalsBus = useEventBus<ModalParams>('show-modal');
        const hideModalsBus = useEventBus<string>('hide-modal');
        const showModal = (params: ModalParams) => {
            if (params.props) {
                Object.assign(modalProps, params.props);
            }
            root?.emit('bv::show::modal', params.id);
        };

        const clearForwardingModalData = (): void => {
            modalProps.forwardingApiOptions = undefined;
            modalProps.forwardingApiOptionsAlignment = undefined;
            modalProps.forwardingData = '';
        };

        onMounted(() => {
            /* Modals are shown using showModalsBus.emit({id: <MODAL_ID>, props: {<ANY PROPS TO BE PASSED>}});
         where MODAL_ID is the prop "id" passed to the base modal. It is used by Bootstrap-Vue to access the modal
         programmatically. The props are passed to the modal via data attributes of the App-component.

         They are hidden with hideModalsBus.emit(<MODAL_ID>). */
            showModalsBus.on(showModal);
            hideModalsBus.on((id: string) => {
                root?.emit('bv::hide::modal', id);
            });
        });

        // UI related websocket methods, others can be found in rootStore
        const { data } = useToolkitWebsocket();
        watch(
            data,
            (json) => {
                switch (json.mutation) {
                    case 'SOCKET_MaintenanceAlert':
                        if (json.submitBlocked) {
                            alert({
                                title: t('maintenance.notificationTitle'),
                                text: t('maintenance.notificationBody'),
                                useBrowserNotifications: false,
                            });
                        }
                        break;
                    case 'SOCKET_ShowNotification':
                        showNotification(json.title, json.body, json.arguments);
                        break;
                    case 'SOCKET_ShowJobNotification':
                        showJobNotification(json.jobID, json.title, json.body);
                        break;
                    case 'SOCKET_Logout':
                        if (!rootStore.loading.logout) {
                            alert(t('auth.loggedOutByWS'));
                            jobsStore.fetchAllJobs();
                            authStore.user = null;
                        }
                        break;
                    case 'SOCKET_Login':
                        if (!rootStore.loading.login) {
                            alert(t('auth.loggedInByWS'));
                            jobsStore.fetchAllJobs();
                            authStore.fetchUserData();
                        }
                        break;
                    default:
                        break;
                }
            },
            { deep: false }
        );

        // allow for update of human-readable time by updating reference point in store
        const refreshInterval = setInterval(() => {
            rootStore.now = Date.now();
        }, 10000);

        onBeforeUnmount(() => {
            clearInterval(refreshInterval);
        });

        const refreshCounter = ref(0);

        return {
            options,
            steps,
            setTourFinished,
            showJobList,
            refreshCounter,
            rootStore,
            modalProps,
            showModal,
            clearForwardingModalData,
        };
    },
    computed: {
        tour(): Tour {
            return this.$tours['toolkitTour'];
        },
    },
    // Only used for tour
    watch: {
        '$route.path'(path: string): void {
            if (this.tour.currentStep === 2 && path === '/tools/hhpred') {
                this.tour.nextStep();
            } else if (this.tour.currentStep === 10 && path === '/jobmanager') {
                this.tour.nextStep();
            } else if (this.tour.currentStep === 9 && path.includes('/jobs')) {
                this.tour.nextStep();
            } else {
                this.tour.stop();
            }
        },
    },
});
</script>

<!-- This should generally be the only global CSS in the app. -->
<style lang="scss">
@import './assets/scss/reset';
@import 'bootstrap/scss/bootstrap';
@import 'bootstrap-vue/dist/bootstrap-vue.css';
@import '@suadelabs/vue3-multiselect/dist/vue3-multiselect.css';
@import 'v3-tour/dist/vue-tour.css';
@import './assets/scss/form-elements';
@import './assets/scss/modals';
@import './assets/scss/sequence-coloring';
@import url('https://use.fontawesome.com/releases/v5.2.0/css/all.css');

$themeColor: $primary;
@import 'vue-slider-component/lib/theme/default.scss';
@import 'handy-scroll/dist/handy-scroll.css';

@font-face {
    font-family: 'Noto Sans';
    src: url('./assets/fonts/NotoSans-Regular.ttf');
    font-weight: 400;
    font-style: normal;
}

@font-face {
    font-family: 'Noto Sans';
    src: url('./assets/fonts/NotoSans-Italic.ttf');
    font-weight: 400;
    font-style: italic;
}

@font-face {
    font-family: 'Noto Sans';
    src: url('./assets/fonts/NotoSans-Bold.ttf');
    font-weight: 700;
    font-style: normal;
}

@font-face {
    font-family: 'Source Code Pro';
    src: url('./assets/fonts/SourceCodePro-Regular.ttf');
    font-weight: 400;
}

@font-face {
    font-family: 'Source Code Pro';
    src: url('./assets/fonts/SourceCodePro-Bold.ttf');
    font-weight: 700;
}

body {
    overflow-y: scroll;
    font-family: $font-family-sans-serif;
}

.toolkit .vue-notification {
    padding: 10px;
    margin: 5px 10px 0;
    border-radius: $global-radius;

    font-size: 12px;

    color: #ffffff;
    background: $primary-light;
    border-left: 5px solid $primary;

    &.warning,
    &.warn {
        background: $warning-light;
        border-left-color: $warning;
    }

    &.danger,
    &.error {
        background: $danger-light;
        border-left-color: $danger;
    }
}

.main-container {
    background-color: $bg-gray;
    box-shadow: 1px 2px 4px 3px rgba(200, 200, 200, 0.75);
    padding-top: 10px;
    margin-bottom: 3rem;
    border-bottom-left-radius: $global-radius;
    border-bottom-right-radius: $global-radius;
    z-index: 1;
    position: relative;

    @include media-breakpoint-up(md) {
        padding-left: 1.8rem;
        padding-right: 25px;
    }
}

.main-content .job-list-col {
    transition: padding 0.6s, opacity 0.1s, max-width 0.6s;
}

.main-content:not(.job-list-visible) .job-list-col {
    max-width: 0;
    padding: 0;
    opacity: 0;
    position: absolute;
}

.Cookie--toolkit {
    background: $primary;
    color: $white;
    padding: 1.25em 2em;
}

.Cookie--toolkit .Cookie__button {
    background: $tk-dark-green;
    color: $white;
    padding: 0.625em 3.125em;
    border-radius: $global-radius;
    border: 0;
    font-size: 1em;
    margin: 0;
}

.Cookie--toolkit .Cookie__button:hover {
    background: $tk-darker-green;
}

.pagination-container {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.textarea-alignment.loading::before {
    content: '';
    display: block;
    width: 100px;
}

.fetching-spinner {
    position: fixed;
    bottom: 1em;
    left: 1em;
}

.cursor-pointer {
    cursor: pointer;
}

.no-scroll-y {
    overflow-y: hidden !important;
}

.no-wrap {
    white-space: nowrap;
}

.break-all {
    word-break: break-all;
}

.font-small {
    font-size: 0.9em;
}

// prepared
.status-1 {
    background-color: $job-status-1;
}

// queued
.status-2 {
    background-color: $job-status-2;
}

// running
.status-3 {
    background-color: $job-status-3;
}

// error
.status-4 {
    background-color: $job-status-4;
}

// done
.status-5 {
    background-color: $job-status-5;
}

// submitted
.status-6 {
    background-color: $job-status-6;
}

// pending
.status-7 {
    background-color: $job-status-7;
}

// limit reached
.status-8 {
    background-color: $job-status-8;
}

.vue-slider-dot {
    @at-root &-tooltip {
        @at-root &-inner {
            margin-bottom: -0.75em;
            color: $gray-700;
            border: none;
            background: none;
        }
    }
    div:after {
        display: none;
    }
}

.toolkit .vue-switcher-theme--default.vue-switcher-color--default {
    div {
        background-color: lighten($primary, 15%);
    }

    div:after {
        background-color: $primary;
    }
}

.v-tour {
    .v-step,
    .v-step__arrow:before,
    .v-step[data-popper-placement='top'] .v-step__arrow--dark:before,
    .v-step[data-popper-placement='right'] .v-step__arrow--dark:before {
        background: #888888e0;
    }

    .v-step__header,
    .v-step__arrow.v-step__arrow--dark:before {
        background: #666666;
    }
}
</style>
