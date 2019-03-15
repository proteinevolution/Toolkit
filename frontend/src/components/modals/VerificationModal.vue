<template>
    <BaseModal :title="title"
               id="verification"
               :size="isPasswordReset ? 'sm' : 'm'">
        <ResetPassword v-if="isPasswordReset"/>
        <p v-else
           v-text="authMessage.message">
        </p>
    </BaseModal>
</template>

<script lang="ts">
    import Vue from 'vue';
    import BaseModal from './BaseModal.vue';
    import {AuthMessage} from '../../types/toolkit/auth';
    import ResetPassword from '@/components/auth/ResetPassword.vue';
    import {TranslateResult} from 'vue-i18n';

    export default Vue.extend({
        name: 'VerificationModal',
        components: {
            BaseModal,
            ResetPassword,
        },
        computed: {
            isPasswordReset(): boolean {
                return this.authMessage.message === 'showPasswordResetView';
            },
            title(): TranslateResult {
                if (this.isPasswordReset) {
                    return this.$t('auth.resetPassword');
                }
                return this.$t(`auth.verification${this.authMessage.successful ? 'Succeeded' : 'Failed'}`);
            },
        },
        props: {
            authMessage: {
                /*
                 Simply stating the interface type doesn't work, this is a workaround. See
                 https://frontendsociety.com/using-a-typescript-interfaces-and-types-as-a-prop-type-in-vuejs-508ab3f83480
                */
                type: Object as () => AuthMessage,
                required: true,
            },
        },
    });
</script>
