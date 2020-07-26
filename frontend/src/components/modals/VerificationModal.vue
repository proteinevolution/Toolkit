<template>
    <BaseModal id="verification"
               :title="title"
               size="sm"
               @hide="onHide">
        <Loading v-if="loading"
                 variant="primary"
                 class="mb-2"/>
        <p v-else
           v-text="message"></p>
    </BaseModal>
</template>

<script lang="ts">
    import Vue from 'vue';
    import BaseModal from './BaseModal.vue';
    import {TranslateResult} from 'vue-i18n';
    import Loading from '@/components/utils/Loading.vue';
    import {authService} from '@/services/AuthService';
    import {AuthMessage} from '@/types/toolkit/auth';

    export default Vue.extend({
        name: 'VerificationModal',
        components: {
            BaseModal,
            Loading,
        },
        data() {
            return {
                message: '' as TranslateResult,
                successful: false,
                loading: true,
            };
        },
        computed: {
            token(): string {
                return this.$route.query.token as string;
            },
            nameLogin(): string {
                return this.$route.query.nameLogin as string;
            },
            title(): TranslateResult {
                if (this.loading) {
                    return this.$t('auth.verification.loading');
                }
                return this.$t(`auth.verification.${this.successful ? 'succeeded' : 'failed'}`);
            },
        },
        watch: {
            nameLogin: {
                immediate: true,
                handler() {
                    this.verifyEmail();
                },
            },
        },
        methods: {
            async verifyEmail() {
                if (this.nameLogin && this.token) {
                    this.loading = true;
                    try {
                        const msg: AuthMessage = await authService.verifyToken(this.nameLogin, this.token);
                        this.message = this.$t('auth.responses.' + msg.messageKey, msg.messageArguments);
                        this.successful = msg.successful;
                    } catch (error) {
                        this.message = '';
                        this.$alert(this.$t('auth.responses.' + error.messageKey, error.messageArguments), 'danger');
                    }
                    this.loading = false;
                }
            },
            onHide() {
                this.$router.replace('/');
            },
        },
    });
</script>
