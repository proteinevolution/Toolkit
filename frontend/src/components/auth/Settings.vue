<template>
    <div>

        <b-form-group :label="$t('auth.changePassword')">
            <b-form-input v-model="oldPassword"
                          type="password"
                          class="mb-3 mt-2"
                          :placeholder="$t('auth.oldPassword')">
            </b-form-input>
            <b-form-input v-model="newPassword"
                          type="password" class="mb-3"
                          :placeholder="$t('auth.newPassword')">
            </b-form-input>
            <b-form-input v-model="confirmPassword"
                          type="password" class="mb-3"
                          :placeholder="$t('auth.confirmPassword')">
            </b-form-input>
            <b-alert :variant="successful ? 'info' : 'danger'"
                     :show="message !== ''"
                     v-text="message"/>
            <b-btn v-text="$t('auth.changePassword')"
                   @click="changePassword">
            </b-btn>
        </b-form-group>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import {PasswordChangeData} from '@/types/toolkit/auth';
    import AuthService from '@/services/AuthService';

    export default Vue.extend({
        name: 'Settings',
        data() {
            return {
                oldPassword: '',
                newPassword: '',
                confirmPassword: '',
                message: '',
                successful: true,
            };
        },
        methods: {
            async changePassword() {
                const data: PasswordChangeData = {
                    passwordOld: this.oldPassword,
                    passwordNew: this.newPassword,
                };
                try {
                    const msg = await AuthService.changePassword(data);
                    this.message = msg.message;
                    this.successful = msg.successful;
                } catch (error) {
                    this.message = error.message;
                    this.successful = false;
                }
            },
        },
    });
</script>

<style lang="scss" scoped>
</style>
