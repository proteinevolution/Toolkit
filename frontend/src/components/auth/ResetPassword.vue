<template>
    <div>
        <b-form-group :label="$t('auth.enterNewPassword')">
            <b-form-input v-model="newPassword"
                          type="password"
                          class="mt-2 mb-3"
                          :placeholder="$t('auth.newPassword')">
            </b-form-input>
            <b-form-input v-model="confirmPassword"
                          type="password"
                          class="mb-3"
                          :placeholder="$t('auth.confirmPassword')">
            </b-form-input>
            <b-alert variant="danger"
                     :show="message !== ''"
                     v-text="message"/>
            <b-btn v-text="$t('auth.resetPassword')"
                   @click="resetPassword">
            </b-btn>
        </b-form-group>
    </div>
</template>

<script lang="ts">
    import Vue from 'vue';
    import { PasswordResetData} from '@/types/toolkit/auth';
    import AuthService from '@/services/AuthService';
    import EventBus from '@/util/EventBus';

    export default Vue.extend({
        name: 'ResetPassword',
        data() {
            return {
                newPassword: '',
                confirmPassword: '',
                message: '',
            };
        },
        methods: {
            async resetPassword() {
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
