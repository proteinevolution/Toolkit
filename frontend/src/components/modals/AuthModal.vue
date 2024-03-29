<template>
    <BaseModal id="auth" title="" size="sm" body-class="no-scroll-y" modal-class="tk-auth-modal">
        <template #header> &#8203; </template>
        <template #body>
            <b-tabs v-if="loggedIn">
                <b-tab :title="$t('auth.profile')">
                    <Profile />
                </b-tab>
                <b-tab :title="$t('auth.settings')">
                    <Settings />
                </b-tab>
            </b-tabs>
            <b-tabs v-else>
                <b-tab :title="$t('auth.signIn')">
                    <LoginForm />
                </b-tab>
                <b-tab :title="$t('auth.signUp')">
                    <RegisterForm />
                </b-tab>
            </b-tabs>
        </template>
    </BaseModal>
</template>

<script lang="ts">
import Vue from 'vue';
import BaseModal from './BaseModal.vue';
import LoginForm from '../auth/LoginForm.vue';
import RegisterForm from '../auth/RegisterForm.vue';
import Profile from '../auth/Profile.vue';
import Settings from '../auth/Settings.vue';
import { User } from '@/types/toolkit/auth';
import { mapStores } from 'pinia';
import { useAuthStore } from '@/stores/auth';

export default Vue.extend({
    name: 'AuthModal',
    components: {
        BaseModal,
        LoginForm,
        RegisterForm,
        Profile,
        Settings,
    },
    computed: {
        loggedIn(): boolean {
            return this.authStore.loggedIn;
        },
        user(): User | null {
            return this.authStore.user;
        },
        ...mapStores(useAuthStore),
    },
});
</script>

<style lang="scss">
.tk-auth-modal {
    .modal-body {
        display: flex;
        padding: 0;

        .tabs {
            width: 100%;
            display: flex;
            flex-direction: column;

            .nav-tabs {
                width: 100%;
                display: flex;

                .nav-item {
                    flex-grow: 1;
                    text-align: center;

                    .nav-link {
                        background-color: $primary;
                        color: white;
                        padding: 0.9rem 1.2rem;
                        border: 0;
                        font-size: 1.1em;
                    }

                    .nav-link:not(.active) {
                        color: #e1e1e1;
                        background-color: darken($primary, 3%);
                    }

                    &:first-of-type {
                        .nav-link {
                            border-top-right-radius: 0;
                            border-top-left-radius: $global-radius;
                            box-shadow: -1px 0px 1px 0 darken($primary, 8%) inset;
                        }

                        .nav-link.active {
                            box-shadow: none;
                        }
                    }

                    &:last-of-type {
                        .nav-link {
                            border-top-left-radius: 0;
                            border-top-right-radius: $global-radius;
                            box-shadow: 1px 0px 1px 0 darken($primary, 8%) inset;
                        }

                        .nav-link.active {
                            box-shadow: none;
                        }
                    }
                }
            }

            .tab-content {
                overflow-y: auto;
                // trick to prevent shrinkage
                padding: 1.5rem 1.5rem 0;

                & > div {
                    padding-bottom: 1.5rem;
                }
            }
        }
    }
}
</style>
