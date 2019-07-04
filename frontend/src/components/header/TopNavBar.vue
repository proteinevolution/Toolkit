<template>
    <b-col cols="12"
           class="top-navbar">

        <div class="meta-user"></div>
        <div class="social-nav">
            <b-button variant="href"
                      href="https://github.com/proteinevolution/Toolkit"
                      target="_blank"
                      rel="noopener"
                      class="dark-link">
                <i class="fab fa-github"></i>
            </b-button>
            <b-button variant="href"
                      href="https://www.facebook.com/mpitoolkit"
                      target="_blank"
                      rel="noopener"
                      class="dark-link">
                <i class="fab fa-facebook-f"></i>
            </b-button>
            <b-button variant="href"
                      href="https://twitter.com/mpitoolkit"
                      target="_blank"
                      rel="noopener"
                      class="dark-link">
                <i class="fab fa-twitter"></i>
            </b-button>
            <b-button v-if="!loggedIn"
                      variant="href"
                      size="sm"
                      class="sign-in-link"
                      @click="openAuthModal"
                      v-text="$t('auth.signIn')">
            </b-button>
            <b-dropdown v-else
                        :text="user.nameLogin"
                        right
                        no-caret
                        variant="href"
                        size="sm"
                        toggle-class="profile-link">
                <b-dropdown-item @click="openAuthModal">
                    <i class="fas fa-user mr-2"></i>
                    <span v-text="$t('auth.profile')"></span>
                </b-dropdown-item>
                <b-dropdown-item @click="signOut">
                    <i class="fas fa-sign-out-alt mr-2"></i>
                    <span v-text="$t('auth.signOut')"></span>
                </b-dropdown-item>
            </b-dropdown>
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
                try {
                    const msg: AuthMessage = await AuthService.logout();
                    if (msg.successful) {
                        this.$store.commit('auth/setUser', null);
                        this.$alert(this.$t('auth.responses.' + msg.messageKey, msg.messageArguments));
                    }
                } catch (error) {
                    this.$alert(error.message, 'danger');
                }
            },
        },
    });
</script>

<style lang="scss">
    .profile-link {
        color: $primary !important;
    }
</style>

<style lang="scss" scoped>
    .top-navbar {
        width: 100%;
        display: flex;
        flex-direction: row-reverse;
    }

    .social-nav {
        .dark-link i {
            color: $tk-dark-gray;
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
