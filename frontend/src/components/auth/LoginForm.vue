<template>
    <div>
        <b-form-group :label="$t('auth.eMailOrUsername')">
            <b-form-input v-model="username"
                          type="text"
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
        <b-btn @click="login"
               v-text="$t('auth.signIn')"/>
        <a class="password-link"
           @click.stop="forgot.show = !forgot.show">
            {{ $t('auth.forgotPassword') }}
        </a>

        <ExpandHeight>
            <b-alert variant="primary"
                     show
                     class="mt-3"
                     v-if="forgot.show">
                <b-form-group :label="$t('auth.forgotPasswordInstructions')">
                    <b-form-input v-model="forgot.eMailOrUsername"
                                  :placeholder="$t('auth.eMailOrUsername')"
                                  type="text">
                    </b-form-input>
                </b-form-group>
                <b-alert :variant="forgot.successful ? 'info' : 'danger'"
                         :show="forgot.message !== ''"
                         v-text="forgot.message"/>
                <b-btn @click="forgotPasswordSubmit"
                       :disabled="eMailOrUsernameInvalid"
                       v-text="$t('submit')"
                       variant="primary"/>
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
                try {
                    const msg = await AuthService.login(data);
                    if (msg.successful) {
                        this.$store.commit('auth/setUser', msg.user);
                        EventBus.$emit('hide-modal', 'auth');
                        this.$alert(msg.message);
                    }
                    this.message = msg.message;
                } catch (error) {
                    this.message = '';
                    this.$alert(error.message, 'danger');
                }
            },
            async forgotPasswordSubmit() {
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
