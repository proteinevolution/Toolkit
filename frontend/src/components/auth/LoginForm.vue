<template>
    <div>
        <b-form @submit.prevent="login">
            <b-form-group :label="$t('auth.eMailOrUsername')">
                <b-form-input v-model="username"
                              type="text"
                              autofocus
                              required>
                </b-form-input>
            </b-form-group>
            <b-form-group :label="$t('auth.password')">
                <b-form-input v-model="password"
                              type="password"
                              required>
                </b-form-input>
            </b-form-group>
            <b-alert variant="danger"
                     :show="message !== ''"
                     v-text="message"/>
            <b-btn type="submit"
                   v-text="$t('auth.signIn')"/>
            <a class="password-link"
               @click.stop="toggleForgotContainer">
                {{ $t('auth.forgotPassword') }}
            </a>
        </b-form>

        <ExpandHeight>
            <b-alert :variant="!forgot.message || forgot.successful ? 'primary' : 'danger'"
                     show
                     class="mt-3 mb-0"
                     v-if="forgot.show">
                <b-form @submit.prevent="forgotPasswordSubmit"
                        v-show="!(forgot.message && forgot.successful)"
                        :class="[forgot.successful ? '' : 'mb-3']">
                    <b-form-group :label="$t('auth.forgotPasswordInstructions')">
                        <b-form-input v-model="forgot.eMailOrUsername"
                                      :placeholder="$t('auth.eMailOrUsername')"
                                      type="text">
                        </b-form-input>
                    </b-form-group>
                    <b-btn :disabled="eMailOrUsernameInvalid"
                           type="submit"
                           v-text="$t('submit')"
                           variant="primary"/>
                </b-form>
                <div v-show="forgot.message"
                     v-text="forgot.message">
                </div>
            </b-alert>
        </ExpandHeight>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {ForgotPasswordData, LoginData} from '@/types/toolkit/auth';
    import AuthService from '@/services/AuthService';
    import EventBus from '@/util/EventBus';
    import ExpandHeight from '@/transitions/ExpandHeight.vue';

    export default Vue.extend({
        name: 'LoginForm',
        components: {
            ExpandHeight,
        },
        data() {
            return {
                username: '',
                password: '',
                message: '',
                forgot: {
                    show: false,
                    eMailOrUsername: '',
                    successful: true,
                    message: '',
                },
            };
        },
        computed: {
            eMailOrUsernameInvalid(): boolean {
                return this.forgot.eMailOrUsername === '';
            },
        },
        methods: {
            async login() {
                const data: LoginData = {
                    nameLogin: this.username,
                    password: this.password,
                };
                this.$store.commit('startLoading', 'login');
                try {
                    const msg = await AuthService.login(data);
                    if (msg.successful) {
                        this.$store.commit('auth/setUser', msg.user);
                        // get jobs of user
                        this.$store.dispatch('jobs/fetchAllJobs');
                        EventBus.$emit('hide-modal', 'auth');
                        this.$alert(msg.message);
                    }
                    this.message = msg.message;
                } catch (error) {
                    this.message = '';
                    this.$alert(error.message, 'danger');
                }
                this.$store.commit('stopLoading', 'login');
            },
            async forgotPasswordSubmit() {
                if (this.eMailOrUsernameInvalid) {
                    return;
                }
                const data: ForgotPasswordData = {
                    eMailOrUsername: this.forgot.eMailOrUsername,
                };
                try {
                    const msg = await AuthService.forgotPassword(data);
                    this.forgot.message = msg.message;
                    this.forgot.successful = msg.successful;
                } catch (error) {
                    this.forgot.message = error.message;
                    this.forgot.successful = false;
                }
            },
            toggleForgotContainer(): void {
                this.forgot.show = !this.forgot.show;
                if (!this.forgot.successful) {
                    this.forgot.successful = true;
                    this.forgot.message = '';
                }
            },
        },
    });
</script>

<style lang="scss" scoped>
    .password-link {
        padding: 0.375rem 0;
        float: right;
        color: $primary !important;
        cursor: pointer;
    }
</style>
