<template>
    <BaseModal :title="title"
               id="verification"
               size="sm"
               @hide="onHide">
        <Loading v-if="loading"
                 variant="primary"
                 class="mb-2"/>
        <p v-else
           v-text="message">
        </p>
    </BaseModal>
</template>

<script lang="ts">
    import Vue from 'vue';
    import BaseModal from './BaseModal.vue';
    import {TranslateResult} from 'vue-i18n';
    import Loading from '@/components/utils/Loading.vue';
    import AuthService from '@/services/AuthService';

    export default Vue.extend({
        name: 'VerificationModal',
        components: {
            BaseModal,
            Loading,
        },
        data() {
            return {
                message: '',
                successful: false,
                loading: true,
            };
        },
        computed: {
            token(): string {
                return <string>this.$route.query.token;
            },
            nameLogin(): string {
                return <string>this.$route.query.nameLogin;
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
                if (this.nameLogin != '' && this.token != '') {
                    this.loading = true;
                    const res = await AuthService.verifyToken(this.nameLogin, this.token);
                    this.message = res.message;
                    this.successful = res.successful;
                    this.loading = false;
                }
            },
            onHide() {
                this.$router.replace('/');
            },
        },
    });
</script>
