<template>
    <div class="toolkit">
        <VelocityFade>
            <LoadingView v-if="rootStore.loading.tools || rootStore.loading.maintenanceState" />
            <b-container v-else
                         class="main-container">
                <OffscreenMenu />
                <b-row>
                    <Header />
                </b-row>
                <b-row class="pt-3 mb-2 main-content"
                       :class="[showJobList ? 'job-list-visible' : '']">
                    <b-col class="job-list-col d-none d-lg-block"
                           lg="3"
                           xl="2">
                        <SideBar />
                    </b-col>
                    <b-col :class="[showJobList ? 'col-lg-9 col-xl-10':'']">
                        <VelocityFade :duration="1000">
                            <router-view :key="$route.fullPath + refreshCounter"
                                         @refresh="refreshCounter++" />
                        </VelocityFade>
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
            <ForwardingModal :forwarding-data="modalProps.forwardingData"
                             :forwarding-mode="modalProps.forwardingMode"
                             :forwarding-job-i-d="modalProps.forwardingJobID"
                             :forwarding-api-options="modalProps.forwardingApiOptions"
                             :forwarding-api-options-alignment="modalProps.forwardingApiOptionsAlignment"
                             @hidden="clearForwardingModalData" />
            <TemplateAlignmentModal :job-i-d="modalProps.jobID"
                                    :accession="modalProps.accession"
                                    :forwarding-mode="modalProps.forwardingMode" />
            <TemplateStructureModal :accession="modalProps.accessionStructure" />
            <VerificationModal />
            <ResetPasswordModal />
        </div>

        <scroll-top-button />

        <notifications animation-type="velocity" />
        <cookie-law theme="toolkit"
                    :message="$t('cookieLaw.message')">
            <template #default="props">
                <i18n path="cookieLaw.message"
                      tag="div"
                      class="Cookie__content">
                    <b class="cursor-pointer"
                       @click="showModal({id: 'footerLink', props: {modal: 'privacy'}})"
                       v-text="$t('cookieLaw.privacyLink')"></b>
                </i18n>
                <div class="Cookie__buttons">
                    <button class="Cookie__button"
                            @click="props.accept"
                            v-text="$t('cookieLaw.accept')"></button>
                </div>
            </template>
        </cookie-law>
    </div>
</template>

<script lang="ts">
import Vue from 'vue';
import Header from '@/components/navigation/Header.vue';
import Footer from '@/components/navigation/Footer.vue';
import SideBar from '@/components/sidebar/SideBar.vue';
import VelocityFade from '@/transitions/VelocityFade.vue';
import LoadingView from '@/components/utils/LoadingView.vue';
import {Job} from '@/types/toolkit/jobs';
import Logger from 'js-logger';
import {TKNotificationOptions} from '@/modules/notifications/types';
import {ForwardingApiOptions, ForwardingApiOptionsAlignment, Tool} from '@/types/toolkit/tools';
import EventBus from '@/util/EventBus';
import FooterLinkModal from '@/components/modals/FooterLinkModal.vue';
import UpdatesModal from '@/components/modals/UpdatesModal.vue';
import HelpModal from '@/components/modals/HelpModal.vue';
import AuthModal from '@/components/modals/AuthModal.vue';
import ForwardingModal from '@/components/modals/ForwardingModal.vue';
import TemplateAlignmentModal from '@/components/modals/TemplateAlignmentModal.vue';
import TemplateStructureModal from '@/components/modals/TemplateStructureModal.vue';
import {ModalParams} from '@/types/toolkit/utils';
import VerificationModal from '@/components/modals/VerificationModal.vue';
import ResetPasswordModal from '@/components/modals/ResetPasswordModal.vue';
import CookieLaw from 'vue-cookie-law';
import ScrollTopButton from '@/components/utils/ScrollTopButton.vue';
import OffscreenMenu from '@/components/navigation/OffscreenMenu.vue';
import {mapStores} from 'pinia';
import {useRootStore} from '@/stores/root';
import {useToolsStore} from '@/stores/tools';
import {useJobsStore} from '@/stores/jobs';
import {useAuthStore} from '@/stores/auth';

const logger = Logger.get('App');

