<template>
    <b-container>
        <b-row>
            <b-col cols="12" class="top-navbar navbar-light">
                <div class="top-navbar-right">
                    <div class="social-nav">
                        <b-dropdown v-if="false" no-caret class="lang-dropdown">
                            <template #button-content>
                                <img :src="require('../../assets/images/flag-' + $i18n.locale + '.png')" alt="" />
                                <span class="sr-only" v-text="$t('language.lang')"></span>
                            </template>
                            <b-dropdown-item @click="changeLanguage('en')">
                                <img :src="require('../../assets/images/flag-en.png')" class="mr-2" alt="" />
                                <span v-text="$t('language.en')"></span>
                            </b-dropdown-item>
                            <b-dropdown-item @click="changeLanguage('de')">
                                <img :src="require('../../assets/images/flag-de.png')" class="mr-2" alt="" />
                                <span v-text="$t('language.de')"></span>
                            </b-dropdown-item>
                        </b-dropdown>
                        <b-button
                            variant="href"
                            href="https://github.com/proteinevolution/Toolkit"
                            target="_blank"
                            rel="noopener"
                            class="social-link">
                            <i class="fab fa-github"></i>
                        </b-button>
                        <b-button
                            variant="href"
                            href="https://www.facebook.com/mpitoolkit"
                            target="_blank"
                            rel="noopener"
                            class="social-link">
                            <i class="fab fa-facebook-f"></i>
                        </b-button>
                        <b-button
                            variant="href"
                            href="https://twitter.com/mpitoolkit"
                            target="_blank"
                            rel="noopener"
                            class="social-link">
                            <i class="fab fa-twitter"></i>
                        </b-button>
                        <b-button
                            v-if="!loggedIn"
                            variant="href"
                            size="sm"
                            class="top-navbar-link"
                            @click="openAuthModal"
                            v-text="$t('auth.signIn')" />
                        <b-button
                            v-else
                            variant="href"
                            size="sm"
                            class="top-navbar-link"
                            @click="openAuthModal"
                            v-text="user.nameLogin" />
                        <b-button v-if="loggedIn" variant="href" size="sm" @click="signOut">
                            <i class="fas fa-sign-out-alt mr-2"></i>
                        </b-button>
                    </div>

                    <div class="warnings-container d-none d-lg-flex">
                        <MaintenanceMessage />
                        <div v-if="reconnecting" class="offline-alert" @click="reloadApp">
                            <i class="fas fa-retweet"></i>
                            <b v-text="$t('reconnecting')"></b>
                        </div>
                    </div>
                </div>

                <router-link to="/" class="small-logo-link d-md-none mx-auto">
                    <img :src="require('../../assets/images/minlogo.svg')" alt="MPI Bioinformatics Toolkit" />
                </router-link>

                <b-navbar-toggle class="d-lg-none mr-auto" target="offscreenMenu" @click="toggleOffscreenMenu" />
            </b-col>
        </b-row>
        <div class="d-flex d-lg-none justify-content-center">
            <div class="warnings-container mt-4 mt-md-2">
                <MaintenanceMessage />
                <div v-if="reconnecting" class="offline-alert" @click="reloadApp">
                    <i class="fas fa-retweet"></i>
                    <b v-text="$t('reconnecting')"></b>
                </div>
            </div>
        </div>
    </b-container>
</template>

<script lang="ts">
import Vue from 'vue';
import EventBus from '@/util/EventBus';
import { AuthMessage, User } from '@/types/toolkit/auth';
import { authService } from '@/services/AuthService';
import Logger from 'js-logger';
import { loadLanguageAsync, possibleLanguages } from '@/i18n';
import MaintenanceMessage from '@/components/navigation/MaintenanceMessage.vue';
import { mapStores } from 'pinia';
import { useRootStore } from '@/stores/root';
import { useJobsStore } from '@/stores/jobs';
import { useAuthStore } from '@/stores/auth';

const logger = Logger.get('TopNavBar');

export default Vue.extend({
    name: 'TopNavBar',
    components: { MaintenanceMessage },
    computed: {
        reconnecting(): boolean {
            return this.rootStore.reconnecting;
        },
        loggedIn(): boolean {
            return this.authStore.loggedIn;
        },
        user(): User | null {
            return this.authStore.user;
        },
        ...mapStores(useRootStore, useAuthStore, useJobsStore),
    },
    methods: {
        reloadApp(): void {
            window.location.reload();
        },
        openAuthModal(): void {
            EventBus.$emit('show-modal', { id: 'auth' });
        },
        toggleOffscreenMenu(): void {
            this.rootStore.offscreenMenuShow = true;
        },
        changeLanguage(lang: string): void {
            if (possibleLanguages.includes(lang)) {
                loadLanguageAsync(lang).catch(logger.error);
            } else {
                logger.warn('trying to switch to unrecognized language: ' + lang);
            }
        },
        async signOut() {
            this.rootStore.loading.logout = true;
            try {
                const msg: AuthMessage = await authService.logout();
                if (msg.successful) {
                    this.authStore.user = null;
                    this.$alert(this.$t('auth.responses.' + msg.messageKey, msg.messageArguments));
                    // sync jobs
                    await this.jobsStore.fetchAllJobs();
                }
            } catch (error) {
                this.$alert(error.message, 'danger');
            }
            this.rootStore.loading.logout = false;
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
        margin-top: 3em;
    }
}

.top-navbar-right {
    display: flex;
    flex-direction: row-reverse;
    align-items: center;

    .warnings-container {
        margin-right: 0.5rem;
    }

    @include media-breakpoint-down(sm) {
        flex-direction: column;
        align-items: flex-end;

        .warnings-container {
            margin-right: 0;
        }
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
}

.top-navbar-link {
    color: $primary;
}

.warnings-container {
    display: flex;
    align-items: center;

    .offline-alert {
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
