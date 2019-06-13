<template>
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
                type="submit">
        </b-btn>
    </b-form>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {PasswordResetData} from '@/types/toolkit/auth';
    import AuthService from '@/services/AuthService';
    import EventBus from '@/util/EventBus';

    export default Vue.extend({
        name: 'ResetPassword',
        data() {
            return {
                newPassword: '',
                newPasswordState: null as boolean | null,
                confirmPassword: '',
                confirmPasswordState: null as boolean | null,
                message: '',
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
                return this.newPasswordValid
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
                };
                try {
                    const msg = await AuthService.resetPassword(data);
                    if (msg.successful) {
                        this.$store.commit('auth/setUser', msg.user);
                        EventBus.$emit('hide-modal', 'verification');
                        this.$alert(msg.message);
                    }
                    this.message = msg.message;
                } catch (error) {
                    this.message = '';
                    this.$alert(error.message, 'danger');
                }
            },
        },
    });
</script>

<style lang="scss" scoped>
</style>