export default Vue.extend({
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
        CookieLaw,
        ScrollTopButton,
    },
    data() {
        return {
            modalProps: {
                modal: 'help', // for Simple Modal
                toolName: '', // for Help Modal
                sequences: '', // for AlignmentViewerModal
                format: '', // for AlignmentViewerModal
                forwardingMode: {}, // for ForwardingModal and TemplateAlignmentModal
                forwardingData: '', // for ForwardingModal
                forwardingJobID: '', // for ForwardingModal
                forwardingApiOptions: undefined as ForwardingApiOptions | undefined, // for ForwardingModal
                forwardingApiOptionsAlignment: undefined as
                    ForwardingApiOptionsAlignment | undefined, // for ForwardingModal
                jobID: '', // for TemplateAlignmentModal
                accession: '', // for TemplateAlignmentModal
                // care: Don't share the accession properties between modals, otherwise they react to the wrong updates!
                accessionStructure: '', // for TemplateStructureModal
            },
            // allow for update of human readable time by updating reference point in store
            refreshInterval: null as any,
            refreshCounter: 0,
        };
    },
    computed: {
        showJobList(): boolean {
            return this.$route.meta.showJobList;
        },
        openJobId(): string {
            return this.$route.params.jobID;
        },
        ...mapStores(useRootStore, useAuthStore, useToolsStore, useJobsStore),
    },
    created() {
        // remove title star on focus
        document.addEventListener('visibilitychange', () => {
            if (document.visibilityState === 'visible') {
                this.$title.alert(false);
            }
        });

        this.rootStore.fetchMaintenance();
        this.toolsStore.fetchAllTools();
        this.jobsStore.fetchAllJobs();
        // this also makes sure the session id is set
        this.authStore.fetchUserData();

        // handle websocket messages which depend on the ui
        (this.$options as any).sockets.onmessage = (response: any) => {
            const json = JSON.parse(response.data);
            switch (json.mutation) {
                case 'SOCKET_MaintenanceAlert':
                    if (json.submitBlocked) {
                        this.$alert({
                            title: this.$t('maintenance.notificationTitle'),
                            text: this.$t('maintenance.notificationBody'),
                            useBrowserNotifications: false,
                        } as TKNotificationOptions);
                    }
                    break;
                case 'SOCKET_ShowNotification':
                    this.showNotification(json.title, json.body, json.arguments);
                    break;
                case 'SOCKET_ShowJobNotification':
                    this.showJobNotification(json.jobID, json.title, json.body);
                    break;
                case 'SOCKET_Logout':
                    if (!this.rootStore.loading.logout) {
                        this.$alert(this.$t('auth.loggedOutByWS'));
                        this.jobsStore.fetchAllJobs();
                        this.authStore.user = null;
                    }
                    break;
                case 'SOCKET_Login':
                    if (!this.rootStore.loading.login) {
                        this.$alert(this.$t('auth.loggedInByWS'));
                        this.jobsStore.fetchAllJobs();
                        this.authStore.fetchUserData();
                    }
                    break;
                default:
                    break;
            }
        };

        this.refreshInterval = setInterval(() => {
            this.rootStore.now = Date.now();
        }, 10000);
    },
    mounted() {
        /* Modals are shown using EventBus.$emit('show-modal', {id: <MODAL_ID>, props: {<ANY PROPS TO BE PASSED>}});
         where MODAL_ID is the prop "id" passed to the base modal. It is used by Bootstrap-Vue to access the modal
         programmatically. The props are passed to the modal via data attributes of the App-component.

         They are hidden with EventBus.$emit('hide-modal', <MODAL_ID>). */

        EventBus.$on('show-modal', this.showModal);
        EventBus.$on('hide-modal', this.hideModal);
    },
    destroyed(): void {
        delete (this.$options as any).sockets.onmessage;
        if (this.refreshInterval) {
            clearInterval(this.refreshInterval);
        }
    },
    methods: {
        showJobNotification(jobID: string, title: string, body: string): void {
            if (jobID === this.openJobId) {
                const job = this.jobsStore.jobs.find((j: Job) => j.jobID === jobID) as Job;
                const tool = this.toolsStore.tools.find((t: Tool) => t.name === job.tool) as Tool;
                  this.showNotification(title, body, {tool: tool.longname});
                this.$title.alert(true);
            }
        },
        showNotification(title: string, text: string, args: any): void {
            logger.debug('Notification received.', title, text, args);
            this.$alert({
                title: this.$t(title, args),
                text: this.$t(text, args),
                useBrowserNotifications: true,
            } as TKNotificationOptions);
        },
        showModal(params: ModalParams) {
            if (params.props) {
                Object.assign(this.modalProps, params.props);
            }
            this.$root.$emit('bv::show::modal', params.id);
        },
        hideModal(id: string) {
            this.$root.$emit('bv::hide::modal', id);
        },
        clearForwardingModalData(): void {
            this.modalProps.forwardingApiOptions = undefined;
            this.modalProps.forwardingApiOptionsAlignment = undefined;
            this.modalProps.forwardingData = '';
        },
    },
});
</script>

<!-- This should generally be the only global CSS in the app. -->
<style lang="scss">
@import './assets/scss/reset';
@import 'bootstrap/scss/bootstrap';
@import 'bootstrap-vue/dist/bootstrap-vue.css';
@import 'vue-multiselect/dist/vue-multiselect.min.css';
@import './assets/scss/form-elements';
@import './assets/scss/modals';
@import './assets/scss/sequence-coloring';
@import url('https://use.fontawesome.com/releases/v5.2.0/css/all.css');

$themeColor: $primary;
@import 'vue-slider-component/lib/theme/default.scss';
@import 'handy-scroll/dist/handy-scroll.css';

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
</style>
