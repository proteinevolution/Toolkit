<template>
    <div class="toolkit">
        <vue-particles class="tk-particles"
                       color="#d0d0d0"
                       :particleOpacity="0.7"
                       :particlesNumber="80"
                       shapeType="circle"
                       :particleSize="4"
                       linesColor="#ccc"
                       :linesWidth="1"
                       :lineLinked="true"
                       :lineOpacity="0.4"
                       :linesDistance="150"
                       :moveSpeed="2"
                       :hoverEffect="true"
                       hoverMode="grab"
                       :clickEffect="true"
                       clickMode="push"/>

        <VelocityFade>
            <LoadingView v-if="$store.state.loading.tools">
            </LoadingView>
            <b-container v-else class="main-container">
                <b-row>
                    <Header></Header>
                </b-row>
                <b-row class="pt-3 mb-2 main-content"
                       :class="[showJobList ? 'job-list-visible' : '']">
                    <b-col class="job-list-col"
                           lg="3"
                           xl="2">
                        <SideBar/>
                    </b-col>
                    <b-col>
                        <VelocityFade :duration="1000">
                            <router-view :key="$route.fullPath + refreshCounter"
                                         @refresh="refreshCounter++"/>
                        </VelocityFade>
                    </b-col>
                </b-row>
                <b-row>
                    <Footer></Footer>
                </b-row>
            </b-container>
        </VelocityFade>

        <div>
            <!-- Place modals here -->
            <AuthModal/>
            <FooterLinkModal :modal="modalProps.modal"/>
            <UpdatesModal/>
            <AlignmentViewerModal :sequences="modalProps.sequences"
                                  :format="modalProps.format"/>
            <HelpModal :toolName="modalProps.toolName"/>
            <VerificationModal/>
            <ResetPasswordModal/>
        </div>

        <notifications animation-type="velocity"/>
        <cookie-law theme="toolkit"
                    :message="$t('cookieLaw.message')">
            <template slot-scope="props">
                <i18n path="cookieLaw.message"
                      tag="div"
                      class="Cookie__content">
                    <b class="cursor-pointer"
                       v-text="$t('cookieLaw.privacyLink')"
                       @click="showModal({id: 'simple', props: {modal: 'privacy'}})"></b>
                </i18n>
                <div class="Cookie__buttons">
                    <button class="Cookie__button"
                            v-text="$t('cookieLaw.accept')"
                            @click="props.accept"></button>
                </div>
            </template>
        </cookie-law>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import Header from '@/components/header/Header.vue';
    import Footer from '@/components/Footer.vue';
    import SideBar from '@/components/sidebar/SideBar.vue';
    import VelocityFade from '@/transitions/VelocityFade.vue';
    import LoadingView from '@/components/utils/LoadingView.vue';
    import {Job} from '@/types/toolkit/jobs';
    import Logger from 'js-logger';
    import {TKNotificationOptions} from '@/modules/notifications/types';
    import {Tool} from '@/types/toolkit/tools';
    import EventBus from '@/util/EventBus';
    import FooterLinkModal from '@/components/modals/FooterLinkModal.vue';
    import UpdatesModal from '@/components/modals/UpdatesModal.vue';
    import HelpModal from '@/components/modals/HelpModal.vue';
    import AuthModal from '@/components/modals/AuthModal.vue';
    import AlignmentViewerModal from '@/components/modals/AlignmentViewerModal.vue';
    import {ModalParams} from '@/types/toolkit/utils';
    import VerificationModal from '@/components/modals/VerificationModal.vue';
    import ResetPasswordModal from '@/components/modals/ResetPasswordModal.vue';
    import CookieLaw from 'vue-cookie-law';

    const logger = Logger.get('App');

    export default Vue.extend({
        name: 'App',
        components: {
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
            AlignmentViewerModal,
            AuthModal,
            CookieLaw,
        },
        data() {
            return {
                modalProps: {
                    modal: 'help', // for Simple Modal
                    toolName: '', // for Help Modal
                    sequences: '', // for AlignmentViewerModal
                    format: '', // for AlignmentViewerModal
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
        },
        created() {
            // remove title star on focus
            document.addEventListener('visibilitychange', () => {
                if (document.visibilityState === 'visible') {
                    this.$title.alert(false);
                }
            });

            this.$store.dispatch('tools/fetchAllTools');
            this.$store.dispatch('jobs/fetchAllJobs');
            // this also makes sure the session id is set
            this.$store.dispatch('auth/fetchUserData');

            // handle websocket messages which depend on the ui
            (this.$options as any).sockets.onmessage = (response: any) => {
                const json = JSON.parse(response.data);
                switch (json.mutation) {
                    case 'SOCKET_ShowNotification':
                        this.showNotification(json.title, json.body, json.arguments);
                        break;
                    case 'SOCKET_ShowJobNotification':
                        this.showJobNotification(json.jobID, json.title, json.body);
                        break;
                    case 'SOCKET_Logout':
                        if (!this.$store.state.loading.logout) {
                            this.$alert(this.$t('auth.loggedOutByWS'));
                            this.$store.dispatch('jobs/fetchAllJobs');
                            this.$store.commit('auth/setUser', null);
                        }
                        break;
                    case 'SOCKET_Login':
                        if (!this.$store.state.loading.login) {
                            this.$alert(this.$t('auth.loggedInByWS'));
                            this.$store.dispatch('jobs/fetchAllJobs');
                            this.$store.dispatch('auth/fetchUserData');
                        }
                        break;
                    default:
                        break;
                }
            };

            this.refreshInterval = setInterval(() => {
                this.$store.commit('updateNow');
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
                    const job: Job = this.$store.getters['jobs/jobs'].find((j: Job) => j.jobID === jobID);
                    const tool: Tool = this.$store.getters['tools/tools'].find((t: Tool) => t.name === job.tool);
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
        },
    });
</script>

<!-- This should generally be the only global CSS in the app. -->
<style lang="scss">
    @import "./assets/scss/reset";
    @import "~bootstrap/scss/bootstrap";
    @import "~bootstrap-vue/dist/bootstrap-vue.css";
    @import "~vue-multiselect/dist/vue-multiselect.min.css";
    @import "./assets/scss/form-elements";
    @import "./assets/scss/modals";
    @import url("https://use.fontawesome.com/releases/v5.2.0/css/all.css");

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

        &.warning, &.warn {
            background: $warning-light;
            border-left-color: $warning;
        }

        &.danger, &.error {
            background: $danger-light;
            border-left-color: $danger;
        }
    }

    .main-container {
        background-color: $bg-gray;
        box-shadow: 1px 2px 4px 3px rgba(200, 200, 200, 0.75);
        padding: 10px 1.8rem 0 25px;
        margin-bottom: 3rem;
        border-bottom-left-radius: $global-radius;
        border-bottom-right-radius: $global-radius;
        z-index: 1;
        position: relative;
    }

    .main-content .job-list-col {
        transition: padding 0.6s, opacity 0.6s, max-width 0.6s;
    }

    .main-content:not(.job-list-visible) .job-list-col {
        max-width: 0;
        padding: 0;
        opacity: 0;
    }

    .tk-particles {
        position: fixed;
        top: 0;
        bottom: 0;
        left: 0;
        right: 0;
    }

    .Cookie--toolkit {
        background: $primary;
        color: $white;
        padding: 1.25em 2em;
    }

    .Cookie--toolkit .Cookie__button {
        background: $tk-dark-green;
        color: $white;
        padding: .625em 3.125em;
        border-radius: $global-radius;
        border: 0;
        font-size: 1em;
        margin: 0;
    }

    .Cookie--toolkit .Cookie__button:hover {
        background: $tk-darker-green;
    }

    .textarea-alignment.loading::before {
        content: "";
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
</style>
