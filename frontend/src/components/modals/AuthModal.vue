<template>
    <BaseModal title=""
               id="auth"
               size="sm"
               body-class="tk-auth-modal tk-modal">
        <template #header>
            &#8203;
        </template>
        <template #body>
            <b-tabs>
                <b-tab v-if="!loggedIn"
                       :title="$t('auth.signIn')">
                    <LoginForm/>
                </b-tab>
                <b-tab v-if="!loggedIn"
                       :title="$t('auth.signUp')">
                    <RegisterForm/>
                </b-tab>
                <b-tab v-if="loggedIn"
                       :title="$t('auth.profile')">
                    <Profile/>
                </b-tab>
                <b-tab v-if="loggedIn"
                       :title="$t('auth.settings')">
                    <Settings/>
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
    import {User} from '@/types/toolkit/auth';
    import AuthService from '@/services/AuthService';
    import EventBus from '@/util/EventBus';

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
                return this.$store.getters['auth/loggedIn'];
            },
            user(): User | null {
                return this.$store.getters['auth/user'];
            },
        },
    });
</script>

<style lang="scss">
    .tk-auth-modal.modal-body {
        display: flex;
        padding: 0;

        .tabs {
            width: 100%;

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
                padding: 1.5rem;
            }
        }
    }
</style>
