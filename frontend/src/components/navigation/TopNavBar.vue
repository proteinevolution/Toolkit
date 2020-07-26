<template>
    <b-col cols="12"
           class="top-navbar navbar-light">
        <div class="meta-user"></div>
        <div class="social-nav">
            <b-dropdown v-if="false"
                        no-caret
                        class="lang-dropdown">
                <template slot="button-content">
                    <img :src="require('../../assets/images/flag-' + $i18n.locale + '.png')"
                         alt="">
                    <span class="sr-only"
                          v-text="$t('language.lang')"></span>
                </template>
                <b-dropdown-item @click="changeLanguage('en')">
                    <img :src="require('../../assets/images/flag-en.png')"
                         class="mr-2"
                         alt="">
                    <span v-text="$t('language.en')"></span>
                </b-dropdown-item>
                <b-dropdown-item @click="changeLanguage('de')">
                    <img :src="require('../../assets/images/flag-de.png')"
                         class="mr-2"
                         alt="">
                    <span v-text="$t('language.de')"></span>
                </b-dropdown-item>
            </b-dropdown>
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
                      v-text="$t('auth.signIn')"/>
            <b-button v-else
                      variant="href"
                      size="sm"
                      class="sign-in-link"
                      @click="openAuthModal"
                      v-text="user.nameLogin"/>
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
            <div v-if="reconnecting"
                 class="offline-alert"
                 @click="reloadApp">
                <i class="fas fa-retweet"></i>
                <b v-text="$t('reconnecting')"></b>
            </div>
        </div>

        <router-link to="/"
                     class="small-logo-link d-md-none mx-auto">
            <img :src="require('../../assets/images/minlogo.svg')"
                 alt="MPI Bioinformatics Toolkit">
        </router-link>

        <b-navbar-toggle class="d-lg-none mr-auto"
                         target="offscreenMenu"
                         @click="toggleOffscreenMenu"/>
    </b-col>
</template>

<script lang="ts">
    import Vue from 'vue';
    import EventBus from '@/util/EventBus';
    import {AuthMessage, User} from '@/types/toolkit/auth';
    import {authService} from '@/services/AuthService';
    import Logger from 'js-logger';
    import {loadLanguageAsync, possibleLanguages} from '@/i18n';

    const logger = Logger.get('TopNavBar');

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
            toggleOffscreenMenu(): void {
                this.$store.commit('setOffscreenMenuShow', true);
            },
            changeLanguage(lang: string): void {
                if (possibleLanguages.includes(lang)) {
                    loadLanguageAsync(lang)
                        .catch(logger.error);
                } else {
                    logger.warn('trying to switch to unrecognized language: ' + lang);
                }
            },
            async signOut() {
                this.$store.commit('startLoading', 'logout');
                try {
                    const msg: AuthMessage = await authService.logout();
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
        align-items: flex-start;
        padding-top: 0.5rem;

        @media (max-width: 305px) {
            min-height: 75px;
            //min-height: 50px; --- old values
        }
    }

    .small-logo-link {
        position: absolute;
        left: 50%;
        transform: translateX(-50%);
        padding-left: 1.25em; //center the actual image itself, not the cut out image

        img {
            height: 50px;
        }
        @media (max-width: 305px) {
            margin-top:3em;
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

<style lang="scss">
    .lang-dropdown {
        .dropdown-toggle {
            line-height: 1;
        }

        .dropdown-item {
            display: flex;
            align-items: center;
        }
    }
</style>
