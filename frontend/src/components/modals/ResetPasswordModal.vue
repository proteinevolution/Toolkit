<template>
    <BaseModal :title="$t('auth.resetPassword')"
               id="resetPassword"
               size="sm">
        <b-form @submit.prevent="resetPassword">
            <b-form-group :label="$t('auth.enterNewPassword')"
                          :invalid-feedback="$t('constraints.password')">
                <b-form-input v-model="newPassword"
                              type="password"
                              :state="newPasswordState"
                              @change="validateNewPassword"
                              :placeholder="$t('auth.newPassword')">
                </b-form-input>
            </b-form-group>
            <b-form-group :invalid-feedback="$t('constraints.passwordsMatch')">
                <b-form-input v-model="confirmPassword"
                              type="password"
                              :state="confirmPasswordState"
                              @change="validateConfirmPassword"
                              :placeholder="$t('auth.confirmPassword')">
                </b-form-input>
            </b-form-group>
            <b-alert variant="danger"
                     :show="message !== ''"
                     v-text="message"/>
            <b-btn v-text="$t('auth.resetPassword')"
                   :disabled="!valid"
                   variant="primary"
                   type="submit">
            </b-btn>
        </b-form>
    </BaseModal>
</template>

<script lang="ts">
    import Vue from 'vue';
    import BaseModal from './BaseModal.vue';
    import {PasswordResetData, AuthMessage} from '@/types/toolkit/auth';
    import AuthService from '@/services/AuthService';
    import EventBus from '@/util/EventBus';
    import {TranslateResult} from 'vue-i18n';

    export default Vue.extend({
        name: 'ResetPasswordModal',
        components: {
            BaseModal,
        },
        data() {
            return {
                newPassword: '',
                newPasswordState: null as boolean | null,
                confirmPassword: '',
                confirmPasswordState: null as boolean | null,
                message: '' as TranslateResult,
            };
        },
        computed: {
            token(): string {
              return this.$route.query.token as string;
            },
            nameLogin(): string {
                return this.$route.query.nameLogin as string;
            },
            newPasswordValid(): boolean {
                return /^.{8,128}$/.test(this.newPassword);
            },
            confirmPasswordValid(): boolean {
                return this.confirmPassword === this.newPassword;
            },
            valid(): boolean {
                return this.nameLogin !== '' && this.token !== '' &&
                    this.newPasswordValid
                    && this.confirmPasswordValid;
            },
        },
        methods: {
            validateNewPassword() {
                this.newPasswordState = this.newPasswordValid ? null : false;
            },
            validateConfirmPassword() {
                this.confirmPasswordState = this.confirmPasswordValid ? null : false;
            },
            async resetPassword() {
                if (!this.valid) {
                    return;
                }
                const data: PasswordResetData = {
                    passwordNew: this.newPassword,
                    token: this.token,
                    nameLogin: this.nameLogin,
                };
                try {
                    const msg: AuthMessage = await AuthService.resetPassword(data);
                    const message: TranslateResult = this.$t('auth.responses.' + msg.messageKey, msg.messageArguments);
                    if (msg.successful) {
                        this.$store.commit('auth/setUser', msg.user);
                        EventBus.$emit('hide-modal', 'resetPassword');
                        this.$router.replace('/');
                        this.$alert(message);
                    }
                    this.message = message;
                } catch (error) {
                    this.message = '';
                    this.$alert(this.$t('auth.responses.' + error.messageKey, error.messageArguments), 'danger');
                }
            },
        },
    });
</script>
