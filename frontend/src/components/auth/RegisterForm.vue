<template>
    <b-form @submit.prevent="signUp">
        <b-form-group :label="$t('auth.username')" :invalid-feedback="$t('constraints.username')">
            <b-form-input v-model="username" type="text" :state="usernameState" required @change="validateUsername" />
        </b-form-group>
        <b-form-group :label="$t('auth.eMail')" :invalid-feedback="$t('constraints.email')">
            <b-form-input v-model="email" type="email" :state="emailState" required @change="validateEmail" />
        </b-form-group>
        <b-form-group :label="$t('auth.password')" :invalid-feedback="$t('constraints.password')">
            <b-form-input
                v-model="password"
                type="password"
                :state="passwordState"
                required
                @change="validatePassword" />
        </b-form-group>
        <b-form-group :label="$t('auth.passwordRepeat')" :invalid-feedback="$t('constraints.passwordsMatch')">
            <b-form-input
                v-model="passwordRepeat"
                type="password"
                :state="passwordRepeatState"
                required
                @change="validatePasswordRepeat" />
        </b-form-group>
        <b-form-group>
            <b-form-checkbox v-model="privacyAccepted" required>
                <i18n path="auth.privacyAccept" tag="span">
                    <a class="privacy-link" @click.stop="openPrivacyPolicy">
                        {{ $t('footerLinkModals.names.privacy') }}
                    </a>
                </i18n>
            </b-form-checkbox>
        </b-form-group>
        <b-alert :variant="successful ? 'info' : 'danger'" :show="message !== ''" v-text="message" />
        <b-btn type="submit" variant="primary" :disabled="!valid" v-text="$t('auth.signUp')" />
    </b-form>
</template>

<script lang="ts">
import Vue from 'vue';
import EventBus from '@/util/EventBus';
import { authService } from '@/services/AuthService';
import { AuthMessage, SignUpData } from '@/types/toolkit/auth';
import { TranslateResult } from 'vue-i18n';

export default Vue.extend({
    name: 'RegisterForm',
    data() {
        return {
            username: '',
            usernameState: null as boolean | null,
            email: '',
            emailState: null as boolean | null,
            password: '',
            passwordState: null as boolean | null,
            passwordRepeat: '',
            passwordRepeatState: null as boolean | null,
            privacyAccepted: false,
            successful: false,
            message: '' as TranslateResult,
        };
    },
    computed: {
        usernameValid(): boolean {
            return /^[a-zA-Z0-9]{6,40}$/.test(this.username);
        },
        emailValid(): boolean {
            return /^\S+@\S+$/.test(this.email);
        },
        passwordValid(): boolean {
            return /^.{8,128}$/.test(this.password);
        },
        passwordRepeatValid(): boolean {
            return this.passwordRepeat === this.password;
        },
        valid(): boolean {
            return (
                this.usernameValid &&
                this.emailValid &&
                this.passwordValid &&
                this.passwordRepeatValid &&
                this.privacyAccepted
            );
        },
    },
    methods: {
        validateUsername() {
            this.usernameState = this.usernameValid ? null : false;
        },
        validateEmail() {
            this.emailState = this.emailValid ? null : false;
        },
        validatePassword() {
            this.passwordState = this.passwordValid ? null : false;
        },
        validatePasswordRepeat() {
            this.passwordRepeatState = this.passwordRepeatValid ? null : false;
        },
        async signUp() {
            if (!this.valid) {
                return;
            }
            const data: SignUpData = {
                nameLogin: this.username,
                password: this.password,
                eMail: this.email,
                acceptToS: this.privacyAccepted,
            };
            try {
                const msg: AuthMessage = await authService.signUp(data);
                this.successful = msg.successful;
                this.message = this.$t('auth.responses.' + msg.messageKey, msg.messageArguments);
            } catch (error) {
                this.successful = false;
                this.message = '';
                this.$alert(this.$t('auth.responses.' + error.messageKey, error.messageArguments), 'danger');
            }
        },
        openPrivacyPolicy() {
            EventBus.$emit('show-modal', { id: 'footerLink', props: { modal: 'privacy' } });
        },
    },
});
</script>

<style lang="scss" scoped>
.privacy-link {
    color: $primary !important;
    cursor: pointer;
}
</style>
