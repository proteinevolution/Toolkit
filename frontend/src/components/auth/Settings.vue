<template>
    <b-form @submit.prevent="changePassword">
        <b-form-group :label="$t('auth.changePassword')">
            <b-form-input v-model="oldPassword"
                          type="password"
                          :placeholder="$t('auth.oldPassword')">
            </b-form-input>
        </b-form-group>
        <b-form-group :invalid-feedback="$t('constraints.password')">
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
        <b-alert :variant="successful ? 'info' : 'danger'"
                 :show="message !== ''"
                 v-text="message"/>
        <b-btn v-text="$t('auth.changePassword')"
               :disabled="!valid"
               type="submit">
        </b-btn>
    </b-form>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {PasswordChangeData, AuthMessage} from '@/types/toolkit/auth';
    import AuthService from '@/services/AuthService';

    export default Vue.extend({
        name: 'Settings',
        data() {
            return {
                oldPassword: '',
                oldPasswordState: null as boolean | null,
                newPassword: '',
                newPasswordState: null as boolean | null,
                confirmPassword: '',
                confirmPasswordState: null as boolean | null,
                message: '',
                successful: true,
            };
        },
        computed: {
            newPasswordValid(): boolean {
                return /^.{8,128}$/.test(this.newPassword);
            },
            confirmPasswordValid(): boolean {
                return this.confirmPassword === this.newPassword;
            },
            valid(): boolean {
                return this.oldPassword !== ''
                    && this.newPasswordValid
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
            async changePassword() {
                if (!this.valid) {
                    return;
                }
                const data: PasswordChangeData = {
                    passwordOld: this.oldPassword,
                    passwordNew: this.newPassword,
                };
                try {
                    const msg: AuthMessage = await AuthService.changePassword(data);
                    this.message = this.$t('auth.responses.' + msg.messageKey, msg.messageArguments);
                    this.successful = msg.successful;
                } catch (error: AuthMessage) {
                    this.message = this.$t('auth.responses.' + error.messageKey, error.messageArguments);
                    this.successful = false;
                }
            },
        },
    });
</script>

<style lang="scss" scoped>
</style>
