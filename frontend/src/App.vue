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
                            <router-view :key="$route.fullPath"/>
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
        },
        data() {
            return {
                modalProps: {
                    modal: 'help', // for Simple Modal
                    toolName: '', // for Help Modal
                    sequences: '', // for AlignmentViewerModal
                    format: '', // for AlignmentViewerModal
                },
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
                            this.$store.commit('auth/setUser', null);
                        }
                        break;
                    case 'SOCKET_Login':
                        if (!this.$store.state.loading.login) {
                            this.$alert(this.$t('auth.loggedInByWS'));
                            this.$store.dispatch('auth/fetchUserData');
                        }
                        break;
                    default:
                        break;
                }
            };
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
</style>
