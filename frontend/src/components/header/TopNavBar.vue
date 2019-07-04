<template>
    <b-col cols="12"
           class="top-navbar navbar-light">

        <div class="meta-user"></div>
        <div class="social-nav">
            <b-button variant="href"
                      href="https://github.com/proteinevolution/Toolkit"
                      target="_blank"
                      rel="noopener"
                      class="social-link">
                <i class="fab fa-github"></i>
            </b-button>
            <b-button variant="href"
                      href="https://www.facebook.com/mpitoolkit"
                      target="_blank"
                      rel="noopener"
                      class="social-link">
                <i class="fab fa-facebook-f"></i>
            </b-button>
            <b-button variant="href"
                      href="https://twitter.com/mpitoolkit"
                      target="_blank"
                      rel="noopener"
                      class="social-link">
                <i class="fab fa-twitter"></i>
            </b-button>
            <b-button v-if="!loggedIn"
                      variant="href"
                      size="sm"
                      class="sign-in-link"
                      @click="openAuthModal"
                      v-text="$t('auth.signIn')">
            </b-button>
            <b-button v-else
                      variant="href"
                      size="sm"
                      class="sign-in-link"
                      @click="openAuthModal"
                      v-text="user.nameLogin">
            </b-button>
            <b-button v-if="loggedIn"
                      variant="href"
                      size="sm"
                      @click="signOut">
                <i class="fas fa-sign-out-alt mr-2"></i>
            </b-button>
        </div>

        <div class="warnings-container">
            <b-alert variant="warning"
                     class="maintenance-alert"
                     fade
                     :show="maintenanceMode">
                <i class="fa fa-wrench"></i>
                <b v-text="$t('maintenanceWarning')"></b>
            </b-alert>
            <div class="offline-alert"
                 @click="reloadApp"
                 v-if="reconnecting">
                <i class="fas fa-retweet"></i>
                <b v-text="$t('reconnecting')"></b>
            </div>
        </div>

        <router-link to="/"
                     class="small-logo-link d-md-none mx-auto">
            <img :src="require('../../assets/images/minlogo.svg')" alt="MPI Bioinformatics Toolkit"/>
        </router-link>

        <b-navbar-toggle class="d-md-none mr-auto"
                         target="nav_collapse"></b-navbar-toggle>
    </b-col>
</template>

<script lang="ts">
    import Vue from 'vue';
    import EventBus from '@/util/EventBus';
    import {User, AuthMessage} from '@/types/toolkit/auth';
    import AuthService from '@/services/AuthService';

    export default Vue.extend({
        name: 'TopNavBar',
        computed: {
            maintenanceMode(): boolean {
                return this.$store.state.maintenanceMode;
            },
            reconnecting(): boolean {
                return this.$store.state.reconnecting;
            },
            loggedIn(): boolean {
                return this.$store.getters['auth/loggedIn'];
            },
            user(): User | null {
                return this.$store.getters['auth/user'];
            },
        },
        methods: {
            reloadApp(): void {
                window.location.reload();
            },
            openAuthModal(): void {
                EventBus.$emit('show-modal', {id: 'auth'});
            },
            async signOut() {
                this.$store.commit('startLoading', 'logout');
                try {
                    const msg: AuthMessage = await AuthService.logout();
                    if (msg.successful) {
                        this.$store.commit('auth/setUser', null);
                        // sync jobs
                        this.$store.dispatch('jobs/fetchAllJobs');
                        this.$alert(this.$t('auth.responses.' + msg.messageKey, msg.messageArguments));
                    }
                } catch (error) {
                    this.$alert(error.message, 'danger');
                }
                this.$store.commit('stopLoading', 'logout');
            },
        },
    });
</script>

<style lang="scss" scoped>
    .top-navbar {
        width: 100%;
        display: flex;
        flex-direction: row-reverse;

        @include media-breakpoint-down(sm) {
            min-height: 50px;
        }
    }

    .small-logo-link {
        position: absolute;
        left: 50%;
        transform: translateX(-50%);

        img {
            height: 50px;
        }
    }

    .social-nav {
        .social-link {
            @include media-breakpoint-down(sm) {
                display: none;
            }

            i {
                color: $tk-dark-gray;
            }
        }

        .sign-in-link {
            color: $primary;
        }
    }

    .warnings-container {
        margin-right: 0.5rem;
        display: flex;

        .maintenance-alert, .offline-alert {
            font-size: 0.8em;
            padding: 0.5rem 1rem;

            i {
                margin-right: 0.4rem;
            }
        }

        .offline-alert {
            color: $danger;
            cursor: pointer;
        }
    }

</style>
